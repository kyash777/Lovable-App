package com.yash.projects.lovableApp.service.impl;

import com.yash.projects.lovableApp.DTO.project.ProjectRequest;
import com.yash.projects.lovableApp.DTO.project.ProjectResponse;
import com.yash.projects.lovableApp.DTO.project.ProjectSummaryResponse;
import com.yash.projects.lovableApp.Repository.ProjectMemberRepository;
import com.yash.projects.lovableApp.Repository.ProjectRepository;
import com.yash.projects.lovableApp.Repository.UserRepository;
import com.yash.projects.lovableApp.entity.Project;
import com.yash.projects.lovableApp.entity.ProjectMember;
import com.yash.projects.lovableApp.entity.ProjectMemberId;
import com.yash.projects.lovableApp.entity.User;
import com.yash.projects.lovableApp.enums.ProjectRole;
import com.yash.projects.lovableApp.errors.BadRequestException;
import com.yash.projects.lovableApp.errors.ResourceNotFoundException;
import com.yash.projects.lovableApp.mapper.ProjectMapper;
import com.yash.projects.lovableApp.security.AuthUtil;
import com.yash.projects.lovableApp.service.ProjectService;
import com.yash.projects.lovableApp.service.SubscriptionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final ProjectMemberRepository projectMemberRepository;
    private final AuthUtil authUtil;
    private final SubscriptionService subscriptionService;

    @Override
    public ProjectResponse createProject(ProjectRequest request) {

        if(!subscriptionService.canCreateNewProject()){
            throw new BadRequestException("Project creation limit reached. Please upgrade your subscription.");

        }
        Long userId = authUtil.getCurrentUserId();

        User owner = userRepository.getReferenceById(userId);

        Project project = Project.builder()
                .name(request.name())
                .isPublic(false)
                .build();

        project = projectRepository.save(project);

        ProjectMemberId projectMemberId = new ProjectMemberId(project.getId(), owner.getId());
        ProjectMember projectMember = ProjectMember.builder()
                .id(projectMemberId)
                .projectRole(ProjectRole.OWNER)
                .user(owner)
                .acceptedAt(Instant.now())
                .invitedAt(Instant.now())
                .project(project)
                .build();
        projectMemberRepository.save(projectMember);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    public List<ProjectSummaryResponse> getUserProjects() {

        Long userId = authUtil.getCurrentUserId();

//        return projectRepository.findAllAccessibleByUser(userId)
//                .stream()
//                .map(projectMapper::toProjectSummaryResponse)
//                .collect(Collectors.toList());

        var projects = projectRepository.findAllAccessibleByUser(userId);
        return projectMapper.toListOfProjectSummaryResponse(projects);
    }

    @Override
    @PreAuthorize("@security.canViewProject(#projectId)")
    public ProjectResponse getUserProjectById(Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(projectId, userId);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public ProjectResponse updateProject(Long projectId, ProjectRequest request) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(projectId, userId);

        project.setName(request.name());
        project = projectRepository.save(project);

        return projectMapper.toProjectResponse(project);
    }

    @Override
    @PreAuthorize("@security.canDeleteProject(#projectId)")
    public void softDelete(Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(projectId, userId);

        project.setDeletedAt(Instant.now());
        projectRepository.save(project);
    }

    ///  INTERNAL FUNCTIONS

    public Project getAccessibleProjectById(Long projectId, Long userId) {
        return projectRepository.findAccessibleProjectById(projectId, userId).orElseThrow(()->new ResourceNotFoundException("Project", projectId.toString()));
    }
}
