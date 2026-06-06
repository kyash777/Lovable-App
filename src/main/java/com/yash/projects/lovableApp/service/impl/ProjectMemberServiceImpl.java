package com.yash.projects.lovableApp.service.impl;

import com.yash.projects.lovableApp.DTO.member.InviteMemberRequest;
import com.yash.projects.lovableApp.DTO.member.MemberResponse;
import com.yash.projects.lovableApp.DTO.member.UpdateMemberRoleRequest;
import com.yash.projects.lovableApp.service.ProjectMemberService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectMemberServiceImpl implements ProjectMemberService {
    @Override
    public List<MemberResponse> getProjectMembers(Long projectId, Long userId) {
        return List.of();
    }

    @Override
    public MemberResponse inviteMember(Long projectId, InviteMemberRequest request, Long userId) {
        return null;
    }

    @Override
    public MemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request, Long userId) {
        return null;
    }

    @Override
    public MemberResponse deleteProjectMember(Long projectId, Long memberId, Long userId) {
        return null;
    }
}
