package com.hospital.integration;

import com.hospital.dto.AppointmentDTO;
import com.hospital.entity.Appointment;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Appointment Controller - Integration Tests")
class AppointmentControllerIntegrationTest extends BaseIntegrationTest {

    // ── GET all ───────────────────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع جميع المواعيد")
    void shouldReturnAllAppointments() throws Exception {
        mockMvc.perform(get("/appointments")
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── GET today ─────────────────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع مواعيد اليوم")
    void shouldReturnTodayAppointments() throws Exception {
        // Add a today appointment
        appointmentRepository.save(Appointment.builder()
                .patient(patientRepository.findById(savedPatientId).orElseThrow())
                .doctor(doctorRepository.findById(savedDoctorId).orElseThrow())
                .appointmentDateTime(LocalDateTime.now().plusHours(1))
                .reason("فحص اليوم")
                .type(Appointment.AppointmentType.REGULAR)
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .durationMinutes(30).build());

        mockMvc.perform(get("/appointments/today")
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── GET by ID ─────────────────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع الموعد بالمعرف مع بيانات المريض والطبيب")
    void shouldReturnAppointmentById() throws Exception {
        mockMvc.perform(get("/appointments/{id}", savedAppointmentId)
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedAppointmentId))
                .andExpect(jsonPath("$.patientName").value("عمر خالد"))
                .andExpect(jsonPath("$.doctorName").value("Dr. أحمد محمد"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    // ── GET by patient ────────────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع مواعيد مريض معين")
    void shouldReturnPatientAppointments() throws Exception {
        mockMvc.perform(get("/appointments/patient/{id}", savedPatientId)
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].patientId").value(savedPatientId));
    }

    // ── GET by doctor ─────────────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع مواعيد طبيب معين")
    void shouldReturnDoctorAppointments() throws Exception {
        mockMvc.perform(get("/appointments/doctor/{id}", savedDoctorId)
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].doctorId").value(savedDoctorId));
    }

    // ── GET by status ─────────────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع المواعيد حسب الحالة")
    void shouldReturnAppointmentsByStatus() throws Exception {
        mockMvc.perform(get("/appointments/status/SCHEDULED")
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ── GET date range ────────────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع المواعيد في نطاق زمني")
    void shouldReturnAppointmentsByDateRange() throws Exception {
        mockMvc.perform(get("/appointments/range")
                        .param("start", LocalDateTime.now().minusDays(1).toString())
                        .param("end", LocalDateTime.now().plusDays(7).toString())
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ── POST create ───────────────────────────────────────────────
    @Nested
    @DisplayName("POST /appointments")
    class Create {

        @Test
        @DisplayName("✅ حجز موعد جديد بنجاح")
        void shouldCreateAppointmentSuccessfully() throws Exception {
            AppointmentDTO.Request req = new AppointmentDTO.Request();
            req.setPatientId(savedPatientId);
            req.setDoctorId(savedDoctorId);
            req.setAppointmentDateTime(LocalDateTime.now().plusDays(5));
            req.setReason("فحص شامل");
            req.setType(Appointment.AppointmentType.REGULAR);
            req.setDurationMinutes(45);
            req.setNotes("ملاحظات الموعد");

            mockMvc.perform(post("/appointments")
                            .header("Authorization", bearerOf(receptionistToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.status").value("SCHEDULED"))
                    .andExpect(jsonPath("$.patientName").value("عمر خالد"))
                    .andExpect(jsonPath("$.durationMinutes").value(45));
        }

        @Test
        @DisplayName("❌ يرفض موعد في الماضي")
        void shouldRejectPastAppointment() throws Exception {
            AppointmentDTO.Request req = new AppointmentDTO.Request();
            req.setPatientId(savedPatientId);
            req.setDoctorId(savedDoctorId);
            req.setAppointmentDateTime(LocalDateTime.now().minusDays(1)); // in the past
            req.setReason("موعد في الماضي");

            mockMvc.perform(post("/appointments")
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ يرفض موعد لمريض غير موجود")
        void shouldRejectNonExistentPatient() throws Exception {
            AppointmentDTO.Request req = new AppointmentDTO.Request();
            req.setPatientId(99999L);
            req.setDoctorId(savedDoctorId);
            req.setAppointmentDateTime(LocalDateTime.now().plusDays(3));

            mockMvc.perform(post("/appointments")
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isNotFound());
        }
    }

    // ── PATCH status update ───────────────────────────────────────
    @Nested
    @DisplayName("PATCH /appointments/{id}/status")
    class UpdateStatus {

        @Test
        @DisplayName("✅ تحديث حالة الموعد إلى CONFIRMED")
        void shouldUpdateStatusToConfirmed() throws Exception {
            AppointmentDTO.StatusUpdate update = new AppointmentDTO.StatusUpdate();
            update.setStatus(Appointment.AppointmentStatus.CONFIRMED);
            update.setNotes("تم التأكيد");

            mockMvc.perform(patch("/appointments/{id}/status", savedAppointmentId)
                            .header("Authorization", bearerOf(receptionistToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(update)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CONFIRMED"));
        }

        @Test
        @DisplayName("✅ تحديث حالة الموعد إلى IN_PROGRESS")
        void shouldUpdateStatusToInProgress() throws Exception {
            AppointmentDTO.StatusUpdate update = new AppointmentDTO.StatusUpdate();
            update.setStatus(Appointment.AppointmentStatus.IN_PROGRESS);

            mockMvc.perform(patch("/appointments/{id}/status", savedAppointmentId)
                            .header("Authorization", bearerOf(doctorToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(update)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        }
    }

    // ── DELETE cancel ─────────────────────────────────────────────
    @Test
    @DisplayName("✅ إلغاء الموعد بنجاح")
    void shouldCancelAppointment() throws Exception {
        mockMvc.perform(delete("/appointments/{id}/cancel", savedAppointmentId)
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isNoContent());
    }
}
