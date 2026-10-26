CREATE TABLE payments (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    debtor_account_id UUID NOT NULL,
    creditor_account_id UUID NOT NULL,
    amount NUMERIC(19, 4) NOT NULL CHECK (amount > 0),
    currency_code VARCHAR(3) NOT NULL CHECK (currency_code ~ '^[A-Z]{3}$'),
    status VARCHAR(32) NOT NULL CHECK (status = 'DRAFT'),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_payments_company_id ON payments (company_id);
