# 🔄 Flujo End-to-End: Matrícula, Pago y Notificación

En este documento explico cómo funciona el flujo principal de mi sistema de microservicios, desde que un estudiante se matricula hasta que recibe la confirmación de su pago. He documentado los datos exactos que utilicé en Postman para probarlo.

---

## 1. Pre-requisitos: Datos Iniciales

Antes de probar el flujo principal, necesité crear un usuario y un curso disponible.

### Paso 1: Crear un Usuario
Primero, registré un estudiante en el sistema.

*   **Método:** `POST`
*   **URL:** `http://localhost:8080/users`
*   **Body (JSON):**
    ```json
    {
        "fullName": "Juan Perez",
        "email": "juan.perez@example.com",
        "phone": "987654321",
        "address": "Av. Principal 123, Lima"
    }
    ```
*   **Resultado:** El sistema me devolvió el usuario con `id: 1`.

### Paso 2: Crear y Publicar un Curso
Luego, creé un curso de prueba.

*   **Método:** `POST`
*   **URL:** `http://localhost:8081/courses`
*   **Body (JSON):**
    ```json
    {
        "title": "Arquitectura de Microservicios",
        "description": "Curso avanzado de Spring Boot y Cloud"
    }
    ```
*   **Resultado:** Obtuve el curso con `id: 1` y estado `published: false`.

Para que el curso esté disponible, lo publiqué:

*   **Método:** `POST`
*   **URL:** `http://localhost:8081/courses/1/publish`
*   **Resultado:** El estado cambió a `published: true` y se envió un evento a Kafka (`lms.course.events`), aunque esto es interno.

---

## 2. El Flujo Principal (End-to-End)

Aquí es donde ocurre la magia de la comunicación entre servicios.

### Paso 3: Realizar la Matrícula (Enrollment)
Como estudiante (`id: 1`), intenté matricularme en el curso (`id: 1`).

*   **Método:** `POST`
*   **URL:** `http://localhost:8082/enrollments`
*   **Body (JSON):**
    ```json
    {
        "userId": 1,
        "courseId": 1
    }
    ```
*   **Lo que pasó:**
    *   El **Enrollment Service** creó el registro.
    *   El estado inicial se estableció en `PENDING_PAYMENT` (Pendiente de Pago).
*   **Respuesta Esperada:**
    ```json
    {
        "id": 1,
        "userId": 1,
        "courseId": 1,
        "status": "PENDING_PAYMENT",
        "createdAt": "..."
    }
    ```

### Paso 4: Pagar la Matrícula
Ahora procedí a pagar la matrícula `id: 1`.

*   **Método:** `POST`
*   **URL:** `http://localhost:8083/payments`
*   **Body (JSON):**
    ```json
    {
        "enrollmentId": 1,
        "amount": 299.99
    }
    ```
*   **Lo que pasó (Sincrónico):**
    *   El **Payment Service** registró el pago y lo guardó en su base de datos como `APPROVED`.
*   **Lo que pasó (Asincrónico - Kafka):**
    1.  Payment Service envió un evento `PaymentApprovedEvent`.
    2.  **Enrollment Service** escuchó este evento y actualizó el estado de mi matrícula a `ACTIVE`.
    3.  **Notification Service** escuchó el mismo evento y "envió" (simuló) un correo de confirmación.

### Paso 5: Confirmación y Notificación
Finalmente, verifiqué que todo el ciclo se cerró correctamente consultando el estado de mi matrícula.

*   **Método:** `GET`
*   **URL:** `http://localhost:8082/enrollments/1`
*   **Resultado:**
    ```json
    {
        "id": 1,
        "userId": 1,
        "courseId": 1,
        "status": "ACTIVE",  <-- ¡Esto confirma que el flujo funcionó!
        "createdAt": "..."
    }
    ```

Si reviso los logs del **Notification Service**, también veo:
> `INFO: Notificación guardada: Pago 1 aprobado para inscripción 1`

---

## Resumen del Flujo de Datos

1.  **User** -> Inicia Matrícula (REST).
2.  **Enrollment** -> Guarda "PENDIENTE".
3.  **User** -> Paga (REST).
4.  **Payment** -> Guarda Pago + **Publica Evento (Kafka)**.
5.  **Enrollment** (Consumidor) -> **Recibe Evento** -> Actualiza a "ACTIVO".
6.  **Notification** (Consumidor) -> **Recibe Evento** -> Genera Notificación.
