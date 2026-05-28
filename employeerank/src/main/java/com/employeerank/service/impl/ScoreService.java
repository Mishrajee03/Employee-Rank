package com.employeerank.service.impl;

import com.employeerank.dto.ScoreDto;
import com.employeerank.entity.Score;
import com.employeerank.entity.User;
import com.employeerank.enums.Role;
import com.employeerank.enums.ScoreCategory;
import com.employeerank.exception.BadRequestException;
import com.employeerank.exception.ResourceNotFoundException;
import com.employeerank.exception.UnauthorizedException;
import com.employeerank.repository.ScoreRepository;
import com.employeerank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;

    @Transactional
    public ScoreDto.Response addScore(Long scorerUserId, ScoreDto.CreateRequest request) {
        User scorer = userRepository.findById(scorerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Scorer user", scorerUserId));
        User employee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", request.getEmployeeId()));

        // Validate scorer permissions
        if (request.getIsPeerReview()) {
            if (scorer.getRole() == Role.ROLE_COMPANY) {
                throw new UnauthorizedException("Companies cannot give peer reviews");
            }
            if (scorerUserId.equals(request.getEmployeeId())) {
                throw new BadRequestException("Cannot score yourself");
            }
        } else {
            if (scorer.getRole() == Role.ROLE_EMPLOYEE) {
                throw new UnauthorizedException("Employees can only submit peer reviews");
            }
        }

        // Check if already scored this category this month
        scoreRepository.findByEmployeeIdAndScoredByIdAndCategoryAndScoreMonthAndScoreYear(
                employee.getId(), scorer.getId(), request.getCategory(),
                request.getScoreMonth(), request.getScoreYear()
        ).ifPresent(s -> {
            throw new BadRequestException("You have already scored " + employee.getFullName() +
                    " for " + request.getCategory() + " this month");
        });

        Score score = Score.builder()
                .employee(employee)
                .scoredBy(scorer)
                .category(request.getCategory())
                .points(request.getPoints())
                .maxPoints(request.getMaxPoints())
                .comments(request.getComments())
                .scoreMonth(request.getScoreMonth())
                .scoreYear(request.getScoreYear())
                .isPeerReview(request.getIsPeerReview())
                .build();

        score = scoreRepository.save(score);
        log.info("Score added for employee {} by scorer {} - Category: {}, Points: {}/{}",
                employee.getId(), scorer.getId(), request.getCategory(), request.getPoints(), request.getMaxPoints());

        return mapToResponse(score);
    }

    @Transactional(readOnly = true)
    public List<ScoreDto.Response> getEmployeeScores(Long employeeId, Integer month, Integer year) {
        List<Score> scores;
        if (month != null && year != null) {
            scores = scoreRepository.findByEmployeeIdAndScoreMonthAndScoreYear(employeeId, month, year);
        } else {
            scores = scoreRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
        }
        return scores.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ScoreDto.MonthSummary getMonthSummary(Long employeeId, Integer month, Integer year) {
        List<Score> scores = scoreRepository.findByEmployeeIdAndScoreMonthAndScoreYear(employeeId, month, year);

        double totalPoints = scores.stream().mapToInt(Score::getPoints).sum();
        double maxPoints = scores.stream().mapToInt(Score::getMaxPoints).sum();
        double percentage = maxPoints > 0 ? (totalPoints / maxPoints) * 100 : 0;

        Map<ScoreCategory, List<Score>> byCategory = scores.stream()
                .collect(Collectors.groupingBy(Score::getCategory));

        List<ScoreDto.CategorySummary> categoryBreakdown = new ArrayList<>();
        for (Map.Entry<ScoreCategory, List<Score>> entry : byCategory.entrySet()) {
            ScoreDto.CategorySummary summary = new ScoreDto.CategorySummary();
            summary.setCategory(entry.getKey());
            summary.setTotalPoints(entry.getValue().stream().mapToInt(Score::getPoints).sum() * 1.0);
            summary.setMaxPoints(entry.getValue().stream().mapToInt(Score::getMaxPoints).sum() * 1.0);
            summary.setPercentage(summary.getMaxPoints() > 0 ?
                    (summary.getTotalPoints() / summary.getMaxPoints()) * 100 : 0);
            summary.setScoreCount((long) entry.getValue().size());
            categoryBreakdown.add(summary);
        }

        ScoreDto.MonthSummary ms = new ScoreDto.MonthSummary();
        ms.setMonth(month);
        ms.setYear(year);
        ms.setTotalPoints(totalPoints);
        ms.setMaxPossiblePoints(maxPoints);
        ms.setPercentage(Math.round(percentage * 100.0) / 100.0);
        ms.setGrade(calculateGrade(percentage));
        ms.setCategoryBreakdown(categoryBreakdown);
        return ms;
    }

    public static String calculateGrade(double percentage) {
        if (percentage >= 95) return "A+";
        if (percentage >= 90) return "A";
        if (percentage >= 85) return "A-";
        if (percentage >= 80) return "B+";
        if (percentage >= 75) return "B";
        if (percentage >= 70) return "B-";
        if (percentage >= 65) return "C+";
        if (percentage >= 60) return "C";
        if (percentage >= 55) return "C-";
        if (percentage >= 50) return "D";
        return "F";
    }

    public static String getPerformanceCategory(double percentage) {
        if (percentage >= 90) return "Outstanding";
        if (percentage >= 80) return "Exceeds Expectations";
        if (percentage >= 70) return "Meets Expectations";
        if (percentage >= 60) return "Needs Improvement";
        return "Unsatisfactory";
    }

    public static int calculateCredits(double percentage) {
        if (percentage >= 95) return 100;
        if (percentage >= 90) return 90;
        if (percentage >= 85) return 80;
        if (percentage >= 80) return 70;
        if (percentage >= 75) return 60;
        if (percentage >= 70) return 50;
        if (percentage >= 60) return 35;
        if (percentage >= 50) return 20;
        return 10;
    }

    private ScoreDto.Response mapToResponse(Score score) {
        ScoreDto.Response r = new ScoreDto.Response();
        r.setId(score.getId());
        r.setEmployeeId(score.getEmployee().getId());
        r.setEmployeeName(score.getEmployee().getFullName());
        r.setEmployeeAvatar(score.getEmployee().getProfilePicture());
        r.setScoredById(score.getScoredBy().getId());
        r.setScoredByName(score.getScoredBy().getFullName());
        r.setCategory(score.getCategory());
        r.setPoints(score.getPoints());
        r.setMaxPoints(score.getMaxPoints());
        r.setPercentage(Math.round(score.getPercentage() * 100.0) / 100.0);
        r.setComments(score.getComments());
        r.setScoreMonth(score.getScoreMonth());
        r.setScoreYear(score.getScoreYear());
        r.setIsPeerReview(score.getIsPeerReview());
        r.setCreatedAt(score.getCreatedAt());
        return r;
    }
}
