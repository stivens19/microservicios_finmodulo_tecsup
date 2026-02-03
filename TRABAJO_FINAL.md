# ENUNCIADO DEL TRABAJO FINAL

## **Título**

**Migración de un LMS Monolítico a Microservicios con Spring Boot, Comunicación REST (RestTemplate) y Mensajería Asíncrona con Kafka**

---

## 1. Contexto

Se cuenta con un sistema LMS desarrollado inicialmente como un monolito (**lms-project**) en **Spring Boot**, con módulos funcionales para cursos, matrículas, pagos y notificaciones.  
Además, existen microservicios independientes (por ejemplo, **user-service**) y comunicación mediante **Kafka** en el proyecto original.

Con el objetivo de mejorar la escalabilidad, mantenibilidad y despliegue independiente, se propone migrar el LMS a una arquitectura de microservicios usando:

- Spring Boot  
- RestTemplate para invocaciones síncronas entre microservicios  
- Kafka para comunicación asíncrona basada en eventos

---

## 2. Problema

El enfoque monolítico genera:

- Alto acoplamiento entre módulos  
- Dificultad para escalar componentes individualmente  
- Despliegues más riesgosos  
- Mayor complejidad en integraciones internas  

---

## 3. Objetivo general

Diseñar e implementar la migración del LMS monolítico a una arquitectura de microservicios, utilizando RestTemplate para integración síncrona y Kafka para eventos de negocio y notificaciones asíncronas.

---

## 4. Objetivos específicos

- Separar el LMS en microservicios:

  - course-service  
  - enrollment-service  
  - payment-service  
  - notification-service  
  - user-service  

- Implementar endpoints REST por microservicio con buenas prácticas.

- Implementar comunicación síncrona con RestTemplate:

  - enrollment-service consume user-service y course-service.

- Implementar comunicación asíncrona con Kafka:

  - payment-service publica eventos de pago  
  - enrollment-service publica eventos de matrícula  
  - notification-service consume eventos y envía notificaciones  

- Garantizar consistencia eventual con BD independiente por servicio.

---

## 5. Alcance del trabajo

Incluye:

- Implementación de los 5 microservicios  
- Persistencia independiente  
- Kafka topics y eventos  
- Pruebas de integración:

  - Publicación de curso  
  - Solicitud de matrícula  
  - Registro de pago  
  - Envío de notificación  


---

# PROPUESTA DE ARQUITECTURA FINAL

## Tabla 1. Microservicios y responsabilidades

| Microservicio | Responsabilidad | Persistencia sugerida |
|---|---|---|
| course-service | Gestión de cursos | coursedb |
| enrollment-service | Gestión de matrículas | enrollmentdb |
| payment-service | Gestión de pagos | paymentdb |
| notification-service | Notificaciones por eventos | notificationdb |
| user-service | Gestión de usuarios | userdb |

---

## Tabla 2. Comunicación entre microservicios

| Caso | Emisor | Receptor | Tipo |
|---|---|---|---|
| Validar usuario | enrollment-service | user-service | REST |
| Consultar curso | enrollment-service | course-service | REST |
| Matrícula creada (evento) | enrollment-service | notification-service | Kafka |
| Pago aprobado/rechazado | payment-service | enrollment-service / notification-service | Kafka |
| Matrícula actualizada | enrollment-service | notification-service | Kafka |

---

# CASOS DE USO

## Caso 1: Publicar curso

1. Admin crea curso  
2. Admin publica curso (`POST /courses/{id}/publish`)  
3. course-service emite evento opcional `CoursePublishedEvent`

---

## Caso 2: Solicitud de matrícula

1. Cliente llama `POST /enrollments`  
2. enrollment-service valida usuario y curso vía RestTemplate  
3. Matrícula queda en estado `PENDING_PAYMENT`  
4. enrollment-service publica `EnrollmentCreatedEvent`  
5. notification-service notifica al estudiante

---

## Caso 3: Pago

1. Cliente llama `POST /payments`  
2. payment-service registra pago y publica:

   - `PaymentApprovedEvent`  
   - `PaymentRejectedEvent`

3. enrollment-service consume evento y actualiza matrícula:

   - `CONFIRMED` o `CANCELLED`

4. enrollment-service publica `EnrollmentUpdatedEvent`  
5. notification-service notifica el resultado final

---

# TABLAS DE DISEÑO

## Tabla 3. Topics y eventos Kafka

| Topic | Evento | Producer | Consumers |
|---|---|---|---|
| lms.enrollment.events | EnrollmentCreatedEvent | enrollment-service | notification-service |
| lms.payment.events | PaymentApprovedEvent | payment-service | enrollment-service, notification-service |
| lms.payment.events | PaymentRejectedEvent | payment-service | enrollment-service, notification-service |
| lms.enrollment.events | EnrollmentUpdatedEvent | enrollment-service | notification-service |
| (opcional) lms.course.events | CoursePublishedEvent | course-service | notification-service |

---

## Tabla 4. Endpoints REST mínimos

| Servicio | Endpoint | Método | Descripción |
|---|---|---|---|
| course-service | /courses | POST | Crear curso |
| course-service | /courses | GET | Listar cursos |
| course-service | /courses/{id} | GET | Obtener curso |
| course-service | /courses/{id}/publish | POST | Publicar curso |
| enrollment-service | /enrollments | POST | Crear matrícula |
| enrollment-service | /enrollments/{id} | GET | Consultar matrícula |
| enrollment-service | /enrollments?userId= | GET | Matrículas por usuario |
| payment-service | /payments | POST | Registrar pago |
| payment-service | /payments/{id} | GET | Consultar pago |
| user-service | /users/{id} | GET | Consultar usuario |

---

# SCRIPTS SQL DE TABLAS POR MICROSERVICIO

## user-service

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(120) UNIQUE NOT NULL,
    status VARCHAR(30) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## course-service

```sql
CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    published BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## enrollment-service

```sql
CREATE TABLE enrollments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    status VARCHAR(40) DEFAULT 'PENDING_PAYMENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## payment-service

```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    enrollment_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(30) DEFAULT 'APPROVED',
    paid_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## notification-service

```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    sent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

# ENTREGABLES DEL TRABAJO FINAL

- Repositorio con los 5 microservicios  
- Docker Compose con Kafka + Bases de datos  
- Pruebas en Postman/Swagger  
- Flujo end-to-end: matrícula → pago → confirmación → notificación  
