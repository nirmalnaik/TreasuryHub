package com.treasuryhub.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.treasuryhub.customer.entity.Company;
import com.treasuryhub.customer.entity.CompanyStatus;
import com.treasuryhub.customer.repository.CompanyRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("docker")
class CompanyIntegrationTest {

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
    private MockMvc mockMvc;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearCompanies() {
        companyRepository.deleteAll();
    }

    @Test
    void flywayCreatesCompaniesAndPreservesExistingMetadata() {
        Integer tableCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name = 'companies'
                """,
                Integer.class);
        assertThat(tableCount).isEqualTo(1);

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT service_name, schema_version FROM service_metadata LIMIT 1");
        assertThat(row.get("service_name")).isEqualTo("customer-service");
        assertThat(row.get("schema_version")).isEqualTo("1");
    }

    @Test
    void postRegistersAndPersistsPendingCompany() throws Exception {
        mockMvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "legalName": "Example Holdings Ltd",
                                  "registrationNumber": "ab-123",
                                  "countryCode": "ca"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern(
                        "/api/v1/companies/[0-9a-f-]+")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.registrationNumber").value("AB-123"))
                .andExpect(jsonPath("$.countryCode").value("CA"))
                .andExpect(jsonPath("$.status").value("PENDING_VERIFICATION"));

        assertThat(companyRepository.findAll()).singleElement().satisfies(company -> {
            assertThat(company.getLegalName()).isEqualTo("Example Holdings Ltd");
            assertThat(company.getStatus()).isEqualTo(CompanyStatus.PENDING_VERIFICATION);
        });
    }

    @Test
    void postRejectsDuplicateRegistrationWithinCountry() throws Exception {
        companyRepository.saveAndFlush(Company.register("First Company", "AB-123", "CA"));

        mockMvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "legalName": "Duplicate Company",
                                  "registrationNumber": "ab-123",
                                  "countryCode": "ca"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("AB-123")));
    }

    @Test
    void postRejectsInvalidFields() throws Exception {
        mockMvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "legalName": " ",
                                  "registrationNumber": "",
                                  "countryCode": "CAN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.legalName").exists())
                .andExpect(jsonPath("$.fieldErrors.registrationNumber").exists())
                .andExpect(jsonPath("$.fieldErrors.countryCode").exists());
    }

    @Test
    void postRejectsClientSuppliedIdentityAndStatus() throws Exception {
        mockMvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "00000000-0000-0000-0000-000000000001",
                                  "legalName": "Example",
                                  "registrationNumber": "AB-123",
                                  "countryCode": "CA",
                                  "status": "VERIFIED"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void databaseEnforcesCountryAndRegistrationUniqueness() {
        companyRepository.saveAndFlush(Company.register("First Company", "AB-123", "CA"));

        assertThatThrownBy(() ->
                companyRepository.saveAndFlush(Company.register("Second Company", "AB-123", "CA")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void sameRegistrationNumberIsAllowedInDifferentCountries() {
        companyRepository.saveAndFlush(Company.register("Canadian Company", "AB-123", "CA"));
        companyRepository.saveAndFlush(Company.register("US Company", "AB-123", "US"));

        assertThat(companyRepository.count()).isEqualTo(2);
    }
}
