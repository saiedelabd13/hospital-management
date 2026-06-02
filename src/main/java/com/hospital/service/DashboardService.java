package com.hospital.service;

import com.hospital.entity.Appointment;
import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import com.hospital.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final DepartmentRepository departmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();

        // Patients
        stats.setTotalPatients(patientRepository.count());
        stats.setActivePatients(patientRepository.countActivePatients());

        // Doctors
        stats.setTotalDoctors(doctorRepository.count());
        stats.setActiveDoctors((long) doctorRepository.findByStatus(Doctor.DoctorStatus.ACTIVE).size());

        // Departments
        stats.setTotalDepartments(departmentRepository.count());

        // Appointments today
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        stats.setTodayAppointments((long) appointmentRepository.findByDateRange(startOfDay, endOfDay).size());

        // Appointments by status
        Map<String, Long> appointmentsByStatus = new HashMap<>();
        for (Appointment.AppointmentStatus status : Appointment.AppointmentStatus.values()) {
            appointmentsByStatus.put(status.name(), (long) appointmentRepository.findByStatus(status).size());
        }
        stats.setAppointmentsByStatus(appointmentsByStatus);

        // Total records
        stats.setTotalMedicalRecords(medicalRecordRepository.count());

        // This month appointments
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        stats.setThisMonthAppointments((long) appointmentRepository.findByDateRange(startOfMonth, endOfMonth).size());

        return stats;
    }

    @Data
    public static class DashboardStats {
        private long totalPatients;
        private long activePatients;
        private long totalDoctors;
        private long activeDoctors;
        private long totalDepartments;
        private long todayAppointments;
        private long thisMonthAppointments;
        private long totalMedicalRecords;
        private Map<String, Long> appointmentsByStatus;
    }
}
