package com.yash.projects.lovableApp.DTO.auth;

public record AuthResponse(
        String token,
        UserProfileResponse user
) {

}
