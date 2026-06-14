package com.hospital.integration;

import com.hospital.dto.PatientDTO;
import com.hospital.entity.Patient;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Patient Controller - Integration Tests")
class PatientControllerIntegrationTest extends BaseIntegrationTest {

    // ── GET all ───────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /patients")
    class GetAll {

        @Test
        @DisplayName("✅ المدير يمكنه جلب المرضى")
        void shouldReturnPatientsForAdmin() throws Exception {
            mockMvc.perform(get("/patients")
                            .header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("✅ الطبيب يمكنه جلب المرضى")
        void shouldReturnPatientsForDoctor() throws Exception {
            mockMvc.perform(get("/patients")
                            .header("Authorization", bearerOf(doctorToken)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("❌ الوصول بدون توكن مرفوض")
        void shouldRejectUnauthenticated() throws Exception {
            mockMvc.perform(get("/patients")).andExpect(status().isUnauthorized());
        }
    }

    // ── GET by ID ─────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /patients/{id}")
    class GetById {

        @Test
        @DisplayName("✅ يرجع المريض بالمعرف مع جميع بياناته")
        void shouldReturnPatientById() throws Exception {
            mockMvc.perform(get("/patients/{id}", savedPatientId)
                            .header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(savedPatientId))
                    .andExpect(jsonPath("$.firstName").value("عمر"))
                    .andExpect(jsonPath("$.fullName").value("عمر خالد"))
                    .andExpect(jsonPath("$.bloodType").value("A+"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("❌ يرجع 404 لمريض غير موجود")
        void shouldReturn404() throws Exception {
            mockMvc.perform(get("/patients/99999")
                            .header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isNotFound());
        }
    }

    // ── GET search ────────────────────────────────────────────────
    @Test
    @DisplayName("✅ البحث عن مريض بالاسم")
    void shouldSearchPatients() throws Exception {
        mockMvc.perform(get("/patients/search")
                        .param("keyword", "عمر")
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].fullName").value("عمر خالد"));
    }

    @Test
    @DisplayName("✅ البحث بكلمة غير موجودة يرجع قائمة فارغة")
    void shouldReturnEmptyListWhenNotFound() throws Exception {
        mockMvc.perform(get("/patients/search")
                        .param("keyword", "xyz_not_found")
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET count ─────────────────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع عدد المرضى النشطين")
    void shouldReturnActivePatientCount() throws Exception {
        mockMvc.perform(get("/patients/count/active")
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk());
    }

    // ── POST create ───────────────────────────────────────────────
    @Nested
    @DisplayName("POST /patients")
    class Create {

        private PatientDTO.Request buildValidRequest() {
            PatientDTO.Request req = new PatientDTO.Request();
            req.setFirstName("نور");
            req.setLastName("أحمد");
            req.setEmail("nour.integration@email.com");
            req.setDateOfBirth(LocalDate.of(1995, 3, 20));
            req.setGender(Patient.Gender.FEMALE);
            req.setPhoneNumber("010-8888-0000");
            req.setNationalId("99503201234568");
            req.setBloodType("O+");
            req.setStatus(Patient.PatientStatus.ACTIVE);
            return req;
        }

        @Test
        @DisplayName("✅ الموظف ينشئ مريض جديد بنجاح")
        void shouldCreatePatientAsReceptionist() throws Exception {
            mockMvc.perform(post("/patients")
                            .header("Authorization", bearerOf(receptionistToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(buildValidRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.firstName").value("نور"))
                    .andExpect(jsonPath("$.fullName").value("نور أحمد"))
                    .andExpect(jsonPath("$.gender").value("FEMALE"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("❌ يرفض إنشاء مريض بنفس البريد الإلكتروني")
        void shouldRejectDuplicateEmail() throws Exception {
            PatientDTO.Request req = buildValidRequest();
            req.setEmail("test.omar@email.com"); // existing email from seed
            req.setNationalId("99999999999999");

            mockMvc.perform(post("/patients")
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ يرفض إنشاء مريض بدون بيانات مطلوبة")
        void shouldRejectMissingRequiredFields() throws Exception {
            mockMvc.perform(post("/patients")
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isNotEmpty());
        }

        @Test
        @DisplayName("❌ يرفض بريد إلكتروني غير صحيح")
        void shouldRejectInvalidEmail() throws Exception {
            PatientDTO.Request req = buildValidRequest();
            req.setEmail("not-a-valid-email");

            mockMvc.perform(post("/patients")
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.email").isNotEmpty());
        }
    }

    // ── PUT update ────────────────────────────────────────────────
    @Test
    @DisplayName("✅ يعدل بيانات المريض بنجاح")
    void shouldUpdatePatient() throws Exception {
        PatientDTO.Request req = new PatientDTO.Request();
        req.setFirstName("عمر");
        req.setLastName("خالد");
        req.setEmail("test.omar@email.com");
        req.setDateOfBirth(LocalDate.of(1990, 5, 15));
        req.setGender(Patient.Gender.MALE);
        req.setAddress("القاهرة الجديدة - محدث");
        req.setStatus(Patient.PatientStatus.ACTIVE);

        mockMvc.perform(put("/patients/{id}", savedPatientId)
                        .header("Authorization", bearerOf(receptionistToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("القاهرة الجديدة - محدث"));
    }

    // ── DELETE ────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /patients/{id}")
    class Delete {

        @Test
        @DisplayName("✅ المدير يحذف المريض بنجاح")
        void shouldDeletePatientAsAdmin() throws Exception {
            // Create a patient to delete
            Patient toDelete = patientRepository.save(Patient.builder()
                    .firstName("مريض").lastName("للحذف")
                    .email("delete.me@email.com")
                    .dateOfBirth(LocalDate.of(2000, 1, 1))
                    .gender(Patient.Gender.MALE)
                    .status(Patient.PatientStatus.ACTIVE).build());

            mockMvc.perform(delete("/patients/{id}", toDelete.getId())
                            .header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("❌ الطبيب لا يمكنه حذف مريض")
        void shouldForbidDoctorFromDeletingPatient() throws Exception {
            mockMvc.perform(delete("/patients/{id}", savedPatientId)
                            .header("Authorization", bearerOf(doctorToken)))
                    .andExpect(status().isForbidden());
        }
    }
}
