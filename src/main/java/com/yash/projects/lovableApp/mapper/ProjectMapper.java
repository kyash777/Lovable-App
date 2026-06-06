package com.yash.projects.lovableApp.mapper;

import com.yash.projects.lovableApp.DTO.project.ProjectResponse;
import com.yash.projects.lovableApp.DTO.project.ProjectSummaryResponse;
import com.yash.projects.lovableApp.entity.Project;
import org.mapstruct.Mapper;


import java.util.List;

@Mapper(componentModel = "spring")

public interface ProjectMapper {
    ProjectResponse toProjectResponse(Project project);

    ProjectSummaryResponse toProjectSummaryResponse(Project project);

    List<ProjectSummaryResponse> toListOfProjectSummaryResponse(List<Project> projects);
}
