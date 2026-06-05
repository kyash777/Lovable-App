package com.yash.projects.lovableApp.DTO.member;

import com.yash.projects.lovableApp.enums.ProjectRole;

public record InviteMemberRequest(
        String email,
        ProjectRole role
) {
}
