package com.djeno.lab1.bpmn;

import com.djeno.lab1.persistence.models.App;
import com.djeno.lab1.persistence.models.User;
import com.djeno.lab1.services.AppService;
import com.djeno.lab1.services.PurchaseService;
import com.djeno.lab1.services.UserService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("chargePaymentDelegate")
@RequiredArgsConstructor
public class ChargePaymentDelegate implements JavaDelegate {
    private final AppService appService;
    private final UserService userService;
    private final PurchaseService purchaseService;

    @Override
    @Transactional
    public void execute(DelegateExecution ex) throws Exception {
        Long appId = getLong(ex.getVariable("appId"));
        String username = (String) ex.getVariable("username");
        App app = appService.getAppById(appId);
        User user = userService.getByUsername(username);
        purchaseService.purchaseApp(app, user);
    }

    private Long getLong(Object val) {
        if (val == null) return null;
        if (val instanceof Long) return (Long) val;
        if (val instanceof Integer) return ((Integer) val).longValue();
        if (val instanceof String) return Long.parseLong((String) val);
        throw new IllegalArgumentException("Unsupported id type: " + val.getClass());
    }
}
