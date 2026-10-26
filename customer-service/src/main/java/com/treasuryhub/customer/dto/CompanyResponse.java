package com.treasuryhub.customer.dto;

import java.util.UUID;

import com.treasuryhub.customer.entity.Company;
import com.treasuryhub.customer.entity.CompanyStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Registered corporate customer")
public record CompanyResponse(
        @Schema(description = "Server-generated company identifier", format = "uuid")
        UUID id,

        @Schema(description = "Registered legal name", example = "TreasuryHub Holdings Ltd")
        String legalName,

        @Schema(description = "Normalized company registration number", example = "CA-1234567")
        String registrationNumber,

        @Schema(description = "Normalized two-letter country code", example = "CA")
        String countryCode,

        @Schema(
                description = "Current onboarding status",
                example = "PENDING_VERIFICATION",
                allowableValues = "PENDING_VERIFICATION")
        CompanyStatus status) {

    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getLegalName(),
                company.getRegistrationNumber(),
                company.getCountryCode(),
                company.getStatus());
    }
}
