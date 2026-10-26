package com.treasuryhub.payment.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class PaymentTest {

    @Test
    void createsDraftWithNormalizedMoneyFields() {
        UUID companyId = UUID.randomUUID();
        UUID debtorAccountId = UUID.randomUUID();
        UUID creditorAccountId = UUID.randomUUID();

        Payment payment = Payment.draft(
                companyId, debtorAccountId, creditorAccountId, new BigDecimal("125.50"), " cad ");

        assertThat(payment.getId()).isNotNull();
        assertThat(payment.getCompanyId()).isEqualTo(companyId);
        assertThat(payment.getDebtorAccountId()).isEqualTo(debtorAccountId);
        assertThat(payment.getCreditorAccountId()).isEqualTo(creditorAccountId);
        assertThat(payment.getAmount()).isEqualByComparingTo("125.5000");
        assertThat(payment.getCurrencyCode()).isEqualTo("CAD");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.DRAFT);
        assertThat(payment.getCreatedAt()).isNotNull();
    }

    @Test
    void rejectsNonPositiveAmount() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Payment.draft(
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO, "CAD"))
                .withMessage("amount must be greater than zero");
    }

    @Test
    void rejectsUnsupportedAmountScale() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Payment.draft(
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                        new BigDecimal("1.00001"), "CAD"))
                .withMessage("amount must not exceed four decimal places");
    }
}
