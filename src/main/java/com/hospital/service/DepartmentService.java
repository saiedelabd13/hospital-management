package com.hospital.service;

import com.hospital.dto.DepartmentDTO;
import com.hospital.entity.Department;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public List<DepartmentDTO.Response> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public DepartmentDTO.Response getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("القسم", "id", id));
        return toResponse(dept);
    }

    public DepartmentDTO.Response createDepartment(DepartmentDTO.Request request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("يوجد قسم بنفس الاسم مسبقاً: " + request.getName());
        }
        Department dept = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .location(request.getLocation())
                .phoneNumber(request.getPhoneNumber())
                .status(request.getStatus() != null ? request.getStatus() : Department.DepartmentStatus.ACTIVE)
                .build();
        return toResponse(departmentRepository.save(dept));
    }

    public DepartmentDTO.Response updateDepartment(Long id, DepartmentDTO.Request request) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("القسم", "id", id));

        if (!dept.getName().equals(request.getName()) && departmentRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("يوجد قسم بنفس الاسم مسبقاً: " + request.getName());
        }

        dept.setName(request.getName());
        dept.setDescription(request.getDescription());
        dept.setLocation(request.getLocation());
        dept.setPhoneNumber(request.getPhoneNumber());
        if (request.getStatus() != null) dept.setStatus(request.getStatus());

        return toResponse(departmentRepository.save(dept));
    }

    public void deleteDepartment(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("القسم", "id", id));
        if (dept.getDoctors() != null && !dept.getDoctors().isEmpty()) {
            throw new IllegalArgumentException("لا يمكن حذف القسم لأنه يحتوي على أطباء مرتبطين به");
        }
        departmentRepository.delete(dept);
    }

    public List<DepartmentDTO.Response> getActiveDepartments() {
        return departmentRepository.findByStatus(Department.DepartmentStatus.ACTIVE).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private DepartmentDTO.Response toResponse(Department dept) {
        DepartmentDTO.Response response = new DepartmentDTO.Response();
        response.setId(dept.getId());
        response.setName(dept.getName());
        response.setDescription(dept.getDescription());
        response.setLocation(dept.getLocation());
        response.setPhoneNumber(dept.getPhoneNumber());
        response.setStatus(dept.getStatus());
        response.setDoctorCount(dept.getDoctors() != null ? dept.getDoctors().size() : 0);
        response.setCreatedAt(dept.getCreatedAt());
        return response;
    }
}
