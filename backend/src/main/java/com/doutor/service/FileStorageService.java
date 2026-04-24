package com.doutor.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileStorageService {

    String store(UUID assessmentId, MultipartFile file);

    byte[] load(String filePath);

    void delete(String filePath);
}
