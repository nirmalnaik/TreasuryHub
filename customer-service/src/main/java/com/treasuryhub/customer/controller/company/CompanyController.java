package com.treasuryhub.customer.controller.company;

import org.springframework.http.ResponseEntity;

import com.treasuryhub.customer.dto.company.CompanyResponse;
import com.treasuryhub.customer.dto.company.CreateCompanyRequest;

public interface CompanyController {

    ResponseEntity<CompanyResponse> register(CreateCompanyRequest request);
}
