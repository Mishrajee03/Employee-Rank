package com.employeerank.repository;

import com.employeerank.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByApplicantIdOrderByAppliedAtDesc(Long applicantId);

    List<JobApplication> findByJobPostingIdOrderByAppliedAtDesc(Long jobPostingId);

    boolean existsByApplicantIdAndJobPostingId(Long applicantId, Long jobPostingId);

    Optional<JobApplication> findByApplicantIdAndJobPostingId(Long applicantId, Long jobPostingId);

    @Query("SELECT ja FROM JobApplication ja WHERE ja.jobPosting.company.id = :companyId ORDER BY ja.appliedAt DESC")
    List<JobApplication> findByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.jobPosting.id = :jobId")
    long countApplicationsByJob(@Param("jobId") Long jobId);
}
