package com.treasuryhub.customer.repository.corporateuser;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.treasuryhub.customer.entity.corporateuser.CorporateUser;

public interface CorporateUserRepository extends JpaRepository<CorporateUser, UUID> {

    boolean existsByCompanyIdAndEmail(UUID companyId, String email);
}
