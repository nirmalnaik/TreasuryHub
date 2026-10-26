package com.treasuryhub.account.exception;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(UUID accountId) {
        super("Bank account '%s' was not found".formatted(accountId));
    }
}
