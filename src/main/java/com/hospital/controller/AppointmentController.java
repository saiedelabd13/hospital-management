package com.hospital.controller;

import com.hospital.dto.AppointmentDTO;
import com.hospital.entity.Appointment;
import com.hospital.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@Tag(name = "📅 المواعيد", description = "إدارة المواعيد الطبية")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    @Operation(summary = "جلب جميع المواعيد")
    public ResponseEntity<List<AppointmentDTO.Response>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/today")
    @Operation(summary = "مواعيد اليوم")
    public ResponseEntity<List<AppointmentDTO.Response>> getTodayAppointments() {
        return ResponseEntity.ok(appointmentService.getTodayAppointments());
    }

    @GetMapping("/{id}")
    @Operation(summary = "جلب موعد بالمعرف")
    public ResponseEntity<AppointmentDTO.Response> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "مواعيد مريض معين")
    public ResponseEntity<List<AppointmentDTO.Response>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "مواعيد طبيب معين")
    public ResponseEntity<List<AppointmentDTO.Response>> getByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(doctorId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "المواعيد حسب الحالة")
    public ResponseEntity<List<AppointmentDTO.Response>> getByStatus(@PathVariable Appointment.AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByStatus(status));
    }

    @GetMapping("/range")
    @Operation(summary = "المواعيد في نطاق زمني")
    public ResponseEntity<List<AppointmentDTO.Response>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDateRange(start, end));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    @Operation(summary = "حجز موعد جديد")
    public ResponseEntity<AppointmentDTO.Response> createAppointment(@Valid @RequestBody AppointmentDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.createAppointment(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    @Operation(summary = "تعديل موعد")
    public ResponseEntity<AppointmentDTO.Response> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentDTO.Request request) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    @Operation(summary = "تحديث حالة الموعد")
    public ResponseEntity<AppointmentDTO.Response> updateStatus(
            @PathVariable Long id,
            @RequestBody AppointmentDTO.StatusUpdate statusUpdate) {
        return ResponseEntity.ok(appointmentService.updateAppointmentStatus(id, statusUpdate));
    }

    @DeleteMapping("/{id}/cancel")
    @Operation(summary = "إلغاء موعد")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.noContent().build();
    }
}
