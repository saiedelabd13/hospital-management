package com.hospital.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.AuthDTO;
import com.hospital.entity.*;
import com.hospital.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for all integration tests.
 * Sets up a clean database state and provides helper methods.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected DepartmentRepository departmentRepository;
    @Autowired protected DoctorRepository doctorRepository;
    @Autowired protected PatientRepository patientRepository;
    @Autowired protected AppointmentRepository appointmentRepository;
    @Autowired protected MedicalRecordRepository medicalRecordRepository;
    @Autowired protected PasswordEncoder passwordEncoder;

    protected String adminToken;
    protected String doctorToken;
    protected String receptionistToken;
    protected Long savedDepartmentId;
    protected Long savedDoctorId;
    protected Long savedPatientId;
    protected Long savedAppointmentId;

    @BeforeEach
    void baseSetUp() throws Exception {
        seedUsers();
        seedDepartmentsAndDoctors();
        seedPatients();
        seedAppointments();

        adminToken = loginAndGetToken("admin_test", "admin123");
        doctorToken = loginAndGetToken("doctor_test", "doctor123");
        receptionistToken = loginAndGetToken("reception_test", "reception123");
    }

    // ── Seed helpers ──────────────────────────────────────────────

    private void seedUsers() {
        userRepository.save(User.builder()
                .username("admin_test").email("admin_test@hospital.com")
                .password(passwordEncoder.encode("admin123"))
                .roles(Set.of(User.Role.ROLE_ADMIN)).enabled(true).build());

        userRepository.save(User.builder()
                .username("doctor_test").email("doctor_test@hospital.com")
                .password(passwordEncoder.encode("doctor123"))
                .roles(Set.of(User.Role.ROLE_DOCTOR)).enabled(true).build());

        userRepository.save(User.builder()
                .username("reception_test").email("reception_test@hospital.com")
                .password(passwordEncoder.encode("reception123"))
                .roles(Set.of(User.Role.ROLE_RECEPTIONIST)).enabled(true).build());
    }

    private void seedDepartmentsAndDoctors() {
        Department dept = departmentRepository.save(Department.builder()
                .name("قسم القلب - Test")
                .description("وصف")
                .location("الطابق الثاني")
                .phoneNumber("02-0000-0001")
                .status(Department.DepartmentStatus.ACTIVE).build());
        savedDepartmentId = dept.getId();

        Doctor doctor = doctorRepository.save(Doctor.builder()
                .firstName("أحمد").lastName("محمد")
                .email("dr.test.unique@hospital.com")
                .licenseNumber("LIC-TEST-001")
                .phoneNumber("010-0000-0001")
                .specialization("أمراض القلب")
                .yearsOfExperience(10)
                .qualification("دكتوراه")
                .status(Doctor.DoctorStatus.ACTIVE)
                .department(dept).build());
        savedDoctorId = doctor.getId();
    }

    private void seedPatients() {
        Patient patient = patientRepository.save(Patient.builder()
                .firstName("عمر").lastName("خالد")
                .email("test.omar@email.com")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Patient.Gender.MALE)
                .phoneNumber("010-0000-0002")
                .nationalId("99905151234567")
                .bloodType("A+")
                .status(Patient.PatientStatus.ACTIVE).build());
        savedPatientId = patient.getId();
    }

    private void seedAppointments() {
        Patient patient = patientRepository.findById(savedPatientId).orElseThrow();
        Doctor doctor = doctorRepository.findById(savedDoctorId).orElseThrow();

        Appointment appt = appointmentRepository.save(Appointment.builder()
                .patient(patient).doctor(doctor)
                .appointmentDateTime(LocalDateTime.now().plusDays(1))
                .reason("فحص دوري - Test")
                .type(Appointment.AppointmentType.REGULAR)
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .durationMinutes(30).build());
        savedAppointmentId = appt.getId();
    }

    // ── Auth helper ───────────────────────────────────────────────

    protected String loginAndGetToken(String username, String password) throws Exception {
        AuthDTO.LoginRequest req = new AuthDTO.LoginRequest();
        req.setUsernameOrEmail(username);
        req.setPassword(password);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    protected String bearerOf(String token) {
        return "Bearer " + token;
    }

    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
