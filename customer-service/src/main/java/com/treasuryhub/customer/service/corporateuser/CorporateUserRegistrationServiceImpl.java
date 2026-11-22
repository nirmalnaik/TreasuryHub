package com.treasuryhub.customer.service.corporateuser;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.UUID;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.treasuryhub.customer.entity.company.Company;
import com.treasuryhub.customer.entity.corporateuser.CorporateUser;
import com.treasuryhub.customer.entity.corporateuser.CorporateUserRole;
import com.treasuryhub.customer.exception.company.CompanyNotFoundException;
import com.treasuryhub.customer.exception.corporateuser.DuplicateCorporateUserException;
import com.treasuryhub.customer.repository.company.CompanyRepository;
import com.treasuryhub.customer.repository.corporateuser.CorporateUserRepository;

import io.micrometer.observation.annotation.Observed;

@Service
public class CorporateUserRegistrationServiceImpl implements CorporateUserRegistrationService {

    private static final int MAX_BCRYPT_PASSWORD_BYTES = 72;
    private static final String UNIQUE_COMPANY_EMAIL_CONSTRAINT = "uk_corporate_users_company_email";

    private final CompanyRepository companyRepository;
    private final CorporateUserRepository corporateUserRepository;
    private final PasswordEncoder passwordEncoder;

    public CorporateUserRegistrationServiceImpl(
            CompanyRepository companyRepository,
            CorporateUserRepository corporateUserRepository,
            PasswordEncoder passwordEncoder) {
        this.companyRepository = companyRepository;
        this.corporateUserRepository = corporateUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Observed(name = "treasuryhub.corporate.user.registration", contextualName = "register-corporate-user")
    @Override
    public CorporateUserRegistrationResult register(UUID companyId, String email, String plaintextPassword) {
        if (companyId == null) {
            throw new IllegalArgumentException("companyId must not be null");
        }

        String normalizedEmail = CorporateUser.normalizeEmail(email);
        validatePassword(plaintextPassword);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException(companyId));

        if (corporateUserRepository.existsByCompanyIdAndEmail(companyId, normalizedEmail)) {
            throw duplicate(companyId);
        }

        CorporateUser corporateUser = CorporateUser.create(
                company,
                normalizedEmail,
                passwordEncoder.encode(plaintextPassword),
                CorporateUserRole.USER);

        try {
            CorporateUser savedUser = corporateUserRepository.saveAndFlush(corporateUser);
            return CorporateUserRegistrationResult.from(savedUser);
        } catch (DataIntegrityViolationException exception) {
            if (causedByDuplicateEmail(exception)) {
                throw duplicate(companyId);
            }
            throw exception;
        }
    }

    private void validatePassword(String plaintextPassword) {
        if (plaintextPassword == null || plaintextPassword.isBlank()) {
            throw new IllegalArgumentException("password must not be blank");
        }
        if (plaintextPassword.getBytes(StandardCharsets.UTF_8).length > MAX_BCRYPT_PASSWORD_BYTES) {
            throw new IllegalArgumentException("password must not exceed 72 UTF-8 bytes");
        }
    }

    private DuplicateCorporateUserException duplicate(UUID companyId) {
        return new DuplicateCorporateUserException(companyId);
    }

    private boolean causedByDuplicateEmail(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolation
                    && UNIQUE_COMPANY_EMAIL_CONSTRAINT.equalsIgnoreCase(
                            constraintViolation.getConstraintName())) {
                return true;
            }
            if (cause instanceof SQLException sqlException
                    && "23505".equals(sqlException.getSQLState())
                    && sqlException.getMessage() != null
                    && sqlException.getMessage().contains(UNIQUE_COMPANY_EMAIL_CONSTRAINT)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
