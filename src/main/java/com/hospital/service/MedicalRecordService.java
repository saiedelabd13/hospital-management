package com.hospital.service;

import com.hospital.dto.MedicalRecordDTO;
import com.hospital.dto.PrescriptionDTO;
import com.hospital.entity.*;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public List<MedicalRecordDTO.Response> getAllRecords() {
        return medicalRecordRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public MedicalRecordDTO.Response getRecordById(Long id) {
        return toResponse(findById(id));
    }

    public List<MedicalRecordDTO.Response> getRecordsByPatient(Long patientId) {
        return medicalRecordRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<MedicalRecordDTO.Response> getRecordsByDoctor(Long doctorId) {
        return medicalRecordRepository.findByDoctorId(doctorId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public MedicalRecordDTO.Response createRecord(MedicalRecordDTO.Request request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("المريض", "id", request.getPatientId()));
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("الطبيب", "id", request.getDoctorId()));

        MedicalRecord record = MedicalRecord.builder()
                .patient(patient)
                .doctor(doctor)
                .diagnosis(request.getDiagnosis())
                .symptoms(request.getSymptoms())
                .treatmentPlan(request.getTreatmentPlan())
                .doctorNotes(request.getDoctorNotes())
                .weight(request.getWeight())
                .height(request.getHeight())
                .bloodPressure(request.getBloodPressure())
                .heartRate(request.getHeartRate())
                .temperature(request.getTemperature())
                .build();

        if (request.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("الموعد", "id", request.getAppointmentId()));
            record.setAppointment(appointment);
            // Update appointment status to COMPLETED
            appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
            appointmentRepository.save(appointment);
        }

        MedicalRecord savedRecord = medicalRecordRepository.save(record);

        // Add prescriptions
        if (request.getPrescriptions() != null && !request.getPrescriptions().isEmpty()) {
            List<Prescription> prescriptions = request.getPrescriptions().stream()
                    .map(p -> buildPrescription(p, savedRecord))
                    .collect(Collectors.toList());
            savedRecord.setPrescriptions(prescriptions);
            medicalRecordRepository.save(savedRecord);
        }

        return toResponse(savedRecord);
    }

    public MedicalRecordDTO.Response updateRecord(Long id, MedicalRecordDTO.Request request) {
        MedicalRecord record = findById(id);
        record.setDiagnosis(request.getDiagnosis());
        record.setSymptoms(request.getSymptoms());
        record.setTreatmentPlan(request.getTreatmentPlan());
        record.setDoctorNotes(request.getDoctorNotes());
        record.setWeight(request.getWeight());
        record.setHeight(request.getHeight());
        record.setBloodPressure(request.getBloodPressure());
        record.setHeartRate(request.getHeartRate());
        record.setTemperature(request.getTemperature());
        return toResponse(medicalRecordRepository.save(record));
    }

    public void deleteRecord(Long id) {
        medicalRecordRepository.delete(findById(id));
    }

    private MedicalRecord findById(Long id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("السجل الطبي", "id", id));
    }

    private Prescription buildPrescription(PrescriptionDTO.Request req, MedicalRecord record) {
        LocalDate start = req.getStartDate() != null ? req.getStartDate() : LocalDate.now();
        return Prescription.builder()
                .medicalRecord(record)
                .medicationName(req.getMedicationName())
                .dosage(req.getDosage())
                .frequency(req.getFrequency())
                .durationDays(req.getDurationDays())
                .startDate(start)
                .endDate(start.plusDays(req.getDurationDays()))
                .instructions(req.getInstructions())
                .refillAllowed(req.getRefillAllowed() != null ? req.getRefillAllowed() : false)
                .status(Prescription.PrescriptionStatus.ACTIVE)
                .build();
    }

    private MedicalRecordDTO.Response toResponse(MedicalRecord r) {
        MedicalRecordDTO.Response res = new MedicalRecordDTO.Response();
        res.setId(r.getId());
        res.setPatientId(r.getPatient().getId());
        res.setPatientName(r.getPatient().getFullName());
        res.setDoctorId(r.getDoctor().getId());
        res.setDoctorName(r.getDoctor().getFullName());
        if (r.getAppointment() != null) res.setAppointmentId(r.getAppointment().getId());
        res.setDiagnosis(r.getDiagnosis());
        res.setSymptoms(r.getSymptoms());
        res.setTreatmentPlan(r.getTreatmentPlan());
        res.setDoctorNotes(r.getDoctorNotes());
        res.setWeight(r.getWeight());
        res.setHeight(r.getHeight());
        res.setBloodPressure(r.getBloodPressure());
        res.setHeartRate(r.getHeartRate());
        res.setTemperature(r.getTemperature());
        res.setCreatedAt(r.getCreatedAt());
        if (r.getPrescriptions() != null) {
            res.setPrescriptions(r.getPrescriptions().stream().map(this::toPrescriptionResponse).collect(Collectors.toList()));
        }
        return res;
    }

    private PrescriptionDTO.Response toPrescriptionResponse(Prescription p) {
        PrescriptionDTO.Response res = new PrescriptionDTO.Response();
        res.setId(p.getId());
        res.setMedicationName(p.getMedicationName());
        res.setDosage(p.getDosage());
        res.setFrequency(p.getFrequency());
        res.setDurationDays(p.getDurationDays());
        res.setStartDate(p.getStartDate());
        res.setEndDate(p.getEndDate());
        res.setInstructions(p.getInstructions());
        res.setRefillAllowed(p.getRefillAllowed());
        res.setStatus(p.getStatus());
        res.setCreatedAt(p.getCreatedAt());
        return res;
    }
}
