package com.example.echobackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    // The S3Client is automatically configured by Spring Boot if you have the SDK dependencies
    // and credentials in application.properties.
    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file.");
        }

        // Generate a unique file name to avoid collisions
        // Combine UUID with original file extension
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + fileExtension;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName) // The key is the file name in S3
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Construct the public URL for the uploaded file
            // S3 URLs typically follow this pattern:
            // https://<bucket-name>.s3.<region>.amazonaws.com/<key>
            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                    bucketName,
                    s3Client.serviceClientConfiguration().region().id(), // Get region from client config
                    fileName);
            return fileUrl;

        } catch (S3Exception e) {
            // Handle S3-specific errors (e.g., permissions, bucket not found)
            throw new RuntimeException("S3 upload failed: " + e.getMessage(), e);
        } catch (IOException e) {
            // Handle file stream errors
            throw new RuntimeException("Failed to read file for S3 upload: " + e.getMessage(), e);
        }
    }
}