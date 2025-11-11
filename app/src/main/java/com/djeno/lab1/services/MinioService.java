package com.djeno.lab1.services;

import io.minio.*;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MinioService {
    public static final String ICONS_BUCKET = "icons";
    public static final String SCREENSHOTS_BUCKET = "screenshots";
    public static final String APK_BUCKET = "apk";

    private final MinioClient minioClient;
    private final String publicBaseUrl;

    public MinioService(@Value("${minio.endpoint}") String endpoint,
                        @Value("${minio.publicUrl}") String publicBaseUrl,
                        @Value("${minio.root.user}") String accessKey,
                        @Value("${minio.root.password}") String secretKey) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
    }

    @SneakyThrows
    public String uploadFile(MultipartFile file, String bucketName) {
        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID() + "_" + originalFileName; // UUID для получения файла

        createBucketIfNotExist(bucketName); // imported-files

        try (InputStream inputStream = file.getInputStream()) {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("filename", originalFileName);

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(uniqueFileName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .userMetadata(metadata)
                    .build());
            return uniqueFileName;
        }
    }

    @SneakyThrows
    public InputStream downloadFile(String uniqueFileName, String bucketName) {
        createBucketIfNotExist(bucketName);

        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(uniqueFileName)
                        .build());
    }

    @SneakyThrows
    public void deleteFile(String uniqueFileName, String bucketName) {
        createBucketIfNotExist(bucketName);

        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(uniqueFileName)
                        .build());
    }

    @SneakyThrows
    public void createBucketIfNotExist(String bucketName) {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    public String buildPublicUrl(String objectName, String bucket) {
        return String.format("%s/%s/%s",
                publicBaseUrl, bucket, UriUtils.encodePath(objectName, StandardCharsets.UTF_8));
    }

}
