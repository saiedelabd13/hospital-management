package com.hospital.repository;

import com.hospital.entity.*;
import com.hospital.util.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Repository - Integration Tests")
class RepositoryIntegrationTest {

    @Autowired TestEntityManager em;
    @Autowired DepartmentRepository departmentRepository;
    @Autowired DoctorRepository doctorRepository;
    @Autowired PatientRepository patientRepository;
    @Autowired AppointmentRepository appointmentRepository;
    @Autowired MedicalRecordRepository medicalRecordRepository;

    private Department department;
    private Doctor doctor;
    private Patient patient;

    @BeforeEach
    void setUp() {
        department = em.persistAndFlush(TestDataBuilder.buildDepartment());
        doctor     = em.persistAndFlush(TestDataBuilder.buildDoctor(department));
        patient    = em.persistAndFlush(TestDataBuilder.buildPatient());
    }

    // ── DepartmentRepository ──────────────────────────────────────
    @Nested
    @DisplayName("DepartmentRepository")
    class DepartmentRepositoryTests {

        @Test
        @DisplayName("✅ يجد القسم بالاسم")
        void shouldFindByName() {
            Optional<Department> result = departmentRepository.findByName("قسم القلب");
            assertThat(result).isPresent();
            assertThat(result.get().getLocation()).isEqualTo("الطابق الثاني");
        }

        @Test
        @DisplayName("✅ يرجع فارغ لاسم غير موجود")
        void shouldReturnEmptyForUnknownName() {
            Optional<Department> result = departmentRepository.findByName("قسم غير موجود");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("✅ يجد الأقسام النشطة")
        void shouldFindActiveOnly() {
            em.persistAndFlush(TestDataBuilder.buildInactiveDepartment());
            List<Department> active = departmentRepository.findByStatus(Department.DepartmentStatus.ACTIVE);
            assertThat(active).allMatch(d -> d.getStatus() == Department.DepartmentStatus.ACTIVE);
        }

        @Test
        @DisplayName("✅ يتحقق من وجود الاسم")
        void shouldCheckExistenceByName() {
            assertThat(departmentRepository.existsByName("قسم القلب")).isTrue();
            assertThat(departmentRepository.existsByName("قسم غير موجود")).isFalse();
        }
    }

    // ── DoctorRepository ──────────────────────────────────────────
    @Nested
    @DisplayName("DoctorRepository")
    class DoctorRepositoryTests {

        @Test
        @DisplayName("✅ يجد الطبيب بالبريد الإلكتروني")
        void shouldFindByEmail() {
            Optional<Doctor> result = doctorRepository.findByEmail("dr.ahmed@hospital.com");
            assertThat(result).isPresent();
            assertThat(result.get().getSpecialization()).isEqualTo("أمراض القلب");
        }

        @Test
        @DisplayName("✅ يجد الطبيب برقم الترخيص")
        void shouldFindByLicenseNumber() {
            Optional<Doctor> result = doctorRepository.findByLicenseNumber("LIC-001");
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("✅ يجد أطباء القسم")
        void shouldFindByDepartmentId() {
            List<Doctor> doctors = doctorRepository.findByDepartmentId(department.getId());
            assertThat(doctors).hasSize(1);
            assertThat(doctors.get(0).getDepartment().getId()).isEqualTo(department.getId());
        }

        @Test
        @DisplayName("✅ يجد الأطباء النشطين")
        void shouldFindActiveOnly() {
            em.persistAndFlush(TestDataBuilder.buildInactiveDoctor(department));
            List<Doctor> active = doctorRepository.findByStatus(Doctor.DoctorStatus.ACTIVE);
            assertThat(active).allMatch(d -> d.getStatus() == Doctor.DoctorStatus.ACTIVE);
        }

        @Test
        @DisplayName("✅ البحث عن طبيب بالاسم")
        void shouldSearchByName() {
            List<Doctor> results = doctorRepository.searchDoctors("أحمد");
            assertThat(results).isNotEmpty();
            assertThat(results.get(0).getFirstName()).contains("أحمد");
        }

        @Test
        @DisplayName("✅ البحث عن طبيب بالتخصص")
        void shouldSearchBySpecialization() {
            List<Doctor> results = doctorRepository.searchDoctors("القلب");
            assertThat(results).isNotEmpty();
        }

        @Test
        @DisplayName("✅ البحث بكلمة غير موجودة يرجع قائمة فارغة")
        void shouldReturnEmptyForUnknownKeyword() {
            List<Doctor> results = doctorRepository.searchDoctors("xyz_not_exist");
            assertThat(results).isEmpty();
        }
    }

    // ── PatientRepository ─────────────────────────────────────────
    @Nested
    @DisplayName("PatientRepository")
    class PatientRepositoryTests {

        @Test
        @DisplayName("✅ يجد المريض بالبريد الإلكتروني")
        void shouldFindByEmail() {
            Optional<Patient> result = patientRepository.findByEmail("omar@email.com");
            assertThat(result).isPresent();
            assertThat(result.get().getBloodType()).isEqualTo("A+");
        }

        @Test
        @DisplayName("✅ يجد المريض بالرقم القومي")
        void shouldFindByNationalId() {
            Optional<Patient> result = patientRepository.findByNationalId("29005151234567");
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("✅ البحث عن مريض بالاسم")
        void shouldSearchByFirstName() {
            List<Patient> results = patientRepository.searchPatients("عمر");
            assertThat(results).isNotEmpty();
        }

        @Test
        @DisplayName("✅ البحث عن مريض بالاسم الأخير")
        void shouldSearchByLastName() {
            List<Patient> results = patientRepository.searchPatients("خالد");
            assertThat(results).isNotEmpty();
        }

        @Test
        @DisplayName("✅ يعدّ المرضى النشطين")
        void shouldCountActivePatients() {
            Long count = patientRepository.countActivePatients();
            assertThat(count).isEqualTo(1L);
        }
    }

    // ── AppointmentRepository ─────────────────────────────────────
    @Nested
    @DisplayName("AppointmentRepository")
    class AppointmentRepositoryTests {

        private Appointment appointment;

        @BeforeEach
        void createAppointment() {
            appointment = em.persistAndFlush(TestDataBuilder.buildAppointment(patient, doctor));
        }

        @Test
        @DisplayName("✅ يجد مواعيد المريض")
        void shouldFindByPatientId() {
            List<Appointment> results = appointmentRepository.findByPatientId(patient.getId());
            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("✅ يجد مواعيد الطبيب")
        void shouldFindByDoctorId() {
            List<Appointment> results = appointmentRepository.findByDoctorId(doctor.getId());
            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("✅ يجد المواعيد حسب الحالة")
        void shouldFindByStatus() {
            List<Appointment> results = appointmentRepository.findByStatus(Appointment.AppointmentStatus.SCHEDULED);
            assertThat(results).allMatch(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED);
        }

        @Test
        @DisplayName("✅ يجد المواعيد في نطاق زمني")
        void shouldFindByDateRange() {
            LocalDateTime start = LocalDateTime.now().minusHours(1);
            LocalDateTime end   = LocalDateTime.now().plusDays(2);

            List<Appointment> results = appointmentRepository.findByDateRange(start, end);
            assertThat(results).isNotEmpty();
        }

        @Test
        @DisplayName("✅ يعدّ مواعيد الطبيب في نطاق زمني")
        void shouldCountDoctorAppointmentsInRange() {
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end   = LocalDateTime.now().plusDays(2);

            Long count = appointmentRepository.countDoctorAppointmentsInRange(
                    doctor.getId(), start, end);
            assertThat(count).isEqualTo(1L);
        }

        @Test
        @DisplayName("✅ لا يعدّ المواعيد الملغاة")
        void shouldNotCountCancelledAppointments() {
            appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
            em.persistAndFlush(appointment);

            Long count = appointmentRepository.countDoctorAppointmentsInRange(
                    doctor.getId(),
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(2));
            assertThat(count).isEqualTo(0L);
        }
    }

    // ── MedicalRecordRepository ───────────────────────────────────
    @Nested
    @DisplayName("MedicalRecordRepository")
    class MedicalRecordRepositoryTests {

        @Test
        @DisplayName("✅ يجد السجلات الطبية مرتبة بالتاريخ")
        void shouldFindByPatientIdOrderedByDate() {
            MedicalRecord r1 = em.persistAndFlush(TestDataBuilder.buildMedicalRecord(patient, doctor));
            MedicalRecord r2 = em.persistAndFlush(TestDataBuilder.buildMedicalRecord(patient, doctor));

            List<MedicalRecord> results = medicalRecordRepository
                    .findByPatientIdOrderByCreatedAtDesc(patient.getId());

            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("✅ يجد السجلات الطبية للطبيب")
        void shouldFindByDoctorId() {
            em.persistAndFlush(TestDataBuilder.buildMedicalRecord(patient, doctor));
            List<MedicalRecord> results = medicalRecordRepository.findByDoctorId(doctor.getId());
            assertThat(results).isNotEmpty();
        }
    }
}
