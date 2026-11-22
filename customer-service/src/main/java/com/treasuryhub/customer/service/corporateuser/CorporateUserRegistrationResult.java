package com.treasuryhub.customer.service.corporateuser;

import java.util.UUID;

import com.treasuryhub.customer.entity.corporateuser.CorporateUser;
import com.treasuryhub.customer.entity.corporateuser.CorporateUserRole;

public record CorporateUserRegistrationResult(
        UUID userId,
        UUID companyId,
        String email,
        CorporateUserRole role) {

    public static CorporateUserRegistrationResult from(CorporateUser corporateUser) {
        return new CorporateUserRegistrationResult(
                corporateUser.getId(),
                corporateUser.getCompany().getId(),
                corporateUser.getEmail(),
                corporateUser.getRole());
    }
}
