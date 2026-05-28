package com.employeerank.service.impl;

import com.employeerank.dto.AuthDto;
import com.employeerank.entity.Company;
import com.employeerank.entity.User;
import com.employeerank.enums.Role;
import com.employeerank.exception.BadRequestException;
import com.employeerank.exception.ResourceNotFoundException;
import com.employeerank.exception.UnauthorizedException;
import com.employeerank.repository.CompanyRepository;
import com.employeerank.repository.UserRepository;
import com.employeerank.security.JwtUtils;
import com.employeerank.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        User.UserBuilder builder = User.builder()
                .fullName(request.getFullName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.ROLE_EMPLOYEE)
                .jobTitle(request.getJobTitle())
                .department(request.getDepartment())
                .isActive(true)
                .isPublicProfile(true)
                .totalCredits(0);

        if (request.getCompanyId() != null) {
            Company company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company", request.getCompanyId()));
            builder.company(company);
        }

        User user = userRepository.save(builder.build());

        String refreshToken = jwtUtils.generateRefreshToken();
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtUtils.generateAccessToken(userDetails);

        log.info("New user registered: {}", user.getEmail());
        return new AuthDto.AuthResponse(accessToken, refreshToken, user.getId(),
                user.getUsername(), user.getEmail(), user.getFullName(),
                user.getRole().name(), user.getProfilePicture());
    }

    @Transactional
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String accessToken = jwtUtils.generateAccessToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken();
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new AuthDto.AuthResponse(accessToken, refreshToken, user.getId(),
                user.getUsername(), user.getEmail(), user.getFullName(),
                user.getRole().name(), user.getProfilePicture());
    }

    @Transactional
    public AuthDto.AuthResponse refreshToken(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String newAccessToken = jwtUtils.generateAccessToken(userDetails);
        String newRefreshToken = jwtUtils.generateRefreshToken();
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return new AuthDto.AuthResponse(newAccessToken, newRefreshToken, user.getId(),
                user.getUsername(), user.getEmail(), user.getFullName(),
                user.getRole().name(), user.getProfilePicture());
    }

    @Transactional
    public void logout(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setRefreshToken(null);
            userRepository.save(user);
        });
    }
}
