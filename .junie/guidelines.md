Project Development Guidelines (Advanced)

Scope
- This document captures project-specific practices for building, testing, and extending the Pets application using the current DDD architecture. It focuses on Auth Users and the CreateUserService use case as the canonical model for new use cases.

Prerequisites
- JDK: 21 (Gradle toolchain is configured to use Java 21)
- Build: Gradle (Wrapper provided)
- Database: PostgreSQL (runtime)
- Messaging: RabbitMQ (runtime)

Build and Run
- Build: ./gradlew clean build
  - Notes: Building runs tests. To build without tests during local iteration: ./gradlew clean build -x test
- Run (dev): ./gradlew bootRun
  - Environment via application.yml and environment variables (see below). HikariCP and JPA are tuned conservatively for local dev.
- Packaging: ./gradlew bootJar

Runtime Configuration
- Config is centralized in src/main/resources/application.yml with environment overrides.
- Database (PostgreSQL)
  - spring.datasource.url: jdbc:postgresql://${POSTGRES_HOST:localhost}:${POSTGRES_PORT:5432}/${POSTGRES_DB:pets}
  - spring.datasource.username: ${POSTGRES_USER:postgres}
  - spring.datasource.password: ${POSTGRES_PASSWORD:postgrespassword}
  - Hibernate: ddl-auto=update, dialect=PostgreSQL; update suffices for dev but consider migrations in prod (Flyway/Liquibase).
- RabbitMQ
  - spring.rabbitmq.host: ${RABBITMQ_HOST:localhost}
  - spring.rabbitmq.port: ${RABBITMQ_PORT:5672}
  - spring.rabbitmq.username: ${RABBITMQ_USER:guest}
  - spring.rabbitmq.password: ${RABBITMQ_PASSWORD:guest}
  - Domain event bus exchange and routing are set under app.rabbitmq.* (see Messaging section).
- Security/JWT (dev defaults provided)
  - security.jwt.secret-key: ${JWT_SECRET:...}
  - security.jwt.expiration-time: ${JWT_EXPIRATION:3600000}
- CORS
  - app.cors.allowed-origins defaults to http://localhost:5173; adjust per UI host.

Docker (optional, recommended for local env)
- docker-compose.yml includes services for Postgres and RabbitMQ (if present in your environment). Otherwise, export the environment variables above or run corresponding local services.

Testing
- Unit tests do not require Spring context; integration and @SpringBootTest will boot the app and require DB/RabbitMQ if they touch infrastructure.
- Run all tests: ./gradlew test
- Run one test class: ./gradlew test --tests "dev.eduzavarce.pets.auth.users.domain.UserDomainTest"
- Guidelines for new tests
  - Prefer pure domain tests over Spring context tests for speed and determinism.
  - Only use @SpringBootTest when necessary (e.g., wiring, controllers, or messaging listeners).
  - For domain tests that need domain events, create the aggregate via its factory and assert pullDomainEvents(); do not mock AggregateRoot.
  - For infrastructure tests (RabbitMQ, JPA), isolate with @DataJpaTest or dedicated slices when possible; prefer testcontainers for DB/messaging if running outside Docker.
- Example domain test flow (verified locally):
  - Arrange a CreateUserDto with valid data
  - Act by calling User.create(dto)
  - Assert: user.toPrimitives() values and that user.pullDomainEvents() contains a single UserCreated with eventName "user.created"

CreateUserService Use Case (Canonical Pattern)
- Location: src/main/java/dev/eduzavarce/pets/auth/users/application/CreateUserService.java
- Responsibilities
  - Hash password via PasswordHasher port (domain-level dependency inversion)
  - Validate non-existence (id/email) via AuthUserRepository (infrastructure port)
  - Construct User aggregate via factory method User.create(CreateUserDto)
  - Persist the write model (UserPostgresEntity) through repository
  - Publish domain events via EventBus by calling eventBus.publish(user.pullDomainEvents())
- Key Properties
  - The service operates on DTOs from the infrastructure/controller layer: CreateUserRequest -> CreateUserDto
  - It never exposes persistence entities to the domain; mapping is at the boundary (UserPostgresEntity(createUserDto))
  - Event publishing is decoupled via EventBus; the concrete implementation is RabbitMqEventBus

User Aggregate (Domain)
- Aggregate Root: User extends AggregateRoot
  - AggregateRoot stores and exposes domain events with protected record(...) and public pullDomainEvents()
- Value Objects (one per attribute)
  - UserId extends Identifier
  - Username extends StringValueObject with validation (non-null, non-empty, length <= 20)
  - UserEmail extends StringValueObject with extensive validation and normalization (lowercased, RFC-ish checks; disposable domains rejected)
  - UserPassword extends StringValueObject (treated as hashed at the application boundary; validation is delegated to hashing step or future policies)
  - UserRole enum with ROLE_USER default
- Construction Pattern
  - Private constructors; aggregate instances created only through static factories
  - User.create(CreateUserDto dto)
    - Wraps primitives into VO instances
    - Sets default role (ROLE_USER)
    - Records UserCreated domain event with event name "user.created" and body = createdUser.toPrimitives()
  - User.fromPrimitives(UserDto) for rehydration from read model or external sources (no password)
  - User.toPrimitives() exposes a UserDto snapshot without password

Messaging and Eventing
- Event abstraction: EventBus (domain port) -> RabbitMqEventBus (adapter)
  - RabbitMqEventBus publishes serialized DomainEventDto through RabbitTemplate
  - Routing key format: "{app.rabbitmq.routing-prefix}.{aggregateId}.{eventName}"; default prefix is "events"
- Exchange and Queue Binding (RabbitMQConfig)
  - TopicExchange name from app.rabbitmq.exchange (default: domain-events)
  - Queues: user-created-log.q, user-created-pets.q (configurable)
  - Bindings use topic patterns: events.#.user.created (so all user.created events from any aggregate id are routed)
- Consumers (examples)
  - LogSuccessOnUserCreated (auth users context): logs upon UserCreated
  - CreatePetsUserOnUserCreated (pets context): reacts to UserCreated to mirror a PetUser in pets BC

Repository and Entity Mapping
- AuthUserRepository: Spring Data JPA repository for UserPostgresEntity; domain remains persistence-agnostic.
- UserPostgresEntity persists the write model; it is created from CreateUserDto (in boundary) not from domain aggregate; this enforces purity of domain model.
- Similar pattern exists in pets context: PetsUserRepository, PetsUserPostgresEntity, and the PetUser aggregate with its own PetUserCreated event and application service.

Extending with New Use Cases (Follow the Canonical Pattern)
1. Define the use case input DTO (application layer) and request model (infrastructure layer) explicitly.
2. Model or extend the Aggregate
   - Add Value Objects for new attributes with validation
   - Keep constructors private; expose static factory method(s)
   - Record domain events inside factories or state-changing methods
3. Define ports
   - Domain ports: EventBus, domain services as interfaces when needed
   - Infrastructure ports: repositories as interfaces (Spring Data or custom) consumed by application services
4. Application service orchestration
   - Validate preconditions (e.g., ensureNonExistence or invariants)
   - Call aggregate factory/methods
   - Persist via repository
   - Publish pullDomainEvents() through EventBus
5. Integration
   - Expose controller endpoints in infrastructure layer, mapping HTTP -> request -> dto
   - Add outbox/async handlers or RabbitMQ listeners as needed for cross-context reactions

Code Style and Conventions
- Domain layer
  - No framework annotations or dependencies; business logic only
  - Value objects encapsulate validation and normalization; throw domain-specific exceptions (e.g., InvalidUserEmailException)
  - Aggregates are immutable by default; any state changes should return new instances or be funneled through methods that record events
- Application layer
  - Orchestrates domain and ports; no business validations beyond invariants and coordination
  - Avoid exposing persistence classes
- Infrastructure layer
  - Adapters only; map DTOs and entities; keep @Component/@Service/@Repository annotations here
  - Rabbit/JPA config lives under shared.core.infrastructure (cross-cutting) or context-specific infra

Testing Strategy
- Domain
  - Unit tests target pure domain; run fast and isolated
  - Test examples validated locally using JUnit Jupiter (see UserDomainTest in test path for reference)
- Application
  - Mock ports (PasswordHasher, repositories, EventBus) and assert orchestration and invariants
- Infrastructure
  - Optional integration tests with @SpringBootTest or slice tests; prefer Testcontainers when not using local Docker services

Operational Notes
- Changing event names or routing-prefix requires coordinated updates in RabbitMQConfig consumers and cross-context listeners
- Avoid leaking passwords: toPrimitives() for User excludes password by design; treat hashing at the boundary (PasswordHasher)
- For production hardening: replace ddl-auto=update with versioned migrations; configure RabbitMQ dead-letter queues and retry policies

Appendix: Verified Test Example
- A minimal domain test demonstrating the aggregate factory and event recording was executed locally during preparation:
  - User.create(CreateUserDto) produces a UserDto snapshot and a single UserCreated event with name "user.created"
  - Invalid input (email format, blank username) raises domain exceptions from value objects
- This pattern should be followed to add tests for new aggregates and use cases.
