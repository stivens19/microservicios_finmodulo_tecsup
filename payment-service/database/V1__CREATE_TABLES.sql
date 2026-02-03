-- ============================================
-- Migration: V1__Create_payments_table
-- Description: Crear tabla payments
-- Database: paymentdb (Docker container: postgres-payment)
-- ============================================

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    enrollment_id BIGINT NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    status VARCHAR(30) DEFAULT 'APPROVED',
    paid_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_enrollment_id ON payments(enrollment_id);

COMMENT ON TABLE payments IS 'Pagos realizados por matrículas';
COMMENT ON COLUMN payments.enrollment_id IS 'ID de la matrícula asociada';
