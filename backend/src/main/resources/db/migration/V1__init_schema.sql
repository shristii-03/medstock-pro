-- ============================================================
-- MedStock Pro — Complete Database Schema
-- Built for a real family-owned pharmacy in Indore
-- ============================================================

-- Users table
CREATE TABLE users (
    id          BIGSERIAL     PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    email       VARCHAR(150)  UNIQUE NOT NULL,
    password    VARCHAR(255)  NOT NULL,
    role        VARCHAR(20)   NOT NULL DEFAULT 'BILLING',
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_role CHECK (role IN ('ADMIN','MANAGER','BILLING'))
);

-- Suppliers table
CREATE TABLE suppliers (
    id          BIGSERIAL     PRIMARY KEY,
    name        VARCHAR(150)  NOT NULL,
    phone       VARCHAR(15),
    email       VARCHAR(150),
    gst_number  VARCHAR(20),
    address     TEXT,
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Medicines table
CREATE TABLE medicines (
    id              BIGSERIAL     PRIMARY KEY,
    name            VARCHAR(200)  NOT NULL,
    generic_name    VARCHAR(200),
    category        VARCHAR(100),
    hsn_code        VARCHAR(20),
    gst_slab        NUMERIC(5,2)  NOT NULL DEFAULT 12.00,
    unit            VARCHAR(30)   NOT NULL DEFAULT 'strip',
    reorder_level   INTEGER       NOT NULL DEFAULT 10,
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_gst_slab CHECK (gst_slab IN (0, 5, 12, 18))
);

-- Stock batches table
CREATE TABLE stock_batches (
    id                  BIGSERIAL     PRIMARY KEY,
    medicine_id         BIGINT        NOT NULL REFERENCES medicines(id),
    supplier_id         BIGINT        REFERENCES suppliers(id),
    batch_number        VARCHAR(50)   NOT NULL,
    quantity_remaining  INTEGER       NOT NULL,
    purchase_price      NUMERIC(10,2) NOT NULL,
    selling_price       NUMERIC(10,2) NOT NULL,
    manufacturing_date  DATE,
    expiry_date         DATE          NOT NULL,
    received_date       DATE          NOT NULL DEFAULT CURRENT_DATE,
    disposed            BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_qty_remaining  CHECK (quantity_remaining >= 0),
    CONSTRAINT chk_selling_price  CHECK (selling_price > 0),
    CONSTRAINT chk_purchase_price CHECK (purchase_price > 0)
);

-- Sale bills table
CREATE TABLE sale_bills (
    id              BIGSERIAL     PRIMARY KEY,
    bill_number     VARCHAR(30)   UNIQUE NOT NULL,
    customer_name   VARCHAR(150),
    customer_phone  VARCHAR(15),
    subtotal        NUMERIC(12,2) NOT NULL,
    gst_amount      NUMERIC(12,2) NOT NULL,
    total_amount    NUMERIC(12,2) NOT NULL,
    created_by      BIGINT        REFERENCES users(id),
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Sale items table
CREATE TABLE sale_items (
    id              BIGSERIAL     PRIMARY KEY,
    sale_bill_id    BIGINT        NOT NULL REFERENCES sale_bills(id)
                                  ON DELETE CASCADE,
    stock_batch_id  BIGINT        NOT NULL REFERENCES stock_batches(id),
    medicine_id     BIGINT        NOT NULL REFERENCES medicines(id),
    quantity_sold   INTEGER       NOT NULL,
    unit_price      NUMERIC(10,2) NOT NULL,
    gst_slab        NUMERIC(5,2)  NOT NULL,
    gst_amount      NUMERIC(10,2) NOT NULL,
    line_total      NUMERIC(12,2) NOT NULL,
    CONSTRAINT chk_qty_sold CHECK (quantity_sold > 0)
);

-- Purchase orders table
CREATE TABLE purchase_orders (
    id              BIGSERIAL     PRIMARY KEY,
    po_number       VARCHAR(30)   UNIQUE NOT NULL,
    supplier_id     BIGINT        NOT NULL REFERENCES suppliers(id),
    notes           TEXT,
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    total_amount    NUMERIC(12,2),
    created_by      BIGINT        REFERENCES users(id),
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    received_at     TIMESTAMP,
    CONSTRAINT chk_po_status CHECK (
        status IN ('PENDING','RECEIVED','CANCELLED')
    )
);

-- Alert logs table
CREATE TABLE alert_logs (
    id              BIGSERIAL     PRIMARY KEY,
    medicine_id     BIGINT        NOT NULL REFERENCES medicines(id),
    stock_batch_id  BIGINT        REFERENCES stock_batches(id),
    alert_type      VARCHAR(30)   NOT NULL,
    message         TEXT          NOT NULL,
    resolved        BOOLEAN       NOT NULL DEFAULT FALSE,
    triggered_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
    resolved_at     TIMESTAMP,
    CONSTRAINT chk_alert_type CHECK (
        alert_type IN ('EXPIRY_WARNING','EXPIRY_CRITICAL','LOW_STOCK')
    )
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_stock_medicine  ON stock_batches(medicine_id);
CREATE INDEX idx_stock_expiry    ON stock_batches(expiry_date);
CREATE INDEX idx_stock_disposed  ON stock_batches(disposed);
CREATE INDEX idx_sale_items_bill ON sale_items(sale_bill_id);
CREATE INDEX idx_sale_items_med  ON sale_items(medicine_id);
CREATE INDEX idx_sale_bills_date ON sale_bills(created_at);
CREATE INDEX idx_alert_medicine  ON alert_logs(medicine_id);
CREATE INDEX idx_alert_resolved  ON alert_logs(resolved);

-- ============================================================
-- SEED DATA
-- ============================================================

-- Default users (password for all: Admin@123)
INSERT INTO users (name, email, password, role) VALUES
('Admin',
 'admin@medstockpro.com',
 '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHHi',
 'ADMIN'),
('Manager',
 'manager@medstockpro.com',
 '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHHi',
 'MANAGER'),
('Billing Staff',
 'billing@medstockpro.com',
 '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHHi',
 'BILLING');

-- Sample suppliers
INSERT INTO suppliers (name, phone, email, gst_number, address) VALUES
('MedLife Distributors',
 '9876543210',
 'medlife@example.com',
 '22AABCS1429B1ZB',
 'Mumbai, Maharashtra'),
('PharmaCare Supplies',
 '9876543211',
 'pharmacare@example.com',
 '22AABCT1234B1ZC',
 'Pune, Maharashtra');

-- Sample medicines
INSERT INTO medicines
    (name, generic_name, category, hsn_code, gst_slab, unit, reorder_level)
VALUES
('Paracetamol 500mg',  'Paracetamol',    'Analgesic',     '30049099', 12, 'strip',   20),
('Amoxicillin 250mg',  'Amoxicillin',    'Antibiotic',    '30041090', 12, 'strip',   15),
('Metformin 500mg',    'Metformin',      'Antidiabetic',  '30049041',  5, 'strip',   25),
('Omeprazole 20mg',    'Omeprazole',     'Antacid',       '30049099', 12, 'strip',   10),
('Cetirizine 10mg',    'Cetirizine',     'Antihistamine', '30049099', 12, 'strip',   10),
('Aspirin 75mg',       'Aspirin',        'Antiplatelet',  '30049099', 12, 'strip',   20),
('Vitamin D3 60K',     'Cholecalciferol','Supplement',    '30049099',  0, 'capsule', 15),
('ORS Sachet',         'ORS',            'Rehydration',   '30039099',  5, 'sachet',  30);

-- Sample stock batches
INSERT INTO stock_batches
    (medicine_id, supplier_id, batch_number, quantity_remaining,
     purchase_price, selling_price, manufacturing_date, expiry_date)
VALUES
(1, 1, 'PCM-2024-001', 100, 1.20,  2.00, '2024-01-01', '2026-01-01'),
(1, 1, 'PCM-2024-002',  50, 1.25,  2.10, '2024-06-01', '2026-06-01'),
(2, 2, 'AMX-2024-001',  80, 8.00, 14.00, '2024-03-01', '2025-09-01'),
(3, 1, 'MET-2024-001',  60, 3.50,  6.00, '2024-02-01', '2026-02-01'),
(4, 2, 'OMP-2024-001',  40, 5.00,  9.00, '2024-04-01', '2026-04-01');