package com.djeno.lab1.persistence.repositories;

import com.djeno.lab1.persistence.models.PaymentMethod;
import com.djeno.lab1.persistence.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    List<PaymentMethod> findByUser(User user);
    Optional<PaymentMethod> findByUserAndIsPrimary(User user, boolean isPrimary);
    Optional<PaymentMethod> findByIdAndUser(Long id, User user);

    int countByUser(User user);
    Optional<PaymentMethod> findFirstByUser(User user);

    boolean existsByUserAndCardNumber(User user, String cardNumber);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update PaymentMethod pm set pm.user.id = :unknownId where pm.user.id in :userIds")
    void reassignPaymentMethod(@Param("userIds") List<Long> userIds,
                         @Param("unknownId") Long unknownId);
}
