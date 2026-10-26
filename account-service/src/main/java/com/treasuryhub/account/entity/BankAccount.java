package com.treasuryhub.account.entity;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bank_accounts")
public class BankAccount {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @Column(name = "currency_code", nullable = false, length = 3, updatable = false)
    private String currencyCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected BankAccount() {
    }

    private BankAccount(UUID id, UUID companyId, String currencyCode, Instant createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.currencyCode = currencyCode;
        this.createdAt = createdAt;
    }

    public static BankAccount open(UUID companyId, String currencyCode) {
        Objects.requireNonNull(companyId, "companyId must not be null");
        if (currencyCode == null || !currencyCode.strip().matches("[A-Za-z]{3}")) {
            throw new IllegalArgumentException("currencyCode must contain exactly three letters");
        }

        return new BankAccount(
                UUID.randomUUID(),
                companyId,
                currencyCode.strip().toUpperCase(Locale.ROOT),
                Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
