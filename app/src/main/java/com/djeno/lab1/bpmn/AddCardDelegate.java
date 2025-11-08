package com.djeno.lab1.bpmn;

import com.djeno.lab1.persistence.DTO.payment.AddCardRequest;
import com.djeno.lab1.services.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("addCardDelegate")
@RequiredArgsConstructor
public class AddCardDelegate implements JavaDelegate {
    private final IdentityService identityService;
    private final PaymentMethodService paymentMethodService;

    @Override
    public void execute(DelegateExecution ex) {
        String camundaUser = null;
        var auth = identityService.getCurrentAuthentication();
        if(auth != null) {
            camundaUser = auth.getUserId();
        }

        var req = new AddCardRequest();
        req.setCardNumber((String) ex.getVariable("cardNumber"));
        req.setCardHolder((String) ex.getVariable("cardHolder"));
        req.setExpirationDate((String) ex.getVariable("expirationDate"));
        req.setCvv((String) ex.getVariable("cvv"));
        paymentMethodService.addCard(req, camundaUser);
    }
}
