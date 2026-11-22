package com.treasuryhub.customer.repository.company;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.treasuryhub.customer.entity.company.Company;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

    boolean existsByCountryCodeAndRegistrationNumber(String countryCode, String registrationNumber);
}
