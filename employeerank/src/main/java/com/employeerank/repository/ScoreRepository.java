package com.employeerank.repository;

import com.employeerank.entity.Score;
import com.employeerank.enums.ScoreCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {

    List<Score> findByEmployeeIdAndScoreMonthAndScoreYear(Long employeeId, Integer month, Integer year);

    List<Score> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    @Query("SELECT s FROM Score s WHERE s.employee.id = :employeeId AND s.scoreYear = :year ORDER BY s.scoreMonth DESC")
    List<Score> findByEmployeeAndYear(@Param("employeeId") Long employeeId, @Param("year") Integer year);

    @Query("SELECT SUM(s.points) FROM Score s WHERE s.employee.id = :employeeId AND s.scoreMonth = :month AND s.scoreYear = :year")
    Optional<Double> sumPointsByEmployeeAndMonthYear(@Param("employeeId") Long employeeId, @Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT SUM(s.maxPoints) FROM Score s WHERE s.employee.id = :employeeId AND s.scoreMonth = :month AND s.scoreYear = :year")
    Optional<Double> sumMaxPointsByEmployeeAndMonthYear(@Param("employeeId") Long employeeId, @Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT s FROM Score s WHERE s.employee.company.id = :companyId AND s.scoreMonth = :month AND s.scoreYear = :year")
    List<Score> findByCompanyAndMonthYear(@Param("companyId") Long companyId, @Param("month") Integer month, @Param("year") Integer year);

    Optional<Score> findByEmployeeIdAndScoredByIdAndCategoryAndScoreMonthAndScoreYear(
            Long employeeId, Long scoredById, ScoreCategory category, Integer month, Integer year);

    @Query("SELECT s FROM Score s WHERE s.employee.id = :employeeId AND s.category = :category ORDER BY s.createdAt DESC")
    List<Score> findByEmployeeAndCategory(@Param("employeeId") Long employeeId, @Param("category") ScoreCategory category);

    @Query("SELECT COUNT(s) FROM Score s WHERE s.employee.id = :employeeId AND s.scoreMonth = :month AND s.scoreYear = :year")
    long countScoresByEmployeeAndMonthYear(@Param("employeeId") Long employeeId, @Param("month") Integer month, @Param("year") Integer year);
}
