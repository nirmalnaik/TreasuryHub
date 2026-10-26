CREATE TABLE bank_accounts (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    currency_code VARCHAR(3) NOT NULL CHECK (currency_code ~ '^[A-Z]{3}$'),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_bank_accounts_company_id ON bank_accounts (company_id);
