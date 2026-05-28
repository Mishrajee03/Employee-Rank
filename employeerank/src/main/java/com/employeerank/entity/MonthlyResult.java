package com.employeerank.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_results", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "result_month", "result_year"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MonthlyResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @Column(name = "result_month", nullable = false)
    private Integer resultMonth;

    @Column(name = "result_year", nullable = false)
    private Integer resultYear;

    @Column(name = "total_score", nullable = false)
    private Double totalScore;

    @Column(name = "max_possible_score", nullable = false)
    private Double maxPossibleScore;

    @Column(name = "percentage_score", nullable = false)
    private Double percentageScore;

    @Column(name = "grade", length = 2)
    private String grade;

    @Column(name = "rank_in_company")
    private Integer rankInCompany;

    @Column(name = "rank_in_department")
    private Integer rankInDepartment;

    @Column(name = "credits_earned")
    private Integer creditsEarned;

    @Column(name = "performance_category", length = 50)
    private String performanceCategory;

    @Column(name = "manager_comments", length = 1000)
    private String managerComments;

    @Column(name = "is_published")
    @Builder.Default
    private Boolean isPublished = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
