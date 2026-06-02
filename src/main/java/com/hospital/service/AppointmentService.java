package com.hospital.service;

import com.hospital.dto.AppointmentDTO;
import com.hospital.entity.Appointment;
import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public List<AppointmentDTO.Response> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public AppointmentDTO.Response getAppointmentById(Long id) {
        return toResponse(findById(id));
    }

    public AppointmentDTO.Response createAppointment(AppointmentDTO.Request request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("المريض", "id", request.getPatientId()));
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("الطبيب", "id", request.getDoctorId()));

        if (doctor.getStatus() != Doctor.DoctorStatus.ACTIVE) {
            throw new IllegalArgumentException("الطبيب غير متاح حالياً");
        }

        // Check for scheduling conflicts
        LocalDateTime start = request.getAppointmentDateTime();
        LocalDateTime end = start.plusMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 30);
        Long conflicts = appointmentRepository.countDoctorAppointmentsInRange(doctor.getId(), start, end);
        if (conflicts > 0) {
            throw new IllegalArgumentException("الطبيب لديه موعد آخر في هذا الوقت");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDateTime(request.getAppointmentDateTime())
                .reason(request.getReason())
                .type(request.getType() != null ? request.getType() : Appointment.AppointmentType.REGULAR)
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .durationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 30)
                .notes(request.getNotes())
                .build();

        return toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentDTO.Response updateAppointmentStatus(Long id, AppointmentDTO.StatusUpdate statusUpdate) {
        Appointment appointment = findById(id);
        appointment.setStatus(statusUpdate.getStatus());
        if (statusUpdate.getNotes() != null) {
            appointment.setNotes(statusUpdate.getNotes());
        }
        return toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentDTO.Response updateAppointment(Long id, AppointmentDTO.Request request) {
        Appointment appointment = findById(id);
        appointment.setAppointmentDateTime(request.getAppointmentDateTime());
        appointment.setReason(request.getReason());
        if (request.getType() != null) appointment.setType(request.getType());
        if (request.getDurationMinutes() != null) appointment.setDurationMinutes(request.getDurationMinutes());
        appointment.setNotes(request.getNotes());
        return toResponse(appointmentRepository.save(appointment));
    }

    public void cancelAppointment(Long id) {
        Appointment appointment = findById(id);
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    public List<AppointmentDTO.Response> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<AppointmentDTO.Response> getAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<AppointmentDTO.Response> getTodayAppointments() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        return appointmentRepository.findByDateRange(startOfDay, endOfDay).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<AppointmentDTO.Response> getAppointmentsByDateRange(LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findByDateRange(start, end).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<AppointmentDTO.Response> getAppointmentsByStatus(Appointment.AppointmentStatus status) {
        return appointmentRepository.findByStatus(status).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    private Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("الموعد", "id", id));
    }

    private AppointmentDTO.Response toResponse(Appointment a) {
        AppointmentDTO.Response res = new AppointmentDTO.Response();
        res.setId(a.getId());
        res.setPatientId(a.getPatient().getId());
        res.setPatientName(a.getPatient().getFullName());
        res.setDoctorId(a.getDoctor().getId());
        res.setDoctorName(a.getDoctor().getFullName());
        res.setDoctorSpecialization(a.getDoctor().getSpecialization());
        res.setAppointmentDateTime(a.getAppointmentDateTime());
        res.setReason(a.getReason());
        res.setStatus(a.getStatus());
        res.setType(a.getType());
        res.setNotes(a.getNotes());
        res.setDurationMinutes(a.getDurationMinutes());
        res.setCreatedAt(a.getCreatedAt());
        return res;
    }
}
