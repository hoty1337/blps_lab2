package com.djeno.lab1.persistence.repositories;

import com.djeno.lab1.persistence.models.VirtualAccount;
import jakarta.persistence.LockModeType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, String> {

    @NotNull
    @Lock(LockModeType.OPTIMISTIC)
    Optional<VirtualAccount> findById(@NotNull @Param("cardNumber") String cardNumber);
}
