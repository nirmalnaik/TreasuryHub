package com.treasuryhub.customer.exception.company;

import java.util.UUID;

public class CompanyNotFoundException extends RuntimeException {

    public CompanyNotFoundException(UUID companyId) {
        super("Company '%s' does not exist".formatted(companyId));
    }
}
