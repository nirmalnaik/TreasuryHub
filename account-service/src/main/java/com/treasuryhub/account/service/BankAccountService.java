package com.treasuryhub.account.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.treasuryhub.account.entity.BankAccount;
import com.treasuryhub.account.exception.AccountNotFoundException;
import com.treasuryhub.account.repository.BankAccountRepository;

@Service
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;

    public BankAccountService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    @Transactional
    public BankAccount create(UUID companyId, String currencyCode) {
        return bankAccountRepository.saveAndFlush(BankAccount.open(companyId, currencyCode));
    }

    @Transactional(readOnly = true)
    public BankAccount get(UUID accountId) {
        return bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }
}
