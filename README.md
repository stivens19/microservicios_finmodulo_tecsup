# LMS Microservicios

Este proyecto implementa una arquitectura de microservicios para un sistema de gestión de aprendizaje (LMS), utilizando Spring Boot, Docker, PostgreSQL y Kafka para la comunicación asíncrona.

## Arquitectura

El sistema consta de los siguientes microservicios:

*   **User Service** (Puerto `8080`): Gestión de usuarios.
*   **Course Service** (Puerto `8081`): Gestión de cursos y publicación de eventos.
*   **Enrollment Service** (Puerto `8082`): Gestión de matrículas (escucha eventos de cursos).
*   **Payment Service** (Puerto `8083`): Gestión de pagos.
*   **Notification Service** (Puerto `8084`): Envío de notificaciones (simulado).

## Requisitos Previos

*   **Java**
*   **Docker Desktop** (corriendo)
*   **Git Bash** (recomendado para Windows)

## Inicio Rápido (Recomendado)

El proyecto incluye un script automatizado para iniciar toda la infraestructura y los servicios.

1.  Abre una terminal (Git Bash recomendado).
2.  Ejecuta el script de inicio:
    ```bash
    ./start-services.sh
    ```
    *Este script levantará los contenedores de Docker (Postgres, Zookeeper, Kafka) y abrirá una nueva ventana para cada microservicio.*

## Inicio Manual

Si prefieres iniciar los componentes manualmente:

### 1. Iniciar Infraestructura (Docker)
Levanta las bases de datos y el bus de mensajes:
```bash
docker-compose up -d
```
Verifica que los contenedores estén corriendo:
```bash
docker ps
```

### 2. Iniciar Microservicios
Debes iniciar cada servicio en una terminal separada:

**User Service:**
```bash
cd user-service
./mvnw.cmd spring-boot:run
```

**Course Service:**
```bash
cd course-service
./mvnw.cmd spring-boot:run
```

**Enrollment Service:**
```bash
cd enrollment-service
./mvnw.cmd spring-boot:run
```

**Payment Service:**
```bash
cd payment-service
./mvnw.cmd spring-boot:run
```

**Notification Service:**
```bash
cd notification-service
./mvnw.cmd spring-boot:run
```

**Notification Service:**
```bash
cd notification-service
./mvnw.cmd spring-boot:run
```

## Ejecutar Scripts de BD (Opcional)

Aunque los microservicios están configurados para generar las tablas automáticamente (JPA), se adjuntan los scripts SQL de referencia:

### Para microservicio user-service (userdb)
- `user-service/database/V1__CREATE_TABLES.sql`
- `user-service/database/V2__ADD_INDEXES.sql`
- `user-service/database/V3__INSERT_DATA.sql`

### Para microservicio course-service (coursedb)
- `course-service/database/V1__CREATE_TABLES.sql`

### Para microservicio enrollment-service (enrollmentdb)
- `enrollment-service/database/V1__CREATE_TABLES.sql`

### Para microservicio payment-service (paymentdb)
- `payment-service/database/V1__CREATE_TABLES.sql`

### Para microservicio notification-service (notificationdb)
- `notification-service/database/V1__CREATE_TABLES.sql`

## Pruebas y Verificación del Flujo

Se incluye una colección de Postman (`LMS_Microservices.postman_collection.json`) en la raíz del proyecto para probar todos los endpoints.

### Pasos para probar el flujo completo:

1.  **Crear Usuario** (`POST /users` - User Service)
    *   Crea un estudiante que se matriculará.
    
2.  **Crear Curso** (`POST /courses` - Course Service)
    *   Crea un curso nuevo (estado: borrador).

3.  **Publicar Curso** (`POST /courses/{id}/publish` - Course Service)
    *   Cambia el estado a "publicado".
    *   **Evento:** Se envía un mensaje a Kafka (`lms.course.events`).
    *   **Verificación:** Revisa los logs de `user-service` o `enrollment-service` para ver si recibieron la notificación (dependiendo de la lógica de consumidores).

4.  **Matricular Usuario** (`POST /enrollments` - Enrollment Service)
    *   Asocia el usuario al curso creado.
    *   Estado inicial: `PENDING_PAYMENT`.

5.  **Registrar Pago** (`POST /payments` - Payment Service)
    *   Paga la matrícula.
    *   **Evento:** Se envía un evento de pago exitoso.
    *   **Efecto:** El `enrollment-service` actualiza el estado a `ACTIVE`.
    *   **Notificación:** El `notification-service` envía un correo (log) al usuario.

6.  **Verificar Matrícula** (`GET /enrollments/{id}` - Enrollment Service)
    *   Confirma que el estado de la matrícula es ahora `ACTIVE`.

## Solución de Problemas

*   **Error de conexión a Kafka**: Asegúrate de que el contenedor `lms-kafka` esté corriendo (`docker ps`). Si falla, reinicia con `docker-compose down` y `docker-compose up -d`.
*   **"mvnw: command not found"**: Asegúrate de ejecutar `./mvnw.cmd` en Windows (CMD/PowerShell) o `./mvnw` en Bash. Si falta el wrapper, el script `start-services.sh` inlcuye comandos para repararlo.
