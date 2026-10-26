package com.treasuryhub.customer.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.treasuryhub.customer.dto.ApiError;
import com.treasuryhub.customer.dto.CompanyResponse;
import com.treasuryhub.customer.dto.CreateCompanyRequest;
import com.treasuryhub.customer.entity.Company;
import com.treasuryhub.customer.service.CompanyRegistrationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/companies")
@Tag(name = "Companies", description = "Corporate customer registration")
public class CompanyController {

    private final CompanyRegistrationService companyRegistrationService;

    public CompanyController(CompanyRegistrationService companyRegistrationService) {
        this.companyRegistrationService = companyRegistrationService;
    }

    @PostMapping
    @Operation(
            summary = "Register a corporate customer",
            description = "Creates a company in PENDING_VERIFICATION status. Registration does not approve KYC.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Company registered",
                    headers = @Header(
                            name = "Location",
                            description = "Relative URI of the registered company",
                            schema = @Schema(type = "string")),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CompanyResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed request, unsupported field, or validation failure",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "Registration number already exists in the supplied country",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<CompanyResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Company registration details. ID and status are server-managed.",
                    content = @Content(schema = @Schema(implementation = CreateCompanyRequest.class)))
            @Valid @RequestBody CreateCompanyRequest request) {
        Company company = companyRegistrationService.register(
                request.legalName(), request.registrationNumber(), request.countryCode());
        CompanyResponse response = CompanyResponse.from(company);

        return ResponseEntity
                .created(URI.create("/api/v1/companies/" + company.getId()))
                .body(response);
    }
}
