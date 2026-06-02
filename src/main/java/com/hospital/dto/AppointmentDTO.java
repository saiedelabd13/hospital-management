package com.hospital.dto;

import com.hospital.entity.Appointment;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

public class AppointmentDTO {

    @Data
    public static class Request {
        @NotNull(message = "المريض مطلوب")
        private Long patientId;

        @NotNull(message = "الطبيب مطلوب")
        private Long doctorId;

        @NotNull(message = "تاريخ ووقت الموعد مطلوب")
        @Future(message = "يجب أن يكون الموعد في المستقبل")
        private LocalDateTime appointmentDateTime;

        private String reason;
        private Appointment.AppointmentType type;
        private Integer durationMinutes;
        private String notes;
    }

    @Data
    public static class StatusUpdate {
        @NotNull(message = "الحالة مطلوبة")
        private Appointment.AppointmentStatus status;
        private String notes;
    }

    @Data
    public static class Response {
        private Long id;
        private Long patientId;
        private String patientName;
        private Long doctorId;
        private String doctorName;
        private String doctorSpecialization;
        private LocalDateTime appointmentDateTime;
        private String reason;
        private Appointment.AppointmentStatus status;
        private Appointment.AppointmentType type;
        private String notes;
        private Integer durationMinutes;
        private LocalDateTime createdAt;
    }
}
