package com.treasuryhub.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.treasuryhub.account.entity.BankAccount;
import com.treasuryhub.account.exception.AccountNotFoundException;
import com.treasuryhub.account.repository.BankAccountRepository;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {

    @Mock
    private BankAccountRepository repository;

    @InjectMocks
    private BankAccountService service;

    @Test
    void createsAccount() {
        UUID companyId = UUID.randomUUID();
        when(repository.saveAndFlush(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BankAccount account = service.create(companyId, "usd");

        assertThat(account.getCompanyId()).isEqualTo(companyId);
        assertThat(account.getCurrencyCode()).isEqualTo("USD");
        verify(repository).saveAndFlush(account);
    }

    @Test
    void getsExistingAccount() {
        BankAccount account = BankAccount.open(UUID.randomUUID(), "CAD");
        when(repository.findById(account.getId())).thenReturn(Optional.of(account));

        assertThat(service.get(account.getId())).isSameAs(account);
    }

    @Test
    void rejectsUnknownAccount() {
        UUID accountId = UUID.randomUUID();
        when(repository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(accountId))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining(accountId.toString());
    }
}
