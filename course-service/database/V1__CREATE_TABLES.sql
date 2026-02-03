-- ============================================
-- Migration: V1__Create_courses_table
-- Description: Crear tabla courses
-- Database: coursedb (Docker container: postgres-course)
-- ============================================

CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    published BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE courses IS 'Cursos disponibles en la plataforma';
COMMENT ON COLUMN courses.id IS 'Identificador único del curso';
