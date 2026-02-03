-- ============================================
-- Migration: V1__Create_notifications_table
-- Description: Crear tabla notifications
-- Database: notificationdb (Docker container: postgres-notification)
-- ============================================

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    message TEXT NOT NULL,
    sent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);

COMMENT ON TABLE notifications IS 'Notificaciones enviadas o pendientes';
