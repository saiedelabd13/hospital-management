package com.hospital.integration;

import com.hospital.dto.DoctorDTO;
import com.hospital.entity.Doctor;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Doctor Controller - Integration Tests")
class DoctorControllerIntegrationTest extends BaseIntegrationTest {

    // ── GET all ───────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /doctors")
    class GetAll {

        @Test
        @DisplayName("✅ يرجع جميع الأطباء")
        void shouldReturnAllDoctors() throws Exception {
            mockMvc.perform(get("/doctors")
                            .header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].fullName").value("Dr. أحمد محمد"))
                    .andExpect(jsonPath("$[0].specialization").value("أمراض القلب"));
        }

        @Test
        @DisplayName("❌ يرفض الطلب بدون توكن")
        void shouldRejectWithoutToken() throws Exception {
            mockMvc.perform(get("/doctors"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── GET active ────────────────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع الأطباء النشطين فقط")
    void shouldReturnActiveDoctors() throws Exception {
        mockMvc.perform(get("/doctors/active")
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    // ── GET by ID ─────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /doctors/{id}")
    class GetById {

        @Test
        @DisplayName("✅ يرجع الطبيب بالمعرف مع جميع بياناته")
        void shouldReturnDoctorById() throws Exception {
            mockMvc.perform(get("/doctors/{id}", savedDoctorId)
                            .header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(savedDoctorId))
                    .andExpect(jsonPath("$.email").value("dr.test.unique@hospital.com"))
                    .andExpect(jsonPath("$.licenseNumber").value("LIC-TEST-001"))
                    .andExpect(jsonPath("$.departmentName").value("قسم القلب - Test"))
                    .andExpect(jsonPath("$.yearsOfExperience").value(10));
        }

        @Test
        @DisplayName("❌ يرجع 404 لمعرف غير موجود")
        void shouldReturn404() throws Exception {
            mockMvc.perform(get("/doctors/99999")
                            .header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isNotFound());
        }
    }

    // ── GET search ────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /doctors/search")
    class Search {

        @Test
        @DisplayName("✅ يبحث بالاسم الأول")
        void shouldSearchByFirstName() throws Exception {
            mockMvc.perform(get("/doctors/search")
                            .param("keyword", "أحمد")
                            .header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("✅ يبحث بالتخصص")
        void shouldSearchBySpecialization() throws Exception {
            mockMvc.perform(get("/doctors/search")
                            .param("keyword", "القلب")
                            .header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("✅ يرجع قائمة فارغة عند عدم وجود نتائج")
        void shouldReturnEmptyForNoMatch() throws Exception {
            mockMvc.perform(get("/doctors/search")
                            .param("keyword", "xyz_no_match")
                            .header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ── GET by department ─────────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع أطباء القسم")
    void shouldReturnDoctorsByDepartment() throws Exception {
        mockMvc.perform(get("/doctors/department/{id}", savedDepartmentId)
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── GET by specialization ─────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع الأطباء بالتخصص")
    void shouldReturnDoctorsBySpecialization() throws Exception {
        mockMvc.perform(get("/doctors/specialization/{spec}", "أمراض القلب")
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── POST create ───────────────────────────────────────────────
    @Nested
    @DisplayName("POST /doctors")
    class Create {

        private DoctorDTO.Request buildValidRequest() {
            DoctorDTO.Request req = new DoctorDTO.Request();
            req.setFirstName("سارة");
            req.setLastName("علي");
            req.setEmail("dr.sara.integration@hospital.com");
            req.setLicenseNumber("LIC-INTEGRATION-001");
            req.setPhoneNumber("010-7777-7777");
            req.setSpecialization("طب الأطفال");
            req.setYearsOfExperience(8);
            req.setQualification("دكتوراه في طب الأطفال");
            req.setStatus(Doctor.DoctorStatus.ACTIVE);
            req.setDepartmentId(savedDepartmentId);
            return req;
        }

        @Test
        @DisplayName("✅ المدير يضيف طبيب جديد بنجاح")
        void shouldCreateDoctorAsAdmin() throws Exception {
            mockMvc.perform(post("/doctors")
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(buildValidRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.fullName").value("Dr. سارة علي"))
                    .andExpect(jsonPath("$.specialization").value("طب الأطفال"))
                    .andExpect(jsonPath("$.departmentId").value(savedDepartmentId));
        }

        @Test
        @DisplayName("✅ موظف الاستقبال يمكنه إضافة طبيب")
        void shouldCreateDoctorAsReceptionist() throws Exception {
            DoctorDTO.Request req = buildValidRequest();
            req.setEmail("dr.reception.test@hospital.com");
            req.setLicenseNumber("LIC-RECEPTION-001");

            mockMvc.perform(post("/doctors")
                            .header("Authorization", bearerOf(receptionistToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("❌ الطبيب لا يمكنه إضافة طبيب آخر")
        void shouldForbidDoctorFromCreating() throws Exception {
            mockMvc.perform(post("/doctors")
                            .header("Authorization", bearerOf(doctorToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(buildValidRequest())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("❌ يرفض بريد إلكتروني مكرر")
        void shouldRejectDuplicateEmail() throws Exception {
            DoctorDTO.Request req = buildValidRequest();
            req.setEmail("dr.test.unique@hospital.com"); // existing from seed

            mockMvc.perform(post("/doctors")
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ يرفض رقم ترخيص مكرر")
        void shouldRejectDuplicateLicense() throws Exception {
            DoctorDTO.Request req = buildValidRequest();
            req.setLicenseNumber("LIC-TEST-001"); // existing from seed

            mockMvc.perform(post("/doctors")
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ يرفض بدون بيانات مطلوبة")
        void shouldRejectMissingRequiredFields() throws Exception {
            DoctorDTO.Request req = new DoctorDTO.Request();
            req.setFirstName("فقط الاسم");

            mockMvc.perform(post("/doctors")
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isNotEmpty());
        }

        @Test
        @DisplayName("❌ يرجع 404 عند استخدام قسم غير موجود")
        void shouldReturn404ForNonExistentDepartment() throws Exception {
            DoctorDTO.Request req = buildValidRequest();
            req.setDepartmentId(99999L);

            mockMvc.perform(post("/doctors")
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isNotFound());
        }
    }

    // ── PUT update ────────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /doctors/{id}")
    class Update {

        @Test
        @DisplayName("✅ يعدل بيانات الطبيب بنجاح")
        void shouldUpdateDoctorSuccessfully() throws Exception {
            DoctorDTO.Request req = new DoctorDTO.Request();
            req.setFirstName("أحمد");
            req.setLastName("محمد");
            req.setEmail("dr.test.unique@hospital.com");
            req.setLicenseNumber("LIC-TEST-001");
            req.setSpecialization("أمراض القلب والأوعية الدموية");
            req.setYearsOfExperience(12);
            req.setQualification("دكتوراه محدثة");
            req.setStatus(Doctor.DoctorStatus.ACTIVE);
            req.setDepartmentId(savedDepartmentId);

            mockMvc.perform(put("/doctors/{id}", savedDoctorId)
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.specialization").value("أمراض القلب والأوعية الدموية"))
                    .andExpect(jsonPath("$.yearsOfExperience").value(12));
        }

        @Test
        @DisplayName("❌ يرجع 404 عند تعديل طبيب غير موجود")
        void shouldReturn404ForNonExistent() throws Exception {
            DoctorDTO.Request req = new DoctorDTO.Request();
            req.setFirstName("أي اسم");
            req.setLastName("أي لقب");
            req.setEmail("any@email.com");
            req.setLicenseNumber("LIC-XXX");
            req.setSpecialization("أي تخصص");
            req.setStatus(Doctor.DoctorStatus.ACTIVE);

            mockMvc.perform(put("/doctors/99999")
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isNotFound());
        }
    }

    // ── DELETE ────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /doctors/{id}")
    class Delete {

        @Test
        @DisplayName("✅ المدير يحذف الطبيب بنجاح")
        void shouldDeleteDoctor() throws Exception {
            // Create a doctor with no appointments
            Doctor toDelete = doctorRepository.save(Doctor.builder()
                    .firstName("طبيب").lastName("للحذف")
                    .email("delete.doctor@hospital.com")
                    .licenseNumber("LIC-DELETE-001")
                    .specialization("اختبار")
                    .status(Doctor.DoctorStatus.ACTIVE)
                    .department(departmentRepository.findById(savedDepartmentId).orElseThrow())
                    .build());

            mockMvc.perform(delete("/doctors/{id}", toDelete.getId())
                            .header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("❌ موظف الاستقبال لا يمكنه حذف طبيب")
        void shouldForbidReceptionistFromDeleting() throws Exception {
            mockMvc.perform(delete("/doctors/{id}", savedDoctorId)
                            .header("Authorization", bearerOf(receptionistToken)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("❌ يرجع 404 عند حذف طبيب غير موجود")
        void shouldReturn404ForNonExistentDoctor() throws Exception {
            mockMvc.perform(delete("/doctors/99999")
                            .header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isNotFound());
        }
    }
}
