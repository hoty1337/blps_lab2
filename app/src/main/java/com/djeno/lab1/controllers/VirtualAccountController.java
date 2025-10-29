package com.djeno.lab1.controllers;

import com.djeno.lab1.persistence.DTO.payment.CardOperationData;
import com.djeno.lab1.services.VirtualAccountService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "Виртуальные счета")
@RequiredArgsConstructor
@RequestMapping("/accounts")
@RestController
public class VirtualAccountController {
    public final VirtualAccountService virtualAccountService;

    @GetMapping("/balance")
    public ResponseEntity<String> getBalance(@RequestParam String cardNumber) {
        BigDecimal balance = virtualAccountService.getBalance(cardNumber);
        return ResponseEntity.ok(balance.toString());
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(
            @Parameter(description = "Данные карты", required = true)
            @RequestBody
            @Valid
            CardOperationData data) {
        virtualAccountService.deposit(data.getCardNumber(), data.getAmount());
        return ResponseEntity.ok("Deposit " + data.getAmount() + " to " + data.getCardNumber());
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(
            @Parameter(description = "Данные карты", required = true)
            @RequestBody
            @Valid
            CardOperationData data) {
        virtualAccountService.withdraw(data.getCardNumber(), data.getAmount());
        return ResponseEntity.ok("Withdraw " + data.getAmount() + " from " + data.getCardNumber());
    }



}
