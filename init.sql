-- Counsel CRM Database Schema

-- Customers table
CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100),
    address VARCHAR(500),
    memo VARCHAR(1000),
    grade VARCHAR(20) NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Counselors table
CREATE TABLE IF NOT EXISTS counselors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    employee_id VARCHAR(50) NOT NULL UNIQUE,
    extension_number VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    team VARCHAR(20) NOT NULL DEFAULT 'GENERAL',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Counselings table
CREATE TABLE IF NOT EXISTS counselings (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    counselor_id BIGINT REFERENCES counselors(id),
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    category VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    assigned_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Counsel Notes table
CREATE TABLE IF NOT EXISTS counsel_notes (
    id BIGSERIAL PRIMARY KEY,
    counseling_id BIGINT NOT NULL REFERENCES counselings(id),
    counselor_id BIGINT NOT NULL REFERENCES counselors(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Contracts table
CREATE TABLE IF NOT EXISTS contracts (
    id BIGSERIAL PRIMARY KEY,
    contract_number VARCHAR(30) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    product_name VARCHAR(100) NOT NULL,
    monthly_fee DECIMAL(10, 2) NOT NULL,
    contract_period_months INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    memo VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_customers_phone_number ON customers(phone_number);
CREATE INDEX idx_customers_grade ON customers(grade);
CREATE INDEX idx_counselors_status ON counselors(status);
CREATE INDEX idx_counselors_team ON counselors(team);
CREATE INDEX idx_counselings_status ON counselings(status);
CREATE INDEX idx_counselings_customer_id ON counselings(customer_id);
CREATE INDEX idx_counselings_counselor_id ON counselings(counselor_id);
CREATE INDEX idx_counselings_created_at ON counselings(created_at);
CREATE INDEX idx_contracts_customer_id ON contracts(customer_id);
CREATE INDEX idx_contracts_status ON contracts(status);

-- Sample Data for Testing
INSERT INTO counselors (name, employee_id, extension_number, status, team) VALUES
('김상담', 'EMP001', '1001', 'AVAILABLE', 'GENERAL'),
('이상담', 'EMP002', '1002', 'AVAILABLE', 'VIP'),
('박상담', 'EMP003', '1003', 'BUSY', 'COMPLAINT'),
('최상담', 'EMP004', '1004', 'BREAK', 'TECHNICAL');

INSERT INTO customers (name, phone_number, email, grade) VALUES
('홍길동', '01012345678', 'hong@example.com', 'VIP'),
('김철수', '01098765432', 'kim@example.com', 'NORMAL'),
('이영희', '01011112222', 'lee@example.com', 'PREMIUM');

INSERT INTO contracts (contract_number, customer_id, product_name, monthly_fee, contract_period_months, start_date, end_date) VALUES
('CTR-2024-0001', 1, '정수기 렌탈', 35000.00, 36, '2024-01-01', '2027-01-01'),
('CTR-2024-0002', 1, '공기청정기 렌탈', 29000.00, 24, '2024-06-01', '2026-06-01'),
('CTR-2024-0003', 2, '정수기 렌탈', 35000.00, 36, '2024-03-01', '2027-03-01');
