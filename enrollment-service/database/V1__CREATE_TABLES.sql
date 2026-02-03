-- ============================================
-- Migration: V1__Create_enrollments_table
-- Description: Crear tabla enrollments
-- Database: enrollmentdb (Docker container: postgres-enrollment)
-- ============================================

CREATE TABLE enrollments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    status VARCHAR(40) DEFAULT 'PENDING_PAYMENT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_enrollments_user_id ON enrollments(user_id);
CREATE INDEX idx_enrollments_course_id ON enrollments(course_id);

COMMENT ON TABLE enrollments IS 'Matrículas de usuarios en cursos';
COMMENT ON COLUMN enrollments.user_id IS 'ID del usuario (referencia a user-service)';
