package com.hospital.dto;

import com.hospital.entity.Prescription;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class MedicalRecordDTO {

    @Data
    public static class Request {
        @NotNull(message = "المريض مطلوب")
        private Long patientId;

        @NotNull(message = "الطبيب مطلوب")
        private Long doctorId;

        private Long appointmentId;

        @NotBlank(message = "التشخيص مطلوب")
        private String diagnosis;

        private String symptoms;
        private String treatmentPlan;
        private String doctorNotes;
        private Double weight;
        private Double height;
        private String bloodPressure;
        private Integer heartRate;
        private Double temperature;
        private List<PrescriptionDTO.Request> prescriptions;
    }

    @Data
    public static class Response {
        private Long id;
        private Long patientId;
        private String patientName;
        private Long doctorId;
        private String doctorName;
        private Long appointmentId;
        private String diagnosis;
        private String symptoms;
        private String treatmentPlan;
        private String doctorNotes;
        private Double weight;
        private Double height;
        private String bloodPressure;
        private Integer heartRate;
        private Double temperature;
        private List<PrescriptionDTO.Response> prescriptions;
        private LocalDateTime createdAt;
    }
}
