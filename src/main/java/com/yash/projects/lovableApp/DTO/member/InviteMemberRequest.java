package com.yash.projects.lovableApp.DTO.member;

import com.yash.projects.lovableApp.enums.ProjectRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteMemberRequest(
        @NotBlank String username,
        @NotNull ProjectRole role
) {
}
