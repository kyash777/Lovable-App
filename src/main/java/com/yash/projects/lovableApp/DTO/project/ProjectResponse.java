package com.yash.projects.lovableApp.DTO.project;

import com.yash.projects.lovableApp.DTO.auth.UserProfileResponse;

import java.time.Instant;

public record ProjectResponse(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt,
        UserProfileResponse owner
) {
}
