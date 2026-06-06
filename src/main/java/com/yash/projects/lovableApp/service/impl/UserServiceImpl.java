package com.yash.projects.lovableApp.service.impl;

import com.yash.projects.lovableApp.DTO.auth.UserProfileResponse;
import com.yash.projects.lovableApp.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public UserProfileResponse getProfile(Long userId) {
        return null;
    }
}
