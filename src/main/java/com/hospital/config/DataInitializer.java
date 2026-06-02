package com.hospital.config;

import com.hospital.entity.*;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        log.info("🏥 بدء تهيئة بيانات المستشفى...");

        //  Users
        User admin = userRepository.save(User.builder()
                .username("admin")
                .email("admin@hospital.com")
                .password(passwordEncoder.encode("admin123"))
                .roles(Set.of(User.Role.ROLE_ADMIN))
                .enabled(true).build());

        User doctorUser1 = userRepository.save(User.builder()
                .username("dr.ahmed")
                .email("dr.ahmed@hospital.com")
                .password(passwordEncoder.encode("doctor123"))
                .roles(Set.of(User.Role.ROLE_DOCTOR))
                .enabled(true).build());

        User doctorUser2 = userRepository.save(User.builder()
                .username("dr.sara")
                .email("dr.sara@hospital.com")
                .password(passwordEncoder.encode("doctor123"))
                .roles(Set.of(User.Role.ROLE_DOCTOR))
                .enabled(true).build());

        userRepository.save(User.builder()
                .username("receptionist")
                .email("reception@hospital.com")
                .password(passwordEncoder.encode("reception123"))
                .roles(Set.of(User.Role.ROLE_RECEPTIONIST))
                .enabled(true).build());

        //  Departments
        Department cardiology = departmentRepository.save(Department.builder()
                .name("قسم القلب")
                .description("تشخيص وعلاج أمراض القلب والأوعية الدموية")
                .location("الطابق الثاني - جناح A")
                .phoneNumber("02-1234-001")
                .status(Department.DepartmentStatus.ACTIVE).build());

        Department neurology = departmentRepository.save(Department.builder()
                .name("قسم المخ والأعصاب")
                .description("تشخيص وعلاج أمراض الجهاز العصبي")
                .location("الطابق الثالث - جناح B")
                .phoneNumber("02-1234-002")
                .status(Department.DepartmentStatus.ACTIVE).build());

        Department orthopedics = departmentRepository.save(Department.builder()
                .name("قسم العظام")
                .description("جراحة وعلاج أمراض العظام والمفاصل")
                .location("الطابق الأول - جناح C")
                .phoneNumber("02-1234-003")
                .status(Department.DepartmentStatus.ACTIVE).build());

        Department pediatrics = departmentRepository.save(Department.builder()
                .name("قسم الأطفال")
                .description("رعاية وعلاج الأطفال منذ الولادة حتى سن 18")
                .location("الطابق الأول - جناح D")
                .phoneNumber("02-1234-004")
                .status(Department.DepartmentStatus.ACTIVE).build());

        Department emergency = departmentRepository.save(Department.builder()
                .name("قسم الطوارئ")
                .description("الرعاية الطبية الفورية والحالات الحرجة")
                .location("الطابق الأرضي")
                .phoneNumber("02-1234-000")
                .status(Department.DepartmentStatus.ACTIVE).build());

        //  Doctors
        Doctor doc1 = doctorRepository.save(Doctor.builder()
                .firstName("أحمد")
                .lastName("محمد")
                .email("dr.ahmed@hospital.com")
                .licenseNumber("LIC-001")
                .phoneNumber("010-1111-2222")
                .specialization("أمراض القلب")
                .yearsOfExperience(15)
                .qualification("دكتوراه في طب القلب - جامعة القاهرة")
                .status(Doctor.DoctorStatus.ACTIVE)
                .department(cardiology)
                .user(doctorUser1).build());

        Doctor doc2 = doctorRepository.save(Doctor.builder()
                .firstName("سارة")
                .lastName("علي")
                .email("dr.sara@hospital.com")
                .licenseNumber("LIC-002")
                .phoneNumber("010-2222-3333")
                .specialization("أمراض الأعصاب")
                .yearsOfExperience(10)
                .qualification("ماجستير في طب الأعصاب - جامعة الإسكندرية")
                .status(Doctor.DoctorStatus.ACTIVE)
                .department(neurology)
                .user(doctorUser2).build());

        Doctor doc3 = doctorRepository.save(Doctor.builder()
                .firstName("محمد")
                .lastName("حسن")
                .email("dr.hassan@hospital.com")
                .licenseNumber("LIC-003")
                .phoneNumber("010-3333-4444")
                .specialization("جراحة العظام")
                .yearsOfExperience(20)
                .qualification("دكتوراه في جراحة العظام")
                .status(Doctor.DoctorStatus.ACTIVE)
                .department(orthopedics)
                .build());

        Doctor doc4 = doctorRepository.save(Doctor.builder()
                .firstName("فاطمة")
                .lastName("إبراهيم")
                .email("dr.fatima@hospital.com")
                .licenseNumber("LIC-004")
                .phoneNumber("010-4444-5555")
                .specialization("طب الأطفال")
                .yearsOfExperience(8)
                .qualification("بكالوريوس طب وجراحة - زمالة طب الأطفال")
                .status(Doctor.DoctorStatus.ACTIVE)
                .department(pediatrics)
                .build());

        //  Patients
        Patient p1 = patientRepository.save(Patient.builder()
                .firstName("عمر")
                .lastName("خالد")
                .email("omar.khaled@email.com")
                .dateOfBirth(LocalDate.of(1985, 3, 15))
                .gender(Patient.Gender.MALE)
                .phoneNumber("010-5555-6666")
                .address("القاهرة، مصر الجديدة")
                .nationalId("28503151234567")
                .bloodType("A+")
                .emergencyContactName("خالد أحمد")
                .emergencyContactPhone("010-6666-7777")
                .chronicDiseases("ضغط الدم")
                .status(Patient.PatientStatus.ACTIVE).build());

        Patient p2 = patientRepository.save(Patient.builder()
                .firstName("نور")
                .lastName("أحمد")
                .email("nour.ahmed@email.com")
                .dateOfBirth(LocalDate.of(1992, 7, 22))
                .gender(Patient.Gender.FEMALE)
                .phoneNumber("010-7777-8888")
                .address("الإسكندرية، سموحة")
                .nationalId("29207221234567")
                .bloodType("O-")
                .emergencyContactName("أحمد محمود")
                .emergencyContactPhone("010-8888-9999")
                .allergies("بنسلين")
                .status(Patient.PatientStatus.ACTIVE).build());

        Patient p3 = patientRepository.save(Patient.builder()
                .firstName("يوسف")
                .lastName("منصور")
                .email("yousef.mansour@email.com")
                .dateOfBirth(LocalDate.of(1978, 11, 5))
                .gender(Patient.Gender.MALE)
                .phoneNumber("010-9999-0000")
                .address("الجيزة، المهندسين")
                .nationalId("27811051234567")
                .bloodType("B+")
                .chronicDiseases("سكري النوع الثاني")
                .status(Patient.PatientStatus.ACTIVE).build());

        //  Appointments
        appointmentRepository.save(Appointment.builder()
                .patient(p1)
                .doctor(doc1)
                .appointmentDateTime(LocalDateTime.now().plusHours(2))
                .reason("فحص دوري للقلب")
                .type(Appointment.AppointmentType.REGULAR)
                .status(Appointment.AppointmentStatus.CONFIRMED)
                .durationMinutes(30)
                .build());

        appointmentRepository.save(Appointment.builder()
                .patient(p2)
                .doctor(doc2)
                .appointmentDateTime(LocalDateTime.now().plusHours(4))
                .reason("صداع مستمر ودوار")
                .type(Appointment.AppointmentType.CONSULTATION)
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .durationMinutes(45)
                .build());

        appointmentRepository.save(Appointment.builder()
                .patient(p3)
                .doctor(doc3)
                .appointmentDateTime(LocalDateTime.now().plusDays(1))
                .reason("ألم في الركبة اليمنى")
                .type(Appointment.AppointmentType.FOLLOW_UP)
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .durationMinutes(30)
                .build());

        appointmentRepository.save(Appointment.builder()
                .patient(p1)
                .doctor(doc1)
                .appointmentDateTime(LocalDateTime.now().minusDays(5))
                .reason("ضيق في التنفس")
                .type(Appointment.AppointmentType.EMERGENCY)
                .status(Appointment.AppointmentStatus.COMPLETED)
                .durationMinutes(60)
                .notes("تمت المعالجة بنجاح، تحسن الحالة")
                .build());

        log.info("تم تهيئة بيانات المستشفى بنجاح!");
        log.info(" المستخدمون:");
        log.info("   admin / admin123  → مدير النظام");
        log.info("   dr.ahmed / doctor123  → طبيب");
        log.info("   dr.sara / doctor123  → طبيبة");
        log.info("   receptionist / reception123  → موظف استقبال");
        log.info(" Swagger UI: http://localhost:8080/api/swagger-ui.html");
        log.info("🗄️  H2 Console:  http://localhost:8080/api/h2-console");
    }
}
