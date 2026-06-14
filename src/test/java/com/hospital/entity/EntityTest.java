package com.hospital.entity;

import com.hospital.util.TestDataBuilder;
import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Entity - Unit Tests")
class EntityTest {

    // ── Doctor ────────────────────────────────────────────────────
    @Nested
    @DisplayName("Doctor Entity")
    class DoctorEntityTest {

        @Test
        @DisplayName("✅ getFullName يرجع الاسم الكامل مع Dr.")
        void shouldReturnFullNameWithPrefix() {
            Department dept = TestDataBuilder.buildDepartment();
            Doctor doctor = TestDataBuilder.buildDoctor(dept);

            assertThat(doctor.getFullName()).isEqualTo("Dr. أحمد محمد");
        }

        @Test
        @DisplayName("✅ Doctor Builder يضبط القيم بشكل صحيح")
        void shouldBuildDoctorCorrectly() {
            Department dept = TestDataBuilder.buildDepartment();
            Doctor doctor = Doctor.builder()
                    .firstName("كريم")
                    .lastName("إبراهيم")
                    .email("dr.karim@hospital.com")
                    .licenseNumber("LIC-100")
                    .specialization("جراحة عامة")
                    .yearsOfExperience(15)
                    .status(Doctor.DoctorStatus.ACTIVE)
                    .department(dept)
                    .build();

            assertThat(doctor.getFullName()).isEqualTo("Dr. كريم إبراهيم");
            assertThat(doctor.getStatus()).isEqualTo(Doctor.DoctorStatus.ACTIVE);
            assertThat(doctor.getDepartment()).isNotNull();
        }

        @Test
        @DisplayName("✅ جميع حالات الطبيب متاحة")
        void shouldHaveAllDoctorStatuses() {
            assertThat(Doctor.DoctorStatus.values())
                    .containsExactlyInAnyOrder(
                            Doctor.DoctorStatus.ACTIVE,
                            Doctor.DoctorStatus.ON_LEAVE,
                            Doctor.DoctorStatus.INACTIVE);
        }
    }

    // ── Patient ───────────────────────────────────────────────────
    @Nested
    @DisplayName("Patient Entity")
    class PatientEntityTest {

        @Test
        @DisplayName("✅ getFullName يرجع الاسم الكامل بدون prefix")
        void shouldReturnFullNameWithoutPrefix() {
            Patient patient = TestDataBuilder.buildPatient();

            assertThat(patient.getFullName()).isEqualTo("عمر خالد");
        }

        @Test
        @DisplayName("✅ Patient Builder يضبط القيم بشكل صحيح")
        void shouldBuildPatientCorrectly() {
            Patient patient = Patient.builder()
                    .firstName("هدى")
                    .lastName("سليمان")
                    .email("huda@email.com")
                    .dateOfBirth(LocalDate.of(1988, 4, 10))
                    .gender(Patient.Gender.FEMALE)
                    .bloodType("B-")
                    .status(Patient.PatientStatus.ACTIVE)
                    .build();

            assertThat(patient.getFullName()).isEqualTo("هدى سليمان");
            assertThat(patient.getGender()).isEqualTo(Patient.Gender.FEMALE);
            assertThat(patient.getBloodType()).isEqualTo("B-");
        }

        @Test
        @DisplayName("✅ جميع حالات المريض متاحة")
        void shouldHaveAllPatientStatuses() {
            assertThat(Patient.PatientStatus.values())
                    .containsExactlyInAnyOrder(
                            Patient.PatientStatus.ACTIVE,
                            Patient.PatientStatus.DISCHARGED,
                            Patient.PatientStatus.DECEASED);
        }

        @Test
        @DisplayName("✅ جنسين فقط MALE و FEMALE")
        void shouldHaveTwoGenders() {
            assertThat(Patient.Gender.values())
                    .containsExactlyInAnyOrder(Patient.Gender.MALE, Patient.Gender.FEMALE);
        }
    }

    // ── Appointment ───────────────────────────────────────────────
    @Nested
    @DisplayName("Appointment Entity")
    class AppointmentEntityTest {

        @Test
        @DisplayName("✅ جميع حالات الموعد متاحة")
        void shouldHaveAllAppointmentStatuses() {
            assertThat(Appointment.AppointmentStatus.values())
                    .containsExactlyInAnyOrder(
                            Appointment.AppointmentStatus.SCHEDULED,
                            Appointment.AppointmentStatus.CONFIRMED,
                            Appointment.AppointmentStatus.IN_PROGRESS,
                            Appointment.AppointmentStatus.COMPLETED,
                            Appointment.AppointmentStatus.CANCELLED,
                            Appointment.AppointmentStatus.NO_SHOW);
        }

        @Test
        @DisplayName("✅ جميع أنواع الموعد متاحة")
        void shouldHaveAllAppointmentTypes() {
            assertThat(Appointment.AppointmentType.values())
                    .containsExactlyInAnyOrder(
                            Appointment.AppointmentType.REGULAR,
                            Appointment.AppointmentType.EMERGENCY,
                            Appointment.AppointmentType.FOLLOW_UP,
                            Appointment.AppointmentType.CONSULTATION);
        }

        @Test
        @DisplayName("✅ Appointment Builder يضبط القيم بشكل صحيح")
        void shouldBuildAppointmentCorrectly() {
            Department dept = TestDataBuilder.buildDepartment();
            Doctor doctor = TestDataBuilder.buildDoctor(dept);
            Patient patient = TestDataBuilder.buildPatient();
            Appointment appt = TestDataBuilder.buildAppointment(patient, doctor);

            assertThat(appt.getStatus()).isEqualTo(Appointment.AppointmentStatus.SCHEDULED);
            assertThat(appt.getType()).isEqualTo(Appointment.AppointmentType.REGULAR);
            assertThat(appt.getDurationMinutes()).isEqualTo(30);
            assertThat(appt.getPatient()).isEqualTo(patient);
            assertThat(appt.getDoctor()).isEqualTo(doctor);
        }
    }

    // ── Department ────────────────────────────────────────────────
    @Nested
    @DisplayName("Department Entity")
    class DepartmentEntityTest {

        @Test
        @DisplayName("✅ Department Builder يضبط القيم بشكل صحيح")
        void shouldBuildDepartmentCorrectly() {
            Department dept = TestDataBuilder.buildDepartment();

            assertThat(dept.getName()).isEqualTo("قسم القلب");
            assertThat(dept.getStatus()).isEqualTo(Department.DepartmentStatus.ACTIVE);
        }

        @Test
        @DisplayName("✅ حالتان فقط ACTIVE و INACTIVE")
        void shouldHaveTwoDepartmentStatuses() {
            assertThat(Department.DepartmentStatus.values())
                    .containsExactlyInAnyOrder(
                            Department.DepartmentStatus.ACTIVE,
                            Department.DepartmentStatus.INACTIVE);
        }
    }

    // ── Prescription ──────────────────────────────────────────────
    @Nested
    @DisplayName("Prescription Entity")
    class PrescriptionEntityTest {

        @Test
        @DisplayName("✅ جميع حالات الوصفة متاحة")
        void shouldHaveAllPrescriptionStatuses() {
            assertThat(Prescription.PrescriptionStatus.values())
                    .containsExactlyInAnyOrder(
                            Prescription.PrescriptionStatus.ACTIVE,
                            Prescription.PrescriptionStatus.COMPLETED,
                            Prescription.PrescriptionStatus.CANCELLED,
                            Prescription.PrescriptionStatus.EXPIRED);
        }

        @Test
        @DisplayName("✅ Prescription Builder يضبط القيم بشكل صحيح")
        void shouldBuildPrescriptionCorrectly() {
            Department dept = TestDataBuilder.buildDepartment();
            Doctor doctor = TestDataBuilder.buildDoctor(dept);
            Patient patient = TestDataBuilder.buildPatient();
            MedicalRecord record = TestDataBuilder.buildMedicalRecord(patient, doctor);
            Prescription prescription = TestDataBuilder.buildPrescription(record);

            assertThat(prescription.getMedicationName()).isEqualTo("أملوديبين");
            assertThat(prescription.getDosage()).isEqualTo("5mg");
            assertThat(prescription.getDurationDays()).isEqualTo(30);
            assertThat(prescription.getStatus()).isEqualTo(Prescription.PrescriptionStatus.ACTIVE);
            assertThat(prescription.getRefillAllowed()).isTrue();
            assertThat(prescription.getEndDate())
                    .isEqualTo(prescription.getStartDate().plusDays(30));
        }
    }

    // ── User ──────────────────────────────────────────────────────
    @Nested
    @DisplayName("User Entity")
    class UserEntityTest {

        @Test
        @DisplayName("✅ جميع أدوار النظام متاحة")
        void shouldHaveAllRoles() {
            assertThat(User.Role.values())
                    .containsExactlyInAnyOrder(
                            User.Role.ROLE_ADMIN,
                            User.Role.ROLE_DOCTOR,
                            User.Role.ROLE_NURSE,
                            User.Role.ROLE_RECEPTIONIST,
                            User.Role.ROLE_PATIENT);
        }

        @Test
        @DisplayName("✅ User Builder يضبط القيم بشكل صحيح")
        void shouldBuildUserCorrectly() {
            User admin = TestDataBuilder.buildAdminUser();
            assertThat(admin.getRoles()).contains(User.Role.ROLE_ADMIN);
            assertThat(admin.getEnabled()).isTrue();
        }
    }
}
