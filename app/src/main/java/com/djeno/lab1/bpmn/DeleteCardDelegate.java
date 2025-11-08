package com.djeno.lab1.bpmn;

import com.djeno.lab1.services.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("deleteCardDelegate")
@RequiredArgsConstructor
public class DeleteCardDelegate implements JavaDelegate {
    private final PaymentMethodService paymentMethodService;
    private final IdentityService identityService;

    @Override
    public void execute(DelegateExecution ex) throws Exception {
        String camundaUser = null;
        var auth = identityService.getCurrentAuthentication();
        if(auth != null) {
            camundaUser = auth.getUserId();
        }

        Long cardId = toLong(ex.getVariable("cardId"));
        paymentMethodService.deleteCard(cardId, camundaUser);
    }

    private Long toLong(Object v){
        if (v instanceof Long l) return l;
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof String s) return Long.parseLong(s);
        throw new IllegalArgumentException("cardId type unsupported: " + v);
    }
}
