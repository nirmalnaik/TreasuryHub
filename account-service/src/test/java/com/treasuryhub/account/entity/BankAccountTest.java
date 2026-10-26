package com.treasuryhub.account.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class BankAccountTest {

    @Test
    void opensAccountWithGeneratedIdentityAndNormalizedCurrency() {
        UUID companyId = UUID.randomUUID();

        BankAccount account = BankAccount.open(companyId, " cad ");

        assertThat(account.getId()).isNotNull();
        assertThat(account.getCompanyId()).isEqualTo(companyId);
        assertThat(account.getCurrencyCode()).isEqualTo("CAD");
        assertThat(account.getCreatedAt()).isNotNull();
    }

    @Test
    void rejectsMissingCompany() {
        assertThatNullPointerException()
                .isThrownBy(() -> BankAccount.open(null, "CAD"))
                .withMessage("companyId must not be null");
    }

    @Test
    void rejectsInvalidCurrency() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> BankAccount.open(UUID.randomUUID(), "CA"))
                .withMessage("currencyCode must contain exactly three letters");
    }
}
