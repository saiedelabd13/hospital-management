package com.hospital.controller;

import com.hospital.dto.PatientDTO;
import com.hospital.service.PatientService;
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
@RequestMapping("/patients")
@RequiredArgsConstructor
@Tag(name = "🧑‍🤝‍🧑 المرضى", description = "إدارة المرضى")
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    @Operation(summary = "جلب جميع المرضى")
    public ResponseEntity<List<PatientDTO.Response>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping("/active")
    @Operation(summary = "جلب المرضى النشطين")
    public ResponseEntity<List<PatientDTO.Summary>> getActivePatients() {
        return ResponseEntity.ok(patientService.getActivePatients());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    @Operation(summary = "جلب مريض بالمعرف")
    public ResponseEntity<PatientDTO.Response> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    @Operation(summary = "البحث عن مريض")
    public ResponseEntity<List<PatientDTO.Summary>> searchPatients(@RequestParam String keyword) {
        return ResponseEntity.ok(patientService.searchPatients(keyword));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "تسجيل مريض جديد")
    public ResponseEntity<PatientDTO.Response> createPatient(@Valid @RequestBody PatientDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.createPatient(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "تعديل بيانات مريض")
    public ResponseEntity<PatientDTO.Response> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientDTO.Request request) {
        return ResponseEntity.ok(patientService.updatePatient(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "حذف مريض")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count/active")
    @Operation(summary = "عدد المرضى النشطين")
    public ResponseEntity<Long> countActivePatients() {
        return ResponseEntity.ok(patientService.countActivePatients());
    }
}
