CREATE TABLE companies (
    id UUID PRIMARY KEY,
    legal_name VARCHAR(200) NOT NULL CHECK (BTRIM(legal_name) <> ''),
    registration_number VARCHAR(100) NOT NULL CHECK (BTRIM(registration_number) <> ''),
    country_code VARCHAR(2) NOT NULL CHECK (country_code ~ '^[A-Z]{2}$'),
    status VARCHAR(32) NOT NULL CHECK (status = 'PENDING_VERIFICATION'),
    CONSTRAINT uk_companies_country_registration
        UNIQUE (country_code, registration_number)
);
