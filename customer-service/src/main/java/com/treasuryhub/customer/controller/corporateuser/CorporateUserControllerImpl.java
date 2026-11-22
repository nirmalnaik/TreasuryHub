package com.treasuryhub.customer.controller.corporateuser;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.treasuryhub.customer.dto.common.ApiError;
import com.treasuryhub.customer.dto.corporateuser.CorporateUserResponse;
import com.treasuryhub.customer.dto.corporateuser.CreateCorporateUserRequest;
import com.treasuryhub.customer.service.corporateuser.CorporateUserRegistrationResult;
import com.treasuryhub.customer.service.corporateuser.CorporateUserRegistrationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/users")
@Tag(name = "Corporate Users", description = "Corporate-user administration")
public class CorporateUserControllerImpl implements CorporateUserController {

    private final CorporateUserRegistrationService registrationService;

    public CorporateUserControllerImpl(CorporateUserRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    @Operation(
            summary = "Register a corporate user",
            description = "Creates a USER under an existing company. Access is denied until ADMIN authorization exists.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Corporate user registered",
                    headers = @Header(
                            name = "Location",
                            description = "Relative URI of the registered corporate user",
                            schema = @Schema(type = "string")),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CorporateUserResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed request, unsupported field, or validation failure",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Corporate ADMIN authorization is not implemented"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Company not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already exists within the company",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    @Override
    public ResponseEntity<CorporateUserResponse> register(
            @PathVariable UUID companyId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Corporate-user details. ID, company, and role are server-managed.",
                    content = @Content(schema = @Schema(implementation = CreateCorporateUserRequest.class)))
            @Valid @RequestBody CreateCorporateUserRequest request) {
        CorporateUserRegistrationResult registration = registrationService.register(
                companyId, request.email(), request.password());
        CorporateUserResponse response = CorporateUserResponse.from(registration);

        return ResponseEntity
                .created(URI.create("/api/v1/companies/%s/users/%s"
                        .formatted(companyId, registration.userId())))
                .body(response);
    }
}
