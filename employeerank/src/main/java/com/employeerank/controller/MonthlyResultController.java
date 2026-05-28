package com.employeerank.controller;

import com.employeerank.dto.ApiResponse;
import com.employeerank.dto.MonthlyResultDto;
import com.employeerank.entity.User;
import com.employeerank.repository.UserRepository;
import com.employeerank.service.impl.MonthlyResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
@Tag(name = "Monthly Results", description = "Monthly result and ranking endpoints")
public class MonthlyResultController {

    private final MonthlyResultService monthlyResultService;
    private final UserRepository userRepository;

    @PostMapping("/process")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Trigger monthly result processing")
    public ResponseEntity<ApiResponse<String>> processResults(
            @RequestParam int month, @RequestParam int year) {
        monthlyResultService.processMonthlyResults(month, year);
        return ResponseEntity.ok(ApiResponse.success("Results processed successfully", null));
    }

    @PostMapping("/generate/{employeeId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Generate result for a specific employee")
    public ResponseEntity<ApiResponse<MonthlyResultDto.Response>> generateResult(
            @PathVariable Long employeeId,
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success(
                monthlyResultService.triggerManualProcessing(employeeId, month, year)));
    }

    @PostMapping("/{resultId}/publish")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Publish a monthly result")
    public ResponseEntity<ApiResponse<MonthlyResultDto.Response>> publishResult(
            @PathVariable Long resultId,
            @RequestBody(required = false) MonthlyResultDto.PublishRequest request) {
        String comments = request != null ? request.getManagerComments() : null;
        return ResponseEntity.ok(ApiResponse.success("Result published",
                monthlyResultService.publishResult(resultId, comments)));
    }

    @GetMapping("/my-results")
    @Operation(summary = "Get current user's monthly results")
    public ResponseEntity<ApiResponse<List<MonthlyResultDto.Response>>> getMyResults(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(ApiResponse.success(
                monthlyResultService.getEmployeeResults(user.getId())));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get monthly results for an employee")
    public ResponseEntity<ApiResponse<List<MonthlyResultDto.Response>>> getEmployeeResults(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success(
                monthlyResultService.getEmployeeResults(employeeId)));
    }

    @GetMapping("/company/{companyId}/leaderboard")
    @Operation(summary = "Get company leaderboard for a month")
    public ResponseEntity<ApiResponse<MonthlyResultDto.CompanyLeaderboard>> getCompanyLeaderboard(
            @PathVariable Long companyId,
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success(
                monthlyResultService.getCompanyLeaderboard(companyId, month, year)));
    }
}
