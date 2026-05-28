package com.employeerank.service.impl;

import com.employeerank.dto.MonthlyResultDto;
import com.employeerank.dto.UserDto;
import com.employeerank.entity.Badge;
import com.employeerank.entity.MonthlyResult;
import com.employeerank.entity.User;
import com.employeerank.enums.Role;
import com.employeerank.exception.ResourceNotFoundException;
import com.employeerank.repository.BadgeRepository;
import com.employeerank.repository.MonthlyResultRepository;
import com.employeerank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final MonthlyResultRepository monthlyResultRepository;

    @Transactional(readOnly = true)
    public UserDto.ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return mapToProfile(user);
    }

    @Transactional(readOnly = true)
    public UserDto.PublicProfile getPublicProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return mapToPublicProfile(user);
    }

    @Transactional
    public UserDto.ProfileResponse updateProfile(Long userId, UserDto.UpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getJobTitle() != null) user.setJobTitle(request.getJobTitle());
        if (request.getDepartment() != null) user.setDepartment(request.getDepartment());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getIsPublicProfile() != null) user.setIsPublicProfile(request.getIsPublicProfile());
        if (request.getLinkedinUrl() != null) user.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getGithubUrl() != null) user.setGithubUrl(request.getGithubUrl());
        if (request.getPortfolioUrl() != null) user.setPortfolioUrl(request.getPortfolioUrl());
        if (request.getYearsOfExperience() != null) user.setYearsOfExperience(request.getYearsOfExperience());
        if (request.getSkills() != null) user.setSkills(request.getSkills());

        return mapToProfile(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Page<UserDto.LeaderboardEntry> getPublicLeaderboard(Pageable pageable) {
        return userRepository.findPublicLeaderboard(Role.ROLE_EMPLOYEE, pageable)
                .map(this::mapToLeaderboardEntry);
    }

    @Transactional(readOnly = true)
    public List<UserDto.LeaderboardEntry> getCompanyLeaderboard(Long companyId) {
        return userRepository.findCompanyLeaderboard(companyId)
                .stream().map(this::mapToLeaderboardEntry).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<UserDto.PublicProfile> searchProfiles(String query, Pageable pageable) {
        return userRepository.searchPublicProfiles(query, pageable).map(this::mapToPublicProfile);
    }

    private UserDto.ProfileResponse mapToProfile(User user) {
        UserDto.ProfileResponse profile = new UserDto.ProfileResponse();
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setEmail(user.getEmail());
        profile.setFullName(user.getFullName());
        profile.setProfilePicture(user.getProfilePicture());
        profile.setBio(user.getBio());
        profile.setJobTitle(user.getJobTitle());
        profile.setDepartment(user.getDepartment());
        profile.setPhone(user.getPhone());
        profile.setRole(user.getRole());
        profile.setIsPublicProfile(user.getIsPublicProfile());
        profile.setTotalCredits(user.getTotalCredits());
        profile.setLinkedinUrl(user.getLinkedinUrl());
        profile.setGithubUrl(user.getGithubUrl());
        profile.setPortfolioUrl(user.getPortfolioUrl());
        profile.setYearsOfExperience(user.getYearsOfExperience());
        profile.setSkills(user.getSkills());
        if (user.getCompany() != null) {
            profile.setCompanyId(user.getCompany().getId());
            profile.setCompanyName(user.getCompany().getName());
        }
        profile.setBadges(mapBadges(badgeRepository.findByEmployeeIdOrderByAwardedAtDesc(user.getId())));
        profile.setCreatedAt(user.getCreatedAt());
        return profile;
    }

    private UserDto.PublicProfile mapToPublicProfile(User user) {
        UserDto.PublicProfile profile = new UserDto.PublicProfile();
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setFullName(user.getFullName());
        profile.setProfilePicture(user.getProfilePicture());
        profile.setBio(user.getBio());
        profile.setJobTitle(user.getJobTitle());
        profile.setDepartment(user.getDepartment());
        profile.setRole(user.getRole());
        profile.setTotalCredits(user.getTotalCredits());
        profile.setLinkedinUrl(user.getLinkedinUrl());
        profile.setGithubUrl(user.getGithubUrl());
        profile.setPortfolioUrl(user.getPortfolioUrl());
        profile.setYearsOfExperience(user.getYearsOfExperience());
        profile.setSkills(user.getSkills());
        if (user.getCompany() != null) profile.setCompanyName(user.getCompany().getName());
        profile.setBadges(mapBadges(badgeRepository.findByEmployeeIdOrderByAwardedAtDesc(user.getId())));

        List<MonthlyResultDto.PublicResult> recentResults = monthlyResultRepository
                .findPublishedByEmployee(user.getId()).stream()
                .limit(6)
                .map(r -> {
                    MonthlyResultDto.PublicResult pr = new MonthlyResultDto.PublicResult();
                    pr.setResultMonth(r.getResultMonth());
                    pr.setResultYear(r.getResultYear());
                    pr.setPercentageScore(r.getPercentageScore());
                    pr.setGrade(r.getGrade());
                    pr.setRankInCompany(r.getRankInCompany());
                    pr.setCreditsEarned(r.getCreditsEarned());
                    pr.setPerformanceCategory(r.getPerformanceCategory());
                    return pr;
                }).collect(Collectors.toList());
        profile.setRecentResults(recentResults);
        return profile;
    }

    private UserDto.LeaderboardEntry mapToLeaderboardEntry(User user) {
        UserDto.LeaderboardEntry entry = new UserDto.LeaderboardEntry();
        entry.setId(user.getId());
        entry.setUsername(user.getUsername());
        entry.setFullName(user.getFullName());
        entry.setProfilePicture(user.getProfilePicture());
        entry.setJobTitle(user.getJobTitle());
        entry.setDepartment(user.getDepartment());
        entry.setTotalCredits(user.getTotalCredits());
        if (user.getCompany() != null) entry.setCompanyName(user.getCompany().getName());
        entry.setBadges(mapBadges(badgeRepository.findByEmployeeIdOrderByAwardedAtDesc(user.getId())));
        return entry;
    }

    private List<UserDto.BadgeResponse> mapBadges(List<Badge> badges) {
        return badges.stream().map(b -> {
            UserDto.BadgeResponse br = new UserDto.BadgeResponse();
            br.setId(b.getId());
            br.setBadgeType(b.getBadgeType());
            br.setBadgeName(b.getBadgeName());
            br.setDescription(b.getDescription());
            br.setAwardedMonth(b.getAwardedMonth());
            br.setAwardedYear(b.getAwardedYear());
            br.setAwardedAt(b.getAwardedAt());
            return br;
        }).collect(Collectors.toList());
    }
}
