package com.treasuryhub.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.treasuryhub.account.repository.BankAccountRepository;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.resources.Resource;

@SpringBootTest(properties = {
        "treasuryhub.api-docs.ui-origin=http://localhost:8091",
        "management.endpoints.web.exposure.include=health,info,prometheus"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Resource openTelemetryResource;

    @MockitoBean
    private BankAccountRepository bankAccountRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void prometheusEndpointExposesMetricsWithAccountServiceIdentity() throws Exception {
        String metrics = mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(openTelemetryResource.getAttribute(AttributeKey.stringKey("service.name")))
                .isEqualTo("account-service");
        assertThat(metrics)
                .contains("jvm_memory_used_bytes")
                .contains("application=\"account-service\"");
    }

    @Test
    void openApiDocumentsAccountEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/accounts'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/accounts'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/accounts'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/accounts/{accountId}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/accounts/{accountId}'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/accounts/{accountId}'].get.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/accounts/{accountId}'].get.responses['404']").exists());
    }

    @Test
    void openApiAllowsConfiguredCentralCatalogOrigin() throws Exception {
        mockMvc.perform(get("/v3/api-docs")
                        .header(HttpHeaders.ORIGIN, "http://localhost:8091"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:8091"));
    }
}
