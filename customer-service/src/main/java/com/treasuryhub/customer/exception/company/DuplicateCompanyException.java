package com.treasuryhub.customer.exception.company;

public class DuplicateCompanyException extends RuntimeException {

    public DuplicateCompanyException(String countryCode, String registrationNumber) {
        super("A company with registrationNumber '%s' already exists in country '%s'"
                .formatted(registrationNumber, countryCode));
    }
}
