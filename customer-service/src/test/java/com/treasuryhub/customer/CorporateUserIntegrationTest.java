package com.treasuryhub.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.treasuryhub.customer.entity.Company;
import com.treasuryhub.customer.entity.CorporateUser;
import com.treasuryhub.customer.entity.CorporateUserRole;
import com.treasuryhub.customer.repository.CompanyRepository;
import com.treasuryhub.customer.repository.CorporateUserRepository;

@SpringBootTest
@Testcontainers
@Tag("docker")
class CorporateUserIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("customer_db")
            .withUsername("treasury")
            .withPassword("treasury");

    @DynamicPropertySource
    static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CorporateUserRepository corporateUserRepository;

    @BeforeEach
    void clearData() {
        corporateUserRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void persistsUserLinkedToCompany() {
        Company company = companyRepository.saveAndFlush(Company.register("Example Ltd", "REG-1", "CA"));

        CorporateUser saved = corporateUserRepository.saveAndFlush(CorporateUser.create(
                company, "ADMIN@EXAMPLE.COM", "stored-password-hash", CorporateUserRole.ADMIN));

        CorporateUser reloaded = corporateUserRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getCompany().getId()).isEqualTo(company.getId());
        assertThat(reloaded.getEmail()).isEqualTo("admin@example.com");
        assertThat(reloaded.getRole()).isEqualTo(CorporateUserRole.ADMIN);
    }

    @Test
    void databaseRejectsDuplicateEmailWithinCompany() {
        Company company = companyRepository.saveAndFlush(Company.register("Example Ltd", "REG-1", "CA"));
        corporateUserRepository.saveAndFlush(CorporateUser.create(
                company, "user@example.com", "hash-one", CorporateUserRole.USER));

        assertThatThrownBy(() -> corporateUserRepository.saveAndFlush(CorporateUser.create(
                company, "USER@EXAMPLE.COM", "hash-two", CorporateUserRole.ADMIN)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
