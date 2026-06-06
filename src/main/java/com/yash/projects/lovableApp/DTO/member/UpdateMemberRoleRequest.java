package com.yash.projects.lovableApp.DTO.member;


import com.yash.projects.lovableApp.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(
        @NotNull ProjectRole role
) {
}
