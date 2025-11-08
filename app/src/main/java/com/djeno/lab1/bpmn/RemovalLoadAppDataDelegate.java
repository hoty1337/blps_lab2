package com.djeno.lab1.bpmn;

import com.djeno.lab1.persistence.models.App;
import com.djeno.lab1.persistence.repositories.AppRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("removalLoadAppDataDelegate")
@RequiredArgsConstructor
@Slf4j
public class RemovalLoadAppDataDelegate implements JavaDelegate {
    private final AppRepository appRepository;

    @Override
    public void execute(DelegateExecution ex) throws Exception {
        Long appId = toLong(ex.getVariable("appId"));
        App app = appRepository.findById(appId).orElse(null);
        boolean found = app != null;
        ex.setVariable("appFound", found);

        if (!found) {
            log.info("[app_remove] App с fileId={} не найден", appId);
            return;
        }

        log.info("[app_remove] Найден app id={}, fileId={}", appId, app.getFileId());
    }

    private Long toLong(Object v){
        if (v instanceof Long l) return l;
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof String s) return Long.parseLong(s);
        throw new IllegalArgumentException("appId type unsupported: " + v);
    }
}
