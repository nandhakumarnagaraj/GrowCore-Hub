package com.growcorehub.controller;

import com.growcorehub.dto.request.LoginRequest;
import com.growcorehub.dto.request.RegisterRequest;
import com.growcorehub.dto.response.AuthResponse;
import com.growcorehub.service.AuthService;
import com.growcorehub.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        
        // Send welcome email
        emailService.sendWelcomeEmail(request.getEmail(), request.getFirstName());
        
        return ResponseEntity.ok(response);
    }
}