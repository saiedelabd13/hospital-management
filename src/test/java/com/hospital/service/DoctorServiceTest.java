package com.hospital.service;

import com.hospital.dto.DoctorDTO;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.DoctorRepository;
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
@DisplayName("DoctorService - Unit Tests")
class DoctorServiceTest {

    @Mock private DoctorRepository doctorRepository;
    @Mock private DepartmentRepository departmentRepository;

    @InjectMocks
    private DoctorService doctorService;

    private Department department;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        department = TestDataBuilder.buildDepartment();
        department.setId(1L);

        doctor = TestDataBuilder.buildDoctor(department);
        doctor.setId(1L);
    }

    // ── getAllDoctors ─────────────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع جميع الأطباء")
    void shouldReturnAllDoctors() {
        given(doctorRepository.findAll()).willReturn(List.of(doctor));

        List<DoctorDTO.Response> result = doctorService.getAllDoctors();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFullName()).isEqualTo("Dr. أحمد محمد");
        assertThat(result.get(0).getDepartmentName()).isEqualTo("قسم القلب");
    }

    // ── getDoctorById ─────────────────────────────────────────────
    @Nested
    @DisplayName("getDoctorById()")
    class GetDoctorById {

        @Test
        @DisplayName("✅ يرجع الطبيب بالمعرف")
        void shouldReturnDoctorById() {
            given(doctorRepository.findById(1L)).willReturn(Optional.of(doctor));

            DoctorDTO.Response result = doctorService.getDoctorById(1L);

            assertThat(result.getEmail()).isEqualTo("dr.ahmed@hospital.com");
            assertThat(result.getLicenseNumber()).isEqualTo("LIC-001");
            assertThat(result.getSpecialization()).isEqualTo("أمراض القلب");
        }

        @Test
        @DisplayName("❌ يرمي استثناء عند عدم وجود الطبيب")
        void shouldThrowWhenDoctorNotFound() {
            given(doctorRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> doctorService.getDoctorById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── createDoctor ──────────────────────────────────────────────
    @Nested
    @DisplayName("createDoctor()")
    class CreateDoctor {

        private DoctorDTO.Request buildRequest() {
            DoctorDTO.Request req = new DoctorDTO.Request();
            req.setFirstName("سارة");
            req.setLastName("علي");
            req.setEmail("dr.sara@hospital.com");
            req.setLicenseNumber("LIC-002");
            req.setSpecialization("طب الأطفال");
            req.setStatus(Doctor.DoctorStatus.ACTIVE);
            req.setDepartmentId(1L);
            return req;
        }

        @Test
        @DisplayName("✅ ينشئ طبيباً جديداً بنجاح")
        void shouldCreateDoctorSuccessfully() {
            DoctorDTO.Request req = buildRequest();
            Doctor newDoctor = TestDataBuilder.buildDoctor("dr.sara@hospital.com", "LIC-002", department);
            newDoctor.setId(2L);

            given(doctorRepository.existsByEmail("dr.sara@hospital.com")).willReturn(false);
            given(doctorRepository.existsByLicenseNumber("LIC-002")).willReturn(false);
            given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
            given(doctorRepository.save(any())).willReturn(newDoctor);

            DoctorDTO.Response result = doctorService.createDoctor(req);

            assertThat(result.getEmail()).isEqualTo("dr.sara@hospital.com");
            then(doctorRepository).should().save(any(Doctor.class));
        }

        @Test
        @DisplayName("❌ يرمي استثناء عند تكرار البريد الإلكتروني")
        void shouldThrowWhenEmailExists() {
            DoctorDTO.Request req = buildRequest();
            given(doctorRepository.existsByEmail("dr.sara@hospital.com")).willReturn(true);

            assertThatThrownBy(() -> doctorService.createDoctor(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("البريد الإلكتروني");

            then(doctorRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("❌ يرمي استثناء عند تكرار رقم الترخيص")
        void shouldThrowWhenLicenseExists() {
            DoctorDTO.Request req = buildRequest();
            given(doctorRepository.existsByEmail(any())).willReturn(false);
            given(doctorRepository.existsByLicenseNumber("LIC-002")).willReturn(true);

            assertThatThrownBy(() -> doctorService.createDoctor(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("الترخيص");
        }

        @Test
        @DisplayName("❌ يرمي استثناء عند وجود قسم غير موجود")
        void shouldThrowWhenDepartmentNotFound() {
            DoctorDTO.Request req = buildRequest();
            given(doctorRepository.existsByEmail(any())).willReturn(false);
            given(doctorRepository.existsByLicenseNumber(any())).willReturn(false);
            given(departmentRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> doctorService.createDoctor(req))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── searchDoctors ─────────────────────────────────────────────
    @Test
    @DisplayName("✅ يبحث عن الأطباء بالكلمة المفتاحية")
    void shouldSearchDoctors() {
        given(doctorRepository.searchDoctors("أحمد")).willReturn(List.of(doctor));

        List<DoctorDTO.Response> result = doctorService.searchDoctors("أحمد");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("أحمد");
    }

    @Test
    @DisplayName("✅ يرجع قائمة فارغة عند عدم وجود نتائج للبحث")
    void shouldReturnEmptyWhenSearchNotFound() {
        given(doctorRepository.searchDoctors("xyz")).willReturn(List.of());

        List<DoctorDTO.Response> result = doctorService.searchDoctors("xyz");

        assertThat(result).isEmpty();
    }

    // ── getDoctorsByDepartment ────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع الأطباء حسب القسم")
    void shouldReturnDoctorsByDepartment() {
        given(doctorRepository.findByDepartmentId(1L)).willReturn(List.of(doctor));

        List<DoctorDTO.Response> result = doctorService.getDoctorsByDepartment(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDepartmentId()).isEqualTo(1L);
    }

    // ── deleteDoctor ──────────────────────────────────────────────
    @Test
    @DisplayName("✅ يحذف الطبيب بنجاح")
    void shouldDeleteDoctor() {
        given(doctorRepository.findById(1L)).willReturn(Optional.of(doctor));

        assertThatCode(() -> doctorService.deleteDoctor(1L)).doesNotThrowAnyException();
        then(doctorRepository).should().delete(doctor);
    }

    @Test
    @DisplayName("❌ يرمي استثناء عند حذف طبيب غير موجود")
    void shouldThrowWhenDeletingNonExistentDoctor() {
        given(doctorRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.deleteDoctor(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
