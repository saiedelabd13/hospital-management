package com.hospital.integration;

import com.hospital.dto.MedicalRecordDTO;
import com.hospital.dto.PrescriptionDTO;
import com.hospital.entity.*;
import com.hospital.util.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Medical Record Controller - Integration Tests")
class MedicalRecordControllerIntegrationTest extends BaseIntegrationTest {

    private Long savedRecordId;

    @BeforeEach
    void createRecord() {
        MedicalRecord record = medicalRecordRepository.save(
                TestDataBuilder.buildMedicalRecord(
                        patientRepository.findById(savedPatientId).orElseThrow(),
                        doctorRepository.findById(savedDoctorId).orElseThrow()));
        savedRecordId = record.getId();
    }

    // ── GET all ───────────────────────────────────────────────────
    @Test
    @DisplayName("✅ المدير يجلب جميع السجلات الطبية")
    void shouldReturnAllRecords() throws Exception {
        mockMvc.perform(get("/medical-records")
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("❌ الموظف لا يمكنه الوصول للسجلات الطبية")
    void shouldForbidReceptionistAccess() throws Exception {
        mockMvc.perform(get("/medical-records")
                        .header("Authorization", bearerOf(receptionistToken)))
                .andExpect(status().isForbidden());
    }

    // ── GET by ID ─────────────────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع السجل الطبي بالمعرف مع التفاصيل")
    void shouldReturnRecordById() throws Exception {
        mockMvc.perform(get("/medical-records/{id}", savedRecordId)
                        .header("Authorization", bearerOf(doctorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedRecordId))
                .andExpect(jsonPath("$.diagnosis").value("ارتفاع ضغط الدم"))
                .andExpect(jsonPath("$.patientName").value("عمر خالد"))
                .andExpect(jsonPath("$.bloodPressure").value("140/90"))
                .andExpect(jsonPath("$.heartRate").value(85));
    }

    // ── GET by patient ────────────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع السجلات الطبية للمريض")
    void shouldReturnRecordsByPatient() throws Exception {
        mockMvc.perform(get("/medical-records/patient/{id}", savedPatientId)
                        .header("Authorization", bearerOf(doctorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].patientId").value(savedPatientId));
    }

    // ── GET by doctor ─────────────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع السجلات الطبية للطبيب")
    void shouldReturnRecordsByDoctor() throws Exception {
        mockMvc.perform(get("/medical-records/doctor/{id}", savedDoctorId)
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ── POST create ───────────────────────────────────────────────
    @Nested
    @DisplayName("POST /medical-records")
    class Create {

        @Test
        @DisplayName("✅ الطبيب ينشئ سجل طبي مع وصفات")
        void shouldCreateRecordWithPrescriptions() throws Exception {
            PrescriptionDTO.Request p1 = new PrescriptionDTO.Request();
            p1.setMedicationName("أملوديبين");
            p1.setDosage("5mg");
            p1.setFrequency("مرة يومياً");
            p1.setDurationDays(30);
            p1.setStartDate(LocalDate.now());
            p1.setInstructions("مع الإفطار");
            p1.setRefillAllowed(true);

            PrescriptionDTO.Request p2 = new PrescriptionDTO.Request();
            p2.setMedicationName("أسبرين");
            p2.setDosage("100mg");
            p2.setFrequency("مرة يومياً");
            p2.setDurationDays(30);
            p2.setRefillAllowed(false);

            MedicalRecordDTO.Request req = new MedicalRecordDTO.Request();
            req.setPatientId(savedPatientId);
            req.setDoctorId(savedDoctorId);
            req.setDiagnosis("ارتفاع ضغط الدم المزمن");
            req.setSymptoms("صداع ودوار");
            req.setTreatmentPlan("أدوية وتغيير نمط الحياة");
            req.setDoctorNotes("متابعة شهرية");
            req.setWeight(82.0);
            req.setHeight(175.0);
            req.setBloodPressure("150/95");
            req.setHeartRate(90);
            req.setTemperature(37.1);
            req.setPrescriptions(List.of(p1, p2));

            mockMvc.perform(post("/medical-records")
                            .header("Authorization", bearerOf(doctorToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.diagnosis").value("ارتفاع ضغط الدم المزمن"))
                    .andExpect(jsonPath("$.prescriptions").isArray())
                    .andExpect(jsonPath("$.prescriptions.length()").value(2))
                    .andExpect(jsonPath("$.prescriptions[0].medicationName").value("أملوديبين"))
                    .andExpect(jsonPath("$.prescriptions[0].status").value("ACTIVE"));
        }

        @Test
        @DisplayName("✅ إنشاء سجل طبي بدون وصفات")
        void shouldCreateRecordWithoutPrescriptions() throws Exception {
            MedicalRecordDTO.Request req = new MedicalRecordDTO.Request();
            req.setPatientId(savedPatientId);
            req.setDoctorId(savedDoctorId);
            req.setDiagnosis("التهاب الحلق");
            req.setSymptoms("ألم في الحلق");
            req.setTreatmentPlan("راحة وتناول سوائل");

            mockMvc.perform(post("/medical-records")
                            .header("Authorization", bearerOf(doctorToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.diagnosis").value("التهاب الحلق"));
        }

        @Test
        @DisplayName("❌ الموظف لا يمكنه إنشاء سجل طبي")
        void shouldForbidReceptionistFromCreatingRecord() throws Exception {
            MedicalRecordDTO.Request req = new MedicalRecordDTO.Request();
            req.setPatientId(savedPatientId);
            req.setDoctorId(savedDoctorId);
            req.setDiagnosis("تشخيص غير مسموح");

            mockMvc.perform(post("/medical-records")
                            .header("Authorization", bearerOf(receptionistToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("❌ يرفض سجل طبي بدون تشخيص")
        void shouldRejectRecordWithoutDiagnosis() throws Exception {
            MedicalRecordDTO.Request req = new MedicalRecordDTO.Request();
            req.setPatientId(savedPatientId);
            req.setDoctorId(savedDoctorId);

            mockMvc.perform(post("/medical-records")
                            .header("Authorization", bearerOf(doctorToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.diagnosis").isNotEmpty());
        }
    }

    // ── PUT update ────────────────────────────────────────────────
    @Test
    @DisplayName("✅ يعدل السجل الطبي بنجاح")
    void shouldUpdateMedicalRecord() throws Exception {
        MedicalRecordDTO.Request req = new MedicalRecordDTO.Request();
        req.setPatientId(savedPatientId);
        req.setDoctorId(savedDoctorId);
        req.setDiagnosis("ارتفاع ضغط الدم - محدث");
        req.setSymptoms("تحسن ملحوظ");
        req.setTreatmentPlan("الاستمرار في العلاج");
        req.setBloodPressure("130/85");
        req.setHeartRate(78);
        req.setTemperature(36.8);

        mockMvc.perform(put("/medical-records/{id}", savedRecordId)
                        .header("Authorization", bearerOf(doctorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosis").value("ارتفاع ضغط الدم - محدث"))
                .andExpect(jsonPath("$.bloodPressure").value("130/85"));
    }

    // ── DELETE ────────────────────────────────────────────────────
    @Test
    @DisplayName("✅ المدير يحذف السجل الطبي بنجاح")
    void shouldDeleteRecord() throws Exception {
        mockMvc.perform(delete("/medical-records/{id}", savedRecordId)
                        .header("Authorization", bearerOf(adminToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("❌ الطبيب لا يمكنه حذف السجل الطبي")
    void shouldForbidDoctorFromDeletingRecord() throws Exception {
        mockMvc.perform(delete("/medical-records/{id}", savedRecordId)
                        .header("Authorization", bearerOf(doctorToken)))
                .andExpect(status().isForbidden());
    }
}
