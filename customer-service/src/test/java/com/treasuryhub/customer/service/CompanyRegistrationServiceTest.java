package com.treasuryhub.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.treasuryhub.customer.entity.Company;
import com.treasuryhub.customer.entity.CompanyStatus;
import com.treasuryhub.customer.exception.DuplicateCompanyException;
import com.treasuryhub.customer.repository.CompanyRepository;

@ExtendWith(MockitoExtension.class)
class CompanyRegistrationServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyRegistrationService service;

    @Test
    void registersAUniqueCompany() {
        when(companyRepository.existsByCountryCodeAndRegistrationNumber("CA", "AB-123")).thenReturn(false);
        when(companyRepository.saveAndFlush(any(Company.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Company company = service.register("Example", "ab-123", "ca");

        assertThat(company.getStatus()).isEqualTo(CompanyStatus.PENDING_VERIFICATION);
        verify(companyRepository).saveAndFlush(company);
    }

    @Test
    void rejectsAnExistingRegistrationInTheSameCountry() {
        when(companyRepository.existsByCountryCodeAndRegistrationNumber("CA", "AB-123")).thenReturn(true);

        assertThatThrownBy(() -> service.register("Example", "ab-123", "ca"))
                .isInstanceOf(DuplicateCompanyException.class)
                .hasMessageContaining("AB-123")
                .hasMessageContaining("CA");

        verify(companyRepository, never()).saveAndFlush(any());
    }
}
