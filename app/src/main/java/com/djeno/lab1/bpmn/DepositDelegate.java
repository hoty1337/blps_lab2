package com.djeno.lab1.bpmn;

import com.djeno.lab1.services.VirtualAccountService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("depositDelegate")
@RequiredArgsConstructor
public class DepositDelegate implements JavaDelegate {
    private final VirtualAccountService virtualAccountService;

    @Override
    public void execute(DelegateExecution ex) {
        String cardNumber = (String) ex.getVariable("cardNumber");
        BigDecimal amount = BigDecimal.valueOf((Long) ex.getVariable("amount"));
        virtualAccountService.deposit(cardNumber, amount);
    }
}
