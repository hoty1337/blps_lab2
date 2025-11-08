package com.djeno.lab1.bpmn;

import com.djeno.lab1.persistence.DTO.app.JarUploadedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("verifyMessageAppDataDelegate")
@RequiredArgsConstructor
public class VerifyMessageAppDataDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution ex) throws Exception {
        String payload = (String) ex.getVariable("payload");
        JarUploadedEvent evt = new ObjectMapper().readValue(payload, JarUploadedEvent.class);
        if (evt != null && evt.appId != null) {
            ex.setVariable("valid", true);
            ex.setVariable("fileId", evt.appId);
        } else {
            ex.setVariable("valid", false);
        }
    }
}
