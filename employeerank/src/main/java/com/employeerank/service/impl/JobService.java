package com.employeerank.service.impl;

import com.employeerank.dto.JobPostingDto;
import com.employeerank.entity.Company;
import com.employeerank.entity.JobApplication;
import com.employeerank.entity.JobPosting;
import com.employeerank.entity.User;
import com.employeerank.exception.BadRequestException;
import com.employeerank.exception.ResourceNotFoundException;
import com.employeerank.repository.CompanyRepository;
import com.employeerank.repository.JobApplicationRepository;
import com.employeerank.repository.JobPostingRepository;
import com.employeerank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Transactional
    public JobPostingDto.Response createJobPosting(Long companyId, JobPostingDto.CreateRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        JobPosting job = JobPosting.builder()
                .company(company)
                .title(request.getTitle())
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .salaryRange(request.getSalaryRange())
                .location(request.getLocation())
                .jobType(request.getJobType())
                .minCreditsRequired(request.getMinCreditsRequired())
                .minGrade(request.getMinGrade())
                .expiresAt(request.getExpiresAt())
                .build();

        return mapToResponse(jobPostingRepository.save(job), null);
    }

    @Transactional(readOnly = true)
    public Page<JobPostingDto.Response> getJobsForEmployee(Long employeeId, String query, Pageable pageable) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));
        int credits = employee.getTotalCredits();

        Page<JobPosting> jobs;
        if (query != null && !query.isBlank()) {
            jobs = jobPostingRepository.searchEligibleJobs(credits, query, pageable);
        } else {
            jobs = jobPostingRepository.findEligibleJobs(credits, pageable);
        }

        return jobs.map(j -> mapToResponse(j, employeeId));
    }

    @Transactional(readOnly = true)
    public Page<JobPostingDto.Response> searchAllJobs(String query, Pageable pageable) {
        if (query != null && !query.isBlank()) {
            return jobPostingRepository.searchJobs(query, pageable).map(j -> mapToResponse(j, null));
        }
        return jobPostingRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable).map(j -> mapToResponse(j, null));
    }

    @Transactional
    public JobPostingDto.ApplicationResponse applyForJob(Long applicantId, Long jobId, String coverLetter) {
        User applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", applicantId));
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));

        if (!job.getIsActive()) throw new BadRequestException("This job posting is no longer active");
        if (jobApplicationRepository.existsByApplicantIdAndJobPostingId(applicantId, jobId)) {
            throw new BadRequestException("You have already applied for this position");
        }
        if (applicant.getTotalCredits() < job.getMinCreditsRequired()) {
            throw new BadRequestException("You need at least " + job.getMinCreditsRequired() +
                    " credits to apply. You currently have " + applicant.getTotalCredits());
        }

        JobApplication application = JobApplication.builder()
                .applicant(applicant)
                .jobPosting(job)
                .coverLetter(coverLetter)
                .status("PENDING")
                .build();

        return mapApplicationToResponse(jobApplicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public List<JobPostingDto.ApplicationResponse> getApplicationsForJob(Long jobId) {
        return jobApplicationRepository.findByJobPostingIdOrderByAppliedAtDesc(jobId)
                .stream().map(this::mapApplicationToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobPostingDto.ApplicationResponse> getMyApplications(Long applicantId) {
        return jobApplicationRepository.findByApplicantIdOrderByAppliedAtDesc(applicantId)
                .stream().map(this::mapApplicationToResponse).collect(Collectors.toList());
    }

    @Transactional
    public JobPostingDto.ApplicationResponse updateApplicationStatus(Long applicationId, String status, String notes) {
        JobApplication app = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", applicationId));
        app.setStatus(status);
        app.setCompanyNotes(notes);
        return mapApplicationToResponse(jobApplicationRepository.save(app));
    }

    private JobPostingDto.Response mapToResponse(JobPosting job, Long requestingUserId) {
        JobPostingDto.Response r = new JobPostingDto.Response();
        r.setId(job.getId());
        r.setCompanyId(job.getCompany().getId());
        r.setCompanyName(job.getCompany().getName());
        r.setCompanyLogoUrl(job.getCompany().getLogoUrl());
        r.setCompanyIndustry(job.getCompany().getIndustry());
        r.setCompanyIsVerified(job.getCompany().getIsVerified());
        r.setTitle(job.getTitle());
        r.setDescription(job.getDescription());
        r.setRequirements(job.getRequirements());
        r.setSalaryRange(job.getSalaryRange());
        r.setLocation(job.getLocation());
        r.setJobType(job.getJobType());
        r.setMinCreditsRequired(job.getMinCreditsRequired());
        r.setMinGrade(job.getMinGrade());
        r.setIsActive(job.getIsActive());
        r.setViewsCount(job.getViewsCount());
        r.setApplicationCount(jobApplicationRepository.countApplicationsByJob(job.getId()));
        r.setCreatedAt(job.getCreatedAt());
        r.setExpiresAt(job.getExpiresAt());
        if (requestingUserId != null) {
            r.setAlreadyApplied(jobApplicationRepository.existsByApplicantIdAndJobPostingId(requestingUserId, job.getId()));
        }
        return r;
    }

    private JobPostingDto.ApplicationResponse mapApplicationToResponse(JobApplication app) {
        JobPostingDto.ApplicationResponse r = new JobPostingDto.ApplicationResponse();
        r.setId(app.getId());
        r.setApplicantId(app.getApplicant().getId());
        r.setApplicantName(app.getApplicant().getFullName());
        r.setApplicantEmail(app.getApplicant().getEmail());
        r.setApplicantCredits(app.getApplicant().getTotalCredits());
        r.setApplicantJobTitle(app.getApplicant().getJobTitle());
        r.setCoverLetter(app.getCoverLetter());
        r.setStatus(app.getStatus());
        r.setCompanyNotes(app.getCompanyNotes());
        r.setAppliedAt(app.getAppliedAt());
        return r;
    }
}
