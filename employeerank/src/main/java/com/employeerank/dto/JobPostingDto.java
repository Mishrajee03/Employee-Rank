package com.employeerank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

public class JobPostingDto {

    @Data
    public static class CreateRequest {
        @NotBlank
        @Size(max = 200)
        private String title;

        @NotBlank
        private String description;

        private String requirements;

        @Size(max = 100)
        private String salaryRange;

        @Size(max = 200)
        private String location;

        private String jobType;

        @Min(0)
        private Integer minCreditsRequired = 0;

        private String minGrade;
        private LocalDateTime expiresAt;
    }

    @Data
    public static class Response {
        private Long id;
        private Long companyId;
        private String companyName;
        private String companyLogoUrl;
        private String companyIndustry;
        private Boolean companyIsVerified;
        private String title;
        private String description;
        private String requirements;
        private String salaryRange;
        private String location;
        private String jobType;
        private Integer minCreditsRequired;
        private String minGrade;
        private Boolean isActive;
        private Integer viewsCount;
        private Long applicationCount;
        private Boolean alreadyApplied;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
    }

    @Data
    public static class ApplicationRequest {
        private String coverLetter;
    }

    @Data
    public static class ApplicationResponse {
        private Long id;
        private Long applicantId;
        private String applicantName;
        private String applicantEmail;
        private Integer applicantCredits;
        private String applicantGrade;
        private String applicantJobTitle;
        private String coverLetter;
        private String status;
        private String companyNotes;
        private LocalDateTime appliedAt;
    }
}
