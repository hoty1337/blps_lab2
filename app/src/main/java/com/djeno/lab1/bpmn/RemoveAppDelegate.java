package com.djeno.lab1.bpmn;

import com.djeno.lab1.persistence.repositories.PurchaseRepository;
import com.djeno.lab1.services.AppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("removeAppDelegate")
@RequiredArgsConstructor
@Slf4j
public class RemoveAppDelegate implements JavaDelegate {
    private final AppService appService;
    private final PurchaseRepository purchaseRepository;
    private final IdentityService identityService;

    @Override
    public void execute(DelegateExecution ex) throws Exception {
        String camundaUser = null;
        var auth = identityService.getCurrentAuthentication();
        if(auth != null) {
            camundaUser = auth.getUserId();
        }
        Long appId = toLong(ex.getVariable("appId"));
        purchaseRepository.deleteByApp_id(appId);
        appService.deleteApp(appId, camundaUser);
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Long l) return l;
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof String s && !s.isBlank()) return Long.parseLong(s);
        throw new IllegalArgumentException("Unsupported id type: " + v.getClass());
    }
}
