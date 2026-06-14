package com.hospital.util;

import com.hospital.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Factory class for building test entities consistently across all test classes
 */
public class TestDataBuilder {

    // ── Department ──────────────────────────────────────────────
    public static Department buildDepartment() {
        return Department.builder()
                .name("قسم القلب")
                .description("تشخيص وعلاج أمراض القلب")
                .location("الطابق الثاني")
                .phoneNumber("02-1000-0001")
                .status(Department.DepartmentStatus.ACTIVE)
                .build();
    }

    public static Department buildDepartment(String name) {
        Department d = buildDepartment();
        d.setName(name);
        return d;
    }

    public static Department buildInactiveDepartment() {
        Department d = buildDepartment("قسم مغلق");
        d.setStatus(Department.DepartmentStatus.INACTIVE);
        return d;
    }

    // ── Doctor ───────────────────────────────────────────────────
    public static Doctor buildDoctor(Department department) {
        return Doctor.builder()
                .firstName("أحمد")
                .lastName("محمد")
                .email("dr.ahmed@hospital.com")
                .licenseNumber("LIC-001")
                .phoneNumber("010-1111-2222")
                .specialization("أمراض القلب")
                .yearsOfExperience(10)
                .qualification("دكتوراه في طب القلب")
                .status(Doctor.DoctorStatus.ACTIVE)
                .department(department)
                .build();
    }

    public static Doctor buildDoctor(String email, String license, Department department) {
        Doctor d = buildDoctor(department);
        d.setEmail(email);
        d.setLicenseNumber(license);
        return d;
    }

    public static Doctor buildInactiveDoctor(Department department) {
        Doctor d = buildDoctor("inactive@hospital.com", "LIC-OFF", department);
        d.setStatus(Doctor.DoctorStatus.ON_LEAVE);
        return d;
    }

    // ── Patient ──────────────────────────────────────────────────
    public static Patient buildPatient() {
        return Patient.builder()
                .firstName("عمر")
                .lastName("خالد")
                .email("omar@email.com")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Patient.Gender.MALE)
                .phoneNumber("010-5555-6666")
                .address("القاهرة")
                .nationalId("29005151234567")
                .bloodType("A+")
                .emergencyContactName("خالد عمر")
                .emergencyContactPhone("010-6666-7777")
                .status(Patient.PatientStatus.ACTIVE)
                .build();
    }

    public static Patient buildPatient(String email, String nationalId) {
        Patient p = buildPatient();
        p.setEmail(email);
        p.setNationalId(nationalId);
        return p;
    }

    public static Patient buildFemalePatient() {
        Patient p = buildPatient("nour@email.com", "29205151234568");
        p.setFirstName("نور");
        p.setLastName("أحمد");
        p.setGender(Patient.Gender.FEMALE);
        return p;
    }

    // ── Appointment ──────────────────────────────────────────────
    public static Appointment buildAppointment(Patient patient, Doctor doctor) {
        return Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDateTime(LocalDateTime.now().plusDays(1))
                .reason("فحص دوري")
                .type(Appointment.AppointmentType.REGULAR)
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .durationMinutes(30)
                .build();
    }

    public static Appointment buildCompletedAppointment(Patient patient, Doctor doctor) {
        Appointment a = buildAppointment(patient, doctor);
        a.setAppointmentDateTime(LocalDateTime.now().minusDays(1));
        a.setStatus(Appointment.AppointmentStatus.COMPLETED);
        return a;
    }

    public static Appointment buildTodayAppointment(Patient patient, Doctor doctor) {
        Appointment a = buildAppointment(patient, doctor);
        a.setAppointmentDateTime(LocalDateTime.now().plusHours(2));
        return a;
    }

    // ── MedicalRecord ────────────────────────────────────────────
    public static MedicalRecord buildMedicalRecord(Patient patient, Doctor doctor) {
        return MedicalRecord.builder()
                .patient(patient)
                .doctor(doctor)
                .diagnosis("ارتفاع ضغط الدم")
                .symptoms("صداع ودوار")
                .treatmentPlan("أدوية وتغيير نمط الحياة")
                .doctorNotes("متابعة شهرية")
                .weight(80.0)
                .height(175.0)
                .bloodPressure("140/90")
                .heartRate(85)
                .temperature(37.0)
                .build();
    }

    // ── Prescription ─────────────────────────────────────────────
    public static Prescription buildPrescription(MedicalRecord record) {
        return Prescription.builder()
                .medicalRecord(record)
                .medicationName("أملوديبين")
                .dosage("5mg")
                .frequency("مرة يومياً")
                .durationDays(30)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .instructions("مع الطعام")
                .refillAllowed(true)
                .status(Prescription.PrescriptionStatus.ACTIVE)
                .build();
    }

    // ── User ─────────────────────────────────────────────────────
    public static User buildAdminUser() {
        return User.builder()
                .username("admin")
                .email("admin@hospital.com")
                .password("$2a$10$hashedpassword")
                .roles(Set.of(User.Role.ROLE_ADMIN))
                .enabled(true)
                .build();
    }

    public static User buildDoctorUser() {
        return User.builder()
                .username("dr.test")
                .email("dr.test@hospital.com")
                .password("$2a$10$hashedpassword")
                .roles(Set.of(User.Role.ROLE_DOCTOR))
                .enabled(true)
                .build();
    }
}
