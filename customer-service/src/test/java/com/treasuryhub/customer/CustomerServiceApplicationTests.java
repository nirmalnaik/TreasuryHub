package com.treasuryhub.customer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.treasuryhub.customer.repository.CompanyRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompanyRepository companyRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void openApiDocumentsCompanyRegistration() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("TreasuryHub Customer API"))
                .andExpect(jsonPath("$.paths['/api/v1/companies'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/companies'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/companies'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/companies'].post.responses['409']").exists());
    }
}
