# AeroSecure Codebase Explanation Guide

This document is designed to help you understand every part of the AeroSecure backend. If you are asked questions like "Why did you do this?" or "What does this file do?", this guide contains all the answers.

---

## 1. Architectural Concept: Layered Architecture

The project follows a standard **Spring Boot Layered Architecture**. We separate the application into distinct layers so each part only has one job (Separation of Concerns). 

**What happens if we don't use it?** If everything was in one single file, the code would be a nightmare to read, debug, and test. If you want to change the database, you'd have to rewrite business logic. Layers prevent this.

The flow of data is strictly:
`Client (Postman/Browser) <--> Controller <--> Service <--> Repository <--> Database`

---

## 2. Package & File Breakdown

### A. The Root File
#### `AeroSecureApplication.java`
* **What it does:** The main entry point of the Spring Boot application. It contains the `public static void main` method.
* **Why it's there:** Java apps need a starting point. The `@SpringBootApplication` annotation tells Spring to auto-configure everything, scan for components (like controllers and services), and start the built-in Tomcat server.
* **If it's missing:** The application simply cannot start.

### B. `entity` Package (The Database Blueprint)
Entities are Java classes that map exactly to tables in your MySQL database.
* **`Aircraft.java` & `User.java`:** Map to `aircraft` and `users` tables. They use JPA annotations like `@Entity`, `@Id`, and `@Column`.
* **`AircraftStatus.java` & `Role.java`:** Enums limiting the possible values (e.g., an aircraft can only be ACTIVE, UNDER_MAINTENANCE, or RETIRED).
* **Why they exist:** Hibernate (our ORM) reads these files to automatically write SQL for us. 
* **If missing:** The app wouldn't recognize our data structure, Hibernate couldn't create the tables, and the Database couldn't talk to our Java code.

### C. `dto` Package (Data Transfer Objects)
DTOs define the exact JSON payload we expect from the frontend, and what we send back.
* **`AircraftDTO.java`:** Used to pass aircraft data between the client and controller.
* **`AuthRequest.java` / `AuthResponse.java`:** Used for login payloads.
* **`ApiResponse.java`:** A generic wrapper to ensure every API returns a consistent JSON structure (`success`, `message`, `data`).
* **Why they exist:** NEVER expose your raw database Entities to the internet wrapper. If you add a "password" field to a User Entity, you don't want it accidentally leaking in a response. DTOs act as a selective filter.
* **If missing:** We would have to expose Database entities directly, creating massive security risks and rigid, brittle APIs.

### D. `repository` Package (The Database Workers)
* **`AircraftRepository.java` & `UserRepository.java`:** Interfaces extending `JpaRepository`.
* **What they do:** They provide free, out-of-the-box methods like `save()`, `findById()`, and `findAll()`.
* **Why they exist:** Instead of manually writing `SELECT * FROM aircraft...` in JDBC, Spring Data JPA automatically writes the SQL based on the method names (like `findByStatus`).
* **If missing:** We would have to manually open database connections, write SQL queries as strings, and manually parse the results, which is millions of lines of boilerplate code.

### E. `service` Package (The Brain)
* **`AircraftService.java` (Interface) & `AircraftServiceImpl.java` (Class):**
* **What they do:** This is where the core business logic lives. For example, validating if an aircraft can be updated, catching errors, calling the repository, and converting Entities to DTOs.
* **Why they exist:** Controllers shouldn't talk to the database directly. If you need to add logic (e.g., "Send an email when an aircraft is retired"), it goes in the Service. 
* **If missing:** Controllers would swell with massive amounts of code, making them untestable and violating the Single Responsibility Principle.

### F. `controller` Package (The Traffic Cops)
* **`AircraftController.java` & `AuthController.java`:**
* **What they do:** Listen for HTTP actions (GET, POST, PUT, DELETE) on specific URLs (like `/api/aircraft`), read the JSON body, hand the job to the Service layer, and return the HTTP response code (200 OK, 201 CREATED).
* **Why they exist:** To expose our Java application's internal functions to the outside web.
* **If missing:** Nobody can access the application from the outside.

### G. `exception` Package (Error Handling)
* **`ResourceNotFoundException` & `BadRequestException`:** Custom error classes.
* **`GlobalExceptionHandler.java`:** Marked with `@RestControllerAdvice`.
* **What it does:** It intercepts ANY crash or exception that happens ANYWHERE in the app, preventing the user from seeing an ugly stack trace. Instead, it converts the error into a clean JSON response using our `ApiResponse`.
* **If missing:** If a user searches for an ID that doesn't exist, Spring Boot returns a generic "Whitelabel 500 Server Error" HTML page, which breaks frontend applications expecting JSON.

### H. `security` Package (The Bouncers)
* **`JwtTokenProvider.java`:** Generates and reads secure JWT strings.
* **`JwtAuthenticationFilter.java`:** Intercepts every single HTTP request. Looks at the `Authorization` header, extracts the JWT, proves it isn't tampered with, and tells Spring Boot "This person is Admin".
* **`CustomUserDetailsService.java`:** Connects Spring Security to our MySQL `UserRepository`.
* **`SecurityConfig.java`:** The master configuration. Tells the app endpoints like `/api/auth/login` don't require passwords, but `POST /api/aircraft` strictly requires `ROLE_ADMIN`.
* **Why it exists:** To prevent random people on the internet from dumping, creating, or destroying our fleet database.
* **If missing:** Anyone with Postman could delete all our database records instantaneously.

### I. `config` Package and `resources/`
* **`OpenApiConfig.java`:** Generates the amazing Swagger visual UI docs automatically by skimming our controllers.
* **`application.properties`:** Holds our DB passwords, port numbers, and JWT secrets.
* **`schema.sql` & `data.sql`:** Scripts that automatically prep the empty MySQL database with tables and test records. 

---

## 3. Core Concepts To Understand

**1. What is a JWT (JSON Web Token)?**
When a user logs in, the server generates a cryptographically signed string. The user includes this string in the Header of their future HTTP requests. It allows the server to recognize the user **statelessly** (the server doesn't have to keep track of logged-in sessions in memory, saving huge RAM space).

**2. Why do you use `@Autowired`?**
This is Spring's "Dependency Injection". Instead of writing `AircraftRepository repo = new AircraftRepository();` in every class, Spring creates exactly one instance of the repository when the app starts, and automatically "injects" (wires) it into the Service. It makes code easier to test and highly memory efficient.

**3. Why use `BCrypt` for passwords?**
If a hacker steals the database, they cannot see the real passwords. BCrypt hashes them with mathematically complex algorithms and "salt" so a password like `admin123` appears as `$2a$10$2P4rnO...`

---

## 4. API Testing Guide (How to demonstrate it works)

When asked to demonstrate the app, follow this exact sequence:

1. **Start the application.** (`mvn spring-boot:run`)
2. Show them that **Swagger UI** exists at `http://localhost:8080/swagger-ui.html`. This proves your API is well-documented.
3. Open **Postman** (or use Swagger) to prove security works:
   * Try `GET http://localhost:8080/api/aircraft`. Explain that the server returned **401 Unauthorized** because you didn't provide a token.
4. Prove **Login** works:
   * Perform `POST http://localhost:8080/api/auth/login` with body:
     `{"username": "admin", "password": "admin123"}`
   * Explain that exactly here, the `CustomUserDetailsService` checks MySQL, verifies the BCrypt match, and `JwtTokenProvider` hands back the Token.
   * **Copy the token.**
5. Prove **Authorization / Role-based Access** works:
   * Open `POST http://localhost:8080/api/aircraft` (Add Aircraft)
   * Go to "Auth" tab, select "Bearer Token", and paste the token. Add a JSON body.
   * Hit Send. Explain that the `JwtAuthenticationFilter` parsed the token, realized you had `ROLE_ADMIN`, and `SecurityConfig` permitted the request. The controller called the Service, which told the Repository to `save()` to MySQL.
6. Prove **Global Exception Handling** works:
   * Do `GET http://localhost:8080/api/aircraft/999` (ID 999 doesn't exist).
   * Note the clean JSON response: `"success": false, "message": "Aircraft not found..."`
   * Explain that `AircraftServiceImpl` threw a `ResourceNotFoundException`, and `GlobalExceptionHandler` elegantly intercepted it.
