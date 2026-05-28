package com.employeerank.service.impl;

import com.employeerank.dto.CompanyDto;
import com.employeerank.entity.Company;
import com.employeerank.entity.User;
import com.employeerank.exception.BadRequestException;
import com.employeerank.exception.ResourceNotFoundException;
import com.employeerank.repository.CompanyRepository;
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
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Transactional
    public CompanyDto.Response createCompany(Long adminUserId, CompanyDto.CreateRequest request) {
        if (companyRepository.existsByName(request.getName())) {
            throw new BadRequestException("Company with this name already exists");
        }
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", adminUserId));

        Company company = Company.builder()
                .name(request.getName())
                .description(request.getDescription())
                .industry(request.getIndustry())
                .location(request.getLocation())
                .websiteUrl(request.getWebsiteUrl())
                .companySize(request.getCompanySize())
                .minCreditThreshold(request.getMinCreditThreshold())
                .adminUser(admin)
                .build();

        return mapToResponse(companyRepository.save(company));
    }

    @Transactional(readOnly = true)
    public CompanyDto.Response getCompany(Long id) {
        return companyRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
    }

    @Transactional
    public CompanyDto.Response updateCompany(Long id, CompanyDto.UpdateRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
        if (request.getDescription() != null) company.setDescription(request.getDescription());
        if (request.getIndustry() != null) company.setIndustry(request.getIndustry());
        if (request.getLocation() != null) company.setLocation(request.getLocation());
        if (request.getWebsiteUrl() != null) company.setWebsiteUrl(request.getWebsiteUrl());
        if (request.getCompanySize() != null) company.setCompanySize(request.getCompanySize());
        if (request.getIsHiring() != null) company.setIsHiring(request.getIsHiring());
        if (request.getMinCreditThreshold() != null) company.setMinCreditThreshold(request.getMinCreditThreshold());
        return mapToResponse(companyRepository.save(company));
    }

    @Transactional(readOnly = true)
    public Page<CompanyDto.Response> searchCompanies(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return companyRepository.findAll(pageable).map(this::mapToResponse);
        }
        return companyRepository.searchCompanies(query, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<CompanyDto.Response> getHiringCompanies() {
        return companyRepository.findByIsHiringTrue().stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    private CompanyDto.Response mapToResponse(Company c) {
        CompanyDto.Response r = new CompanyDto.Response();
        r.setId(c.getId());
        r.setName(c.getName());
        r.setDescription(c.getDescription());
        r.setLogoUrl(c.getLogoUrl());
        r.setWebsiteUrl(c.getWebsiteUrl());
        r.setIndustry(c.getIndustry());
        r.setLocation(c.getLocation());
        r.setCompanySize(c.getCompanySize());
        r.setIsVerified(c.getIsVerified());
        r.setIsHiring(c.getIsHiring());
        r.setMinCreditThreshold(c.getMinCreditThreshold());
        r.setEmployeeCount(userRepository.countByCompanyId(c.getId()));
        r.setCreatedAt(c.getCreatedAt());
        return r;
    }
}
