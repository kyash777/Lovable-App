package com.yash.projects.lovableApp.DTO.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @Email @NotBlank String username,
        @Size(min=1, max=32) String name,
        @Size(min=4) String password
) {
}
