# CPT202Program

A web-services backend built with **Spring Boot 3** (Spring Framework), tested with **JUnit 5 + Mockito**, and shipped via a **GitHub Actions CI/CD pipeline**.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 (Spring MVC, Spring Data JPA) |
| Database | H2 (in-memory, for dev/test) |
| Build tool | Maven 3 |
| Testing | JUnit 5 · Mockito · AssertJ · Spring MockMvc |
| CI/CD | GitHub Actions |

---

## Directory Structure

```
CPT202Program/
├── .github/
│   └── workflows/
│       └── ci.yml                   # CI/CD pipeline
├── src/
│   ├── main/
│   │   ├── java/com/cpt202/
│   │   │   ├── CPT202Application.java        # Entry point
│   │   │   ├── controller/
│   │   │   │   ├── HelloController.java      # /api/hello, /api/health
│   │   │   │   └── ItemController.java       # /api/items  (CRUD)
│   │   │   ├── service/
│   │   │   │   ├── HelloService.java         # Greeting logic
│   │   │   │   └── ItemService.java          # Item business logic
│   │   │   ├── model/
│   │   │   │   ├── ApiResponse.java          # Generic response wrapper
│   │   │   │   └── Item.java                 # JPA entity
│   │   │   └── repository/
│   │   │       └── ItemRepository.java       # Spring Data JPA repo
│   │   └── resources/
│   │       ├── application.properties        # General properties & DB config
│   │       ├── firstPage_front/              # Basic HTML views (Login/Register)
│   │       └── static/teacher-review/        # Teacher frontend application
│   └── test/
│       └── java/com/cpt202/
│           ├── CPT202ApplicationTests.java   # Context smoke test
│           ├── controller/
│           │   ├── HelloControllerTest.java  # MockMvc slice tests
│           │   └── ItemControllerTest.java   # MockMvc CRUD slice tests
│           └── service/
│               ├── HelloServiceTest.java     # Pure unit tests
│               └── ItemServiceTest.java      # Mockito unit tests
├── .gitignore
├── pom.xml
└── README.md
```

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+

### Run locally

```bash
mvn spring-boot:run
```

The server starts on **http://localhost:8080**.

### Try the endpoints

```bash
# Health check
curl http://localhost:8080/api/health

# Greeting
curl "http://localhost:8080/api/hello?name=Alice"

# Create an item
curl -X POST http://localhost:8080/api/items \
     -H "Content-Type: application/json" \
     -d '{"name":"Widget","description":"A sample widget"}'

# List all items
curl http://localhost:8080/api/items
```

### Run tests

```bash
mvn test
```

---

## CI/CD Pipeline

The GitHub Actions workflow (`.github/workflows/ci.yml`) runs on every push and pull-request to `main` / `master` / `develop`:

1. **Build** – compiles the project with Maven.
2. **Test** – executes all JUnit 5 tests and uploads the Surefire report as an artefact.
3. **Package** *(main/master only)* – creates the executable JAR and uploads it as an artefact.

