package com.employeerank.dto;

import com.employeerank.enums.BadgeType;
import com.employeerank.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class UserDto {

    @Data
    public static class ProfileResponse {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String profilePicture;
        private String bio;
        private String jobTitle;
        private String department;
        private String phone;
        private Role role;
        private Boolean isPublicProfile;
        private Integer totalCredits;
        private String linkedinUrl;
        private String githubUrl;
        private String portfolioUrl;
        private Integer yearsOfExperience;
        private String skills;
        private String companyName;
        private Long companyId;
        private String grade;
        private Integer rankInCompany;
        private List<BadgeResponse> badges;
        private LocalDateTime createdAt;
    }

    @Data
    public static class PublicProfile {
        private Long id;
        private String username;
        private String fullName;
        private String profilePicture;
        private String bio;
        private String jobTitle;
        private String department;
        private Role role;
        private Integer totalCredits;
        private String linkedinUrl;
        private String githubUrl;
        private String portfolioUrl;
        private Integer yearsOfExperience;
        private String skills;
        private String companyName;
        private String grade;
        private List<BadgeResponse> badges;
        private List<MonthlyResultDto.PublicResult> recentResults;
    }

    @Data
    public static class UpdateRequest {
        @Size(max = 100)
        private String fullName;

        @Size(max = 500)
        private String bio;

        @Size(max = 100)
        private String jobTitle;

        @Size(max = 100)
        private String department;

        @Size(max = 20)
        private String phone;

        private Boolean isPublicProfile;
        private String linkedinUrl;
        private String githubUrl;
        private String portfolioUrl;
        private Integer yearsOfExperience;

        @Size(max = 1000)
        private String skills;
    }

    @Data
    public static class LeaderboardEntry {
        private Long id;
        private String username;
        private String fullName;
        private String profilePicture;
        private String jobTitle;
        private String department;
        private Integer totalCredits;
        private String grade;
        private Integer rank;
        private String companyName;
        private List<BadgeResponse> badges;
    }

    @Data
    public static class BadgeResponse {
        private Long id;
        private BadgeType badgeType;
        private String badgeName;
        private String description;
        private Integer awardedMonth;
        private Integer awardedYear;
        private LocalDateTime awardedAt;
    }
}
