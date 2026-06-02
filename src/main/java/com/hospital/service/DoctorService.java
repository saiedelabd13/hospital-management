package com.hospital.service;

import com.hospital.dto.DoctorDTO;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;

    public List<DoctorDTO.Response> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public DoctorDTO.Response getDoctorById(Long id) {
        return toResponse(findDoctorById(id));
    }

    public DoctorDTO.Response createDoctor(DoctorDTO.Request request) {
        if (doctorRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("البريد الإلكتروني مسجل مسبقاً: " + request.getEmail());
        }
        if (doctorRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new IllegalArgumentException("رقم الترخيص مسجل مسبقاً: " + request.getLicenseNumber());
        }

        Doctor doctor = Doctor.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .licenseNumber(request.getLicenseNumber())
                .phoneNumber(request.getPhoneNumber())
                .specialization(request.getSpecialization())
                .yearsOfExperience(request.getYearsOfExperience())
                .qualification(request.getQualification())
                .status(request.getStatus() != null ? request.getStatus() : Doctor.DoctorStatus.ACTIVE)
                .build();

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("القسم", "id", request.getDepartmentId()));
            doctor.setDepartment(dept);
        }

        return toResponse(doctorRepository.save(doctor));
    }

    public DoctorDTO.Response updateDoctor(Long id, DoctorDTO.Request request) {
        Doctor doctor = findDoctorById(id);

        if (!doctor.getEmail().equals(request.getEmail()) && doctorRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("البريد الإلكتروني مسجل مسبقاً: " + request.getEmail());
        }

        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setEmail(request.getEmail());
        doctor.setPhoneNumber(request.getPhoneNumber());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setYearsOfExperience(request.getYearsOfExperience());
        doctor.setQualification(request.getQualification());
        if (request.getStatus() != null) doctor.setStatus(request.getStatus());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("القسم", "id", request.getDepartmentId()));
            doctor.setDepartment(dept);
        }

        return toResponse(doctorRepository.save(doctor));
    }

    public void deleteDoctor(Long id) {
        Doctor doctor = findDoctorById(id);
        doctorRepository.delete(doctor);
    }

    public List<DoctorDTO.Response> getDoctorsByDepartment(Long departmentId) {
        return doctorRepository.findByDepartmentId(departmentId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<DoctorDTO.Response> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<DoctorDTO.Response> searchDoctors(String keyword) {
        return doctorRepository.searchDoctors(keyword).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<DoctorDTO.Summary> getActiveDoctors() {
        return doctorRepository.findByStatus(Doctor.DoctorStatus.ACTIVE).stream()
                .map(this::toSummary).collect(Collectors.toList());
    }

    private Doctor findDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("الطبيب", "id", id));
    }

    private DoctorDTO.Response toResponse(Doctor doctor) {
        DoctorDTO.Response res = new DoctorDTO.Response();
        res.setId(doctor.getId());
        res.setFirstName(doctor.getFirstName());
        res.setLastName(doctor.getLastName());
        res.setFullName(doctor.getFullName());
        res.setEmail(doctor.getEmail());
        res.setLicenseNumber(doctor.getLicenseNumber());
        res.setPhoneNumber(doctor.getPhoneNumber());
        res.setSpecialization(doctor.getSpecialization());
        res.setYearsOfExperience(doctor.getYearsOfExperience());
        res.setQualification(doctor.getQualification());
        res.setStatus(doctor.getStatus());
        res.setCreatedAt(doctor.getCreatedAt());
        if (doctor.getDepartment() != null) {
            res.setDepartmentId(doctor.getDepartment().getId());
            res.setDepartmentName(doctor.getDepartment().getName());
        }
        return res;
    }

    private DoctorDTO.Summary toSummary(Doctor doctor) {
        DoctorDTO.Summary s = new DoctorDTO.Summary();
        s.setId(doctor.getId());
        s.setFullName(doctor.getFullName());
        s.setSpecialization(doctor.getSpecialization());
        s.setStatus(doctor.getStatus());
        if (doctor.getDepartment() != null) s.setDepartmentName(doctor.getDepartment().getName());
        return s;
    }
}
