package com.yash.projects.lovableApp.service;

import com.yash.projects.lovableApp.DTO.auth.UserProfileResponse;

public interface UserService {
    UserProfileResponse getProfile(Long userId);
}
