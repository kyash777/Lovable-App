package com.yash.projects.lovableApp.service;

import com.yash.projects.lovableApp.DTO.project.ProjectRequest;
import com.yash.projects.lovableApp.DTO.project.ProjectResponse;
import com.yash.projects.lovableApp.DTO.project.ProjectSummaryResponse;

import java.util.List;

public interface ProjectService {
    List<ProjectSummaryResponse> getUserProjects(Long userId);

    ProjectResponse getUserProjectById(Long id, Long userId);

    ProjectResponse createProject(ProjectRequest request, Long userId);

    ProjectResponse updateProject(Long id, ProjectRequest request, Long userId);

    void softDelete(Long id, Long userId);
}
