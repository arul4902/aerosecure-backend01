# AeroSecure Backend - Technical Interview Guide

This guide is designed to prepare you for a technical interview based on your AeroSecure Backend project. It breaks down the entire system step-by-step.

---

## 1. PROJECT OVERVIEW

**What the project does (in simple terms):**
AeroSecure is an Aircraft Maintenance & Compliance Management System. This specific backend module handles "Aircraft Registration & Fleet Management." It provides an API for admins to register and manage aircraft, and for engineers to view the fleet's status. It includes secure login (Authentication) to ensure only authorized personnel can access or modify the data.

**Real-world use case:**
An airline or aviation maintenance company would use this system to keep track of their planes. If a plane lands and needs repairs, an admin can update its status to `UNDER_MAINTENANCE` so it isn't assigned to flights.

**Architecture style:**
**Monolith (Layered Architecture).** All components (authentication, aircraft management, database access) are bundled into a single deployable application.

**High-level flow:**
A client (e.g., Postman, Frontend web app) sends an HTTP Request (like "Give me all aircraft"). The request is intercepted by Security (JWT Filter) to ensure the user is valid. If valid, the Controller receives the request, passes it to the Service layer (business logic), which talks to the Repository layer (database access) to fetch data from MySQL. The data is converted to a DTO (Data Transfer Object) and sent back as a JSON Response.

---

## 2. PROJECT STRUCTURE

The project follows the standard **Spring Boot 3-Tier Layered Architecture**:

- `src/main/java/com/aerosecure/`
  - `controller/`: The **Presentation Layer**. Handles incoming HTTP requests, maps them to URLs, and returns HTTP responses.
  - `service/`: The **Business Layer**. Contains the core logic. It dictates *what* should happen when a request comes in.
  - `repository/`: The **Data Access Layer (DAO)**. Interfaces directly with the database using Spring Data JPA.
  - `entity/`: The **Domain Layer**. Java classes that map directly to database tables (ORM).
  - `dto/`: **Data Transfer Objects**. Objects used to transfer data between the client and the server without exposing the raw database entities.
  - `security/`: Contains all configuration and filters for JWT-based Authentication and Authorization.
  - `exception/`: Global error handling logic so the API returns consistent, clean error messages.
  - `config/`: Application configuration classes (like Swagger/OpenAPI setup).
- `src/main/resources/`
  - `application.properties`: Configuration file for database connection, JWT secret, server port, etc.
  - `schema.sql` & `data.sql`: Scripts to automatically create database tables and insert initial dummy data when the app starts.

**Why these layers exist? (Separation of Concerns)**
If we change our database from MySQL to PostgreSQL, we only touch the `repository` and `application.properties`. If we change how data is presented, we only touch the `controller` and `dto`. The `service` layer remains untouched. This makes the code maintainable, scalable, and testable.

---

## 3. FILE-BY-FILE BREAKDOWN

### `com.aerosecure.AeroSecureApplication.java`
- **Purpose:** The entry point of the Spring Boot application.
- **Important Code:** The `main` method calls `SpringApplication.run()`, which starts the embedded Tomcat server and initializes the Spring application context (Dependency Injection container).

### Presentation Layer (Controllers)
#### `AircraftController.java`
- **Purpose:** Exposes REST endpoints for CRUD operations on aircraft.
- **Line-by-line concept:**
  - `@RestController`, `@RequestMapping("/api/aircraft")`: Defines this as a REST controller base URL.
  - `@Autowired private AircraftService aircraftService;`: Injects the service layer.
  - `@PostMapping`, `@GetMapping`: Maps HTTP methods to Java methods.
  - `@PreAuthorize("hasRole('ADMIN')")`: Ensures only users with the ADMIN role can execute specific methods (like creating or deleting aircraft).
  - `ResponseEntity<ApiResponse<AircraftDTO>>`: Returns standard HTTP responses wrapped in a custom `ApiResponse` envelope for consistency.
- **Why it exists:** To be the front door of the application. It shouldn't contain business logic, only routing.

#### `AuthController.java`
- **Purpose:** Handles user login and JWT token generation.
- **Key Flow:** It takes a username and password, uses the `AuthenticationManager` to verify them against the database. If successful, it asks `JwtTokenProvider` to generate a JWT token and returns it to the client.

### Business Layer (Services)
#### `AircraftService.java` (Interface)
- **Purpose:** Defines the contract for aircraft operations. Useful for loose coupling.

#### `AircraftServiceImpl.java` (Class)
- **Purpose:** Implements the interface and contains actual business logic.
- **Line-by-line concept:**
  - `@Service`: Tells Spring to manage this class.
  - `@Transactional`: Ensures that methods are wrapped in database transactions. If something fails mid-way, the transaction is rolled back.
  - `aircraftRepository.findById(id).orElseThrow(...)`: Tries to find an aircraft; if it fails, it throws a custom exception rather than returning null.
  - `mapToDTO()` / `mapToEntity()`: Helper methods to convert between Database Entities and client-facing DTOs.
- **Why it exists:** Keeps business rules out of controllers and repositories.

### Data Access Layer (Repositories)
#### `AircraftRepository.java` & `UserRepository.java`
- **Purpose:** Interfaces extending `JpaRepository`.
- **Key Concept:** You don't write SQL here. By extending `JpaRepository`, Spring Data JPA automatically provides methods like `save()`, `findAll()`, `findById()`, and `delete()`.
- **Custom Methods:** `findByStatus(AircraftStatus status)` is automatically translated into `SELECT * FROM aircraft WHERE status = ?` by Spring Data JPA just based on the method name.

### Domain Layer (Entities & DTOs)
#### `Aircraft.java` & `User.java` (Entities)
- **Purpose:** Blueprint for database tables.
- **Important Code:**
  - `@Entity`, `@Table(name = "aircraft")`: Maps class to a table.
  - `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`: Marks the primary key and auto-increment behavior.
  - `@NotBlank`, `@NotNull`: Jakarta Validation rules to prevent invalid data from hitting the database.
  - `@PrePersist`, `@PreUpdate`: JPA lifecycle hooks that automatically set `created_at` and `updated_at` timestamps before saving.

#### `AircraftDTO.java`, `AuthRequest.java`, `ApiResponse.java` (DTOs)
- **Purpose:** Carry data between processes. `ApiResponse` is a generic wrapper to ensure every API response has the same format (`{ "success": true, "message": "...", "data": ... }`).

### Security Layer
#### `SecurityConfig.java`
- **Purpose:** The main security firewall configuration.
- **Key logic:** Disables CSRF (common for stateless REST APIs), configures sessions to be `STATELESS` (because we use JWT), and defines which endpoints are public (`/api/auth/**`) and which require specific roles (`hasRole("ADMIN")`). It registers the `JwtAuthenticationFilter`.

#### `JwtAuthenticationFilter.java`
- **Purpose:** Intercepts every single incoming request.
- **Key logic:** It extracts the `Authorization: Bearer <token>` header, validates the token using `JwtTokenProvider`, extracts the username, loads the user details, and sets the user as "Authenticated" in the `SecurityContextHolder`.

#### `JwtTokenProvider.java`
- **Purpose:** Utility class to create and verify JWT tokens using the `io.jsonwebtoken` library and a secret key.

#### `CustomUserDetailsService.java`
- **Purpose:** Tells Spring Security *how* to find a user in your database (by calling `userRepository.findByUsername`).

### Error Handling
#### `GlobalExceptionHandler.java`
- **Purpose:** A centralized error handler (`@RestControllerAdvice`).
- **Key logic:** Instead of a try-catch block in every controller method, if a `ResourceNotFoundException` is thrown anywhere in the app, this class catches it and returns a neat `404 Not Found` JSON response.

---

## 4. ANNOTATIONS EXPLANATION

| Annotation | What it does | Why it is used | What happens if removed? |
| :--- | :--- | :--- | :--- |
| **@RestController** | Marks a class as a Spring MVC controller that returns data (JSON/XML) directly to the response body. | To create REST APIs. | Spring won't route HTTP requests to this class, and it won't automatically serialize returns to JSON. |
| **@RequestMapping** | Maps a specific URL path to an entire class or method. | To define the base URL (e.g., `/api/aircraft`). | You'd have to specify the full URL on every single method. |
| **@Service** | Marks a class as a business logic provider. | To let Spring's Component Scan find it and inject it as a Bean. | Spring won't create an instance of it, leading to a `NullPointerException` when a controller tries to `@Autowired` it. |
| **@Repository** | Marks a class as a Data Access Object. | Tells Spring to handle database exceptions gracefully and creates a Bean. | Spring Data JPA won't generate the implementation for the interface; database calls will fail. |
| **@Entity** | Marks a Java class as a JPA entity. | Tells Hibernate to map this class to a relational database table. | Hibernate will ignore the class; no table will be created or queried. |
| **@Autowired** | Asks Spring to inject an instance of an object (Dependency Injection). | To get an instance of a Service or Repository without writing `new Object()`. | The variable remains `null`, causing a `NullPointerException` on use. |
| **@Transactional** | Wraps a method in a database transaction. | To ensure all database operations in the method either completely succeed or completely fail (ACID properties). | If an error occurs halfway through a complex operation, partial/corrupted data might be left in the database. |
| **@PreAuthorize** | Method-level security. Evaluates an expression before allowing method execution. | To restrict methods based on user roles (`hasRole('ADMIN')`). | Any authenticated user could access the endpoint, leading to privilege escalation. |
| **@Valid** | Triggers validation on an object. | To ensure DTOs match their constraints (e.g., `@NotBlank`) before the method runs. | Invalid data (like an empty aircraft model) would slip through to the service layer or database. |

---

## 5. DEPENDENCIES (pom.xml)

1. **`spring-boot-starter-web`**:
   - **Why**: Provides embedded Tomcat server, Spring MVC, and REST capabilities.
   - **Problem Solved**: Saves you from manually configuring a web server and servlets.
   - **Analogy**: It’s the engine and chassis of a car. Without it, you have no vehicle to drive on the web.
2. **`spring-boot-starter-data-jpa`**:
   - **Why**: Integrates Hibernate and Spring Data.
   - **Problem Solved**: Eliminates the need to write complex SQL queries and JDBC boilerplate.
3. **`spring-boot-starter-security`**:
   - **Why**: Provides robust authentication and authorization.
   - **Problem Solved**: Secures the API from unauthorized access.
4. **`mysql-connector-j`**:
   - **Why**: The JDBC driver for MySQL.
   - **Problem Solved**: Allows Java (JPA/Hibernate) to actually speak the MySQL protocol to connect to the database.
5. **`jjwt-api`, `jjwt-impl`, `jjwt-jackson`**:
   - **Why**: Libraries to generate and parse JSON Web Tokens (JWT).
   - **Problem Solved**: Spring Security doesn't handle JWTs out-of-the-box. These libraries do the heavy cryptographic lifting.
6. **`lombok`**:
   - **Why**: Auto-generates getters, setters, constructors, and builders via annotations.
   - **Problem Solved**: Removes hundreds of lines of boilerplate code, making files clean and readable.
7. **`springdoc-openapi-starter-webmvc-ui`**:
   - **Why**: Automatically generates a Swagger UI interface.
   - **Problem Solved**: You don't have to write API documentation manually.

---

## 6. DATABASE FLOW

**Flow:** `Client -> Controller -> Service -> Repository (JPA) -> Hibernate -> MySQL DB`

**ORM (Hibernate/JPA) Behavior:**
AeroSecure uses Object-Relational Mapping (ORM). You interact with Java objects (`Aircraft.java`), and Hibernate translates these interactions into SQL queries (`INSERT`, `SELECT`, `UPDATE`).
- Because `application.properties` has `spring.jpa.hibernate.ddl-auto=update`, Hibernate looks at your `@Entity` classes on startup. If the `aircraft` table doesn't exist, it creates it. If you add a new field, it alters the table.

**Query Execution Flow (Example: Find By Status):**
1. Service calls `aircraftRepository.findByStatus(AircraftStatus.ACTIVE)`.
2. Spring Data JPA intercepts this method call.
3. It parses the method name `findByStatus` and generates HQL/SQL: `SELECT * FROM aircraft WHERE status = ?`.
4. Hibernate executes this query against MySQL via the MySQL Driver.
5. Hibernate maps the returned SQL result set back into a `List<Aircraft>` Java objects.

---

## 7. REQUEST FLOW (VERY IMPORTANT)

**Scenario: An Admin sends a POST request to create an Aircraft.**

1. **Client (Postman):** Sends HTTP POST to `http://localhost:8080/api/aircraft` with a JSON body and an `Authorization: Bearer <token>` header.
2. **Tomcat Server:** Receives the raw HTTP request.
3. **JwtAuthenticationFilter:** Intercepts the request. Extracts the token. Validates the signature using the secret key. Extracts the username ("admin") and role ("ROLE_ADMIN"). Places this info into the `SecurityContextHolder`. Request is allowed to proceed.
4. **DispatcherServlet:** Spring's front controller routes the request to the `AircraftController.createAircraft()` method because of the `@PostMapping` and URL match.
5. **Spring Validation:** The `@Valid` annotation triggers. It checks the JSON body against `AircraftDTO` rules. If valid, execution enters the controller.
6. **Controller:** Checks `@PreAuthorize("hasRole('ADMIN')")`. The context says user is ADMIN. Access granted. Controller passes the DTO to `AircraftService`.
7. **Service:** `@Transactional` starts a new database transaction. `AircraftServiceImpl` maps the DTO to an `Aircraft` Entity and calls `aircraftRepository.save()`.
8. **Repository / JPA:** Hibernate translates the entity into an `INSERT INTO aircraft...` SQL statement. The `@PrePersist` hook fires, setting the `createdAt` timestamp. The SQL is executed.
9. **Database (MySQL):** The record is saved. An auto-incremented ID (e.g., `9`) is generated and returned to Hibernate.
10. **Service:** The transaction commits successfully. Service maps the saved entity back to a DTO and returns it to the Controller.
11. **Controller:** Wraps the DTO in an `ApiResponse` object and returns a `201 Created` `ResponseEntity`.
12. **Client:** Receives the JSON response.

---

## 8. KEY CONCEPTS USED

- **Dependency Injection (DI) & Inversion of Control (IoC):**
  Instead of classes creating their own dependencies (e.g., `AircraftService service = new AircraftServiceImpl()`), the Spring Framework (IoC container) creates the objects (Beans) at startup and "injects" them where needed using `@Autowired`. This makes unit testing easier (you can inject mock objects).
- **REST API Principles:**
  The project follows stateless, resource-based architecture using standard HTTP methods (GET for read, POST for create, PUT for update, DELETE for remove) and returns JSON.
- **Exception Handling:**
  Using `@RestControllerAdvice` ensures that error handling logic is separated from business logic, preventing duplicate try-catch blocks and ensuring the frontend always receives a standard error format.
- **DTO Pattern:**
  Separates the internal database schema (`Aircraft` entity) from the external API contract (`AircraftDTO`). This prevents over-posting attacks and allows the API response to be shaped differently from the database table.

---

## 9. INTERVIEW QUESTIONS

### Beginner Questions
1. **What is Spring Boot and why did you use it instead of pure Spring?**
   *Answer: Spring Boot provides auto-configuration and an embedded server (Tomcat). It removes the need for complex XML configurations and allows me to set up a production-ready application quickly.*
2. **What is the difference between `@Controller` and `@RestController`?**
   *Answer: `@RestController` is a combination of `@Controller` and `@ResponseBody`. It tells Spring that the returned object should be serialized directly into JSON/XML, rather than resolving a web view/HTML page.*
3. **What is an Entity in JPA?**
   *Answer: A plain Java class annotated with `@Entity` that maps directly to a table in a relational database.*

### Intermediate Questions
1. **Explain how Dependency Injection works in your project.**
   *Answer: I use constructor/field injection via `@Autowired`. Spring's IoC container manages the lifecycle of Beans (like `AircraftServiceImpl`). When `AircraftController` is created, Spring automatically injects the `AircraftService` bean into it.*
2. **How does JWT Authentication work statelessly in your app?**
   *Answer: Upon login, the server generates a token containing the user's identity and signs it with a secret key. The server stores nothing in memory (stateless). For subsequent requests, the client sends the token. The server validates the signature using the secret key to verify the user.*
3. **Why do you use DTOs instead of returning Entities directly?**
   *Answer: Entities contain database-specific logic and sometimes sensitive data (like passwords in User entities). DTOs decouple the database schema from the API contract, preventing accidental data exposure and allowing API versioning without changing the database.*

### Advanced Questions
1. **What happens if two users try to update the same aircraft at the exact same time?**
   *Answer: Currently, the last one to save overwrites the other (Last Commit Wins). To fix this, I would implement Optimistic Locking by adding an `@Version` column to the `Aircraft` entity. Hibernate would throw an `ObjectOptimisticLockingFailureException` if the version numbers don't match during an update.*
2. **Explain the purpose of the `@Transactional` annotation and how Spring implements it.**
   *Answer: It ensures atomic database operations. Spring implements this using AOP (Aspect-Oriented Programming). It creates a proxy around the Service class. When a method is called, the proxy opens a database connection, begins a transaction, executes the method, and then either commits (if successful) or rolls back (if an unchecked exception is thrown).*
3. **How does Spring Security know which filter to execute first?**
   *Answer: In `SecurityConfig`, I explicitly defined the order using `http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)`. This ensures my custom JWT filter runs before Spring's default authentication mechanisms.*

### Scenario-Based Questions
1. **Scenario:** The API is getting very slow when fetching the list of aircraft because the fleet grew to 50,000 planes. How do you fix it?
   *Answer: I have already implemented Pagination in `AircraftController` using `Pageable`. Instead of fetching all 50,000 records, the client requests `page=0&size=20`. This executes a `LIMIT` and `OFFSET` SQL query, which is highly efficient.*
2. **Scenario:** A new requirement says we need to log every time an aircraft's status changes to `RETIRED`. Where would you add this?
   *Answer: I would add this in the `AircraftServiceImpl.updateAircraft` method. Alternatively, to keep business logic clean, I could use Spring AOP or JPA lifecycle events (`@PostUpdate`) inside the `Aircraft` entity to trigger a logging event.*

---

## 10. SIMPLIFIED EXPLANATION (ELI5 Style)

Imagine the **AeroSecure Backend** is a high-end restaurant:

- **The Database (MySQL)** is the **Pantry**. It stores all the raw ingredients (data about aircraft and users).
- **The Entities (`Aircraft.java`)** are the **Tupperware containers** that exactly match the shelves in the pantry.
- **The Repository (`AircraftRepository.java`)** is the **Pantry Manager**. It knows exactly how to fetch containers from the pantry or put new ones in without you having to understand how the pantry is organized.
- **The Service (`AircraftServiceImpl.java`)** is the **Head Chef**. The Chef takes the raw ingredients, applies recipes (business rules), checks if the food is fresh, and prepares the meal.
- **The DTO (`AircraftDTO.java`)** is the **Fancy Plate**. The Chef doesn't serve the food in Tupperware (Entities). They plate it nicely on a DTO so it looks good for the customer.
- **The Controller (`AircraftController.java`)** is the **Waiter**. The Waiter takes orders (HTTP Requests) from the customers, gives them to the Chef, and then carries the Fancy Plate (HTTP Response) back to the customer's table.
- **Spring Security (`JwtAuthenticationFilter.java`)** is the **Bouncer at the door**. Before the Waiter even talks to you, the Bouncer checks your ID (JWT Token) to make sure you are allowed inside the restaurant. If you try to enter the VIP kitchen (Admin endpoints), the Bouncer stops you unless you have a VIP badge (`ROLE_ADMIN`).
- **Global Exception Handler** is the **Manager**. If a waiter drops a plate or a customer asks for a meal that doesn't exist (404 Not Found), the Manager steps in, apologizes politely, and explains what went wrong in a calm, consistent way.
