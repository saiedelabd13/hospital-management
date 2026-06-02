package com.hospital.controller;

import com.hospital.dto.DoctorDTO;
import com.hospital.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
@Tag(name = "👨‍⚕️ الأطباء", description = "إدارة الأطباء")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    @Operation(summary = "جلب جميع الأطباء")
    public ResponseEntity<List<DoctorDTO.Response>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/active")
    @Operation(summary = "جلب الأطباء النشطين")
    public ResponseEntity<List<DoctorDTO.Summary>> getActiveDoctors() {
        return ResponseEntity.ok(doctorService.getActiveDoctors());
    }

    @GetMapping("/{id}")
    @Operation(summary = "جلب طبيب بالمعرف")
    public ResponseEntity<DoctorDTO.Response> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "جلب أطباء قسم معين")
    public ResponseEntity<List<DoctorDTO.Response>> getDoctorsByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(doctorService.getDoctorsByDepartment(departmentId));
    }

    @GetMapping("/specialization/{specialization}")
    @Operation(summary = "جلب أطباء بالتخصص")
    public ResponseEntity<List<DoctorDTO.Response>> getDoctorsBySpecialization(@PathVariable String specialization) {
        return ResponseEntity.ok(doctorService.getDoctorsBySpecialization(specialization));
    }

    @GetMapping("/search")
    @Operation(summary = "البحث عن طبيب")
    public ResponseEntity<List<DoctorDTO.Response>> searchDoctors(@RequestParam String keyword) {
        return ResponseEntity.ok(doctorService.searchDoctors(keyword));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "إضافة طبيب جديد")
    public ResponseEntity<DoctorDTO.Response> createDoctor(@Valid @RequestBody DoctorDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.createDoctor(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "تعديل بيانات طبيب")
    public ResponseEntity<DoctorDTO.Response> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorDTO.Request request) {
        return ResponseEntity.ok(doctorService.updateDoctor(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "حذف طبيب")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }
}
