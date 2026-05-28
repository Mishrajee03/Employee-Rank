package com.employeerank.dto;

import com.employeerank.enums.ScoreCategory;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

public class ScoreDto {

    @Data
    public static class CreateRequest {
        @NotNull(message = "Employee ID is required")
        private Long employeeId;

        @NotNull(message = "Category is required")
        private ScoreCategory category;

        @NotNull(message = "Points are required")
        @Min(value = 0, message = "Points cannot be negative")
        @Max(value = 100, message = "Points cannot exceed max")
        private Integer points;

        @NotNull
        @Min(1)
        @Max(100)
        private Integer maxPoints;

        @Size(max = 500)
        private String comments;

        private Boolean isPeerReview = false;

        @NotNull @Min(1) @Max(12)
        private Integer scoreMonth;

        @NotNull @Min(2020)
        private Integer scoreYear;
    }

    @Data
    public static class Response {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private String employeeAvatar;
        private Long scoredById;
        private String scoredByName;
        private ScoreCategory category;
        private Integer points;
        private Integer maxPoints;
        private Double percentage;
        private String comments;
        private Integer scoreMonth;
        private Integer scoreYear;
        private Boolean isPeerReview;
        private LocalDateTime createdAt;
    }

    @Data
    public static class CategorySummary {
        private ScoreCategory category;
        private Double totalPoints;
        private Double maxPoints;
        private Double percentage;
        private Long scoreCount;
    }

    @Data
    public static class MonthSummary {
        private Integer month;
        private Integer year;
        private Double totalPoints;
        private Double maxPossiblePoints;
        private Double percentage;
        private String grade;
        private java.util.List<CategorySummary> categoryBreakdown;
    }
}
