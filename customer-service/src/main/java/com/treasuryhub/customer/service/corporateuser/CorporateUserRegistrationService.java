package com.treasuryhub.customer.service.corporateuser;

import java.util.UUID;

public interface CorporateUserRegistrationService {

    CorporateUserRegistrationResult register(
            UUID companyId, String email, String plaintextPassword);
}
