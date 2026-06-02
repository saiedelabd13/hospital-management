package com.hospital.service;

import com.hospital.dto.PatientDTO;
import com.hospital.entity.Patient;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    public List<PatientDTO.Response> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public PatientDTO.Response getPatientById(Long id) {
        return toResponse(findById(id));
    }

    public PatientDTO.Response createPatient(PatientDTO.Request request) {
        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("البريد الإلكتروني مسجل مسبقاً: " + request.getEmail());
        }
        if (request.getNationalId() != null && patientRepository.existsByNationalId(request.getNationalId())) {
            throw new IllegalArgumentException("الرقم القومي مسجل مسبقاً: " + request.getNationalId());
        }

        Patient patient = Patient.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .nationalId(request.getNationalId())
                .bloodType(request.getBloodType())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .allergies(request.getAllergies())
                .chronicDiseases(request.getChronicDiseases())
                .status(request.getStatus() != null ? request.getStatus() : Patient.PatientStatus.ACTIVE)
                .build();

        return toResponse(patientRepository.save(patient));
    }

    public PatientDTO.Response updatePatient(Long id, PatientDTO.Request request) {
        Patient patient = findById(id);

        if (!patient.getEmail().equals(request.getEmail()) && patientRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("البريد الإلكتروني مسجل مسبقاً: " + request.getEmail());
        }

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setEmail(request.getEmail());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setAddress(request.getAddress());
        patient.setBloodType(request.getBloodType());
        patient.setEmergencyContactName(request.getEmergencyContactName());
        patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        patient.setAllergies(request.getAllergies());
        patient.setChronicDiseases(request.getChronicDiseases());
        if (request.getStatus() != null) patient.setStatus(request.getStatus());

        return toResponse(patientRepository.save(patient));
    }

    public void deletePatient(Long id) {
        patientRepository.delete(findById(id));
    }

    public List<PatientDTO.Summary> searchPatients(String keyword) {
        return patientRepository.searchPatients(keyword).stream()
                .map(this::toSummary).collect(Collectors.toList());
    }

    public List<PatientDTO.Summary> getActivePatients() {
        return patientRepository.findByStatus(Patient.PatientStatus.ACTIVE).stream()
                .map(this::toSummary).collect(Collectors.toList());
    }

    public Long countActivePatients() {
        return patientRepository.countActivePatients();
    }

    private Patient findById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("المريض", "id", id));
    }

    private PatientDTO.Response toResponse(Patient p) {
        PatientDTO.Response res = new PatientDTO.Response();
        res.setId(p.getId());
        res.setFirstName(p.getFirstName());
        res.setLastName(p.getLastName());
        res.setFullName(p.getFullName());
        res.setEmail(p.getEmail());
        res.setDateOfBirth(p.getDateOfBirth());
        res.setGender(p.getGender());
        res.setPhoneNumber(p.getPhoneNumber());
        res.setAddress(p.getAddress());
        res.setNationalId(p.getNationalId());
        res.setBloodType(p.getBloodType());
        res.setEmergencyContactName(p.getEmergencyContactName());
        res.setEmergencyContactPhone(p.getEmergencyContactPhone());
        res.setAllergies(p.getAllergies());
        res.setChronicDiseases(p.getChronicDiseases());
        res.setStatus(p.getStatus());
        res.setCreatedAt(p.getCreatedAt());
        return res;
    }

    private PatientDTO.Summary toSummary(Patient p) {
        PatientDTO.Summary s = new PatientDTO.Summary();
        s.setId(p.getId());
        s.setFullName(p.getFullName());
        s.setEmail(p.getEmail());
        s.setPhoneNumber(p.getPhoneNumber());
        s.setGender(p.getGender());
        s.setBloodType(p.getBloodType());
        s.setStatus(p.getStatus());
        return s;
    }
}
