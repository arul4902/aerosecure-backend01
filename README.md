# AeroSecure - Aircraft Registration & Fleet Management System

> Production-ready Spring Boot backend for aircraft registration and fleet management with JWT authentication, role-based access control, and Swagger documentation.

---

## 🛠️ Tech Stack

| Component  | Technology                     |
|------------|--------------------------------|
| Backend    | Java 17 + Spring Boot 3.2.5   |
| Database   | MySQL 8                        |
| ORM        | Hibernate / JPA                |
| Security   | Spring Security + JWT (jjwt)   |
| API Docs   | SpringDoc OpenAPI (Swagger UI) |
| Build      | Maven                          |

---

## 📁 Project Structure

```
src/main/java/com/aerosecure/
├── AeroSecureApplication.java       # Main entry point
├── config/
│   ├── DataInitializer.java         # Sample data seeder
│   └── OpenApiConfig.java           # Swagger/OpenAPI config
├── controller/
│   ├── AircraftController.java      # Aircraft REST endpoints
│   └── AuthController.java          # Authentication endpoint
├── dto/
│   ├── AircraftDTO.java             # Aircraft request/response DTO
│   ├── ApiResponse.java             # Generic API response wrapper
│   ├── AuthRequest.java             # Login request DTO
│   └── AuthResponse.java            # Login response DTO
├── entity/
│   ├── Aircraft.java                # Aircraft JPA entity
│   ├── AircraftStatus.java          # Status enum
│   ├── Role.java                    # Role enum
│   └── User.java                    # User JPA entity
├── exception/
│   ├── BadRequestException.java     # 400 exception
│   ├── GlobalExceptionHandler.java  # @ControllerAdvice handler
│   └── ResourceNotFoundException.java # 404 exception
├── repository/
│   ├── AircraftRepository.java      # Aircraft JPA repository
│   └── UserRepository.java          # User JPA repository
├── security/
│   ├── CustomUserDetailsService.java # User details loader
│   ├── JwtAuthenticationEntryPoint.java # 401 handler
│   ├── JwtAuthenticationFilter.java  # JWT filter
│   ├── JwtTokenProvider.java         # JWT utility
│   └── SecurityConfig.java           # Security configuration
└── service/
    ├── AircraftService.java          # Service interface
    └── AircraftServiceImpl.java      # Service implementation
```

---

## 🚀 Setup & Run

### Prerequisites

- **Java 17+** installed
- **MySQL 8** running on `localhost:3306`
- **Maven** installed (or use the Maven wrapper)

### Step 1: Create MySQL Database

```sql
CREATE DATABASE IF NOT EXISTS aerosecure_db;
```

> The application will auto-create tables via Hibernate `ddl-auto=update`.

### Step 2: Configure Database Credentials

Edit `src/main/resources/application.properties` if your MySQL credentials differ:

```properties
spring.datasource.username=root
spring.datasource.password=root
```

### Step 3: Build & Run

```bash
# Build the project
mvn clean install -DskipTests

# Run the application
mvn spring-boot:run
```

The server starts at: **http://localhost:8080**

### Step 4: Access Swagger UI

Open in browser: **http://localhost:8080/swagger-ui.html**

---

## 🔐 Authentication

### Default Users (Auto-created on startup)

| Username   | Password      | Role     | Access              |
|------------|---------------|----------|---------------------|
| `admin`    | `admin123`    | ADMIN    | Full CRUD access    |
| `engineer` | `engineer123` | ENGINEER | Read-only access    |

### Login & Get JWT Token

```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "tokenType": "Bearer",
    "username": "admin",
    "role": "ROLE_ADMIN"
  },
  "timestamp": "2026-04-19T12:00:00"
}
```

### Use Token in Subsequent Requests

Add this header to all protected API calls:

```
Authorization: Bearer <your_jwt_token>
```

---

## 📡 API Endpoints

### Authentication

| Method | Endpoint           | Access  | Description           |
|--------|--------------------|---------|-----------------------|
| POST   | `/api/auth/login`  | Public  | Login & get JWT token |

### Aircraft Management

| Method | Endpoint                              | Access          | Description               |
|--------|---------------------------------------|-----------------|---------------------------|
| POST   | `/api/aircraft`                       | ADMIN           | Create new aircraft       |
| GET    | `/api/aircraft?page=0&size=5&sortBy=model` | ADMIN, ENGINEER | Get all (paginated)  |
| GET    | `/api/aircraft/{id}`                  | ADMIN, ENGINEER | Get by ID                 |
| PUT    | `/api/aircraft/{id}`                  | ADMIN           | Update aircraft           |
| DELETE | `/api/aircraft/{id}`                  | ADMIN           | Delete aircraft           |
| GET    | `/api/aircraft/status/{status}`       | ADMIN, ENGINEER | Filter by status          |
| GET    | `/api/aircraft/search?manufacturer=Boeing` | ADMIN, ENGINEER | Search by manufacturer |

---

## 🧪 Postman Testing Guide

### Step 1: Login as Admin

```
POST http://localhost:8080/api/auth/login

Body (JSON):
{
  "username": "admin",
  "password": "admin123"
}
```

Copy the `token` from the response.

### Step 2: Create Aircraft

```
POST http://localhost:8080/api/aircraft
Authorization: Bearer <token>

Body (JSON):
{
  "model": "Boeing 737",
  "manufacturer": "Boeing",
  "status": "ACTIVE"
}
```

**Expected:** `201 CREATED`

### Step 3: Get All Aircraft (Paginated)

```
GET http://localhost:8080/api/aircraft?page=0&size=5&sortBy=model
Authorization: Bearer <token>
```

**Expected:** `200 OK` with paginated results

### Step 4: Get Aircraft by ID

```
GET http://localhost:8080/api/aircraft/1
Authorization: Bearer <token>
```

**Expected:** `200 OK`

### Step 5: Update Aircraft

```
PUT http://localhost:8080/api/aircraft/1
Authorization: Bearer <token>

Body (JSON):
{
  "model": "Airbus A320",
  "manufacturer": "Airbus",
  "status": "UNDER_MAINTENANCE"
}
```

**Expected:** `200 OK`

### Step 6: Delete Aircraft

```
DELETE http://localhost:8080/api/aircraft/1
Authorization: Bearer <token>
```

**Expected:** `200 OK`

### Step 7: Filter by Status

```
GET http://localhost:8080/api/aircraft/status/ACTIVE
Authorization: Bearer <token>
```

**Expected:** `200 OK` with filtered list

### Step 8: Search by Manufacturer

```
GET http://localhost:8080/api/aircraft/search?manufacturer=Boeing
Authorization: Bearer <token>
```

**Expected:** `200 OK` with search results

### Step 9: Test Role-Based Access (Engineer)

```
POST http://localhost:8080/api/auth/login

Body (JSON):
{
  "username": "engineer",
  "password": "engineer123"
}
```

Use the engineer token to try `POST /api/aircraft`:

**Expected:** `403 FORBIDDEN` (Engineer has read-only access)

### Step 10: Test Without Token

```
GET http://localhost:8080/api/aircraft
(No Authorization header)
```

**Expected:** `401 UNAUTHORIZED`

---

## 📊 Sample Aircraft Data (Auto-seeded)

| Model               | Manufacturer | Status             |
|---------------------|--------------|--------------------|
| Boeing 737-800      | Boeing       | ACTIVE             |
| Airbus A320neo      | Airbus       | ACTIVE             |
| Boeing 777-300ER    | Boeing       | UNDER_MAINTENANCE  |
| Embraer E195-E2     | Embraer      | ACTIVE             |
| Bombardier CRJ-900  | Bombardier   | RETIRED            |
| Airbus A350-1000    | Airbus       | ACTIVE             |
| Boeing 787 Dreamliner | Boeing     | UNDER_MAINTENANCE  |
| ATR 72-600          | ATR          | ACTIVE             |

---

## 📝 HTTP Response Codes

| Code | Status             | When                              |
|------|--------------------|-----------------------------------|
| 200  | OK                 | Successful GET, PUT, DELETE       |
| 201  | CREATED            | Successful POST (create)          |
| 400  | BAD REQUEST        | Validation errors, bad input      |
| 401  | UNAUTHORIZED       | Missing or invalid JWT token      |
| 403  | FORBIDDEN          | Insufficient role permissions     |
| 404  | NOT FOUND          | Resource not found                |
| 500  | INTERNAL ERROR     | Unexpected server error           |
