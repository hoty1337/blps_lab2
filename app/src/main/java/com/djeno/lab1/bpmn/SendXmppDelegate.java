package com.djeno.lab1.bpmn;

import com.djeno.lab1.persistence.DTO.app.JarUploadedEvent;
import com.djeno.lab1.services.MinioService;
import com.djeno.lab1.xmpp.XmppProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendXmppDelegate implements JavaDelegate {
    private final XmppProducer xmppProducer;
    private final ObjectMapper om;

    @Override
    public void execute(DelegateExecution ex) throws Exception {
        var evt = new JarUploadedEvent();
        evt.name  = (String) ex.getVariable("username");
        evt.email = (String) ex.getVariable("email");
        evt.appId = (String) ex.getVariable("fileId");
        evt.bucket = MinioService.APK_BUCKET;
        xmppProducer.sendToTopic("analysis.request", om.writeValueAsString(evt));
    }
}
