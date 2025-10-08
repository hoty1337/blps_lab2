package com.djeno.lab1.services;

import com.djeno.lab1.exceptions.*;
import com.djeno.lab1.persistence.DTO.payment.AddCardRequest;
import com.djeno.lab1.persistence.DTO.payment.PaymentCardDTO;
import com.djeno.lab1.persistence.models.PaymentMethod;
import com.djeno.lab1.persistence.models.User;
import com.djeno.lab1.persistence.models.VirtualAccount;
import com.djeno.lab1.persistence.repositories.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final VirtualAccountService virtualAccountService;
    private final TransactionService transactionService;
    private final UserService userService;

    public PaymentMethod addCard(AddCardRequest request) {
        User user = userService.getCurrentUser();

        if (paymentMethodRepository.existsByUserAndCardNumber(user, request.getCardNumber())) {
            throw new CardAlreadyExistsException("Эта карта уже привязана к вашему аккаунту");
        }

        if (!virtualAccountService.exists(request.getCardNumber())) {
            virtualAccountService.create(request.getCardNumber());
        }

        PaymentMethod card = new PaymentMethod();
        card.setUser(user);
        card.setCardNumber(request.getCardNumber());
        card.setCardHolder(request.getCardHolder());
        card.setExpirationDate(request.getExpirationDate());
        card.setCvv(request.getCvv());
        card.setPrimary(user.getPaymentMethods().isEmpty()); // Первая карта становится основной

        return paymentMethodRepository.save(card);
    }

    public void setPrimaryCard(Long cardId) {
        User user = userService.getCurrentUser();
        paymentMethodRepository.findByIdAndUser(cardId, user)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена"));

        user.getPaymentMethods().forEach(card -> {
            card.setPrimary(card.getId().equals(cardId));
            paymentMethodRepository.save(card);
        });
    }

    public List<PaymentCardDTO> getUserCards() {
        User user = userService.getCurrentUser();
        List<PaymentMethod> cards = paymentMethodRepository.findByUser(user);

        return cards.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCard(Long cardId) {
        User currentUser = userService.getCurrentUser();
        PaymentMethod card = paymentMethodRepository.findByIdAndUser(cardId, currentUser)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена или не принадлежит пользователю"));

        // Нельзя удалить основную карту, если она последняя
        if (card.isPrimary() && paymentMethodRepository.countByUser(currentUser) == 1) {
            throw new LastPrimaryCardException("Нельзя удалить единственную основную карту");
        }

        paymentMethodRepository.delete(card);

        // Если удалили основную карту - назначаем новую основную
        if (card.isPrimary()) {
            paymentMethodRepository.findFirstByUser(currentUser)
                    .ifPresent(newPrimary -> {
                        newPrimary.setPrimary(true);
                        paymentMethodRepository.save(newPrimary);
                    });
        }
    }

    private PaymentCardDTO convertToDto(PaymentMethod card) {
        return PaymentCardDTO.builder()
                .id(card.getId())
                .maskedCardNumber(maskCardNumber(card.getCardNumber()))
                .isPrimary(card.isPrimary())
                .build();
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "****";
        }

        // Удаляем все нецифровые символы
        String cleanNumber = cardNumber.replaceAll("[^0-9]", "");

        if (cleanNumber.length() < 8) {
            return "****" + (cleanNumber.isEmpty() ? "" : " " + cleanNumber);
        }

        // Первые 4 и последние 4 цифры
        String firstFour = cleanNumber.substring(0, 4);
        String lastFour = cleanNumber.substring(cleanNumber.length() - 4);

        // Маскируем среднюю часть (ровно столько символов, сколько нужно)
        int maskedDigits = cleanNumber.length() - 8;
        String maskedMiddle = String.join("", Collections.nCopies(maskedDigits, "*"));

        // Форматируем с пробелами (каждые 4 символа)
        String formatted = firstFour + " " + maskedMiddle + " " + lastFour;

        // Удаляем возможные лишние пробелы
        return formatted.replaceAll(" {2,}", " ").trim();
    }

    public void processPayment(User user, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        PaymentMethod primaryCard = paymentMethodRepository.findByUserAndIsPrimary(user, true)
                .orElseThrow(() -> new PrimaryCardNotFoundException("Основная карта не найдена"));

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("processPaymentTx");
        def.setTimeout(60);
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        transactionService.execute(def, status -> {
            VirtualAccount virtualAccount = virtualAccountService.get(primaryCard.getCardNumber());
            if (virtualAccount.getBalance().compareTo(amount) < 0) {
                throw new NotEnoughMoneyException("Недостаточно денег на балансе");
            }
            virtualAccount.setBalance(virtualAccount.getBalance().subtract(amount));
            virtualAccountService.save(virtualAccount);
            log.info("Withdraw: {} | Card: {} | Balance: {}", amount, primaryCard.getCardNumber(), virtualAccount.getBalance());
            return true;
        });
    }
}

