package com.djeno.lab1.services;

import com.djeno.lab1.exceptions.CardAlreadyExistsException;
import com.djeno.lab1.exceptions.CardNotFoundException;
import com.djeno.lab1.exceptions.InvalidValueException;
import com.djeno.lab1.exceptions.NotEnoughMoneyException;
import com.djeno.lab1.persistence.models.VirtualAccount;
import com.djeno.lab1.persistence.repositories.VirtualAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class VirtualAccountService {
    private final VirtualAccountRepository virtualAccountRepository;
    private final TransactionService transactionService;

    public VirtualAccount create(String cardNumber) {
        if (virtualAccountRepository.existsById(cardNumber)) {
            throw new CardAlreadyExistsException("Виртуальный счет уже сущществует");
        }
        VirtualAccount virtualAccount = new VirtualAccount();
        virtualAccount.setCardNumber(cardNumber);
        virtualAccount.setBalance(BigDecimal.ZERO);
        return virtualAccountRepository.save(virtualAccount);
    }

    public boolean exists(String cardNumber) {
        return virtualAccountRepository.existsById(cardNumber);
    }

    public VirtualAccount get(String cardNumber) {
        return virtualAccountRepository.findById(cardNumber)
                .orElseThrow(() -> new CardNotFoundException("Виртуальный счет не существует"));
    }

    public void save(VirtualAccount virtualAccount) {
        virtualAccountRepository.save(virtualAccount);
    }

    public BigDecimal getBalance(String cardNumber) {
        return virtualAccountRepository.findById(cardNumber)
                .map(VirtualAccount::getBalance)
                .orElseThrow(() -> new CardNotFoundException("Виртуальный счет не найден"));
    }

    public void deposit(String cardNumber, BigDecimal amount) {
        if(amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidValueException("Сумма для пополнения должна быть больше нуля");
        }
        VirtualAccount virtualAccount = get(cardNumber);
        virtualAccount.setBalance(virtualAccount.getBalance().add(amount));
        virtualAccountRepository.save(virtualAccount);
    }

    public void withdraw(String cardNumber, BigDecimal amount) {
        if(amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidValueException("Сумма для снятия должна быть больше нуля");
        }
        VirtualAccount virtualAccount = get(cardNumber);
        if (virtualAccount.getBalance().compareTo(amount) < 0) {
            throw new NotEnoughMoneyException("Недостаточно денег на балансе");
        }
        virtualAccount.setBalance(virtualAccount.getBalance().subtract(amount));
        virtualAccountRepository.save(virtualAccount);
        log.info("Withdraw: {} | Card: {} | Balance: {}", amount, virtualAccount.getCardNumber(), virtualAccount.getBalance());

    }
}
