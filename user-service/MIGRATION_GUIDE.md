# 📋 Guía de Migración: De Arquitectura Tradicional a Clean Architecture

## 🎯 Resumen de Cambios

Este documento detalla la transformación del proyecto original a Clean Architecture sin Flyway.

## 🔄 Cambios Principales

### 1. Eliminación de Flyway

#### Antes:
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

```properties
# application.properties
spring.flyway.enabled=true
spring.jpa.hibernate.ddl-auto=validate
```

#### Ahora:
- Sin dependencias de Flyway
- JPA con `ddl-auto=update` para desarrollo
- Scripts SQL opcionales en `/database` para uso manual

```properties
# application.properties
spring.jpa.hibernate.ddl-auto=update
```

**Beneficios:**
- ✅ Menos dependencias
- ✅ Más simple para desarrollo
- ✅ Scripts SQL disponibles si se necesitan

---

### 2. Reestructuración a Clean Architecture

#### Antes (Arquitectura por Capas):
```
src/main/java/com/tecsup/app/micro/user/
├── controller/
│   └── UserController.java
├── service/
│   └── UserService.java
├── repository/
│   └── UserRepository.java
├── entity/
│   └── UserEntity.java
├── model/
│   └── User.java
└── dto/
    └── UserDto.java
```

#### Ahora (Clean Architecture):
```
src/main/java/com/tecsup/app/micro/user/
├── domain/                    # CORE - Sin dependencias externas
│   ├── model/                 # Entidades de negocio
│   ├── repository/            # Interfaces (puertos)
│   └── exception/             # Excepciones de dominio
├── application/               # Casos de uso
│   ├── usecase/              # Casos de uso específicos
│   └── service/              # Orquestadores
├── infrastructure/            # Implementaciones técnicas
│   └── persistence/
│       ├── entity/           # Entidades JPA
│       └── repository/       # Adaptadores
└── presentation/             # API REST
    ├── controller/
    ├── dto/
    └── mapper/
```

**Beneficios:**
- ✅ Separación clara de responsabilidades
- ✅ Dominio independiente de frameworks
- ✅ Fácil de testear
- ✅ Más mantenible y escalable

---

### 3. Separación de Responsabilidades

#### Antes:
- `UserService` hacía todo: lógica de negocio + persistencia + mapeo

#### Ahora:
- **Domain Layer**: Define las reglas de negocio
- **Use Cases**: Un caso de uso = una responsabilidad
- **Application Service**: Orquesta los casos de uso
- **Repository Adapter**: Implementa la persistencia

---

### 4. Inversión de Dependencias

#### Antes:
```java
// UserService dependía directamente de Spring Data JPA
public class UserService {
    private final JpaUserRepository repository; // Dependencia concreta
}
```

#### Ahora:
```java
// Use Case depende de una interfaz del dominio
public class CreateUserUseCase {
    private final UserRepository repository; // Interfaz del dominio
}

// Infrastructure implementa la interfaz
public class UserRepositoryImpl implements UserRepository {
    private final JpaUserRepository jpaRepository;
}
```

**Beneficio:** El dominio no conoce nada sobre JPA, Spring, o bases de datos.

---

### 5. DTOs Mejorados

#### Antes:
```java
@Data
public class UserDto {
    // Clase vacía
}
```

#### Ahora:
```java
// Request DTO con validaciones
@Data
public class CreateUserRequest {
    @NotBlank
    @Size(max = 100)
    private String name;
    
    @Email
    @NotBlank
    private String email;
    // ...
}

// Response DTO separado
@Data
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    // ...
}
```

**Beneficios:**
- ✅ Validación automática con Bean Validation
- ✅ Separación entre entrada y salida
- ✅ API más clara

---

### 6. Manejo de Excepciones Mejorado

#### Antes:
```java
throw new RuntimeException("User not found");
```

#### Ahora:
```java
// Excepciones de dominio tipadas
throw new UserNotFoundException(id);
throw new DuplicateEmailException(email);
throw new InvalidUserDataException("...");

// Manejador global con respuestas estructuradas
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(...) {
        // Respuesta JSON estructurada
    }
}
```

**Beneficios:**
- ✅ Excepciones tipadas y específicas
- ✅ Respuestas de error consistentes
- ✅ Códigos HTTP apropiados

---

## 📊 Mapeo de Clases

| Antes | Ahora | Capa |
|-------|-------|------|
| `User` (model) | `User` (domain/model) | Domain |
| - | `UserRepository` (interface) | Domain |
| - | `UserNotFoundException` | Domain |
| `UserEntity` | `UserEntity` | Infrastructure |
| `UserRepository` (JPA) | `JpaUserRepository` | Infrastructure |
| - | `UserRepositoryImpl` | Infrastructure |
| `UserService` | 5 Use Cases + `UserApplicationService` | Application |
| `UserController` | `UserController` | Presentation |
| `UserDto` | `CreateUserRequest`, `UpdateUserRequest`, `UserResponse` | Presentation |
| `UserMapper` | `UserDtoMapper` | Presentation |

---

## 🆕 Nuevas Características

1. **Validación de Negocio en Dominio**
   - `User.isValid()` - Valida reglas de negocio
   
2. **Casos de Uso Explícitos**
   - Un caso de uso por operación
   - Responsabilidad única
   
3. **DTOs Tipados**
   - Request/Response separados
   - Validación con Bean Validation
   
4. **Excepciones de Dominio**
   - Excepciones específicas del negocio
   - Manejo centralizado
   
5. **Documentación Completa**
   - README.md detallado
   - ARCHITECTURE.md con diagramas
   - QUICKSTART.md para inicio rápido
   
6. **Docker Compose**
   - PostgreSQL listo para usar
   - Configuración simplificada

---

## 🚀 Migrando tu Código Existente

Si tienes código personalizado en el proyecto original, aquí está cómo migrarlo:

### 1. Agregar Nuevos Campos al Usuario

**Antes:**
```java
// UserEntity
@Column
private String newField;
```

**Ahora:**
```java
// 1. Agregar al modelo de dominio
// domain/model/User.java
private String newField;

// 2. Agregar a la entidad JPA
// infrastructure/persistence/entity/UserEntity.java
@Column
private String newField;

// 3. Actualizar el mapper en UserRepositoryImpl
// 4. Agregar a los DTOs si es necesario
```

### 2. Agregar Nueva Operación

**Ejemplo: Buscar usuarios por nombre**

```java
// 1. Agregar método a la interfaz del dominio
// domain/repository/UserRepository.java
List<User> findByNameContaining(String name);

// 2. Implementar en infrastructure
// infrastructure/persistence/repository/JpaUserRepository.java
List<UserEntity> findByNameContaining(String name);

// 3. Adaptar en UserRepositoryImpl
@Override
public List<User> findByNameContaining(String name) {
    return jpaUserRepository.findByNameContaining(name)
        .stream()
        .map(this::toDomain)
        .collect(Collectors.toList());
}

// 4. Crear nuevo Use Case
// application/usecase/FindUsersByNameUseCase.java
@Component
public class FindUsersByNameUseCase {
    private final UserRepository repository;
    
    public List<User> execute(String name) {
        return repository.findByNameContaining(name);
    }
}

// 5. Agregar al servicio de aplicación
// 6. Agregar endpoint en el controlador
```

---

## 📝 Notas Importantes

1. **Base de Datos**
   - JPA creará las tablas automáticamente con `ddl-auto=update`
   - Para producción, usa `ddl-auto=validate` y ejecuta scripts manualmente

2. **Transacciones**
   - Gestionadas en `UserApplicationService`
   - Los Use Cases son stateless

3. **Testing**
   - El dominio es fácil de testear (sin dependencias de Spring)
   - Los Use Cases pueden testearse con mocks
   - Los adaptadores pueden testearse con base de datos en memoria

4. **Escalabilidad**
   - Fácil agregar nuevos casos de uso
   - Fácil cambiar implementaciones de infraestructura
   - Dominio permanece estable

---

## ✅ Checklist de Migración Completa

- [x] Eliminado Flyway
- [x] Reestructurado a Clean Architecture
- [x] Creadas capas de Domain, Application, Infrastructure, Presentation
- [x] Implementados 5 casos de uso
- [x] Agregadas excepciones de dominio
- [x] Mejorados DTOs con validación
- [x] Agregado manejo global de excepciones
- [x] Creada documentación completa
- [x] Agregado Docker Compose para PostgreSQL
- [x] Creados scripts SQL opcionales
- [x] Agregada colección de Postman

---

## 🎓 Aprendizaje Clave

La Clean Architecture no es solo sobre organizar carpetas, es sobre:

1. **Independencia**: El core no depende de frameworks
2. **Testabilidad**: Fácil escribir tests unitarios
3. **Mantenibilidad**: Cambios aislados por capa
4. **Escalabilidad**: Fácil agregar nuevas features
5. **Claridad**: Cada clase tiene una responsabilidad clara

¡Disfruta tu nuevo proyecto con Clean Architecture! 🎉
