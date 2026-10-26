package com.treasuryhub.customer.entity;

import java.util.Locale;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "companies")
public class Company {

    static final int MAX_LEGAL_NAME_LENGTH = 200;
    static final int MAX_REGISTRATION_NUMBER_LENGTH = 100;

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "legal_name", nullable = false, length = MAX_LEGAL_NAME_LENGTH)
    private String legalName;

    @Column(name = "registration_number", nullable = false, length = MAX_REGISTRATION_NUMBER_LENGTH)
    private String registrationNumber;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CompanyStatus status;

    protected Company() {
    }

    private Company(UUID id, String legalName, String registrationNumber, String countryCode,
                    CompanyStatus status) {
        this.id = id;
        this.legalName = legalName;
        this.registrationNumber = registrationNumber;
        this.countryCode = countryCode;
        this.status = status;
    }

    public static Company register(String legalName, String registrationNumber, String countryCode) {
        String normalizedLegalName = requireText(legalName, "legalName", MAX_LEGAL_NAME_LENGTH);
        String normalizedRegistrationNumber = requireText(
                registrationNumber, "registrationNumber", MAX_REGISTRATION_NUMBER_LENGTH)
                .toUpperCase(Locale.ROOT);
        String normalizedCountryCode = requireText(countryCode, "countryCode", 2)
                .toUpperCase(Locale.ROOT);

        if (!normalizedCountryCode.matches("[A-Z]{2}")) {
            throw new IllegalArgumentException("countryCode must contain exactly two letters");
        }

        return new Company(
                UUID.randomUUID(),
                normalizedLegalName,
                normalizedRegistrationNumber,
                normalizedCountryCode,
                CompanyStatus.PENDING_VERIFICATION);
    }

    private static String requireText(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        String normalized = value.strip();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must not exceed " + maxLength + " characters");
        }
        return normalized;
    }

    public UUID getId() {
        return id;
    }

    public String getLegalName() {
        return legalName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public CompanyStatus getStatus() {
        return status;
    }
}
