package com.hospital.integration;

import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests that verify role-based access control (RBAC) across all endpoints.
 * Ensures security boundaries are enforced correctly.
 */
@DisplayName("Security - Role Based Access Control Tests")
class SecurityAccessControlTest extends BaseIntegrationTest {

    // ── Unauthenticated access ────────────────────────────────────
    @Nested
    @DisplayName("🔒 Unauthenticated - Should return 401")
    class UnauthenticatedAccess {

        @Test void patients()       throws Exception { mockMvc.perform(get("/patients")).andExpect(status().isUnauthorized()); }
        @Test void doctors()        throws Exception { mockMvc.perform(get("/doctors")).andExpect(status().isUnauthorized()); }
        @Test void appointments()   throws Exception { mockMvc.perform(get("/appointments")).andExpect(status().isUnauthorized()); }
        @Test void medicalRecords() throws Exception { mockMvc.perform(get("/medical-records")).andExpect(status().isUnauthorized()); }
        @Test void dashboard()      throws Exception { mockMvc.perform(get("/dashboard/stats")).andExpect(status().isUnauthorized()); }
    }

    // ── Admin - full access ───────────────────────────────────────
    @Nested
    @DisplayName("👑 Admin - Full access")
    class AdminAccess {

        @Test
        @DisplayName("✅ Admin يصل لكل الأقسام")
        void shouldAccessAllDepartments() throws Exception {
            mockMvc.perform(get("/departments").header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("✅ Admin يصل لكل المرضى")
        void shouldAccessAllPatients() throws Exception {
            mockMvc.perform(get("/patients").header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("✅ Admin يصل لكل السجلات الطبية")
        void shouldAccessMedicalRecords() throws Exception {
            mockMvc.perform(get("/medical-records").header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("✅ Admin يصل للوحة التحكم")
        void shouldAccessDashboard() throws Exception {
            mockMvc.perform(get("/dashboard/stats").header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isOk());
        }
    }

    // ── Doctor access ─────────────────────────────────────────────
    @Nested
    @DisplayName("👨‍⚕️ Doctor - Restricted access")
    class DoctorAccess {

        @Test
        @DisplayName("✅ Doctor يصل للمرضى")
        void shouldAccessPatients() throws Exception {
            mockMvc.perform(get("/patients").header("Authorization", bearerOf(doctorToken)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("✅ Doctor يصل للسجلات الطبية")
        void shouldAccessMedicalRecords() throws Exception {
            mockMvc.perform(get("/medical-records").header("Authorization", bearerOf(doctorToken)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("❌ Doctor لا يمكنه إنشاء قسم جديد")
        void shouldNotCreateDepartment() throws Exception {
            mockMvc.perform(post("/departments")
                            .header("Authorization", bearerOf(doctorToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"قسم جديد\",\"status\":\"ACTIVE\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("❌ Doctor لا يمكنه حذف قسم")
        void shouldNotDeleteDepartment() throws Exception {
            mockMvc.perform(delete("/departments/{id}", savedDepartmentId)
                            .header("Authorization", bearerOf(doctorToken)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("❌ Doctor لا يمكنه حذف مريض")
        void shouldNotDeletePatient() throws Exception {
            mockMvc.perform(delete("/patients/{id}", savedPatientId)
                            .header("Authorization", bearerOf(doctorToken)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("❌ Doctor لا يمكنه حذف سجل طبي")
        void shouldNotDeleteMedicalRecord() throws Exception {
            mockMvc.perform(delete("/medical-records/{id}", 1L)
                            .header("Authorization", bearerOf(doctorToken)))
                    .andExpect(status().isForbidden());
        }
    }

    // ── Receptionist access ───────────────────────────────────────
    @Nested
    @DisplayName("🗓️ Receptionist - Limited access")
    class ReceptionistAccess {

        @Test
        @DisplayName("✅ Receptionist يصل لمواعيد اليوم")
        void shouldAccessTodayAppointments() throws Exception {
            mockMvc.perform(get("/appointments/today").header("Authorization", bearerOf(receptionistToken)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("✅ Receptionist يصل للوحة التحكم")
        void shouldAccessDashboard() throws Exception {
            mockMvc.perform(get("/dashboard/stats").header("Authorization", bearerOf(receptionistToken)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("❌ Receptionist لا يمكنه الوصول للسجلات الطبية")
        void shouldNotAccessMedicalRecords() throws Exception {
            mockMvc.perform(get("/medical-records").header("Authorization", bearerOf(receptionistToken)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("❌ Receptionist لا يمكنه حذف مريض")
        void shouldNotDeletePatient() throws Exception {
            mockMvc.perform(delete("/patients/{id}", savedPatientId)
                            .header("Authorization", bearerOf(receptionistToken)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("❌ Receptionist لا يمكنه حذف طبيب")
        void shouldNotDeleteDoctor() throws Exception {
            mockMvc.perform(delete("/doctors/{id}", savedDoctorId)
                            .header("Authorization", bearerOf(receptionistToken)))
                    .andExpect(status().isForbidden());
        }
    }

    // ── Invalid token ─────────────────────────────────────────────
    @Nested
    @DisplayName("🚫 Invalid / Expired Token")
    class InvalidToken {

        @Test
        @DisplayName("❌ توكن غير صحيح يُرفض")
        void shouldRejectInvalidToken() throws Exception {
            mockMvc.perform(get("/patients")
                            .header("Authorization", "Bearer invalid.token.value"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("❌ header بدون Bearer يُرفض")
        void shouldRejectNonBearerToken() throws Exception {
            mockMvc.perform(get("/patients")
                            .header("Authorization", "Basic dXNlcjpwYXNz"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("✅ Public endpoints تعمل بدون توكن")
        void publicEndpointsShouldWork() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }
    }
}
