CREATE TABLE corporate_users (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL CHECK (BTRIM(password_hash) <> ''),
    role VARCHAR(32) NOT NULL CHECK (role IN ('ADMIN', 'USER')),
    CONSTRAINT fk_corporate_users_company
        FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT uk_corporate_users_company_email
        UNIQUE (company_id, email)
);

CREATE INDEX idx_corporate_users_company_id ON corporate_users (company_id);
