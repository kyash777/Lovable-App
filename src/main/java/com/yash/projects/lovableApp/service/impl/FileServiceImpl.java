package com.yash.projects.lovableApp.service.impl;

import com.yash.projects.lovableApp.DTO.project.FileContentResponse;
import com.yash.projects.lovableApp.DTO.project.FileNode;
import com.yash.projects.lovableApp.service.FileService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileServiceImpl implements FileService {
    @Override
    public List<FileNode> getFileTree(Long projectId, Long userId) {
        return List.of();
    }

    @Override
    public FileContentResponse getFileContent(Long projectId, String path, Long userId) {
        return null;
    }
}
