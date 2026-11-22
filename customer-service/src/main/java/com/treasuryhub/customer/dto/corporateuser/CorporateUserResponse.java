package com.treasuryhub.customer.dto.corporateuser;

import java.util.UUID;

import com.treasuryhub.customer.entity.corporateuser.CorporateUserRole;
import com.treasuryhub.customer.service.corporateuser.CorporateUserRegistrationResult;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Registered corporate user")
public record CorporateUserResponse(
        @Schema(description = "Server-generated corporate-user identifier", format = "uuid")
        UUID userId,

        @Schema(description = "Company that owns the corporate user", format = "uuid")
        UUID companyId,

        @Schema(description = "Normalized corporate-user email", example = "user@example.com")
        String email,

        @Schema(description = "Server-assigned role", example = "USER", allowableValues = "USER")
        CorporateUserRole role) {

    public static CorporateUserResponse from(CorporateUserRegistrationResult registration) {
        return new CorporateUserResponse(
                registration.userId(),
                registration.companyId(),
                registration.email(),
                registration.role());
    }
}
