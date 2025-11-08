package com.djeno.lab1.bpmn;

import com.djeno.lab1.persistence.models.App;
import com.djeno.lab1.persistence.repositories.AppRepository;
import com.djeno.lab1.services.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("deleteAppDelegate")
@RequiredArgsConstructor
@Slf4j
public class DeleteAppDelegate implements JavaDelegate {
    private final AppRepository appRepository;
    private final MinioService minioService;

    @Override
    public void execute(DelegateExecution ex) throws Exception {
        String fileId = ex.getVariable("fileId").toString();
        deleteApp(fileId);
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
