package com.hospital.dto;

import com.hospital.entity.Prescription;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PrescriptionDTO {

    @Data
    public static class Request {
        @NotBlank(message = "اسم الدواء مطلوب")
        private String medicationName;

        @NotBlank(message = "الجرعة مطلوبة")
        private String dosage;

        @NotBlank(message = "التكرار مطلوب")
        private String frequency;

        @NotNull(message = "مدة العلاج مطلوبة")
        private Integer durationDays;

        private LocalDate startDate;
        private String instructions;
        private Boolean refillAllowed;
    }

    @Data
    public static class Response {
        private Long id;
        private String medicationName;
        private String dosage;
        private String frequency;
        private Integer durationDays;
        private LocalDate startDate;
        private LocalDate endDate;
        private String instructions;
        private Boolean refillAllowed;
        private Prescription.PrescriptionStatus status;
        private LocalDateTime createdAt;
    }
}
