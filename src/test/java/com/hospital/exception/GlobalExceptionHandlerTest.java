package com.hospital.exception;

import com.hospital.integration.BaseIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("GlobalExceptionHandler - Integration Tests")
class GlobalExceptionHandlerTest extends BaseIntegrationTest {

    // ── 404 Not Found ─────────────────────────────────────────────
    @Nested
    @DisplayName("404 - ResourceNotFoundException")
    class NotFound {

        @Test
        @DisplayName("✅ يرجع 404 مع رسالة عربية عند عدم وجود المريض")
        void shouldReturn404WithArabicMessage() throws Exception {
            mockMvc.perform(get("/patients/99999")
                            .header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @Test
        @DisplayName("✅ يرجع 404 عند عدم وجود الطبيب")
        void shouldReturn404ForDoctor() throws Exception {
            mockMvc.perform(get("/doctors/99999")
                            .header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("✅ يرجع 404 عند عدم وجود القسم")
        void shouldReturn404ForDepartment() throws Exception {
            mockMvc.perform(get("/departments/99999")
                            .header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("✅ يرجع 404 عند عدم وجود الموعد")
        void shouldReturn404ForAppointment() throws Exception {
            mockMvc.perform(get("/appointments/99999")
                            .header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    // ── 400 Validation Error ──────────────────────────────────────
    @Nested
    @DisplayName("400 - Validation Errors")
    class ValidationErrors {

        @Test
        @DisplayName("✅ يرجع 400 مع أخطاء التحقق لجميع الحقول")
        void shouldReturn400WithFieldErrors() throws Exception {
            mockMvc.perform(post("/patients")
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.errors").isNotEmpty())
                    .andExpect(jsonPath("$.errors.firstName").isNotEmpty())
                    .andExpect(jsonPath("$.errors.lastName").isNotEmpty())
                    .andExpect(jsonPath("$.errors.email").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @Test
        @DisplayName("✅ يرجع خطأ للبريد الإلكتروني غير الصحيح")
        void shouldReturnEmailValidationError() throws Exception {
            String body = """
                    {
                      "firstName": "اسم",
                      "lastName": "لقب",
                      "email": "not-valid",
                      "dateOfBirth": "1990-01-01",
                      "gender": "MALE"
                    }
                    """;

            mockMvc.perform(post("/patients")
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.email").isNotEmpty());
        }

        @Test
        @DisplayName("✅ يرجع خطأ عند إنشاء قسم بدون اسم")
        void shouldReturnNameValidationError() throws Exception {
            mockMvc.perform(post("/departments")
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"ACTIVE\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").isNotEmpty());
        }
    }

    // ── 401 Unauthorized ──────────────────────────────────────────
    @Nested
    @DisplayName("401 - Unauthorized")
    class Unauthorized {

        @Test
        @DisplayName("✅ يرجع 401 بدون توكن")
        void shouldReturn401WithoutToken() throws Exception {
            mockMvc.perform(get("/patients"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("✅ يرجع 401 لبيانات دخول خاطئة")
        void shouldReturn401ForBadCredentials() throws Exception {
            String body = """
                    {
                      "usernameOrEmail": "admin_test",
                      "password": "wrongpassword"
                    }
                    """;

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }
    }

    // ── 403 Forbidden ─────────────────────────────────────────────
    @Nested
    @DisplayName("403 - Forbidden")
    class Forbidden {

        @Test
        @DisplayName("✅ يرجع 403 عند تجاوز صلاحيات المستخدم")
        void shouldReturn403WhenAccessDenied() throws Exception {
            mockMvc.perform(get("/medical-records")
                            .header("Authorization", bearerOf(receptionistToken)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }
    }

    // ── 400 Business Logic Errors ─────────────────────────────────
    @Nested
    @DisplayName("400 - Business Logic Errors")
    class BusinessLogicErrors {

        @Test
        @DisplayName("✅ يرجع 400 عند تكرار اسم القسم")
        void shouldReturn400ForDuplicateDepartmentName() throws Exception {
            String body = """
                    {
                      "name": "قسم القلب - Test",
                      "status": "ACTIVE"
                    }
                    """;

            mockMvc.perform(post("/departments")
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }

        @Test
        @DisplayName("✅ يرجع 400 عند تكرار البريد الإلكتروني للمريض")
        void shouldReturn400ForDuplicatePatientEmail() throws Exception {
            String body = """
                    {
                      "firstName": "اسم",
                      "lastName": "لقب",
                      "email": "test.omar@email.com",
                      "dateOfBirth": "1990-01-01",
                      "gender": "MALE",
                      "status": "ACTIVE"
                    }
                    """;

            mockMvc.perform(post("/patients")
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }
    }
}
