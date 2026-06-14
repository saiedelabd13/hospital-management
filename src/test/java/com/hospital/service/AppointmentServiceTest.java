package com.hospital.service;

import com.hospital.dto.AppointmentDTO;
import com.hospital.entity.*;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import com.hospital.util.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService - Unit Tests")
class AppointmentServiceTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient patient;
    private Doctor doctor;
    private Appointment appointment;
    private Department department;

    @BeforeEach
    void setUp() {
        department = TestDataBuilder.buildDepartment();
        department.setId(1L);

        doctor = TestDataBuilder.buildDoctor(department);
        doctor.setId(1L);

        patient = TestDataBuilder.buildPatient();
        patient.setId(1L);

        appointment = TestDataBuilder.buildAppointment(patient, doctor);
        appointment.setId(1L);
    }

    // ── getAllAppointments ─────────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع جميع المواعيد")
    void shouldReturnAllAppointments() {
        given(appointmentRepository.findAll()).willReturn(List.of(appointment));

        List<AppointmentDTO.Response> result = appointmentService.getAllAppointments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatientName()).isEqualTo("عمر خالد");
        assertThat(result.get(0).getDoctorName()).isEqualTo("Dr. أحمد محمد");
    }

    // ── createAppointment ─────────────────────────────────────────
    @Nested
    @DisplayName("createAppointment()")
    class CreateAppointment {

        private AppointmentDTO.Request buildRequest() {
            AppointmentDTO.Request req = new AppointmentDTO.Request();
            req.setPatientId(1L);
            req.setDoctorId(1L);
            req.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
            req.setReason("فحص دوري");
            req.setType(Appointment.AppointmentType.REGULAR);
            req.setDurationMinutes(30);
            return req;
        }

        @Test
        @DisplayName("✅ ينشئ موعد جديد بنجاح")
        void shouldCreateAppointmentSuccessfully() {
            AppointmentDTO.Request req = buildRequest();

            given(patientRepository.findById(1L)).willReturn(Optional.of(patient));
            given(doctorRepository.findById(1L)).willReturn(Optional.of(doctor));
            given(appointmentRepository.countDoctorAppointmentsInRange(any(), any(), any())).willReturn(0L);
            given(appointmentRepository.save(any())).willReturn(appointment);

            AppointmentDTO.Response result = appointmentService.createAppointment(req);

            assertThat(result.getStatus()).isEqualTo(Appointment.AppointmentStatus.SCHEDULED);
            then(appointmentRepository).should().save(any(Appointment.class));
        }

        @Test
        @DisplayName("❌ يرمي استثناء عند وجود تعارض في المواعيد")
        void shouldThrowWhenDoctorHasConflictingAppointment() {
            AppointmentDTO.Request req = buildRequest();

            given(patientRepository.findById(1L)).willReturn(Optional.of(patient));
            given(doctorRepository.findById(1L)).willReturn(Optional.of(doctor));
            given(appointmentRepository.countDoctorAppointmentsInRange(any(), any(), any())).willReturn(1L);

            assertThatThrownBy(() -> appointmentService.createAppointment(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("موعد آخر");

            then(appointmentRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("❌ يرمي استثناء عند وجود طبيب غير نشط")
        void shouldThrowWhenDoctorIsNotActive() {
            AppointmentDTO.Request req = buildRequest();
            doctor.setStatus(Doctor.DoctorStatus.ON_LEAVE);

            given(patientRepository.findById(1L)).willReturn(Optional.of(patient));
            given(doctorRepository.findById(1L)).willReturn(Optional.of(doctor));

            assertThatThrownBy(() -> appointmentService.createAppointment(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("غير متاح");
        }

        @Test
        @DisplayName("❌ يرمي استثناء عند عدم وجود المريض")
        void shouldThrowWhenPatientNotFound() {
            AppointmentDTO.Request req = buildRequest();
            given(patientRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> appointmentService.createAppointment(req))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("❌ يرمي استثناء عند عدم وجود الطبيب")
        void shouldThrowWhenDoctorNotFound() {
            AppointmentDTO.Request req = buildRequest();
            given(patientRepository.findById(1L)).willReturn(Optional.of(patient));
            given(doctorRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> appointmentService.createAppointment(req))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── updateAppointmentStatus ───────────────────────────────────
    @Nested
    @DisplayName("updateAppointmentStatus()")
    class UpdateStatus {

        @Test
        @DisplayName("✅ يحدث حالة الموعد إلى CONFIRMED")
        void shouldUpdateStatusToConfirmed() {
            AppointmentDTO.StatusUpdate update = new AppointmentDTO.StatusUpdate();
            update.setStatus(Appointment.AppointmentStatus.CONFIRMED);
            update.setNotes("تم التأكيد");

            given(appointmentRepository.findById(1L)).willReturn(Optional.of(appointment));
            given(appointmentRepository.save(any())).willReturn(appointment);

            AppointmentDTO.Response result = appointmentService.updateAppointmentStatus(1L, update);

            assertThat(result).isNotNull();
            then(appointmentRepository).should().save(any());
        }

        @Test
        @DisplayName("✅ يحدث حالة الموعد إلى COMPLETED")
        void shouldUpdateStatusToCompleted() {
            AppointmentDTO.StatusUpdate update = new AppointmentDTO.StatusUpdate();
            update.setStatus(Appointment.AppointmentStatus.COMPLETED);

            given(appointmentRepository.findById(1L)).willReturn(Optional.of(appointment));
            given(appointmentRepository.save(any())).willReturn(appointment);

            assertThatCode(() -> appointmentService.updateAppointmentStatus(1L, update))
                    .doesNotThrowAnyException();
        }
    }

    // ── cancelAppointment ─────────────────────────────────────────
    @Test
    @DisplayName("✅ يلغي الموعد بنجاح")
    void shouldCancelAppointment() {
        given(appointmentRepository.findById(1L)).willReturn(Optional.of(appointment));
        given(appointmentRepository.save(any())).willReturn(appointment);

        assertThatCode(() -> appointmentService.cancelAppointment(1L)).doesNotThrowAnyException();
        then(appointmentRepository).should().save(argThat(a ->
                a.getStatus() == Appointment.AppointmentStatus.CANCELLED));
    }

    // ── getAppointmentsByPatient ──────────────────────────────────
    @Test
    @DisplayName("✅ يرجع مواعيد المريض")
    void shouldReturnPatientAppointments() {
        given(appointmentRepository.findByPatientId(1L)).willReturn(List.of(appointment));

        List<AppointmentDTO.Response> result = appointmentService.getAppointmentsByPatient(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatientId()).isEqualTo(1L);
    }

    // ── getTodayAppointments ──────────────────────────────────────
    @Test
    @DisplayName("✅ يرجع مواعيد اليوم")
    void shouldReturnTodayAppointments() {
        given(appointmentRepository.findByDateRange(any(), any())).willReturn(List.of(appointment));

        List<AppointmentDTO.Response> result = appointmentService.getTodayAppointments();

        assertThat(result).hasSize(1);
    }

    // ── getAppointmentsByStatus ───────────────────────────────────
    @Test
    @DisplayName("✅ يرجع المواعيد حسب الحالة")
    void shouldReturnAppointmentsByStatus() {
        given(appointmentRepository.findByStatus(Appointment.AppointmentStatus.SCHEDULED))
                .willReturn(List.of(appointment));

        List<AppointmentDTO.Response> result = appointmentService
                .getAppointmentsByStatus(Appointment.AppointmentStatus.SCHEDULED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Appointment.AppointmentStatus.SCHEDULED);
    }
}
