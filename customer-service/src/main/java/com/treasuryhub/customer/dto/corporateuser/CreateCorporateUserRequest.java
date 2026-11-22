package com.treasuryhub.customer.dto.corporateuser;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Input for registering a corporate user")
public record CreateCorporateUserRequest(
        @Schema(
                description = "Corporate user's email address",
                example = "user@example.com",
                maxLength = 320,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        @Size(max = 320, message = "email must not exceed 320 characters")
        String email,

        @Schema(
                description = "Plaintext password. It is accepted only as input and stored as a BCrypt hash.",
                format = "password",
                maxLength = 72,
                accessMode = Schema.AccessMode.WRITE_ONLY,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @NotBlank(message = "password is required")
        @Size(max = 72, message = "password must not exceed 72 characters")
        String password) {
}
