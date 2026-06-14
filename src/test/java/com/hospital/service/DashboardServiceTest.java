package com.hospital.service;

import com.hospital.entity.Appointment;
import com.hospital.entity.Doctor;
import com.hospital.repository.*;
import com.hospital.util.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService - Unit Tests")
class DashboardServiceTest {

    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private MedicalRecordRepository medicalRecordRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("✅ يرجع إحصائيات صحيحة لجميع الحقول")
    void shouldReturnCorrectStats() {
        given(patientRepository.count()).willReturn(10L);
        given(patientRepository.countActivePatients()).willReturn(8L);
        given(doctorRepository.count()).willReturn(5L);
        given(doctorRepository.findByStatus(Doctor.DoctorStatus.ACTIVE))
                .willReturn(List.of(
                        TestDataBuilder.buildDoctor(TestDataBuilder.buildDepartment()),
                        TestDataBuilder.buildDoctor(TestDataBuilder.buildDepartment()),
                        TestDataBuilder.buildDoctor(TestDataBuilder.buildDepartment())));
        given(departmentRepository.count()).willReturn(4L);
        given(medicalRecordRepository.count()).willReturn(20L);

        // today/month appointments
        given(appointmentRepository.findByDateRange(any(), any())).willReturn(List.of(
                TestDataBuilder.buildAppointment(
                        TestDataBuilder.buildPatient(),
                        TestDataBuilder.buildDoctor(TestDataBuilder.buildDepartment()))));

        // status counts
        for (Appointment.AppointmentStatus status : Appointment.AppointmentStatus.values()) {
            given(appointmentRepository.findByStatus(status)).willReturn(List.of());
        }

        DashboardService.DashboardStats stats = dashboardService.getDashboardStats();

        assertThat(stats.getTotalPatients()).isEqualTo(10L);
        assertThat(stats.getActivePatients()).isEqualTo(8L);
        assertThat(stats.getTotalDoctors()).isEqualTo(5L);
        assertThat(stats.getActiveDoctors()).isEqualTo(3L);
        assertThat(stats.getTotalDepartments()).isEqualTo(4L);
        assertThat(stats.getTotalMedicalRecords()).isEqualTo(20L);
        assertThat(stats.getAppointmentsByStatus()).isNotNull();
        assertThat(stats.getAppointmentsByStatus()).containsKeys(
                "SCHEDULED", "CONFIRMED", "COMPLETED", "CANCELLED");
    }

    @Test
    @DisplayName("✅ يرجع أصفار عندما لا توجد بيانات")
    void shouldReturnZerosWhenEmpty() {
        given(patientRepository.count()).willReturn(0L);
        given(patientRepository.countActivePatients()).willReturn(0L);
        given(doctorRepository.count()).willReturn(0L);
        given(doctorRepository.findByStatus(any())).willReturn(List.of());
        given(departmentRepository.count()).willReturn(0L);
        given(medicalRecordRepository.count()).willReturn(0L);
        given(appointmentRepository.findByDateRange(any(), any())).willReturn(List.of());
        for (Appointment.AppointmentStatus s : Appointment.AppointmentStatus.values()) {
            given(appointmentRepository.findByStatus(s)).willReturn(List.of());
        }

        DashboardService.DashboardStats stats = dashboardService.getDashboardStats();

        assertThat(stats.getTotalPatients()).isZero();
        assertThat(stats.getTotalDoctors()).isZero();
        assertThat(stats.getTodayAppointments()).isZero();
    }

    @Test
    @DisplayName("✅ يحتوي على إحصائيات المواعيد لكل حالة")
    void shouldContainAllAppointmentStatuses() {
        given(patientRepository.count()).willReturn(0L);
        given(patientRepository.countActivePatients()).willReturn(0L);
        given(doctorRepository.count()).willReturn(0L);
        given(doctorRepository.findByStatus(any())).willReturn(List.of());
        given(departmentRepository.count()).willReturn(0L);
        given(medicalRecordRepository.count()).willReturn(0L);
        given(appointmentRepository.findByDateRange(any(), any())).willReturn(List.of());
        for (Appointment.AppointmentStatus s : Appointment.AppointmentStatus.values()) {
            given(appointmentRepository.findByStatus(s)).willReturn(List.of());
        }

        DashboardService.DashboardStats stats = dashboardService.getDashboardStats();

        for (Appointment.AppointmentStatus status : Appointment.AppointmentStatus.values()) {
            assertThat(stats.getAppointmentsByStatus()).containsKey(status.name());
        }
    }
}
