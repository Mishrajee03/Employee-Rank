package com.employeerank.service.impl;

import com.employeerank.dto.MonthlyResultDto;
import com.employeerank.entity.Badge;
import com.employeerank.entity.MonthlyResult;
import com.employeerank.entity.User;
import com.employeerank.enums.BadgeType;
import com.employeerank.exception.BadRequestException;
import com.employeerank.exception.ResourceNotFoundException;
import com.employeerank.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonthlyResultService {

    private final MonthlyResultRepository monthlyResultRepository;
    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final CompanyRepository companyRepository;

    @Scheduled(cron = "${app.scheduling.monthly-result.cron}")
    @Transactional
    public void processMonthlyResultsScheduled() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        processMonthlyResults(lastMonth.getMonthValue(), lastMonth.getYear());
    }

    @Transactional
    public void processMonthlyResults(int month, int year) {
        log.info("Processing monthly results for {}/{}", month, year);
        List<User> allEmployees = userRepository.findAll().stream()
                .filter(u -> u.getRole().name().contains("EMPLOYEE") || u.getRole().name().contains("MANAGER"))
                .collect(Collectors.toList());

        for (User employee : allEmployees) {
            computeAndSaveResult(employee, month, year);
        }

        // Rank within companies
        companyRepository.findAll().forEach(company -> {
            List<MonthlyResult> companyResults = monthlyResultRepository
                    .findCompanyResultsForMonth(company.getId(), month, year);
            AtomicInteger rank = new AtomicInteger(1);
            companyResults.stream()
                    .sorted(Comparator.comparingDouble(MonthlyResult::getPercentageScore).reversed())
                    .forEach(r -> {
                        r.setRankInCompany(rank.getAndIncrement());
                        monthlyResultRepository.save(r);
                    });

            // Award badge to top performer
            if (!companyResults.isEmpty()) {
                MonthlyResult top = companyResults.get(0);
                awardBadge(top.getEmployee(), top, month, year);
            }
        });

        log.info("Monthly results processed successfully for {}/{}", month, year);
    }

    private void computeAndSaveResult(User employee, int month, int year) {
        if (monthlyResultRepository.existsByEmployeeIdAndResultMonthAndResultYear(
                employee.getId(), month, year)) {
            return;
        }

        Optional<Double> totalScoreOpt = scoreRepository.sumPointsByEmployeeAndMonthYear(employee.getId(), month, year);
        Optional<Double> maxScoreOpt = scoreRepository.sumMaxPointsByEmployeeAndMonthYear(employee.getId(), month, year);

        double totalScore = totalScoreOpt.orElse(0.0);
        double maxScore = maxScoreOpt.orElse(0.0);
        double percentage = maxScore > 0 ? (totalScore / maxScore) * 100 : 0;
        String grade = ScoreService.calculateGrade(percentage);
        int credits = ScoreService.calculateCredits(percentage);

        MonthlyResult result = MonthlyResult.builder()
                .employee(employee)
                .resultMonth(month)
                .resultYear(year)
                .totalScore(totalScore)
                .maxPossibleScore(maxScore)
                .percentageScore(Math.round(percentage * 100.0) / 100.0)
                .grade(grade)
                .creditsEarned(credits)
                .performanceCategory(ScoreService.getPerformanceCategory(percentage))
                .isPublished(false)
                .build();

        monthlyResultRepository.save(result);

        // Update total credits
        employee.setTotalCredits(employee.getTotalCredits() + credits);
        userRepository.save(employee);
    }

    private void awardBadge(User employee, MonthlyResult result, int month, int year) {
        BadgeType badgeType = determineBadgeType(result.getPercentageScore());
        if (!badgeRepository.existsByEmployeeIdAndBadgeTypeAndAwardedMonthAndAwardedYear(
                employee.getId(), badgeType, month, year)) {
            Badge badge = Badge.builder()
                    .employee(employee)
                    .badgeType(badgeType)
                    .badgeName(badgeType.name() + " Performer")
                    .description("Awarded for " + result.getPercentageScore() + "% performance in " + month + "/" + year)
                    .awardedMonth(month)
                    .awardedYear(year)
                    .build();
            badgeRepository.save(badge);
        }
    }

    private BadgeType determineBadgeType(double percentage) {
        if (percentage >= 95) return BadgeType.LEGEND;
        if (percentage >= 90) return BadgeType.DIAMOND;
        if (percentage >= 80) return BadgeType.PLATINUM;
        if (percentage >= 70) return BadgeType.GOLD;
        if (percentage >= 60) return BadgeType.SILVER;
        return BadgeType.BRONZE;
    }

    @Transactional
    public MonthlyResultDto.Response publishResult(Long resultId, String managerComments) {
        MonthlyResult result = monthlyResultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("MonthlyResult", resultId));
        result.setIsPublished(true);
        result.setManagerComments(managerComments);
        return mapToResponse(monthlyResultRepository.save(result));
    }

    @Transactional(readOnly = true)
    public List<MonthlyResultDto.Response> getEmployeeResults(Long employeeId) {
        return monthlyResultRepository.findByEmployeeIdOrderByResultYearDescResultMonthDesc(employeeId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MonthlyResultDto.CompanyLeaderboard getCompanyLeaderboard(Long companyId, int month, int year) {
        List<MonthlyResult> results = monthlyResultRepository.findCompanyResultsForMonth(companyId, month, year);
        List<MonthlyResultDto.LeaderboardEntry> entries = new ArrayList<>();
        AtomicInteger rank = new AtomicInteger(1);
        results.stream()
                .sorted(Comparator.comparingDouble(MonthlyResult::getPercentageScore).reversed())
                .forEach(r -> {
                    MonthlyResultDto.LeaderboardEntry entry = new MonthlyResultDto.LeaderboardEntry();
                    entry.setRank(rank.getAndIncrement());
                    entry.setUserId(r.getEmployee().getId());
                    entry.setFullName(r.getEmployee().getFullName());
                    entry.setUsername(r.getEmployee().getUsername());
                    entry.setProfilePicture(r.getEmployee().getProfilePicture());
                    entry.setJobTitle(r.getEmployee().getJobTitle());
                    entry.setDepartment(r.getEmployee().getDepartment());
                    entry.setPercentageScore(r.getPercentageScore());
                    entry.setGrade(r.getGrade());
                    entry.setCreditsEarned(r.getCreditsEarned());
                    entry.setPerformanceCategory(r.getPerformanceCategory());
                    entries.add(entry);
                });

        MonthlyResultDto.CompanyLeaderboard lb = new MonthlyResultDto.CompanyLeaderboard();
        lb.setMonth(month);
        lb.setYear(year);
        lb.setEntries(entries);
        return lb;
    }

    @Transactional
    public MonthlyResultDto.Response triggerManualProcessing(Long employeeId, int month, int year) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));

        if (monthlyResultRepository.existsByEmployeeIdAndResultMonthAndResultYear(employeeId, month, year)) {
            throw new BadRequestException("Result already exists for this month. Use publish instead.");
        }

        computeAndSaveResult(employee, month, year);
        return monthlyResultRepository.findByEmployeeIdAndResultMonthAndResultYear(employeeId, month, year)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Failed to generate result"));
    }

    private MonthlyResultDto.Response mapToResponse(MonthlyResult r) {
        MonthlyResultDto.Response res = new MonthlyResultDto.Response();
        res.setId(r.getId());
        res.setEmployeeId(r.getEmployee().getId());
        res.setEmployeeName(r.getEmployee().getFullName());
        res.setEmployeeAvatar(r.getEmployee().getProfilePicture());
        res.setEmployeeJobTitle(r.getEmployee().getJobTitle());
        res.setEmployeeDepartment(r.getEmployee().getDepartment());
        res.setResultMonth(r.getResultMonth());
        res.setResultYear(r.getResultYear());
        res.setTotalScore(r.getTotalScore());
        res.setMaxPossibleScore(r.getMaxPossibleScore());
        res.setPercentageScore(r.getPercentageScore());
        res.setGrade(r.getGrade());
        res.setRankInCompany(r.getRankInCompany());
        res.setRankInDepartment(r.getRankInDepartment());
        res.setCreditsEarned(r.getCreditsEarned());
        res.setPerformanceCategory(r.getPerformanceCategory());
        res.setManagerComments(r.getManagerComments());
        res.setIsPublished(r.getIsPublished());
        res.setCreatedAt(r.getCreatedAt());
        return res;
    }
}
