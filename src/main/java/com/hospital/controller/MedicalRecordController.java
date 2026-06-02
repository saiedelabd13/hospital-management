package com.hospital.controller;

import com.hospital.dto.MedicalRecordDTO;
import com.hospital.service.MedicalRecordService;
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
@RequestMapping("/medical-records")
@RequiredArgsConstructor
@Tag(name = "📋 السجلات الطبية", description = "إدارة السجلات الطبية والوصفات")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @Operation(summary = "جلب جميع السجلات الطبية")
    public ResponseEntity<List<MedicalRecordDTO.Response>> getAllRecords() {
        return ResponseEntity.ok(medicalRecordService.getAllRecords());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @Operation(summary = "جلب سجل طبي بالمعرف")
    public ResponseEntity<MedicalRecordDTO.Response> getRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(medicalRecordService.getRecordById(id));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @Operation(summary = "جلب السجلات الطبية لمريض")
    public ResponseEntity<List<MedicalRecordDTO.Response>> getRecordsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(medicalRecordService.getRecordsByPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "جلب السجلات الطبية لطبيب")
    public ResponseEntity<List<MedicalRecordDTO.Response>> getRecordsByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(medicalRecordService.getRecordsByDoctor(doctorId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "إنشاء سجل طبي جديد")
    public ResponseEntity<MedicalRecordDTO.Response> createRecord(@Valid @RequestBody MedicalRecordDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicalRecordService.createRecord(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "تعديل سجل طبي")
    public ResponseEntity<MedicalRecordDTO.Response> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody MedicalRecordDTO.Request request) {
        return ResponseEntity.ok(medicalRecordService.updateRecord(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "حذف سجل طبي")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        medicalRecordService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }
}
