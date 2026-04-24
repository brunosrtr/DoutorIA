package com.doutor.service.impl;

import com.doutor.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    @Value("${upload.dir:/data/uploads}")
    private String uploadDir;

    @Override
    public String store(UUID assessmentId, MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename() != null
                    ? file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_")
                    : "file";
            String fileName = UUID.randomUUID() + "-" + originalFilename;
            Path dir = Paths.get(uploadDir, assessmentId.toString());
            Files.createDirectories(dir);
            Path filePath = dir.resolve(fileName);
            Files.write(filePath, file.getBytes());
            String relativePath = assessmentId + "/" + fileName;
            log.info("Stored file: {}", relativePath);
            return relativePath;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar arquivo: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] load(String filePath) {
        try {
            Path path = Paths.get(uploadDir, filePath);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao carregar arquivo: " + filePath, e);
        }
    }

    @Override
    public void delete(String filePath) {
        try {
            Path path = Paths.get(uploadDir, filePath);
            Files.deleteIfExists(path);
            // Remove parent directory if empty
            Path parent = path.getParent();
            if (parent != null && Files.isDirectory(parent)) {
                try (var stream = Files.list(parent)) {
                    if (stream.findAny().isEmpty()) {
                        Files.deleteIfExists(parent);
                    }
                }
            }
        } catch (IOException e) {
            log.warn("Falha ao deletar arquivo: {}", filePath, e);
        }
    }
}
