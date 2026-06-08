package com.yash.projects.lovableApp.service.impl;

import com.yash.projects.lovableApp.DTO.member.InviteMemberRequest;
import com.yash.projects.lovableApp.DTO.member.MemberResponse;
import com.yash.projects.lovableApp.DTO.member.UpdateMemberRoleRequest;
import com.yash.projects.lovableApp.Repository.ProjectMemberRepository;
import com.yash.projects.lovableApp.Repository.ProjectRepository;
import com.yash.projects.lovableApp.Repository.UserRepository;
import com.yash.projects.lovableApp.entity.Project;
import com.yash.projects.lovableApp.entity.ProjectMember;
import com.yash.projects.lovableApp.entity.ProjectMemberId;
import com.yash.projects.lovableApp.entity.User;
import com.yash.projects.lovableApp.mapper.ProjectMemberMapper;
import com.yash.projects.lovableApp.security.AuthUtil;
import com.yash.projects.lovableApp.service.ProjectMemberService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMemberMapper projectMemberMapper;
    private final UserRepository userRepository;
    private final AuthUtil authUtil;


    @Override
    public List<MemberResponse> getProjectMembers(Long projectId) {

        Long userId = authUtil.getCurrentUserId();

        Project project = getAccessibleProjectById(projectId, userId);

        return projectMemberRepository.findByIdProjectId(projectId)
                .stream()
                .map(projectMemberMapper::toProjectMemberResponseFromMember)
                .toList();
    }

    @Override
    public MemberResponse inviteMember(Long projectId, InviteMemberRequest request) {

        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(projectId, userId);

        User invitee = userRepository.findByUsername(request.username()).orElseThrow();

        if(invitee.getId().equals(userId)) {
            throw new RuntimeException("Cannot invite yourself");
        }

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, invitee.getId());

        if(projectMemberRepository.existsById(projectMemberId)) {
            throw new RuntimeException("Cannot invite once again");
        }

        ProjectMember member = ProjectMember.builder()
                .id(projectMemberId)
                .project(project)
                .user(invitee)
                .projectRole(request.role())
                .invitedAt(Instant.now())
                .build();

        projectMemberRepository.save(member);

        return projectMemberMapper.toProjectMemberResponseFromMember(member);
    }

    @Override
    public MemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request) {

        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(projectId, userId);

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);
        ProjectMember projectMember = projectMemberRepository.findById(projectMemberId).orElseThrow();

        projectMember.setProjectRole(request.role());

        projectMemberRepository.save(projectMember);

        return projectMemberMapper.toProjectMemberResponseFromMember(projectMember);
    }

    @Override
    public void deleteProjectMember(Long projectId, Long memberId) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(projectId, userId);

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);
        if(!projectMemberRepository.existsById(projectMemberId)) {
            throw new RuntimeException("Member not found in project");
        }

        projectMemberRepository.deleteById(projectMemberId);
    }

    ///  INTERNAL FUNCTIONS

    public Project getAccessibleProjectById(Long projectId, Long userId) {
        return projectRepository.findAccessibleProjectById(projectId, userId).orElseThrow();
    }
}
