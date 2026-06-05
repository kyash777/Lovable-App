package com.yash.projects.lovableApp.DTO.auth;

public record SignupRequest(
        String email,
        String name,
        String password
) {
}
