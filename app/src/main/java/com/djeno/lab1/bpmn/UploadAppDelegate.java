package com.djeno.lab1.bpmn;

import com.djeno.lab1.services.MinioService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component("uploadAppDelegate")
@RequiredArgsConstructor
public class UploadAppDelegate implements JavaDelegate {
    private final MinioService minioService;
    @Override public void execute(DelegateExecution ex) {
        MultipartFile icon = (MultipartFile) ex.getVariable("icon");
        MultipartFile file = (MultipartFile) ex.getVariable("file");

        if (file == null || file.isEmpty()) {
            throw new BpmnError("ERR_INVALID_FILE", "APK файл не загружен");
        }
        String iconId = null;
        try {
            if (icon != null && !icon.isEmpty()) iconId = minioService.uploadFile(icon, MinioService.ICONS_BUCKET);
            String fileId = minioService.uploadFile(file, MinioService.APK_BUCKET);
            ex.setVariable("iconId", iconId);
            ex.setVariable("fileId", fileId);
        } catch (Exception e) {
            throw new BpmnError("ERR_UPLOAD", e.getMessage());
        }
    }
}
