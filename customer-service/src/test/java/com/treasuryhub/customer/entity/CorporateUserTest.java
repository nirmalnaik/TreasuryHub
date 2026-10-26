package com.treasuryhub.customer.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.Test;

class CorporateUserTest {

    private final Company company = Company.register("Example Ltd", "REG-1", "CA");

    @Test
    void createsUserWithNormalizedEmailAndAssignedRole() {
        CorporateUser user = CorporateUser.create(
                company, " Admin@Example.COM ", "$2a$10$already-hashed", CorporateUserRole.ADMIN);

        assertThat(user.getId()).isNotNull();
        assertThat(user.getCompany()).isSameAs(company);
        assertThat(user.getEmail()).isEqualTo("admin@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("$2a$10$already-hashed");
        assertThat(user.getRole()).isEqualTo(CorporateUserRole.ADMIN);
    }

    @Test
    void rejectsInvalidEmail() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> CorporateUser.create(
                        company, "not-an-email", "hash", CorporateUserRole.USER))
                .withMessage("email must be valid");
    }

    @Test
    void requiresCompanyAndRole() {
        assertThatNullPointerException()
                .isThrownBy(() -> CorporateUser.create(null, "user@example.com", "hash", CorporateUserRole.USER));
        assertThatNullPointerException()
                .isThrownBy(() -> CorporateUser.create(company, "user@example.com", "hash", null));
    }
}
