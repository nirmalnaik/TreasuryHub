package com.treasuryhub.account.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.treasuryhub.account.entity.BankAccount;

public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {
}
