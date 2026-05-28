package com.employeerank.entity;

import com.employeerank.enums.ScoreCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(name = "scores")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scored_by_id", nullable = false)
    private User scoredBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ScoreCategory category;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "max_points", nullable = false)
    private Integer maxPoints;

    @Column(name = "comments", length = 500)
    private String comments;

    @Column(name = "score_month", nullable = false)
    private Integer scoreMonth;

    @Column(name = "score_year", nullable = false)
    private Integer scoreYear;

    @Column(name = "is_peer_review")
    @Builder.Default
    private Boolean isPeerReview = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public double getPercentage() {
        if (maxPoints == 0) return 0;
        return (double) points / maxPoints * 100;
    }
}
