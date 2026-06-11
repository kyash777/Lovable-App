package com.yash.projects.lovableApp.mapper;


import com.yash.projects.lovableApp.DTO.project.FileNode;
import com.yash.projects.lovableApp.entity.ProjectFile;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectFileMapper {

    List<FileNode> toListOfFileNode(List<ProjectFile> projectFileList);
}
