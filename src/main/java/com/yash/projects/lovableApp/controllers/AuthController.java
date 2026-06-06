package com.yash.projects.lovableApp.controllers;

import com.yash.projects.lovableApp.DTO.auth.AuthResponse;
import com.yash.projects.lovableApp.DTO.auth.LoginRequest;
import com.yash.projects.lovableApp.DTO.auth.SignupRequest;
import com.yash.projects.lovableApp.DTO.auth.UserProfileResponse;
import com.yash.projects.lovableApp.service.AuthService;
import com.yash.projects.lovableApp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile() {
        Long userId = 1L;
        return ResponseEntity.ok(userService.getProfile(userId));
    }

}
