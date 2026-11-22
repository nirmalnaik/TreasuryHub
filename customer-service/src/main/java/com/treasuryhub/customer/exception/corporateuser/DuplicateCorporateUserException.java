package com.treasuryhub.customer.exception.corporateuser;

import java.util.UUID;

public class DuplicateCorporateUserException extends RuntimeException {

    public DuplicateCorporateUserException(UUID companyId) {
        super("A corporate user with this email already exists for company '%s'".formatted(companyId));
    }
}
