package com.employeerank.controller;

import com.employeerank.dto.ApiResponse;
import com.employeerank.dto.ScoreDto;
import com.employeerank.entity.User;
import com.employeerank.repository.UserRepository;
import com.employeerank.service.impl.ScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
@Tag(name = "Scores", description = "Employee scoring endpoints")
public class ScoreController {

    private final ScoreService scoreService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Add a score for an employee")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<ScoreDto.Response>> addScore(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ScoreDto.CreateRequest request) {
        User scorer = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();
        return ResponseEntity.ok(ApiResponse.success("Score added successfully",
                scoreService.addScore(scorer.getId(), request)));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get scores for an employee")
    public ResponseEntity<ApiResponse<List<ScoreDto.Response>>> getEmployeeScores(
            @PathVariable Long employeeId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.success(
                scoreService.getEmployeeScores(employeeId, month, year)));
    }

    @GetMapping("/employee/{employeeId}/summary")
    @Operation(summary = "Get monthly score summary for employee")
    public ResponseEntity<ApiResponse<ScoreDto.MonthSummary>> getMonthSummary(
            @PathVariable Long employeeId,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return ResponseEntity.ok(ApiResponse.success(
                scoreService.getMonthSummary(employeeId, month, year)));
    }

    @GetMapping("/my-scores")
    @Operation(summary = "Get current user's scores")
    public ResponseEntity<ApiResponse<List<ScoreDto.Response>>> getMyScores(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(ApiResponse.success(
                scoreService.getEmployeeScores(user.getId(), month, year)));
    }

    @GetMapping("/my-summary")
    @Operation(summary = "Get current user's monthly summary")
    public ResponseEntity<ApiResponse<ScoreDto.MonthSummary>> getMyMonthSummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(ApiResponse.success(
                scoreService.getMonthSummary(user.getId(), month, year)));
    }
}
