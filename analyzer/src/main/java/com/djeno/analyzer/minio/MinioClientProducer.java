package com.djeno.analyzer.minio;

import io.minio.MinioClient;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

public class MinioClientProducer {

    @Produces
    @Dependent
    public MinioClient minioClient() {
        String url = System.getenv().getOrDefault("MINIO_URL", "http://localhost:9000");
        String ak  = System.getenv().getOrDefault("MINIO_ROOT_USER", "admin");
        String sk  = System.getenv().getOrDefault("MINIO_ROOT_PASSWORD", "12345678");
        return MinioClient.builder().endpoint(url).credentials(ak, sk).build();
    }
}
