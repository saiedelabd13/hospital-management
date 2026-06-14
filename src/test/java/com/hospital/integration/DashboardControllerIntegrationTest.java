package com.hospital.integration;

import org.junit.jupiter.api.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Dashboard Controller - Integration Tests")
class DashboardControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("✅ يرجع إحصائيات لوحة التحكم للمدير")
    void shouldReturnDashboardStatsForAdmin() throws Exception {
        mockMvc.perform(get("/dashboard/stats")
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPatients").value(1))
                .andExpect(jsonPath("$.totalDoctors").value(1))
                .andExpect(jsonPath("$.totalDepartments").value(1))
                .andExpect(jsonPath("$.activePatients").isNumber())
                .andExpect(jsonPath("$.todayAppointments").isNumber())
                .andExpect(jsonPath("$.thisMonthAppointments").isNumber())
                .andExpect(jsonPath("$.appointmentsByStatus").isNotEmpty())
                .andExpect(jsonPath("$.appointmentsByStatus.SCHEDULED").isNumber());
    }

    @Test
    @DisplayName("✅ الطبيب يمكنه الوصول لإحصائيات لوحة التحكم")
    void shouldAllowDoctorAccess() throws Exception {
        mockMvc.perform(get("/dashboard/stats")
                        .header("Authorization", bearerOf(doctorToken)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ موظف الاستقبال يمكنه الوصول لإحصائيات لوحة التحكم")
    void shouldAllowReceptionistAccess() throws Exception {
        mockMvc.perform(get("/dashboard/stats")
                        .header("Authorization", bearerOf(receptionistToken)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("❌ الوصول بدون توكن مرفوض")
    void shouldRejectUnauthenticated() throws Exception {
        mockMvc.perform(get("/dashboard/stats"))
                .andExpect(status().isUnauthorized());
    }
}
