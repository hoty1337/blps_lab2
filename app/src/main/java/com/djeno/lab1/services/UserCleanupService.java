package com.djeno.lab1.services;

import com.djeno.lab1.persistence.models.User;
import com.djeno.lab1.persistence.repositories.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCleanupService {
    private final UserRepository userRepository;
    private final AppRepository appRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PurchaseRepository purchaseRepository;

    private static final Long UNKNOWN_USER = 1L;
    private final ReviewRepository reviewRepository;


    @Transactional
    public int deleteInactiveUsers(int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);

        List<User> inactive = userRepository.findInactiveUsers(threshold);
        if(inactive.isEmpty()) {
            return 0;
        }

        List<Long> ids = inactive.stream()
                .map(User::getId)
                .toList();

        appRepository.reassignAppOwner(ids, UNKNOWN_USER);
        paymentMethodRepository.reassignPaymentMethod(ids, UNKNOWN_USER);
        reviewRepository.reassignReview(ids, UNKNOWN_USER);
        purchaseRepository.deleteByUserIdIn(ids);

        userRepository.deleteAllByIdInBatch(ids);
        return ids.size();
    }
}
