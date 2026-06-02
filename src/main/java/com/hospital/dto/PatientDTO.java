package com.hospital.dto;

import com.hospital.entity.Patient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PatientDTO {

    @Data
    public static class Request {
        @NotBlank(message = "الاسم الأول مطلوب")
        private String firstName;

        @NotBlank(message = "الاسم الأخير مطلوب")
        private String lastName;

        @Email(message = "البريد الإلكتروني غير صحيح")
        @NotBlank(message = "البريد الإلكتروني مطلوب")
        private String email;

        @NotNull(message = "تاريخ الميلاد مطلوب")
        private LocalDate dateOfBirth;

        @NotNull(message = "الجنس مطلوب")
        private Patient.Gender gender;

        private String phoneNumber;
        private String address;
        private String nationalId;
        private String bloodType;
        private String emergencyContactName;
        private String emergencyContactPhone;
        private String allergies;
        private String chronicDiseases;
        private Patient.PatientStatus status;
    }

    @Data
    public static class Response {
        private Long id;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private LocalDate dateOfBirth;
        private Patient.Gender gender;
        private String phoneNumber;
        private String address;
        private String nationalId;
        private String bloodType;
        private String emergencyContactName;
        private String emergencyContactPhone;
        private String allergies;
        private String chronicDiseases;
        private Patient.PatientStatus status;
        private LocalDateTime createdAt;
    }

    @Data
    public static class Summary {
        private Long id;
        private String fullName;
        private String email;
        private String phoneNumber;
        private Patient.Gender gender;
        private String bloodType;
        private Patient.PatientStatus status;
    }
}
