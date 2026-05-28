package com.employeerank.dto;

import com.employeerank.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDto {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50)
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;

        private Role role = Role.ROLE_EMPLOYEE;

        private String jobTitle;
        private String department;
        private Long companyId;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        private Long userId;
        private String username;
        private String email;
        private String fullName;
        private String role;
        private String profilePicture;

        public AuthResponse(String accessToken, String refreshToken, Long userId,
                            String username, String email, String fullName,
                            String role, String profilePicture) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.fullName = fullName;
            this.role = role;
            this.profilePicture = profilePicture;
        }
    }

    @Data
    public static class RefreshTokenRequest {
        @NotBlank
        private String refreshToken;
    }
}
