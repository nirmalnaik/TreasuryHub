package com.treasuryhub.account.dto;

import java.time.Instant;
import java.util.UUID;

import com.treasuryhub.account.entity.BankAccount;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Corporate bank account")
public record AccountResponse(
        @Schema(description = "Server-generated account identifier", format = "uuid")
        UUID id,
        @Schema(description = "Owning company identifier", format = "uuid")
        UUID companyId,
        @Schema(description = "Normalized three-letter currency code", example = "CAD")
        String currencyCode,
        @Schema(description = "Account creation time", format = "date-time")
        Instant createdAt) {

    public static AccountResponse from(BankAccount account) {
        return new AccountResponse(
                account.getId(), account.getCompanyId(), account.getCurrencyCode(), account.getCreatedAt());
    }
}
