package com.treasuryhub.customer.service;

import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.treasuryhub.customer.entity.Company;
import com.treasuryhub.customer.exception.DuplicateCompanyException;
import com.treasuryhub.customer.repository.CompanyRepository;

@Service
public class CompanyRegistrationService {

    private static final String UNIQUE_REGISTRATION_CONSTRAINT = "uk_companies_country_registration";

    private final CompanyRepository companyRepository;

    public CompanyRegistrationService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional
    public Company register(String legalName, String registrationNumber, String countryCode) {
        Company company = Company.register(legalName, registrationNumber, countryCode);

        if (companyRepository.existsByCountryCodeAndRegistrationNumber(
                company.getCountryCode(), company.getRegistrationNumber())) {
            throw duplicate(company);
        }

        try {
            return companyRepository.saveAndFlush(company);
        } catch (DataIntegrityViolationException exception) {
            if (causedByDuplicateRegistration(exception)) {
                throw duplicate(company);
            }
            throw exception;
        }
    }

    private DuplicateCompanyException duplicate(Company company) {
        return new DuplicateCompanyException(company.getCountryCode(), company.getRegistrationNumber());
    }

    private boolean causedByDuplicateRegistration(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolation
                    && UNIQUE_REGISTRATION_CONSTRAINT.equalsIgnoreCase(constraintViolation.getConstraintName())) {
                return true;
            }
            if (cause instanceof SQLException sqlException
                    && "23505".equals(sqlException.getSQLState())
                    && sqlException.getMessage() != null
                    && sqlException.getMessage().contains(UNIQUE_REGISTRATION_CONSTRAINT)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
