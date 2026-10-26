package com.treasuryhub.customer.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.treasuryhub.customer.entity.Company;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

    boolean existsByCountryCodeAndRegistrationNumber(String countryCode, String registrationNumber);
}
