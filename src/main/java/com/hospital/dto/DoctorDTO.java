package com.hospital.dto;

import com.hospital.entity.Doctor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

public class DoctorDTO {

    @Data
    public static class Request {
        @NotBlank(message = "الاسم الأول مطلوب")
        private String firstName;

        @NotBlank(message = "الاسم الأخير مطلوب")
        private String lastName;

        @Email(message = "البريد الإلكتروني غير صحيح")
        @NotBlank(message = "البريد الإلكتروني مطلوب")
        private String email;

        @NotBlank(message = "رقم الترخيص مطلوب")
        private String licenseNumber;

        private String phoneNumber;

        @NotBlank(message = "التخصص مطلوب")
        private String specialization;

        private Integer yearsOfExperience;
        private String qualification;

        @NotNull(message = "الحالة مطلوبة")
        private Doctor.DoctorStatus status;

        private Long departmentId;
    }

    @Data
    public static class Response {
        private Long id;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String licenseNumber;
        private String phoneNumber;
        private String specialization;
        private Integer yearsOfExperience;
        private String qualification;
        private Doctor.DoctorStatus status;
        private String departmentName;
        private Long departmentId;
        private LocalDateTime createdAt;
    }

    @Data
    public static class Summary {
        private Long id;
        private String fullName;
        private String specialization;
        private String departmentName;
        private Doctor.DoctorStatus status;
    }
}
