package com.treasuryhub.customer.entity;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "corporate_users",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_corporate_users_company_email",
                columnNames = {"company_id", "email"}))
public class CorporateUser {

    private static final int MAX_EMAIL_LENGTH = 320;
    private static final int MAX_PASSWORD_HASH_LENGTH = 255;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, updatable = false)
    private Company company;

    @Column(nullable = false, length = MAX_EMAIL_LENGTH)
    private String email;

    @Column(name = "password_hash", nullable = false, length = MAX_PASSWORD_HASH_LENGTH)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CorporateUserRole role;

    protected CorporateUser() {
    }

    private CorporateUser(UUID id, Company company, String email, String passwordHash, CorporateUserRole role) {
        this.id = id;
        this.company = company;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public static CorporateUser create(
            Company company, String email, String passwordHash, CorporateUserRole role) {
        Objects.requireNonNull(company, "company must not be null");
        Objects.requireNonNull(role, "role must not be null");

        String normalizedEmail = requireText(email, "email", MAX_EMAIL_LENGTH).toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new IllegalArgumentException("email must be valid");
        }

        return new CorporateUser(
                UUID.randomUUID(),
                company,
                normalizedEmail,
                requireText(passwordHash, "passwordHash", MAX_PASSWORD_HASH_LENGTH),
                role);
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

    public Company getCompany() {
        return company;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public CorporateUserRole getRole() {
        return role;
    }
}
