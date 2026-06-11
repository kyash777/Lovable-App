package com.yash.projects.lovableApp.service;
import com.yash.projects.lovableApp.DTO.project.FileContentResponse;
import com.yash.projects.lovableApp.DTO.project.FileNode;

import java.util.List;

public interface ProjectFileService {
    List<FileNode> getFileTree(Long projectId, Long userId);

    FileContentResponse getFileContent(Long projectId, String path, Long userId);

    void saveFile(Long projectId, String filePath, String fileContent);
}
