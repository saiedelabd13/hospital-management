package com.hospital.service;

import com.hospital.dto.MedicalRecordDTO;
import com.hospital.dto.PrescriptionDTO;
import com.hospital.entity.*;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import com.hospital.util.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicalRecordService - Unit Tests")
class MedicalRecordServiceTest {

    @Mock private MedicalRecordRepository medicalRecordRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private AppointmentRepository appointmentRepository;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    private Patient patient;
    private Doctor doctor;
    private MedicalRecord medicalRecord;
    private Department department;

    @BeforeEach
    void setUp() {
        department = TestDataBuilder.buildDepartment();
        department.setId(1L);

        doctor = TestDataBuilder.buildDoctor(department);
        doctor.setId(1L);

        patient = TestDataBuilder.buildPatient();
        patient.setId(1L);

        medicalRecord = TestDataBuilder.buildMedicalRecord(patient, doctor);
        medicalRecord.setId(1L);
    }

    // ── getAllRecords ──────────────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع جميع السجلات الطبية")
    void shouldReturnAllRecords() {
        given(medicalRecordRepository.findAll()).willReturn(List.of(medicalRecord));

        List<MedicalRecordDTO.Response> result = medicalRecordService.getAllRecords();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDiagnosis()).isEqualTo("ارتفاع ضغط الدم");
    }

    // ── getRecordById ─────────────────────────────────────────────
    @Nested
    @DisplayName("getRecordById()")
    class GetById {

        @Test
        @DisplayName("✅ يرجع السجل بالمعرف")
        void shouldReturnRecordById() {
            given(medicalRecordRepository.findById(1L)).willReturn(Optional.of(medicalRecord));

            MedicalRecordDTO.Response result = medicalRecordService.getRecordById(1L);

            assertThat(result.getPatientName()).isEqualTo("عمر خالد");
            assertThat(result.getDoctorName()).isEqualTo("Dr. أحمد محمد");
            assertThat(result.getBloodPressure()).isEqualTo("140/90");
        }

        @Test
        @DisplayName("❌ يرمي استثناء عند عدم وجود السجل")
        void shouldThrowWhenNotFound() {
            given(medicalRecordRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> medicalRecordService.getRecordById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── createRecord ──────────────────────────────────────────────
    @Nested
    @DisplayName("createRecord()")
    class CreateRecord {

        private MedicalRecordDTO.Request buildRequest() {
            MedicalRecordDTO.Request req = new MedicalRecordDTO.Request();
            req.setPatientId(1L);
            req.setDoctorId(1L);
            req.setDiagnosis("التهاب الحلق");
            req.setSymptoms("ألم في الحلق وارتفاع حرارة");
            req.setTreatmentPlan("مضادات حيوية");
            req.setWeight(75.0);
            req.setHeight(170.0);
            req.setBloodPressure("120/80");
            req.setHeartRate(72);
            req.setTemperature(38.5);
            return req;
        }

        @Test
        @DisplayName("✅ ينشئ سجل طبي بدون وصفات")
        void shouldCreateRecordWithoutPrescriptions() {
            MedicalRecordDTO.Request req = buildRequest();

            given(patientRepository.findById(1L)).willReturn(Optional.of(patient));
            given(doctorRepository.findById(1L)).willReturn(Optional.of(doctor));
            given(medicalRecordRepository.save(any())).willReturn(medicalRecord);

            MedicalRecordDTO.Response result = medicalRecordService.createRecord(req);

            assertThat(result).isNotNull();
            then(medicalRecordRepository).should(atLeastOnce()).save(any(MedicalRecord.class));
        }

        @Test
        @DisplayName("✅ ينشئ سجل طبي مع وصفات")
        void shouldCreateRecordWithPrescriptions() {
            MedicalRecordDTO.Request req = buildRequest();

            PrescriptionDTO.Request pReq = new PrescriptionDTO.Request();
            pReq.setMedicationName("أموكسيسيلين");
            pReq.setDosage("500mg");
            pReq.setFrequency("3 مرات يومياً");
            pReq.setDurationDays(7);
            req.setPrescriptions(List.of(pReq));

            given(patientRepository.findById(1L)).willReturn(Optional.of(patient));
            given(doctorRepository.findById(1L)).willReturn(Optional.of(doctor));
            given(medicalRecordRepository.save(any())).willReturn(medicalRecord);

            MedicalRecordDTO.Response result = medicalRecordService.createRecord(req);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("❌ يرمي استثناء عند عدم وجود المريض")
        void shouldThrowWhenPatientNotFound() {
            MedicalRecordDTO.Request req = buildRequest();
            given(patientRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> medicalRecordService.createRecord(req))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("❌ يرمي استثناء عند عدم وجود الطبيب")
        void shouldThrowWhenDoctorNotFound() {
            MedicalRecordDTO.Request req = buildRequest();
            given(patientRepository.findById(1L)).willReturn(Optional.of(patient));
            given(doctorRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> medicalRecordService.createRecord(req))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── getRecordsByPatient ───────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع السجلات الطبية للمريض مرتبة بالتاريخ")
    void shouldReturnRecordsByPatient() {
        given(medicalRecordRepository.findByPatientIdOrderByCreatedAtDesc(1L))
                .willReturn(List.of(medicalRecord));

        List<MedicalRecordDTO.Response> result = medicalRecordService.getRecordsByPatient(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatientId()).isEqualTo(1L);
    }

    // ── deleteRecord ──────────────────────────────────────────────
    @Test
    @DisplayName("✅ يحذف السجل الطبي بنجاح")
    void shouldDeleteRecord() {
        given(medicalRecordRepository.findById(1L)).willReturn(Optional.of(medicalRecord));

        assertThatCode(() -> medicalRecordService.deleteRecord(1L)).doesNotThrowAnyException();
        then(medicalRecordRepository).should().delete(medicalRecord);
    }
}
