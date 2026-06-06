package com.yash.projects.lovableApp.mapper;
import com.yash.projects.lovableApp.DTO.auth.SignupRequest;
import com.yash.projects.lovableApp.DTO.auth.UserProfileResponse;
import com.yash.projects.lovableApp.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(SignupRequest signupRequest);

    UserProfileResponse toUserProfileResponse(User user);

}
