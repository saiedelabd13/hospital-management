package com.hospital.dto;

import com.hospital.entity.Department;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// =====================
// Department DTOs
// =====================
public class DepartmentDTO {

    @Data
    public static class Request {
        @NotBlank(message = "اسم القسم مطلوب")
        private String name;
        private String description;
        private String location;
        private String phoneNumber;
        @NotNull(message = "حالة القسم مطلوبة")
        private Department.DepartmentStatus status;
    }

    @Data
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private String location;
        private String phoneNumber;
        private Department.DepartmentStatus status;
        private int doctorCount;
        private LocalDateTime createdAt;
    }
}
