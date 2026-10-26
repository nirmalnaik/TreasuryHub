package com.treasuryhub.payment.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @Column(name = "debtor_account_id", nullable = false, updatable = false)
    private UUID debtorAccountId;

    @Column(name = "creditor_account_id", nullable = false, updatable = false)
    private UUID creditorAccountId;

    @Column(nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3, updatable = false)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Payment() {
    }

    private Payment(
            UUID id,
            UUID companyId,
            UUID debtorAccountId,
            UUID creditorAccountId,
            BigDecimal amount,
            String currencyCode,
            PaymentStatus status,
            Instant createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.debtorAccountId = debtorAccountId;
        this.creditorAccountId = creditorAccountId;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Payment draft(
            UUID companyId,
            UUID debtorAccountId,
            UUID creditorAccountId,
            BigDecimal amount,
            String currencyCode) {
        Objects.requireNonNull(companyId, "companyId must not be null");
        Objects.requireNonNull(debtorAccountId, "debtorAccountId must not be null");
        Objects.requireNonNull(creditorAccountId, "creditorAccountId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        if (amount.stripTrailingZeros().scale() > 4) {
            throw new IllegalArgumentException("amount must not exceed four decimal places");
        }
        if (currencyCode == null || !currencyCode.strip().matches("[A-Za-z]{3}")) {
            throw new IllegalArgumentException("currencyCode must contain exactly three letters");
        }

        return new Payment(
                UUID.randomUUID(),
                companyId,
                debtorAccountId,
                creditorAccountId,
                amount.setScale(4, RoundingMode.UNNECESSARY),
                currencyCode.strip().toUpperCase(Locale.ROOT),
                PaymentStatus.DRAFT,
                Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public UUID getDebtorAccountId() {
        return debtorAccountId;
    }

    public UUID getCreditorAccountId() {
        return creditorAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
