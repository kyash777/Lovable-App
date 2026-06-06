package com.yash.projects.lovableApp.service.impl;

import com.yash.projects.lovableApp.DTO.auth.AuthResponse;
import com.yash.projects.lovableApp.DTO.auth.LoginRequest;
import com.yash.projects.lovableApp.DTO.auth.SignupRequest;
import com.yash.projects.lovableApp.Repository.UserRepository;
import com.yash.projects.lovableApp.entity.User;
import com.yash.projects.lovableApp.errors.BadRequestException;
import com.yash.projects.lovableApp.mapper.UserMapper;
import com.yash.projects.lovableApp.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse signup(SignupRequest request) {
        userRepository.findByUsername(request.username()).ifPresent(user -> {
            throw new BadRequestException("User already exists with username: "+request.username());
        });

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user = userRepository.save(user);

        return new AuthResponse("dummy", userMapper.toUserProfileResponse(user));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        return null;
    }
}
