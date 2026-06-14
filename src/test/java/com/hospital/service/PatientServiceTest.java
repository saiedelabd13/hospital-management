package com.hospital.service;

import com.hospital.dto.PatientDTO;
import com.hospital.entity.Patient;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.PatientRepository;
import com.hospital.util.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatientService - Unit Tests")
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = TestDataBuilder.buildPatient();
        patient.setId(1L);
    }

    // ── getAllPatients ─────────────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع جميع المرضى")
    void shouldReturnAllPatients() {
        given(patientRepository.findAll()).willReturn(List.of(patient));

        List<PatientDTO.Response> result = patientService.getAllPatients();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFullName()).isEqualTo("عمر خالد");
    }

    // ── getPatientById ────────────────────────────────────────────
    @Nested
    @DisplayName("getPatientById()")
    class GetPatientById {

        @Test
        @DisplayName("✅ يرجع المريض بالمعرف")
        void shouldReturnPatient() {
            given(patientRepository.findById(1L)).willReturn(Optional.of(patient));

            PatientDTO.Response result = patientService.getPatientById(1L);

            assertThat(result.getEmail()).isEqualTo("omar@email.com");
            assertThat(result.getGender()).isEqualTo(Patient.Gender.MALE);
            assertThat(result.getBloodType()).isEqualTo("A+");
        }

        @Test
        @DisplayName("❌ يرمي استثناء عند عدم وجود المريض")
        void shouldThrowWhenNotFound() {
            given(patientRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> patientService.getPatientById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ── createPatient ─────────────────────────────────────────────
    @Nested
    @DisplayName("createPatient()")
    class CreatePatient {

        private PatientDTO.Request buildRequest() {
            PatientDTO.Request req = new PatientDTO.Request();
            req.setFirstName("محمود");
            req.setLastName("سعيد");
            req.setEmail("mahmoud@email.com");
            req.setDateOfBirth(LocalDate.of(1995, 1, 1));
            req.setGender(Patient.Gender.MALE);
            req.setNationalId("29501011234567");
            req.setStatus(Patient.PatientStatus.ACTIVE);
            return req;
        }

        @Test
        @DisplayName("✅ ينشئ مريض جديد بنجاح")
        void shouldCreatePatient() {
            PatientDTO.Request req = buildRequest();
            Patient saved = TestDataBuilder.buildPatient("mahmoud@email.com", "29501011234567");
            saved.setId(2L);

            given(patientRepository.existsByEmail("mahmoud@email.com")).willReturn(false);
            given(patientRepository.existsByNationalId("29501011234567")).willReturn(false);
            given(patientRepository.save(any())).willReturn(saved);

            PatientDTO.Response result = patientService.createPatient(req);

            assertThat(result.getEmail()).isEqualTo("mahmoud@email.com");
            then(patientRepository).should().save(any(Patient.class));
        }

        @Test
        @DisplayName("❌ يرمي استثناء عند تكرار البريد الإلكتروني")
        void shouldThrowWhenEmailExists() {
            PatientDTO.Request req = buildRequest();
            given(patientRepository.existsByEmail("mahmoud@email.com")).willReturn(true);

            assertThatThrownBy(() -> patientService.createPatient(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("البريد الإلكتروني");

            then(patientRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("❌ يرمي استثناء عند تكرار الرقم القومي")
        void shouldThrowWhenNationalIdExists() {
            PatientDTO.Request req = buildRequest();
            given(patientRepository.existsByEmail(any())).willReturn(false);
            given(patientRepository.existsByNationalId("29501011234567")).willReturn(true);

            assertThatThrownBy(() -> patientService.createPatient(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("الرقم القومي");
        }
    }

    // ── updatePatient ─────────────────────────────────────────────
    @Test
    @DisplayName("✅ يعدل بيانات المريض بنجاح")
    void shouldUpdatePatient() {
        PatientDTO.Request req = new PatientDTO.Request();
        req.setFirstName("عمر");
        req.setLastName("خالد");
        req.setEmail("omar@email.com");
        req.setDateOfBirth(LocalDate.of(1990, 5, 15));
        req.setGender(Patient.Gender.MALE);
        req.setAddress("القاهرة الجديدة");
        req.setStatus(Patient.PatientStatus.ACTIVE);

        given(patientRepository.findById(1L)).willReturn(Optional.of(patient));
        given(patientRepository.existsByEmail(any())).willReturn(false);
        given(patientRepository.save(any())).willReturn(patient);

        PatientDTO.Response result = patientService.updatePatient(1L, req);

        assertThat(result).isNotNull();
        then(patientRepository).should().save(any(Patient.class));
    }

    // ── deletePatient ─────────────────────────────────────────────
    @Test
    @DisplayName("✅ يحذف المريض بنجاح")
    void shouldDeletePatient() {
        given(patientRepository.findById(1L)).willReturn(Optional.of(patient));

        assertThatCode(() -> patientService.deletePatient(1L)).doesNotThrowAnyException();
        then(patientRepository).should().delete(patient);
    }

    // ── searchPatients ────────────────────────────────────────────
    @Test
    @DisplayName("✅ يبحث عن المرضى بالاسم")
    void shouldSearchPatients() {
        given(patientRepository.searchPatients("عمر")).willReturn(List.of(patient));

        List<PatientDTO.Summary> result = patientService.searchPatients("عمر");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFullName()).contains("عمر");
    }

    // ── countActivePatients ───────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع عدد المرضى النشطين")
    void shouldCountActivePatients() {
        given(patientRepository.countActivePatients()).willReturn(5L);

        Long count = patientService.countActivePatients();

        assertThat(count).isEqualTo(5L);
    }
}
