package com.djeno.lab1.persistence.DTO.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "Данные карты для пополнения/снятия денег")
@Data
public class CardOperationData {

    @Schema(description = "Номер карты (16-19 цифр)", example = "4111111111111111")
    @NotBlank
    @Pattern(regexp = "^[0-9]{13,19}$", message = "Номер карты должен содержать 13-19 цифр")
    private String cardNumber;

    @Schema(
            description = "Сумма для пополнения/списания",
            example = "10.0",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    @DecimalMin(value = "1.0")
    private BigDecimal amount;
}
