package com.hospital.integration;

import com.hospital.dto.AuthDTO;
import com.hospital.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Auth Controller - Integration Tests")
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    // ── Login ─────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /auth/login")
    class Login {

        @Test
        @DisplayName("✅ تسجيل دخول ناجح - يرجع توكن")
        void shouldLoginSuccessfully() throws Exception {
            AuthDTO.LoginRequest req = new AuthDTO.LoginRequest();
            req.setUsernameOrEmail("admin_test");
            req.setPassword("admin123");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.username").value("admin_test"))
                    .andExpect(jsonPath("$.type").value("Bearer"));
        }

        @Test
        @DisplayName("✅ تسجيل الدخول بالبريد الإلكتروني")
        void shouldLoginWithEmail() throws Exception {
            AuthDTO.LoginRequest req = new AuthDTO.LoginRequest();
            req.setUsernameOrEmail("admin_test@hospital.com");
            req.setPassword("admin123");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty());
        }

        @Test
        @DisplayName("❌ تسجيل دخول فاشل - كلمة مرور خاطئة")
        void shouldFailWithWrongPassword() throws Exception {
            AuthDTO.LoginRequest req = new AuthDTO.LoginRequest();
            req.setUsernameOrEmail("admin_test");
            req.setPassword("wrongpassword");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("❌ تسجيل دخول فاشل - مستخدم غير موجود")
        void shouldFailWithNonExistentUser() throws Exception {
            AuthDTO.LoginRequest req = new AuthDTO.LoginRequest();
            req.setUsernameOrEmail("nonexistent_user");
            req.setPassword("password");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("❌ تسجيل دخول فاشل - بيانات ناقصة")
        void shouldFailWithMissingFields() throws Exception {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── Register ──────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /auth/register")
    class Register {

        @Test
        @DisplayName("✅ تسجيل مستخدم جديد بنجاح")
        void shouldRegisterNewUser() throws Exception {
            AuthDTO.RegisterRequest req = new AuthDTO.RegisterRequest();
            req.setUsername("newuser_integration");
            req.setEmail("newuser_integration@hospital.com");
            req.setPassword("password123");
            req.setRoles(Set.of(User.Role.ROLE_PATIENT));

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }

        @Test
        @DisplayName("❌ تسجيل فاشل - اسم مستخدم موجود")
        void shouldFailWhenUsernameExists() throws Exception {
            AuthDTO.RegisterRequest req = new AuthDTO.RegisterRequest();
            req.setUsername("admin_test"); // already exists
            req.setEmail("another@hospital.com");
            req.setPassword("password123");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ تسجيل فاشل - بريد إلكتروني موجود")
        void shouldFailWhenEmailExists() throws Exception {
            AuthDTO.RegisterRequest req = new AuthDTO.RegisterRequest();
            req.setUsername("brandnewuser");
            req.setEmail("admin_test@hospital.com"); // already exists
            req.setPassword("password123");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ تسجيل فاشل - كلمة مرور قصيرة")
        void shouldFailWithShortPassword() throws Exception {
            AuthDTO.RegisterRequest req = new AuthDTO.RegisterRequest();
            req.setUsername("shortpassuser");
            req.setEmail("short@hospital.com");
            req.setPassword("123"); // too short

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.password").isNotEmpty());
        }
    }
}
