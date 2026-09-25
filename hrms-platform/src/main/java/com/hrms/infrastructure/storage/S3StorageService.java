package com.hrms.infrastructure.storage;

import com.hrms.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Service
public class S3StorageService implements StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final long presignedUrlExpiryMinutes;

    public S3StorageService(S3Client s3Client, S3Presigner s3Presigner,
                             @Value("${app.aws.s3.bucket-name}") String bucketName,
                             @Value("${app.aws.s3.presigned-url-expiry-minutes}") long presignedUrlExpiryMinutes) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
        this.presignedUrlExpiryMinutes = presignedUrlExpiryMinutes;
    }

    @Override
    public String upload(MultipartFile file, String key) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("File uploaded to S3: {}", key);
            return key;
        } catch (IOException ex) {
            throw new BusinessException("STORAGE_ERROR", "Failed to upload file: " + ex.getMessage());
        }
    }

    @Override
    public String generatePresignedUrl(String key) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignedUrlExpiryMinutes))
                .getObjectRequest(r -> r.bucket(bucketName).key(key))
                .build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public void delete(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build());
        log.info("File deleted from S3: {}", key);
    }
}
