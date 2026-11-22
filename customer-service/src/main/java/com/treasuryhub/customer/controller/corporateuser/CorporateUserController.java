package com.treasuryhub.customer.controller.corporateuser;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.treasuryhub.customer.dto.corporateuser.CorporateUserResponse;
import com.treasuryhub.customer.dto.corporateuser.CreateCorporateUserRequest;

public interface CorporateUserController {

    ResponseEntity<CorporateUserResponse> register(
            UUID companyId, CreateCorporateUserRequest request);
}
