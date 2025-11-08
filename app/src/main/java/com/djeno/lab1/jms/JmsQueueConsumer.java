package com.djeno.lab1.jms;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JmsQueueConsumer {

    private final RuntimeService runtimeService;

    public JmsQueueConsumer(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @JmsListener(destination = "analysis.queue", containerFactory = "queueFactory")
    public void receive(String payload) {
        log.info("[JMS] got: " + payload);
        runtimeService.createMessageCorrelation("MESSAGE_ANALYZE_FINISH")
                .setVariable("payload", payload)
                .correlateStartMessage();
    }

}