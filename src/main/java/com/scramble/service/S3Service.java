package com.scramble.service;

import com.scramble.dto.article.PresignedUrlResponse;
import com.scramble.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;


import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private final SecurityUtils securityUtils;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public PresignedUrlResponse generatePresignedUrl(
            String fileName,
            String contentType,
            String folder
    ) {


        long userId = securityUtils.getCurrentUser().getId();


        String safeFileName = fileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");


        String key = folder + "/" + userId + "/" +
                UUID.randomUUID() + "-" + safeFileName;


        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();


        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest =
                s3Presigner.presignPutObject(presignRequest);


        String uploadUrl = presignedRequest.url().toString();
        String fileUrl = getPublicUrl(key);

        return new PresignedUrlResponse(uploadUrl, fileUrl);
    }


    public void deleteFile(String fileUrl) {

        if (fileUrl == null || fileUrl.isEmpty()) return;

        if (!fileUrl.contains(bucketName)) return;

        try {
            String key = fileUrl.substring(fileUrl.indexOf(".com/") + 5);

            if (key.startsWith("default-Avatar/")) {
                return;
            }

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);

        } catch (Exception e) {
            System.out.println("S3 DELETE ERROR: " + e.getMessage());
        }
    }

    public String getPublicUrl(String key) {
        return "https://" + bucketName + ".s3.amazonaws.com/" + key;
    }
}