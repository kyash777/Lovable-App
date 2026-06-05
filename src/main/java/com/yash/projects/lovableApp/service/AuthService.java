package com.yash.projects.lovableApp.service;

import com.yash.projects.lovableApp.DTO.auth.AuthResponse;
import com.yash.projects.lovableApp.DTO.auth.LoginRequest;
import com.yash.projects.lovableApp.DTO.auth.SignupRequest;

public interface AuthService {
    AuthResponse signup(SignupRequest request);

    AuthResponse login(LoginRequest request);
}