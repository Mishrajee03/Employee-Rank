package com.employeerank.controller;

import com.employeerank.dto.ApiResponse;
import com.employeerank.dto.CompanyDto;
import com.employeerank.entity.User;
import com.employeerank.repository.UserRepository;
import com.employeerank.service.impl.CompanyService;
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

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Company management endpoints")
public class CompanyController {

    private final CompanyService companyService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY')")
    @Operation(summary = "Create a company")
    public ResponseEntity<ApiResponse<CompanyDto.Response>> createCompany(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CompanyDto.CreateRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(ApiResponse.success("Company created", companyService.createCompany(user.getId(), request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get company by ID")
    public ResponseEntity<ApiResponse<CompanyDto.Response>> getCompany(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(companyService.getCompany(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY')")
    @Operation(summary = "Update company")
    public ResponseEntity<ApiResponse<CompanyDto.Response>> updateCompany(
            @PathVariable Long id,
            @RequestBody CompanyDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Company updated", companyService.updateCompany(id, request)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search companies")
    public ResponseEntity<ApiResponse<Page<CompanyDto.Response>>> searchCompanies(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(companyService.searchCompanies(query, pageable)));
    }

    @GetMapping("/hiring")
    @Operation(summary = "Get companies that are hiring")
    public ResponseEntity<ApiResponse<List<CompanyDto.Response>>> getHiringCompanies() {
        return ResponseEntity.ok(ApiResponse.success(companyService.getHiringCompanies()));
    }
}
