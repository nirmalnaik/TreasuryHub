package com.treasuryhub.customer;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.treasuryhub.customer.repository.company.CompanyRepository;
import com.treasuryhub.customer.repository.corporateuser.CorporateUserRepository;
import com.treasuryhub.customer.service.corporateuser.CorporateUserRegistrationService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompanyRepository companyRepository;

    @MockitoBean
    private CorporateUserRepository corporateUserRepository;

    @MockitoBean
    private CorporateUserRegistrationService corporateUserRegistrationService;

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

    @Test
    void openApiDocumentsCorporateUserRegistration() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/companies/{companyId}/users'].post").exists())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/companies/{companyId}/users'].post.responses['201']").exists())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/companies/{companyId}/users'].post.responses['403']").exists())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/companies/{companyId}/users'].post.responses['404']").exists())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/companies/{companyId}/users'].post.responses['409']").exists());
    }

    @Test
    void corporateUserRegistrationIsDeniedUntilAdminAuthorizationExists() throws Exception {
        UUID companyId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/companies/{companyId}/users", companyId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "a-valid-password"
                                }
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(corporateUserRegistrationService);
    }
}
