package com.djeno.lab1.jms;

import com.djeno.lab1.persistence.DTO.app.JarUploadedEvent;
import com.djeno.lab1.persistence.models.App;
import com.djeno.lab1.persistence.repositories.AppRepository;
import com.djeno.lab1.services.MinioService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JmsQueueConsumer {

    private final MinioService minioService;
    private final AppRepository appRepository;

    public JmsQueueConsumer(MinioService minioService, AppRepository appRepository) {
        this.minioService = minioService;
        this.appRepository = appRepository;
    }

    @JmsListener(destination = "analysis.queue", containerFactory = "queueFactory")
    public void receive(String payload) {
        log.info("[JMS] got: " + payload);
        try {
            JarUploadedEvent evt = new ObjectMapper().readValue(payload, JarUploadedEvent.class);
            deleteApp(evt.appId);
        } catch (JsonProcessingException e) {
            log.warn("[JMS] Не удалось преобразовать объект: " + e.getMessage());
        }
    }

    private void deleteApp(String fileId) {
        App app = appRepository.findByFileId(fileId);
        if (app.getIconId() != null) {
            try {
                minioService.deleteFile(app.getIconId(), MinioService.ICONS_BUCKET);
            } catch (Exception e) {
                log.warn("Не удалось удалить иконку приложения из Minio: {}", e.getMessage());
            }
        }

        if (app.getFileId() != null) {
            try {
                minioService.deleteFile(app.getFileId(), MinioService.APK_BUCKET);
            } catch (Exception e) {
                log.warn("Не удалось удалить APK файл из Minio: {}", e.getMessage());
            }
        }

        if (app.getScreenshotsIds() != null) {
            for (String screenId : app.getScreenshotsIds()) {
                try {
                    minioService.deleteFile(screenId, MinioService.SCREENSHOTS_BUCKET);
                } catch (Exception e) {
                    log.warn("Не удалось удалить скриншот из Minio: {}", e.getMessage());
                }
            }
        }

        appRepository.delete(app);
    }
}