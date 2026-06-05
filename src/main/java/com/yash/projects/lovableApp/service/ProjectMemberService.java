package com.yash.projects.lovableApp.service;

import com.yash.projects.lovableApp.DTO.member.InviteMemberRequest;
import com.yash.projects.lovableApp.DTO.member.MemberResponse;
import com.yash.projects.lovableApp.DTO.member.ProjectMember;

import java.util.List;

public interface ProjectMemberService {
    List<ProjectMember> getProjectMembers(Long projectId, Long userId);

    MemberResponse inviteMember(Long projectId, InviteMemberRequest request, Long userId);

    MemberResponse updateMemberRole(Long projectId, Long memberId, InviteMemberRequest request, Long userId);

    MemberResponse deleteProjectMember(Long projectId, Long memberId, Long userId);
}
