package com.treasuryhub.account.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Input for opening a basic corporate bank account")
public record CreateAccountRequest(
        @Schema(description = "Company identifier owned by customer-service", format = "uuid",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "companyId is required")
        UUID companyId,

        @Schema(description = "Three-letter currency code", example = "CAD", pattern = "^[A-Za-z]{3}$",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "currencyCode is required")
        @Pattern(regexp = "[A-Za-z]{3}", message = "currencyCode must contain exactly three letters")
        String currencyCode) {
}
