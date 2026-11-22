package com.treasuryhub.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.resources.Resource;

@SpringBootTest(properties = {
        "treasuryhub.api-docs.ui-origin=http://localhost:8091",
        "management.endpoints.web.exposure.include=health,info,prometheus"
})
@AutoConfigureObservability
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Resource openTelemetryResource;

    @Test
    void contextLoads() {
    }

    @Test
    void prometheusEndpointExposesMetricsWithPaymentServiceIdentity() throws Exception {
        String metrics = mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(openTelemetryResource.getAttribute(AttributeKey.stringKey("service.name")))
                .isEqualTo("payment-service");
        assertThat(metrics)
                .contains("jvm_memory_used_bytes")
                .contains("application=\"payment-service\"");
    }

    @Test
    void openApiIsAvailableWithoutInventingPaymentEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("TreasuryHub Payment API"))
                .andExpect(jsonPath("$.paths").isEmpty());
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
