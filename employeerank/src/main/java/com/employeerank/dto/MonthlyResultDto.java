package com.employeerank.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

public class MonthlyResultDto {

    @Data
    public static class Response {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private String employeeAvatar;
        private String employeeJobTitle;
        private String employeeDepartment;
        private Integer resultMonth;
        private Integer resultYear;
        private Double totalScore;
        private Double maxPossibleScore;
        private Double percentageScore;
        private String grade;
        private Integer rankInCompany;
        private Integer rankInDepartment;
        private Integer creditsEarned;
        private String performanceCategory;
        private String managerComments;
        private Boolean isPublished;
        private LocalDateTime createdAt;
    }

    @Data
    public static class PublicResult {
        private Integer resultMonth;
        private Integer resultYear;
        private Double percentageScore;
        private String grade;
        private Integer rankInCompany;
        private Integer creditsEarned;
        private String performanceCategory;
    }

    @Data
    public static class CompanyLeaderboard {
        private Integer month;
        private Integer year;
        private List<LeaderboardEntry> entries;
    }

    @Data
    public static class LeaderboardEntry {
        private Integer rank;
        private Long userId;
        private String fullName;
        private String username;
        private String profilePicture;
        private String jobTitle;
        private String department;
        private Double percentageScore;
        private String grade;
        private Integer creditsEarned;
        private String performanceCategory;
    }

    @Data
    public static class PublishRequest {
        private String managerComments;
    }
}
