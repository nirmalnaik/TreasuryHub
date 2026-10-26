package com.treasuryhub.customer.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class CompanyTest {

    @Test
    void registrationGeneratesIdentityNormalizesFieldsAndStartsPending() {
        Company company = Company.register("  Example Holdings Ltd  ", "  ab-123  ", " ca ");

        assertThat(company.getId()).isNotNull();
        assertThat(company.getLegalName()).isEqualTo("Example Holdings Ltd");
        assertThat(company.getRegistrationNumber()).isEqualTo("AB-123");
        assertThat(company.getCountryCode()).isEqualTo("CA");
        assertThat(company.getStatus()).isEqualTo(CompanyStatus.PENDING_VERIFICATION);
    }

    @Test
    void registrationRejectsInvalidCountryCode() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Company.register("Example", "123", "CAN"))
                .withMessage("countryCode must not exceed 2 characters");
    }

    @Test
    void registrationRejectsBlankFields() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Company.register(" ", "123", "CA"))
                .withMessage("legalName must not be blank");
    }
}
