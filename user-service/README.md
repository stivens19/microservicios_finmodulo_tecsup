# 🚀 Guía de Inicio Rápido

## Requisitos Previos

- Java 21
- Maven 3.6+
- Docker Compose (para PostgreSQL)

## Usando Docker para PostgreSQL

### 1. Iniciar PostgreSQL con Docker

```bash
# Desde el directorio raíz del proyecto
docker-compose up -d
```

Esto iniciará un contenedor de PostgreSQL con:
- Base de datos: `userdb`
- Usuario: `postgres`
- Password: `postgres`
- Puerto: `5432`

### 2. Compilar el proyecto

```bash
mvn clean install
```

### 3. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

### 4. Verificar que funciona

Abre tu navegador y visita:
```
http://localhost:8081/api/users/health
```

Deberías ver: `User Service running with Clean Architecture!`

## 🧪 Probar la API

### Usando curl

#### Obtener todos los usuarios
```bash
curl http://localhost:8081/api/users
```

#### Crear un usuario
```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "phone": "+51-999-999-999",
    "address": "Test Address"
  }'
```

#### Obtener un usuario por ID
```bash
curl http://localhost:8081/api/users/1
```

#### Actualizar un usuario
```bash
curl -X PUT http://localhost:8081/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated User",
    "email": "updated@example.com",
    "phone": "+51-888-888-888",
    "address": "Updated Address"
  }'
```

#### Eliminar un usuario
```bash
curl -X DELETE http://localhost:8081/api/users/1
```

### Usando Postman

1. Importa la colección de Postman (si tienes una)
2. O crea manualmente las requests con los endpoints listados arriba

## 🎯 Endpoints Disponibles

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/users | Obtener todos los usuarios |
| GET | /api/users/{id} | Obtener usuario por ID |
| POST | /api/users | Crear nuevo usuario |
| PUT | /api/users/{id} | Actualizar usuario |
| DELETE | /api/users/{id} | Eliminar usuario |
| GET | /api/users/health | Health check |

