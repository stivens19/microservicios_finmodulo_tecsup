# Diagrama de Clean Architecture

```
┌───────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                         │
│                                                                   │
│  ┌────────────────┐   ┌──────────────┐   ┌─────────────────┐      │
│  │ UserController │   │ DTOs         │   │ DtoMapper       │      │
│  │                │   │ - Request    │   │                 │      │
│  │ REST API       │   │ - Response   │   │ DTO ↔ Domain    │      │
│  └────────────────┘   └──────────────┘   └─────────────────┘      │
│           │                    ↑                    ↑             │
└───────────┼────────────────────┼────────────────────┼─────────────┘
            │                    │                    │
            ↓                    │                    │
┌───────────────────────────────────────────────────────────────────┐
│                       APPLICATION LAYER                           │
│                                                                   │
│  ┌──────────────────────┐         ┌────────────────────────┐      │
│  │UserApplicationService│         │      Use Cases         │      │
│  │                      │         │                        │      │
│  │ - Orchestrates       │    ┌───▶│ GetAllUsersUseCase     │      │
│  │   use cases          │    │    │ GetUserByIdUseCase     │      │
│  │ - Manages            │    │    │ CreateUserUseCase      │      │
│  │   transactions       │────┘    │ UpdateUserUseCase      │      │
│  └──────────────────────┘         │ DeleteUserUseCase      │      │
│           │                       └────────────────────────┘      │
└───────────┼─────────────────────────────────┬─────────────────────┘
            │                                 │
            ↓                                 ↓
┌────────────────────────────────────────────────────────────────────┐
│                          DOMAIN LAYER (CORE)                       │
│                                                                    │
│  ┌──────────────┐   ┌────────────────────┐   ┌──────────────────┐  │
│  │ User (Model) │   │ UserRepository     │   │ Exceptions       │  │
│  │              │   │    (Interface)     │   │                  │  │
│  │ - Business   │   │                    │   │ - UserNotFound   │  │
│  │   Entity     │   │ - findAll()        │   │ - DuplicateEmail │  │
│  │ - isValid()  │   │ - findById()       │   │ - InvalidData    │  │
│  │              │   │ - save()           │   │                  │  │
│  └──────────────┘   │ - delete()         │   └──────────────────┘  │
│                     └────────────────────┘                         │
│                              ↑                                     │
└──────────────────────────────┼─────────────────────────────────────┘
                               │ (implements)
                               │
┌──────────────────────────────┼─────────────────────────────────────┐
│                    INFRASTRUCTURE LAYER                            │
│                              │                                     │
│  ┌────────────────────────────────────────────────────┐            │
│  │          UserRepositoryImpl (Adapter)              │            │
│  │                                                    │            │
│  │  Implements UserRepository interface from Domain   │            │
│  │                                                    │            │
│  │  ┌──────────────┐    ┌────────────────────────┐    │            │
│  │  │ UserEntity   │───▶│ JpaUserRepository      │    │            │
│  │  │              │    │                        │    │            │
│  │  │ - JPA        │    │ - Spring Data JPA      │    │            │
│  │  │ - @Entity    │    │ - Database operations  │    │            │
│  │  └──────────────┘    └────────────────────────┘    │            │
│  └────────────────────────────────────────────────────┘            │
│                              │                                     │
│                              ↓                                     │
│                    ┌──────────────────┐                            │
│                    │   PostgreSQL     │                            │
│                    │   Database       │                            │
│                    └──────────────────┘                            │
└────────────────────────────────────────────────────────────────────┘
```

## Flujo de Dependencias

```
Presentation → Application → Domain ← Infrastructure
```

### Principio de Inversión de Dependencias

- **Domain** define interfaces (puertos)
- **Infrastructure** implementa esas interfaces (adaptadores)
- **Application** usa las interfaces del dominio
- **Presentation** usa los servicios de aplicación

Las dependencias apuntan **hacia adentro**, hacia el dominio.
