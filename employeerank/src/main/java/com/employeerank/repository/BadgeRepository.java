package com.employeerank.repository;

import com.employeerank.entity.Badge;
import com.employeerank.enums.BadgeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    List<Badge> findByEmployeeIdOrderByAwardedAtDesc(Long employeeId);
    boolean existsByEmployeeIdAndBadgeTypeAndAwardedMonthAndAwardedYear(Long employeeId, BadgeType badgeType, Integer month, Integer year);
}
