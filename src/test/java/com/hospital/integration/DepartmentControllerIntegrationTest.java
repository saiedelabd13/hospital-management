package com.hospital.integration;

import com.hospital.dto.DepartmentDTO;
import com.hospital.entity.Department;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Department Controller - Integration Tests")
class DepartmentControllerIntegrationTest extends BaseIntegrationTest {

    // ── GET all ───────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /departments")
    class GetAll {

        @Test
        @DisplayName("✅ يرجع جميع الأقسام للمستخدم المصادق")
        void shouldReturnAllDepartments() throws Exception {
            mockMvc.perform(get("/departments")
                            .header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].name").isNotEmpty());
        }

        @Test
        @DisplayName("❌ يرفض طلب بدون توكن")
        void shouldRejectWithoutToken() throws Exception {
            mockMvc.perform(get("/departments"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── GET active ────────────────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع الأقسام النشطة فقط")
    void shouldReturnOnlyActiveDepartments() throws Exception {
        mockMvc.perform(get("/departments/active")
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ── GET by ID ─────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /departments/{id}")
    class GetById {

        @Test
        @DisplayName("✅ يرجع القسم بالمعرف")
        void shouldReturnDepartmentById() throws Exception {
            mockMvc.perform(get("/departments/{id}", savedDepartmentId)
                            .header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(savedDepartmentId))
                    .andExpect(jsonPath("$.name").isNotEmpty());
        }

        @Test
        @DisplayName("❌ يرجع 404 لمعرف غير موجود")
        void shouldReturn404ForNonExistentId() throws Exception {
            mockMvc.perform(get("/departments/99999")
                            .header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isNotFound());
        }
    }

    // ── POST create ───────────────────────────────────────────────
    @Nested
    @DisplayName("POST /departments")
    class Create {

        @Test
        @DisplayName("✅ المدير ينشئ قسم جديد بنجاح")
        void shouldCreateDepartmentAsAdmin() throws Exception {
            DepartmentDTO.Request req = new DepartmentDTO.Request();
            req.setName("قسم الأمراض الجلدية - Integration Test");
            req.setDescription("علاج الأمراض الجلدية");
            req.setLocation("الطابق السادس");
            req.setPhoneNumber("02-9999-9999");
            req.setStatus(Department.DepartmentStatus.ACTIVE);

            mockMvc.perform(post("/departments")
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.name").value("قسم الأمراض الجلدية - Integration Test"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("❌ الدكتور لا يمكنه إنشاء قسم")
        void shouldForbidDoctorFromCreatingDepartment() throws Exception {
            DepartmentDTO.Request req = new DepartmentDTO.Request();
            req.setName("قسم غير مسموح");
            req.setStatus(Department.DepartmentStatus.ACTIVE);

            mockMvc.perform(post("/departments")
                            .header("Authorization", bearerOf(doctorToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("❌ يرفض إنشاء قسم بنفس الاسم")
        void shouldRejectDuplicateDepartmentName() throws Exception {
            DepartmentDTO.Request req = new DepartmentDTO.Request();
            req.setName("قسم القلب - Test"); // already exists from BaseIntegrationTest seed
            req.setStatus(Department.DepartmentStatus.ACTIVE);

            mockMvc.perform(post("/departments")
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ يرفض إنشاء قسم بدون اسم")
        void shouldRejectCreationWithoutName() throws Exception {
            DepartmentDTO.Request req = new DepartmentDTO.Request();
            req.setStatus(Department.DepartmentStatus.ACTIVE);

            mockMvc.perform(post("/departments")
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").isNotEmpty());
        }
    }

    // ── PUT update ────────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /departments/{id}")
    class Update {

        @Test
        @DisplayName("✅ يعدل القسم بنجاح")
        void shouldUpdateDepartment() throws Exception {
            DepartmentDTO.Request req = new DepartmentDTO.Request();
            req.setName("قسم القلب - Test");
            req.setDescription("وصف محدث");
            req.setLocation("الطابق الثالث المحدث");
            req.setStatus(Department.DepartmentStatus.ACTIVE);

            mockMvc.perform(put("/departments/{id}", savedDepartmentId)
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").value("وصف محدث"));
        }

        @Test
        @DisplayName("❌ يرجع 404 عند تعديل قسم غير موجود")
        void shouldReturn404WhenUpdatingNonExistent() throws Exception {
            DepartmentDTO.Request req = new DepartmentDTO.Request();
            req.setName("أي اسم");
            req.setStatus(Department.DepartmentStatus.ACTIVE);

            mockMvc.perform(put("/departments/99999")
                            .header("Authorization", bearerOf(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isNotFound());
        }
    }

    // ── DELETE ────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /departments/{id}")
    class Delete {

        @Test
        @DisplayName("✅ يحذف القسم الفارغ بنجاح")
        void shouldDeleteEmptyDepartment() throws Exception {
            // Create a department with no doctors first
            Department emptyDept = departmentRepository.save(Department.builder()
                    .name("قسم مؤقت للحذف")
                    .status(Department.DepartmentStatus.ACTIVE).build());

            mockMvc.perform(delete("/departments/{id}", emptyDept.getId())
                            .header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("❌ يرفض حذف قسم يحتوي على أطباء")
        void shouldRejectDeletionOfDepartmentWithDoctors() throws Exception {
            mockMvc.perform(delete("/departments/{id}", savedDepartmentId)
                            .header("Authorization", bearerOf(adminToken)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ الموظف لا يمكنه حذف قسم")
        void shouldForbidReceptionistFromDeletingDepartment() throws Exception {
            mockMvc.perform(delete("/departments/{id}", savedDepartmentId)
                            .header("Authorization", bearerOf(receptionistToken)))
                    .andExpect(status().isForbidden());
        }
    }
}
