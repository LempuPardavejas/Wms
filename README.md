# WMS - Warehouse Management System

A comprehensive warehouse management system with order processing and customer returns functionality.

## Features

- **Order Management** - Create, confirm, and track orders
- **Quick Order Entry** - Fast order creation with product codes
- **Customer Returns** - Complete returns workflow with inspection and restocking
- **Customer Management** - Customer database with order history
- **Product Catalog** - Product search and management
- **Inventory Management** - Stock tracking and warehouse locations

## Project Structure

```
Wms/
├── backend/          # Spring Boot REST API
├── frontend/         # React TypeScript UI
└── TESTING_GUIDE.md  # Complete testing documentation
```

## Quick Start

### Backend (Spring Boot)

```bash
cd backend
mvn spring-boot:run
```

Backend runs on: http://localhost:8080

### Frontend (React + Vite)

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on: http://localhost:3000

## Testing

### Run All Backend Tests

```bash
cd backend
mvn test
```

### Run All Frontend Tests

```bash
cd frontend
npm test
```

### View Full Testing Guide

See [TESTING_GUIDE.md](TESTING_GUIDE.md) for complete testing instructions including:
- Unit tests
- Integration tests
- Manual testing scenarios
- Test coverage reports
- API testing with Postman

## Tech Stack

### Backend
- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security
- PostgreSQL
- Liquibase
- JUnit 5 + Mockito

### Frontend
- React 18
- TypeScript
- Material-UI
- Redux Toolkit
- React Query
- Vite
- Jest + React Testing Library

## Database Setup

```bash
# Create database
createdb wms_db

# Create user
psql -d wms_db -c "CREATE USER wms_user WITH PASSWORD 'wms_password';"
psql -d wms_db -c "GRANT ALL PRIVILEGES ON DATABASE wms_db TO wms_user;"
```

## Configuration

Edit `backend/src/main/resources/application.properties` for database connection.

## API Documentation

- Base URL: `http://localhost:8080/api`
- Authentication: JWT tokens
- Key endpoints:
  - `/api/orders` - Order management
  - `/api/returns` - Return management
  - `/api/customers` - Customer management
  - `/api/products` - Product catalog

## Contributing

1. Create a feature branch
2. Write tests for new functionality
3. Ensure all tests pass: `mvn test && npm test`
4. Submit pull request

## License

Proprietary - Elektromeistras Ltd.
