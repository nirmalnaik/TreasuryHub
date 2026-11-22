package com.treasuryhub.customer.service.company;

import com.treasuryhub.customer.entity.company.Company;

public interface CompanyRegistrationService {

    Company register(String legalName, String registrationNumber, String countryCode);
}
