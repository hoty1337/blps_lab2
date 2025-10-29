package com.djeno.lab1.persistence.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "virtual_account")
public class VirtualAccount {
    @Id
    @Column(unique = true, nullable = false)
    private String cardNumber;

    @Min(value = 0, message = "Balance must be above zero")
    @Column(nullable = false)
    private BigDecimal balance;

    @Version
    private Integer version;

    @PrePersist
    public void prePersist() {
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }
    }

}
