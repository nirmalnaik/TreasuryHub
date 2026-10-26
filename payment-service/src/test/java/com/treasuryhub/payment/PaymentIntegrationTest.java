package com.treasuryhub.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.treasuryhub.payment.entity.Payment;
import com.treasuryhub.payment.entity.PaymentStatus;
import com.treasuryhub.payment.repository.PaymentRepository;

@SpringBootTest
@Testcontainers
@Tag("docker")
class PaymentIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("payment_db")
            .withUsername("treasury")
            .withPassword("treasury");

    @DynamicPropertySource
    static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.autoconfigure.exclude", () ->
                "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration");
    }

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearPayments() {
        paymentRepository.deleteAll();
    }

    @Test
    void persistsDraftPaymentReferencesWithoutCrossServiceForeignKeys() {
        Payment saved = paymentRepository.saveAndFlush(Payment.draft(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("42.25"),
                "usd"));

        Payment reloaded = paymentRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getAmount()).isEqualByComparingTo("42.2500");
        assertThat(reloaded.getCurrencyCode()).isEqualTo("USD");
        assertThat(reloaded.getStatus()).isEqualTo(PaymentStatus.DRAFT);
    }

    @Test
    void databaseRejectsNonPositiveAmount() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                """
                INSERT INTO payments (
                    id, company_id, debtor_account_id, creditor_account_id,
                    amount, currency_code, status, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.ZERO, "CAD", "DRAFT"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
