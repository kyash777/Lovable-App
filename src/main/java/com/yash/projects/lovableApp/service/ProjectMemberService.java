package com.yash.projects.lovableApp.service;

import com.yash.projects.lovableApp.DTO.member.InviteMemberRequest;
import com.yash.projects.lovableApp.DTO.member.MemberResponse;
import com.yash.projects.lovableApp.DTO.member.UpdateMemberRoleRequest;


import java.util.List;

public interface ProjectMemberService {
    List<MemberResponse> getProjectMembers(Long projectId);

    MemberResponse inviteMember(Long projectId, InviteMemberRequest request);

    MemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request);

    void deleteProjectMember(Long projectId, Long memberId);
}
