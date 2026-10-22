package com.platform.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

/**
 * Local filesystem storage — no AWS needed.
 * Files stored at: {base-path}/{tenantId}/{entityType}/{entityId}/{filename}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalStorageService {

    @Value("${platform.storage.local.base-path:./uploads}")
    private String basePath;

    public String store(MultipartFile file, String tenantId, String entityType, String entityId) throws IOException {
        String relativePath = tenantId + "/" + entityType + "/" + entityId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path fullPath = Paths.get(basePath, relativePath);
        Files.createDirectories(fullPath.getParent());
        Files.copy(file.getInputStream(), fullPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Stored file: {}", relativePath);
        return relativePath;
    }

    public byte[] load(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(basePath, filePath));
    }

    public void delete(String filePath) throws IOException {
        Files.deleteIfExists(Paths.get(basePath, filePath));
        log.info("Deleted file: {}", filePath);
    }
}
