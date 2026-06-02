package com.hospital;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.AuthDTO;
import com.hospital.dto.DepartmentDTO;
import com.hospital.dto.PatientDTO;
import com.hospital.entity.Department;
import com.hospital.entity.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Hospital Management Integration Tests")
class HospitalManagementApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // Get admin token
        AuthDTO.LoginRequest loginRequest = new AuthDTO.LoginRequest();
        loginRequest.setUsernameOrEmail("admin");
        loginRequest.setPassword("admin123");

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        adminToken = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    @DisplayName("تسجيل الدخول بنجاح")
    void testLoginSuccess() throws Exception {
        AuthDTO.LoginRequest loginRequest = new AuthDTO.LoginRequest();
        loginRequest.setUsernameOrEmail("admin");
        loginRequest.setPassword("admin123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    @DisplayName("تسجيل دخول فاشل - كلمة مرور خاطئة")
    void testLoginFailure() throws Exception {
        AuthDTO.LoginRequest loginRequest = new AuthDTO.LoginRequest();
        loginRequest.setUsernameOrEmail("admin");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("جلب جميع الأقسام")
    void testGetAllDepartments() throws Exception {
        mockMvc.perform(get("/departments")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("إنشاء قسم جديد")
    void testCreateDepartment() throws Exception {
        DepartmentDTO.Request request = new DepartmentDTO.Request();
        request.setName("قسم الأمراض الجلدية - Test");
        request.setDescription("علاج الأمراض الجلدية");
        request.setLocation("الطابق الرابع");
        request.setStatus(Department.DepartmentStatus.ACTIVE);

        mockMvc.perform(post("/departments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("قسم الأمراض الجلدية - Test"));
    }

    @Test
    @DisplayName("جلب جميع الأطباء")
    void testGetAllDoctors() throws Exception {
        mockMvc.perform(get("/doctors")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("جلب جميع المرضى")
    void testGetAllPatients() throws Exception {
        mockMvc.perform(get("/patients")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("تسجيل مريض جديد")
    void testCreatePatient() throws Exception {
        PatientDTO.Request request = new PatientDTO.Request();
        request.setFirstName("اختبار");
        request.setLastName("المريض");
        request.setEmail("test.patient@email.com");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setGender(Patient.Gender.MALE);
        request.setPhoneNumber("010-0000-0001");
        request.setStatus(Patient.PatientStatus.ACTIVE);

        mockMvc.perform(post("/patients")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("اختبار"));
    }

    @Test
    @DisplayName("جلب مواعيد اليوم")
    void testGetTodayAppointments() throws Exception {
        mockMvc.perform(get("/appointments/today")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("جلب إحصائيات لوحة التحكم")
    void testGetDashboardStats() throws Exception {
        mockMvc.perform(get("/dashboard/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPatients").exists())
                .andExpect(jsonPath("$.totalDoctors").exists())
                .andExpect(jsonPath("$.totalDepartments").exists());
    }

    @Test
    @DisplayName("الوصول بدون توكن - مرفوض")
    void testAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/patients"))
                .andExpect(status().isUnauthorized());
    }
}
