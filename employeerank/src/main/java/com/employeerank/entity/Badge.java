package com.employeerank.entity;

import com.employeerank.enums.BadgeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "badges")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_type", nullable = false)
    private BadgeType badgeType;

    @Column(name = "badge_name", nullable = false, length = 100)
    private String badgeName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "awarded_month")
    private Integer awardedMonth;

    @Column(name = "awarded_year")
    private Integer awardedYear;

    @CreationTimestamp
    @Column(name = "awarded_at", updatable = false)
    private LocalDateTime awardedAt;
}
