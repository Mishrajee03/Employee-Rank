package com.employeerank.repository;

import com.employeerank.entity.MonthlyResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyResultRepository extends JpaRepository<MonthlyResult, Long> {

    Optional<MonthlyResult> findByEmployeeIdAndResultMonthAndResultYear(Long employeeId, Integer month, Integer year);

    List<MonthlyResult> findByEmployeeIdOrderByResultYearDescResultMonthDesc(Long employeeId);

    @Query("SELECT mr FROM MonthlyResult mr WHERE mr.employee.company.id = :companyId AND mr.resultMonth = :month AND mr.resultYear = :year AND mr.isPublished = true ORDER BY mr.percentageScore DESC")
    List<MonthlyResult> findCompanyResultsForMonth(@Param("companyId") Long companyId, @Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT mr FROM MonthlyResult mr WHERE mr.isPublished = true AND mr.resultMonth = :month AND mr.resultYear = :year ORDER BY mr.percentageScore DESC")
    Page<MonthlyResult> findGlobalLeaderboardForMonth(@Param("month") Integer month, @Param("year") Integer year, Pageable pageable);

    @Query("SELECT mr FROM MonthlyResult mr WHERE mr.employee.company.id = :companyId AND mr.resultYear = :year AND mr.isPublished = true ORDER BY mr.resultMonth ASC")
    List<MonthlyResult> findCompanyResultsForYear(@Param("companyId") Long companyId, @Param("year") Integer year);

    boolean existsByEmployeeIdAndResultMonthAndResultYear(Long employeeId, Integer month, Integer year);

    @Query("SELECT mr FROM MonthlyResult mr WHERE mr.employee.id = :employeeId AND mr.isPublished = true ORDER BY mr.resultYear DESC, mr.resultMonth DESC")
    List<MonthlyResult> findPublishedByEmployee(@Param("employeeId") Long employeeId);
}
