Pets Application (DDD, Spring Boot)

Overview
- Pets is a Domain-Driven Design (DDD) sample application built with Java 21 and Spring Boot. It models two bounded contexts:
  - Auth (Users): user registration, login, JWT security, domain events.
  - Pets: pet management for the authenticated user.
- Cross-cutting infrastructure includes PostgreSQL, RabbitMQ, logging, and OpenAPI/Swagger UI.

Prerequisites
- Java: JDK 21 (Gradle toolchain uses Java 21).
- Build: Gradle Wrapper (included).
- Runtime services:
  - PostgreSQL
  - RabbitMQ
- Optional: Docker and Docker Compose (recommended for local env).

Quick Start (Docker Compose)
- Copy or export environment variables as needed (defaults work out of the box for local use):
  - POSTGRES_HOST=localhost, POSTGRES_PORT=5432, POSTGRES_DB=pets
  - POSTGRES_USER=postgres, POSTGRES_PASSWORD=postgrespassword
  - RABBITMQ_HOST=localhost, RABBITMQ_PORT=5672, RABBITMQ_USER=guest, RABBITMQ_PASSWORD=guest
  - JWT_SECRET=dev-secret-please-change, JWT_EXPIRATION=3600000
- Start infrastructure (and optionally app if configured) via docker-compose:
  - docker-compose up -d
- Build and run the app:
  - ./gradlew clean build
  - ./gradlew bootRun
- Swagger UI: http://localhost:8080/swagger-ui/index.html

Local Development Without Docker
- Ensure PostgreSQL and RabbitMQ are running locally.
- Configure environment variables or rely on defaults from src/main/resources/application.yml.
- Build: ./gradlew clean build (use -x test to skip tests locally)
- Run (dev): ./gradlew bootRun

Build, Test, Package
- Build (runs tests): ./gradlew clean build
- Skip tests: ./gradlew clean build -x test
- Run (dev): ./gradlew bootRun
- Tests: ./gradlew test
- Run one test: ./gradlew test --tests "dev.eduzavarce.pets.auth.users.domain.UserDomainTest"
- Package JAR: ./gradlew bootJar

Configuration (application.yml highlights)
- Database (PostgreSQL)
  - spring.datasource.url: jdbc:postgresql://${POSTGRES_HOST:localhost}:${POSTGRES_PORT:5432}/${POSTGRES_DB:pets}
  - spring.datasource.username: ${POSTGRES_USER:postgres}
  - spring.datasource.password: ${POSTGRES_PASSWORD:postgrespassword}
  - spring.jpa.hibernate.ddl-auto: update (dev). Consider Flyway/Liquibase for prod.
- RabbitMQ
  - spring.rabbitmq.host: ${RABBITMQ_HOST:localhost}
  - spring.rabbitmq.port: ${RABBITMQ_PORT:5672}
  - spring.rabbitmq.username: ${RABBITMQ_USER:guest}
  - spring.rabbitmq.password: ${RABBITMQ_PASSWORD:guest}
  - Domain event bus exchange and routing under app.rabbitmq.* (see shared/core/infrastructure/RabbitMQConfig.java).
- Security/JWT
  - security.jwt.secret-key: ${JWT_SECRET:...}
  - security.jwt.expiration-time: ${JWT_EXPIRATION:3600000}
- CORS
  - app.cors.allowed-origins: defaults to http://localhost:5173 (adjust to your UI).

Architecture (DDD)
- Layers
  - Domain: Pure business logic, Value Objects, Aggregates, Domain Events. No framework dependencies.
  - Application: Orchestrates use cases, coordinates domain + ports, publishes domain events.
  - Infrastructure: Adapters for HTTP (controllers), JPA (repositories/entities), RabbitMQ (event bus), security.
- Cross-cutting
  - EventBus abstraction with RabbitMqEventBus implementation.
  - Global exception handling producing standardized error payloads.

Canonical DDD Flow Example: CreateUser
- Goal: Demonstrate end-to-end flow for the Users (Auth) context using CreateUser as the canonical pattern.
- Request (Infrastructure)
  - Controller: auth/users/infrastructure/CreateUserPutController
  - HTTP: PUT /api/v1/users
  - Body (CreateUserRequest): { id, username, email, password }
  - Mapping: request -> CreateUserDto (application boundary primitives)
- Application Service
  - Class: auth/users/application/CreateUserService
  - Responsibilities:
    - Hash password via PasswordHasher (port) using BcryptUtils implementation.
    - Ensure non-existence (id/email) via AuthUserRepository (JPA adapter) to avoid duplicates.
    - Construct aggregate: User.create(CreateUserDto) — wraps primitives in Value Objects, assigns ROLE_USER by default.
    - Persist write model: new UserPostgresEntity(createUserDto) through repository (domain remains persistence-agnostic).
    - Publish domain events: eventBus.publish(user.pullDomainEvents()).
- Domain
  - Aggregate: auth/users/domain/User extends AggregateRoot
  - Value Objects: UserId, Username, UserEmail, UserPassword; UserRole enum.
  - Factory: static User.create(CreateUserDto dto)
    - Validates via VOs (e.g., email format, username constraints).
    - Records a UserCreated event with event name "user.created" and body = user.toPrimitives().
    - toPrimitives() returns UserDto without password (security by design).
- Messaging
  - EventBus: shared/core/infrastructure/RabbitMqEventBus publishes DomainEventDto via RabbitTemplate.
  - Routing: "{routingPrefix}.{aggregateId}.{eventName}", default prefix: "events".
  - Exchange: topic exchange (domain-events) configured in RabbitMQConfig.
  - Consumers (examples):
    - LogSuccessOnUserCreated (auth users context): logs upon UserCreated.
    - CreatePetsUserOnUserCreated (pets context): mirrors PetUser in pets BC when a user is created.
- Persistence Boundary
  - UserPostgresEntity persists the write model; created from CreateUserDto (not from domain aggregate) to keep domain pure.
- Output
  - Controller returns 201 Created with empty body on success.

API Usage Examples
- Create User
  - curl -X PUT http://localhost:8080/api/v1/users \
    -H "Content-Type: application/json" \
    -d '{
      "id": "9a5c9f1e-8a04-4f8e-9a5a-9a5c9f1e8a04",
      "username": "alice",
      "email": "alice@example.com",
      "password": "S3cr3t!pass"
    }'
  - Response: 201 Created (empty body). On validation errors, a ValidationErrorResponse is returned by GlobalExceptionHandler.
- Login (get JWT)
  - curl -X POST http://localhost:8080/api/v1/users/login \
    -H "Content-Type: application/json" \
    -d '{ "email": "alice@example.com", "password": "S3cr3t!pass" }'
  - Response 200 OK: { "status": "success", "data": { "token": "<jwt>" } }
- Get My Pets (requires Authorization: Bearer <jwt>)
  - curl http://localhost:8080/api/v1/pets -H "Authorization: Bearer <jwt>"
  - Response 200 OK: ResponseDto<List<PetWithOwnerDto>>

Security and Authenticated Endpoints
- Controllers obtain the authenticated principal via @AuthenticationPrincipal UserPostgresEntity and pass principal.getId() to services.
- JWT is validated by JwtAuthenticationFilter and JwtService; tokens include userId and roles.

Testing Strategy
- Domain tests: fast, pure domain (no Spring). Example flow: create a valid CreateUserDto, call User.create(dto), assert toPrimitives() and that pullDomainEvents() contains one UserCreated with eventName "user.created".
- Application tests: mock PasswordHasher, repositories, EventBus; assert orchestration and invariants.
- Infrastructure tests: optional; prefer slices (@DataJpaTest) or Testcontainers when not using docker-compose.
- Run all tests: ./gradlew test
- Run one class: ./gradlew test --tests "dev.eduzavarce.pets.auth.users.domain.UserDomainTest"

Troubleshooting
- Ports in use
  - PostgreSQL default 5432; RabbitMQ default 5672; App default 8080.
- DB migrations
  - Dev uses ddl-auto=update. For production, adopt Flyway/Liquibase migrations.
- RabbitMQ routing
  - If you change event names or routing prefix, update consumers and bindings accordingly (see RabbitMQConfig and listeners).
- CORS
  - Adjust app.cors.allowed-origins if your UI runs on a different host/port.

Where to Look in the Code
- CreateUser (canonical):
  - Controller: src/main/java/dev/eduzavarce/pets/auth/users/infrastructure/CreateUserPutController.java
  - Service: src/main/java/dev/eduzavarce/pets/auth/users/application/CreateUserService.java
  - Domain: src/main/java/dev/eduzavarce/pets/auth/users/domain/* (User, Value Objects, UserCreated)
  - Event Bus: src/main/java/dev/eduzavarce/pets/shared/core/infrastructure/RabbitMqEventBus.java
  - RabbitMQ Config: src/main/java/dev/eduzavarce/pets/shared/core/infrastructure/RabbitMQConfig.java

License
- MIT or project-specific (update as appropriate).