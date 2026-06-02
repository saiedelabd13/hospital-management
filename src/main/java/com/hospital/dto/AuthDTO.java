package com.hospital.dto;

import com.hospital.entity.Prescription;
import com.hospital.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

// =====================
// Auth DTOs
// =====================
public class AuthDTO {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "اسم المستخدم مطلوب")
        @Size(min = 3, max = 50)
        private String username;

        @Email(message = "البريد الإلكتروني غير صحيح")
        @NotBlank(message = "البريد الإلكتروني مطلوب")
        private String email;

        @NotBlank(message = "كلمة المرور مطلوبة")
        @Size(min = 6, message = "كلمة المرور يجب أن تكون 6 أحرف على الأقل")
        private String password;

        private Set<User.Role> roles;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "اسم المستخدم أو البريد الإلكتروني مطلوب")
        private String usernameOrEmail;

        @NotBlank(message = "كلمة المرور مطلوبة")
        private String password;
    }

    @Data
    public static class JwtResponse {
        private String token;
        private String type = "Bearer";
        private Long id;
        private String username;
        private String email;
        private Set<User.Role> roles;

        public JwtResponse(String token, Long id, String username, String email, Set<User.Role> roles) {
            this.token = token;
            this.id = id;
            this.username = username;
            this.email = email;
            this.roles = roles;
        }
    }

    @Data
    public static class MessageResponse {
        private String message;
        public MessageResponse(String message) { this.message = message; }
    }
}
