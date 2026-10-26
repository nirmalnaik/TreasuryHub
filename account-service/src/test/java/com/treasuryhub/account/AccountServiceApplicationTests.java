package com.treasuryhub.account;

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

import com.treasuryhub.account.repository.BankAccountRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BankAccountRepository bankAccountRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void openApiDocumentsAccountEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/accounts'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/accounts/{accountId}'].get").exists());
    }
}
