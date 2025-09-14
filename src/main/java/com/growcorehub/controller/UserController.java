package com.growcorehub.controller;

import com.growcorehub.dto.request.ProfileUpdateRequest;
import com.growcorehub.dto.response.UserResponse;
import com.growcorehub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
        UserResponse response = userService.getUserProfile(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestBody ProfileUpdateRequest request,
            Authentication authentication) {
        UserResponse response = userService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }
}