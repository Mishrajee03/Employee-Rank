package com.employeerank.repository;

import com.employeerank.entity.JobPosting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    List<JobPosting> findByCompanyIdAndIsActiveTrue(Long companyId);

    @Query("SELECT j FROM JobPosting j WHERE j.isActive = true AND j.minCreditsRequired <= :credits ORDER BY j.createdAt DESC")
    Page<JobPosting> findEligibleJobs(@Param("credits") Integer credits, Pageable pageable);

    @Query("SELECT j FROM JobPosting j WHERE j.isActive = true AND (LOWER(j.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(j.description) LIKE LOWER(CONCAT('%', :query, '%'))) ORDER BY j.createdAt DESC")
    Page<JobPosting> searchJobs(@Param("query") String query, Pageable pageable);

    Page<JobPosting> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT j FROM JobPosting j WHERE j.isActive = true AND j.minCreditsRequired <= :credits AND LOWER(j.title) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY j.createdAt DESC")
    Page<JobPosting> searchEligibleJobs(@Param("credits") Integer credits, @Param("query") String query, Pageable pageable);
}
