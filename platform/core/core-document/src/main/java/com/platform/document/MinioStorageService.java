package com.platform.document;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MinioStorageService {

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioStorageService(
        @Value("${platform.storage.minio.endpoint:http://localhost:9000}") String endpoint,
        @Value("${platform.storage.minio.access-key:admin}") String accessKey,
        @Value("${platform.storage.minio.secret-key:admin123456}") String secretKey,
        @Value("${platform.storage.minio.bucket:vve-documents}") String bucketName
    ) {
        this.bucketName = bucketName;
        this.minioClient = MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build();
        ensureBucketExists();
    }

    // ─────────────────────────────────────────────
    // UPLOAD - Store file in MinIO
    // Returns the object path (stored in Document.filePath)
    // ─────────────────────────────────────────────
    public String store(MultipartFile file, String tenantId,
                        String entityType, String entityId) throws Exception {
        String objectName = tenantId + "/" + entityType + "/" + entityId
            + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        minioClient.putObject(PutObjectArgs.builder()
            .bucket(bucketName)
            .object(objectName)
            .stream(file.getInputStream(), file.getSize(), -1)
            .contentType(file.getContentType())
            .build());

        log.info("Stored file in MinIO: {}", objectName);
        return objectName;
    }

    // ─────────────────────────────────────────────
    // DOWNLOAD - Get file bytes from MinIO
    // ─────────────────────────────────────────────
    public byte[] load(String objectName) throws Exception {
        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
            .bucket(bucketName)
            .object(objectName)
            .build())) {
            return stream.readAllBytes();
        }
    }

    // ─────────────────────────────────────────────
    // PRESIGNED URL - Temporary download link (1 hour)
    // ─────────────────────────────────────────────
    public String getPresignedUrl(String objectName) throws Exception {
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
            .bucket(bucketName)
            .object(objectName)
            .method(Method.GET)
            .expiry(1, TimeUnit.HOURS)
            .build());
    }

    // ─────────────────────────────────────────────
    // DELETE - Remove file from MinIO
    // ─────────────────────────────────────────────
    public void delete(String objectName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder()
            .bucket(bucketName)
            .object(objectName)
            .build());
        log.info("Deleted file from MinIO: {}", objectName);
    }

    // ─────────────────────────────────────────────
    // Create bucket if not exists on startup
    // ─────────────────────────────────────────────
    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Created MinIO bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("MinIO bucket check failed: {}", e.getMessage());
        }
    }
}
