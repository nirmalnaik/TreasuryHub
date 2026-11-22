package com.treasuryhub.customer.service.corporateuser.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.treasuryhub.customer.entity.company.Company;
import com.treasuryhub.customer.entity.corporateuser.CorporateUser;
import com.treasuryhub.customer.entity.corporateuser.CorporateUserRole;
import com.treasuryhub.customer.exception.company.CompanyNotFoundException;
import com.treasuryhub.customer.exception.corporateuser.DuplicateCorporateUserException;
import com.treasuryhub.customer.repository.company.CompanyRepository;
import com.treasuryhub.customer.repository.corporateuser.CorporateUserRepository;
import com.treasuryhub.customer.service.corporateuser.CorporateUserRegistrationResult;
import com.treasuryhub.customer.service.corporateuser.CorporateUserRegistrationServiceImpl;

@ExtendWith(MockitoExtension.class)
class CorporateUserRegistrationServiceImplTest {

    private static final String PLAINTEXT_PASSWORD = "a-valid-password";

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CorporateUserRepository corporateUserRepository;

    private PasswordEncoder passwordEncoder;
    private CorporateUserRegistrationServiceImpl service;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        service = new CorporateUserRegistrationServiceImpl(
                companyRepository, corporateUserRepository, passwordEncoder);
    }

    @Test
    void registersUserWithNormalizedEmailHashedPasswordAndServerControlledRole() {
        Company company = Company.register("Example Ltd", "REG-1", "CA");
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));
        when(corporateUserRepository.existsByCompanyIdAndEmail(
                company.getId(), "user@example.com")).thenReturn(false);
        when(corporateUserRepository.saveAndFlush(any(CorporateUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CorporateUserRegistrationResult registration = service.register(
                company.getId(), " User@Example.COM ", PLAINTEXT_PASSWORD);

        ArgumentCaptor<CorporateUser> userCaptor = ArgumentCaptor.forClass(CorporateUser.class);
        verify(corporateUserRepository).saveAndFlush(userCaptor.capture());
        CorporateUser savedUser = userCaptor.getValue();

        assertThat(registration.userId()).isEqualTo(savedUser.getId());
        assertThat(registration.companyId()).isEqualTo(company.getId());
        assertThat(registration.email()).isEqualTo("user@example.com");
        assertThat(registration.role()).isEqualTo(CorporateUserRole.USER);
        assertThat(savedUser.getCompany()).isSameAs(company);
        assertThat(savedUser.getEmail()).isEqualTo("user@example.com");
        assertThat(savedUser.getRole()).isEqualTo(CorporateUserRole.USER);
        assertThat(savedUser.getPasswordHash()).isNotEqualTo(PLAINTEXT_PASSWORD);
        assertThat(passwordEncoder.matches(PLAINTEXT_PASSWORD, savedUser.getPasswordHash())).isTrue();
    }

    @Test
    void rejectsDuplicateNormalizedEmailWithinCompany() {
        Company company = Company.register("Example Ltd", "REG-1", "CA");
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));
        when(corporateUserRepository.existsByCompanyIdAndEmail(
                company.getId(), "user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(
                company.getId(), " USER@example.com ", PLAINTEXT_PASSWORD))
                .isInstanceOf(DuplicateCorporateUserException.class)
                .hasMessageContaining(company.getId().toString());

        verify(corporateUserRepository, never()).saveAndFlush(any());
    }

    @Test
    void translatesDuplicateEmailDetectedDuringSave() {
        Company company = Company.register("Example Ltd", "REG-1", "CA");
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));
        when(corporateUserRepository.existsByCompanyIdAndEmail(
                company.getId(), "user@example.com")).thenReturn(false);
        SQLException uniqueViolation = new SQLException(
                "duplicate key violates uk_corporate_users_company_email", "23505");
        when(corporateUserRepository.saveAndFlush(any(CorporateUser.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate user", uniqueViolation));

        assertThatThrownBy(() -> service.register(
                company.getId(), "user@example.com", PLAINTEXT_PASSWORD))
                .isInstanceOf(DuplicateCorporateUserException.class)
                .hasMessageContaining(company.getId().toString());
    }

    @Test
    void rejectsUnknownCompany() {
        UUID companyId = UUID.randomUUID();
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.register(
                companyId, "user@example.com", PLAINTEXT_PASSWORD))
                .isInstanceOf(CompanyNotFoundException.class)
                .hasMessageContaining(companyId.toString());

        verifyNoInteractions(corporateUserRepository);
    }

    @Test
    void rejectsInvalidInputBeforePersistence() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.register(null, "user@example.com", PLAINTEXT_PASSWORD))
                .withMessage("companyId must not be null");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.register(UUID.randomUUID(), "not-an-email", PLAINTEXT_PASSWORD))
                .withMessage("email must be valid");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.register(UUID.randomUUID(), "user@example.com", "   "))
                .withMessage("password must not be blank");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.register(UUID.randomUUID(), "user@example.com", "x".repeat(73)))
                .withMessage("password must not exceed 72 UTF-8 bytes");

        verifyNoInteractions(companyRepository, corporateUserRepository);
    }
}
