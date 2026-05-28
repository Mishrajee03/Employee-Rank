package com.employeerank.controller;

import com.employeerank.dto.ApiResponse;
import com.employeerank.dto.JobPostingDto;
import com.employeerank.entity.User;
import com.employeerank.repository.UserRepository;
import com.employeerank.service.impl.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Job postings and applications")
public class JobController {

    private final JobService jobService;
    private final UserRepository userRepository;

    @PostMapping("/company/{companyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY')")
    @Operation(summary = "Create job posting")
    public ResponseEntity<ApiResponse<JobPostingDto.Response>> createJob(
            @PathVariable Long companyId,
            @Valid @RequestBody JobPostingDto.CreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Job posted successfully",
                jobService.createJobPosting(companyId, request)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search all jobs (public)")
    public ResponseEntity<ApiResponse<Page<JobPostingDto.Response>>> searchJobs(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(jobService.searchAllJobs(query, pageable)));
    }

    @GetMapping("/eligible")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE')")
    @Operation(summary = "Get jobs eligible for current employee based on credits")
    public ResponseEntity<ApiResponse<Page<JobPostingDto.Response>>> getEligibleJobs(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String query,
            @PageableDefault(size = 10) Pageable pageable) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(ApiResponse.success(jobService.getJobsForEmployee(user.getId(), query, pageable)));
    }

    @PostMapping("/{jobId}/apply")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE')")
    @Operation(summary = "Apply for a job")
    public ResponseEntity<ApiResponse<JobPostingDto.ApplicationResponse>> applyForJob(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long jobId,
            @RequestBody(required = false) JobPostingDto.ApplicationRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        String coverLetter = request != null ? request.getCoverLetter() : null;
        return ResponseEntity.ok(ApiResponse.success("Application submitted",
                jobService.applyForJob(user.getId(), jobId, coverLetter)));
    }

    @GetMapping("/my-applications")
    @Operation(summary = "Get my job applications")
    public ResponseEntity<ApiResponse<List<JobPostingDto.ApplicationResponse>>> getMyApplications(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(ApiResponse.success(jobService.getMyApplications(user.getId())));
    }

    @GetMapping("/{jobId}/applications")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY')")
    @Operation(summary = "Get applications for a job posting")
    public ResponseEntity<ApiResponse<List<JobPostingDto.ApplicationResponse>>> getJobApplications(
            @PathVariable Long jobId) {
        return ResponseEntity.ok(ApiResponse.success(jobService.getApplicationsForJob(jobId)));
    }

    @PatchMapping("/applications/{applicationId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY')")
    @Operation(summary = "Update application status")
    public ResponseEntity<ApiResponse<JobPostingDto.ApplicationResponse>> updateStatus(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                jobService.updateApplicationStatus(applicationId,
                        request.get("status"), request.get("notes"))));
    }
}
