package com.treasuryhub.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Input for registering a corporate customer")
public record CreateCompanyRequest(
        @Schema(
                description = "Registered legal name of the company",
                example = "TreasuryHub Holdings Ltd",
                maxLength = 200,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "legalName is required")
        @Size(max = 200, message = "legalName must not exceed 200 characters")
        String legalName,

        @Schema(
                description = "Company registration number within the supplied country",
                example = "CA-1234567",
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "registrationNumber is required")
        @Size(max = 100, message = "registrationNumber must not exceed 100 characters")
        String registrationNumber,

        @Schema(
                description = "Two-letter country code",
                example = "CA",
                pattern = "^[A-Za-z]{2}$",
                minLength = 2,
                maxLength = 2,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "countryCode is required")
        @Pattern(regexp = "[A-Za-z]{2}", message = "countryCode must contain exactly two letters")
        String countryCode) {
}
