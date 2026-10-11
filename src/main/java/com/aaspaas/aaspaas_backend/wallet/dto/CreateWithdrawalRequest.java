package com.aaspaas.aaspaas_backend.wallet.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateWithdrawalRequest {

    @NotNull(message = "Bank account ID is required")
    private Long bankAccountId;
}
