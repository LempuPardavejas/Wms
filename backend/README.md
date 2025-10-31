# WMS Backend - Spring Boot REST API

## Quick Start

### Run Application

```bash
mvn spring-boot:run
```

Server starts on: http://localhost:8080

### Run Tests

```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report

# Run specific test
mvn test -Dtest=OrderServiceTest
```

### View Coverage Report

```bash
mvn test jacoco:report
open target/site/jacoco/index.html
```

## Test Structure

```
src/test/java/lt/elektromeistras/
├── service/                  # Unit tests
│   ├── OrderServiceTest.java
│   └── ReturnServiceTest.java
└── integration/              # Integration tests
    └── OrderControllerIntegrationTest.java
```

## Test Features

- ✅ 26+ OrderService unit tests
- ✅ 20+ ReturnService unit tests
- ✅ 10+ API integration tests
- ✅ H2 in-memory database for tests
- ✅ Mockito for mocking
- ✅ AssertJ for assertions
- ✅ JaCoCo code coverage

## API Endpoints

### Orders
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/customer/{customerId}` - Get customer orders
- `POST /api/orders/quick` - Create quick order
- `POST /api/orders/{id}/confirm` - Confirm order

### Returns
- `POST /api/returns` - Create return
- `POST /api/returns/{id}/approve` - Approve return
- `POST /api/returns/{id}/inspect` - Inspect return
- `POST /api/returns/{id}/restock` - Restock items
- `POST /api/returns/{id}/refund` - Process refund

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/wms_db
spring.datasource.username=wms_user
spring.datasource.password=wms_password

# JWT
jwt.secret=your-secret-key-here
jwt.expiration=86400000
```

## Dependencies

- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security
- PostgreSQL Driver
- Liquibase
- Lombok
- JUnit 5
- Mockito
- AssertJ

## Database Migrations

Liquibase migrations in `src/main/resources/db/changelog/`:

- `001-create-base-tables.xml`
- `002-create-product-tables.xml`
- `003-create-customer-tables.xml`
- `004-create-order-tables.xml`
- `005-create-warehouse-tables.xml`
- `009-create-return-tables.xml`

## Build

```bash
# Clean and build
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Package as JAR
mvn package
```

## Run JAR

```bash
java -jar target/wms-backend-1.0.0-SNAPSHOT.jar
```

## Troubleshooting

**Tests fail with database error:**
- Tests use H2, not PostgreSQL
- Check `src/test/resources/application-test.properties`

**Application won't start:**
- Ensure PostgreSQL is running
- Check database credentials in `application.properties`

**Port already in use:**
- Change port: `server.port=8081` in `application.properties`
