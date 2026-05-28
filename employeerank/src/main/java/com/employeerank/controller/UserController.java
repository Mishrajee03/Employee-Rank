package com.employeerank.controller;

import com.employeerank.dto.ApiResponse;
import com.employeerank.dto.UserDto;
import com.employeerank.entity.User;
import com.employeerank.repository.UserRepository;
import com.employeerank.service.impl.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and leaderboard endpoints")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/api/users/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserDto.ProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(user.getId())));
    }

    @PutMapping("/api/users/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserDto.ProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserDto.UpdateRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(ApiResponse.success("Profile updated", userService.updateProfile(user.getId(), request)));
    }

    @GetMapping("/api/public/profile/{username}")
    @Operation(summary = "Get public profile by username")
    public ResponseEntity<ApiResponse<UserDto.PublicProfile>> getPublicProfile(@PathVariable String username) {
        return ResponseEntity.ok(ApiResponse.success(userService.getPublicProfile(username)));
    }

    @GetMapping("/api/leaderboard/global")
    @Operation(summary = "Get global leaderboard")
    public ResponseEntity<ApiResponse<Page<UserDto.LeaderboardEntry>>> getGlobalLeaderboard(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userService.getPublicLeaderboard(pageable)));
    }

    @GetMapping("/api/leaderboard/company/{companyId}")
    @Operation(summary = "Get company leaderboard")
    public ResponseEntity<ApiResponse<List<UserDto.LeaderboardEntry>>> getCompanyLeaderboard(
            @PathVariable Long companyId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getCompanyLeaderboard(companyId)));
    }

    @GetMapping("/api/public/search")
    @Operation(summary = "Search public profiles")
    public ResponseEntity<ApiResponse<Page<UserDto.PublicProfile>>> searchProfiles(
            @RequestParam String query,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userService.searchProfiles(query, pageable)));
    }
}
