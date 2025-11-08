package com.djeno.lab1.bpmn;

import com.djeno.lab1.persistence.models.App;
import com.djeno.lab1.persistence.models.User;
import com.djeno.lab1.services.AppService;
import com.djeno.lab1.services.PurchaseService;
import com.djeno.lab1.services.UserService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("loadAppDataDelegate")
@RequiredArgsConstructor
public class LoadAppDataDelegate implements JavaDelegate {
    private final UserService userService;
    private final AppService appService;
    private final PurchaseService purchaseService;

    @Override
    public void execute(DelegateExecution ex) throws Exception {
        Long appId = getLong(ex.getVariable("appId"));
        String username = (String) ex.getVariable("username");
        User user = userService.getByUsername(username);
        App app = appService.getAppById(appId);
        boolean isFree = app.getPrice() == null || app.getPrice().compareTo(BigDecimal.ZERO) == 0;
        boolean alreadyPurchased = purchaseService.hasUserPurchasedApp(user, app);

        ex.setVariable("isFree", isFree);
        ex.setVariable("alreadyPurchased", alreadyPurchased);
        ex.setVariable("price", app.getPrice() == null ? BigDecimal.ZERO : app.getPrice());
        ex.setVariable("appName", app.getName());
    }

    private Long getLong(Object val) {
        if (val == null) return null;
        if (val instanceof Long) return (Long) val;
        if (val instanceof Integer) return ((Integer) val).longValue();
        if (val instanceof String) return Long.parseLong((String) val);
        throw new IllegalArgumentException("Unsupported id type: " + val.getClass());
    }
}
