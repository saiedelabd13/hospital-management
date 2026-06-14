package com.hospital.integration;

import com.hospital.dto.*;
import com.hospital.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-End workflow tests simulating real hospital scenarios.
 * Tests complete flows from patient registration to medical record creation.
 */
@DisplayName("🏥 End-to-End Workflow Tests")
class EndToEndWorkflowTest extends BaseIntegrationTest {

    // ─────────────────────────────────────────────────────────────
    // Scenario 1: Complete Patient Visit Flow
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("🔄 سيناريو كامل: تسجيل مريض → حجز موعد → تأكيد → إنشاء سجل طبي")
    void shouldCompleteFullPatientVisitFlow() throws Exception {

        // STEP 1: Register a new patient (as receptionist)
        PatientDTO.Request patientReq = new PatientDTO.Request();
        patientReq.setFirstName("ياسر");
        patientReq.setLastName("الشريف");
        patientReq.setEmail("yaser.e2e@email.com");
        patientReq.setDateOfBirth(LocalDate.of(1985, 8, 20));
        patientReq.setGender(Patient.Gender.MALE);
        patientReq.setPhoneNumber("010-E2E-0001");
        patientReq.setNationalId("E2E01234567890");
        patientReq.setBloodType("O+");
        patientReq.setStatus(Patient.PatientStatus.ACTIVE);

        MvcResult patientResult = mockMvc.perform(post("/patients")
                        .header("Authorization", bearerOf(receptionistToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(patientReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("ياسر الشريف"))
                .andReturn();

        Long newPatientId = objectMapper.readTree(
                patientResult.getResponse().getContentAsString()).get("id").asLong();
        assertThat(newPatientId).isPositive();

        // STEP 2: Book an appointment (as receptionist)
        AppointmentDTO.Request apptReq = new AppointmentDTO.Request();
        apptReq.setPatientId(newPatientId);
        apptReq.setDoctorId(savedDoctorId);
        apptReq.setAppointmentDateTime(LocalDateTime.now().plusDays(2));
        apptReq.setReason("ألم في الصدر");
        apptReq.setType(Appointment.AppointmentType.CONSULTATION);
        apptReq.setDurationMinutes(45);

        MvcResult apptResult = mockMvc.perform(post("/appointments")
                        .header("Authorization", bearerOf(receptionistToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(apptReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.patientName").value("ياسر الشريف"))
                .andReturn();

        Long newApptId = objectMapper.readTree(
                apptResult.getResponse().getContentAsString()).get("id").asLong();

        // STEP 3: Confirm appointment (as receptionist)
        AppointmentDTO.StatusUpdate confirm = new AppointmentDTO.StatusUpdate();
        confirm.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        confirm.setNotes("تم التأكيد هاتفياً");

        mockMvc.perform(patch("/appointments/{id}/status", newApptId)
                        .header("Authorization", bearerOf(receptionistToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(confirm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // STEP 4: Start appointment (doctor marks IN_PROGRESS)
        AppointmentDTO.StatusUpdate inProgress = new AppointmentDTO.StatusUpdate();
        inProgress.setStatus(Appointment.AppointmentStatus.IN_PROGRESS);

        mockMvc.perform(patch("/appointments/{id}/status", newApptId)
                        .header("Authorization", bearerOf(doctorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(inProgress)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // STEP 5: Create medical record with prescription (doctor)
        PrescriptionDTO.Request pReq = new PrescriptionDTO.Request();
        pReq.setMedicationName("نيتروجليسرين");
        pReq.setDosage("0.5mg");
        pReq.setFrequency("عند الحاجة");
        pReq.setDurationDays(14);
        pReq.setStartDate(LocalDate.now());
        pReq.setInstructions("تحت اللسان عند الشعور بألم الصدر");
        pReq.setRefillAllowed(true);

        MedicalRecordDTO.Request recordReq = new MedicalRecordDTO.Request();
        recordReq.setPatientId(newPatientId);
        recordReq.setDoctorId(savedDoctorId);
        recordReq.setAppointmentId(newApptId);
        recordReq.setDiagnosis("ذبحة صدرية مستقرة");
        recordReq.setSymptoms("ألم في الصدر عند المجهود، ضيق في التنفس");
        recordReq.setTreatmentPlan("أدوية لتوسيع الأوعية، راحة");
        recordReq.setDoctorNotes("مريض يحتاج متابعة دورية كل أسبوعين");
        recordReq.setWeight(88.0);
        recordReq.setHeight(178.0);
        recordReq.setBloodPressure("145/92");
        recordReq.setHeartRate(92);
        recordReq.setTemperature(37.0);
        recordReq.setPrescriptions(List.of(pReq));

        MvcResult recordResult = mockMvc.perform(post("/medical-records")
                        .header("Authorization", bearerOf(doctorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(recordReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.diagnosis").value("ذبحة صدرية مستقرة"))
                .andExpect(jsonPath("$.prescriptions.length()").value(1))
                .andExpect(jsonPath("$.prescriptions[0].medicationName").value("نيتروجليسرين"))
                .andReturn();

        Long recordId = objectMapper.readTree(
                recordResult.getResponse().getContentAsString()).get("id").asLong();
        assertThat(recordId).isPositive();

        // STEP 6: Verify appointment is COMPLETED (auto-set when record is created)
        mockMvc.perform(get("/appointments/{id}", newApptId)
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // STEP 7: Verify medical record appears in patient history
        mockMvc.perform(get("/medical-records/patient/{id}", newPatientId)
                        .header("Authorization", bearerOf(doctorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].diagnosis").value("ذبحة صدرية مستقرة"));

        // STEP 8: Verify dashboard reflects new data
        mockMvc.perform(get("/dashboard/stats")
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPatients").value(2)) // original + new
                .andExpect(jsonPath("$.totalMedicalRecords").value(1));
    }

    // ─────────────────────────────────────────────────────────────
    // Scenario 2: Appointment Cancellation Flow
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("🔄 سيناريو: حجز موعد ثم إلغاؤه")
    void shouldHandleAppointmentCancellation() throws Exception {

        // Book appointment
        AppointmentDTO.Request req = new AppointmentDTO.Request();
        req.setPatientId(savedPatientId);
        req.setDoctorId(savedDoctorId);
        req.setAppointmentDateTime(LocalDateTime.now().plusDays(3));
        req.setReason("موعد سيتم إلغاؤه");
        req.setType(Appointment.AppointmentType.REGULAR);
        req.setDurationMinutes(30);

        MvcResult result = mockMvc.perform(post("/appointments")
                        .header("Authorization", bearerOf(receptionistToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isCreated())
                .andReturn();

        Long apptId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();

        // Cancel the appointment
        mockMvc.perform(delete("/appointments/{id}/cancel", apptId)
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isNoContent());

        // Verify status changed
        mockMvc.perform(get("/appointments/{id}", apptId)
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Can book same slot again after cancellation
        AppointmentDTO.Request newReq = new AppointmentDTO.Request();
        newReq.setPatientId(savedPatientId);
        newReq.setDoctorId(savedDoctorId);
        newReq.setAppointmentDateTime(LocalDateTime.now().plusDays(3));
        newReq.setReason("موعد بديل في نفس الوقت");
        newReq.setType(Appointment.AppointmentType.REGULAR);
        newReq.setDurationMinutes(30);

        mockMvc.perform(post("/appointments")
                        .header("Authorization", bearerOf(receptionistToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newReq)))
                .andExpect(status().isCreated());
    }

    // ─────────────────────────────────────────────────────────────
    // Scenario 3: New Department with Doctor
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("🔄 سيناريو: إضافة قسم جديد ثم إضافة طبيب له")
    void shouldAddDepartmentThenDoctor() throws Exception {

        // Create new department
        DepartmentDTO.Request deptReq = new DepartmentDTO.Request();
        deptReq.setName("قسم الأمراض الصدرية - E2E");
        deptReq.setDescription("علاج أمراض الصدر والتنفس");
        deptReq.setLocation("الطابق الرابع");
        deptReq.setStatus(Department.DepartmentStatus.ACTIVE);

        MvcResult deptResult = mockMvc.perform(post("/departments")
                        .header("Authorization", bearerOf(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(deptReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Long newDeptId = objectMapper.readTree(
                deptResult.getResponse().getContentAsString()).get("id").asLong();

        // Add doctor to the new department
        DoctorDTO.Request docReq = new DoctorDTO.Request();
        docReq.setFirstName("حسام");
        docReq.setLastName("الصاوي");
        docReq.setEmail("dr.hossam.e2e@hospital.com");
        docReq.setLicenseNumber("LIC-E2E-001");
        docReq.setSpecialization("أمراض الصدر والتنفس");
        docReq.setYearsOfExperience(12);
        docReq.setStatus(Doctor.DoctorStatus.ACTIVE);
        docReq.setDepartmentId(newDeptId);

        mockMvc.perform(post("/doctors")
                        .header("Authorization", bearerOf(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(docReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.departmentId").value(newDeptId))
                .andExpect(jsonPath("$.departmentName").value("قسم الأمراض الصدرية - E2E"));

        // Verify doctor appears in department
        mockMvc.perform(get("/doctors/department/{id}", newDeptId)
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].specialization").value("أمراض الصدر والتنفس"));

        // Verify dashboard total departments increased
        mockMvc.perform(get("/dashboard/stats")
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDepartments").value(2)); // original + new
    }
}
