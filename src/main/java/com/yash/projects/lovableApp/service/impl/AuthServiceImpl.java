package com.yash.projects.lovableApp.service.impl;

import com.yash.projects.lovableApp.DTO.auth.AuthResponse;
import com.yash.projects.lovableApp.DTO.auth.LoginRequest;
import com.yash.projects.lovableApp.DTO.auth.SignupRequest;
import com.yash.projects.lovableApp.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    @Override
    public AuthResponse signup(SignupRequest request) {
        return null;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        return null;
    }
}
