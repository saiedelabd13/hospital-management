package com.hospital.controller;

import com.hospital.dto.AuthDTO;
import com.hospital.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "🔐 المصادقة", description = "تسجيل الدخول والتسجيل")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "تسجيل الدخول", description = "تسجيل الدخول والحصول على JWT Token")
    public ResponseEntity<AuthDTO.JwtResponse> login(@Valid @RequestBody AuthDTO.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "تسجيل مستخدم جديد")
    public ResponseEntity<AuthDTO.MessageResponse> register(@Valid @RequestBody AuthDTO.RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
}
