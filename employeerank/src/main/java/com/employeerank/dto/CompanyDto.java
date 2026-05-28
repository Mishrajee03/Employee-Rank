package com.employeerank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class CompanyDto {

    @Data
    public static class CreateRequest {
        @NotBlank
        @Size(max = 100)
        private String name;

        @Size(max = 500)
        private String description;

        @Size(max = 100)
        private String industry;

        @Size(max = 200)
        private String location;

        private String websiteUrl;
        private String companySize;
        private Integer minCreditThreshold = 0;
    }

    @Data
    public static class UpdateRequest {
        private String description;
        private String industry;
        private String location;
        private String websiteUrl;
        private String companySize;
        private Boolean isHiring;
        private Integer minCreditThreshold;
    }

    @Data
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private String logoUrl;
        private String websiteUrl;
        private String industry;
        private String location;
        private String companySize;
        private Boolean isVerified;
        private Boolean isHiring;
        private Integer minCreditThreshold;
        private Long employeeCount;
        private LocalDateTime createdAt;
    }
}
