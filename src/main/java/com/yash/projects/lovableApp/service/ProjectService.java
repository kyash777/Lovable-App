package com.yash.projects.lovableApp.service;

import com.yash.projects.lovableApp.DTO.project.ProjectRequest;
import com.yash.projects.lovableApp.DTO.project.ProjectResponse;
import com.yash.projects.lovableApp.DTO.project.ProjectSummaryResponse;

import java.util.List;

public interface ProjectService {
    List<ProjectSummaryResponse> getUserProjects();

    ProjectResponse getUserProjectById(Long id);

    ProjectResponse createProject(ProjectRequest request);

    ProjectResponse updateProject(Long id, ProjectRequest request);

    void softDelete(Long id);
}
