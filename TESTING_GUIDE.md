# WMS Application - Complete Testing Guide

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Backend Testing](#backend-testing)
4. [Frontend Testing](#frontend-testing)
5. [Manual Testing Guide](#manual-testing-guide)
6. [Test Coverage](#test-coverage)
7. [Continuous Integration](#continuous-integration)
8. [Troubleshooting](#troubleshooting)

---

## Overview

This guide provides comprehensive instructions for testing all functionality of the WMS (Warehouse Management System) application, including:

- **Backend Unit Tests** - Testing business logic in isolation
- **Backend Integration Tests** - Testing complete API request/response cycle
- **Frontend Unit Tests** - Testing React components and services
- **Manual Functional Testing** - Testing the complete application end-to-end

### Testing Stack

**Backend:**
- JUnit 5 - Testing framework
- Mockito - Mocking framework
- AssertJ - Fluent assertions
- Spring Boot Test - Integration testing
- H2 Database - In-memory database for tests
- JaCoCo - Code coverage

**Frontend:**
- Jest - Testing framework
- React Testing Library - Component testing
- ts-jest - TypeScript support

---

## Prerequisites

### Required Software

1. **Java Development Kit (JDK) 17 or higher**
   ```bash
   java -version  # Should show version 17+
   ```

2. **Apache Maven 3.8+**
   ```bash
   mvn -version
   ```

3. **Node.js 18+ and npm**
   ```bash
   node --version  # Should show v18+
   npm --version
   ```

4. **PostgreSQL 14+** (for manual testing)
   ```bash
   psql --version
   ```

### Installation Steps

#### 1. Install Backend Dependencies

```bash
cd /home/user/Wms/backend
mvn clean install
```

This will:
- Download all Maven dependencies
- Compile the code
- Run all tests
- Generate code coverage reports

#### 2. Install Frontend Dependencies

```bash
cd /home/user/Wms/frontend
npm install
```

This will:
- Download all npm packages
- Install testing libraries
- Set up the development environment

---

## Backend Testing

### Running Backend Tests

#### Run All Tests

```bash
cd /home/user/Wms/backend
mvn test
```

#### Run Specific Test Class

```bash
mvn test -Dtest=OrderServiceTest
mvn test -Dtest=ReturnServiceTest
```

#### Run Tests with Coverage Report

```bash
mvn test jacoco:report
```

Coverage report will be generated at:
`backend/target/site/jacoco/index.html`

Open it in a browser to see detailed coverage statistics.

### Backend Test Structure

```
backend/src/test/java/lt/elektromeistras/
├── service/
│   ├── OrderServiceTest.java        # Unit tests for order business logic
│   └── ReturnServiceTest.java       # Unit tests for return workflow
├── integration/
│   └── OrderControllerIntegrationTest.java  # API integration tests
├── controller/
│   └── (Additional controller tests)
└── repository/
    └── (Repository tests if needed)
```

### What's Being Tested - Backend

#### OrderService Tests (26 test cases)

**Order Creation:**
- ✅ Create order with lines
- ✅ Create quick order with product codes
- ✅ Generate unique order numbers
- ✅ Validate duplicate order numbers
- ✅ Calculate order totals correctly

**Order Retrieval:**
- ✅ Get order by ID
- ✅ Get order by order number
- ✅ Get orders by customer
- ✅ Get completed orders by customer
- ✅ Search orders
- ✅ Get orders by status

**Order Status Transitions:**
- ✅ Confirm order (DRAFT → CONFIRMED)
- ✅ Cancel order
- ✅ Validate status transition rules
- ✅ Prevent invalid transitions

**Error Handling:**
- ✅ Order not found exceptions
- ✅ Invalid status transitions
- ✅ Empty order validation

#### ReturnService Tests (20 test cases)

**Return Creation:**
- ✅ Create return from completed order
- ✅ Full and partial returns
- ✅ Validate return quantities
- ✅ Validate only completed orders can be returned

**Return Approval Workflow:**
- ✅ Approve return
- ✅ Reject return
- ✅ Add approval notes
- ✅ Set expected return date

**Return Receiving:**
- ✅ Mark return as received
- ✅ Record received date
- ✅ Validate return status

**Return Inspection:**
- ✅ Inspect returned items
- ✅ Set product condition (PERFECT, GOOD, DAMAGED, etc.)
- ✅ Split quantities (accepted vs rejected)
- ✅ Determine restock eligibility
- ✅ Calculate refund amounts

**Restocking:**
- ✅ Restock eligible items
- ✅ Update inventory
- ✅ Record restock date

**Refund Processing:**
- ✅ Process refunds
- ✅ Multiple refund methods (BANK_TRANSFER, CREDIT_CARD, etc.)
- ✅ Validate refund amounts
- ✅ Record refund reference

#### Integration Tests (API Endpoints)

**Order API:**
- ✅ GET /api/orders/{id}
- ✅ GET /api/orders/number/{orderNumber}
- ✅ GET /api/orders/customer/{customerId}
- ✅ GET /api/orders/customer/{customerId}/completed
- ✅ GET /api/orders (with pagination)
- ✅ GET /api/orders/search
- ✅ POST /api/orders/quick
- ✅ Authentication and authorization
- ✅ Error responses (404, 401)

### Running Backend Tests in IntelliJ IDEA

1. Open the project in IntelliJ IDEA
2. Right-click on `src/test/java` folder
3. Select **"Run 'All Tests'"**
4. View results in the Run panel

Or run individual test classes:
1. Open test file (e.g., `OrderServiceTest.java`)
2. Click the green play button next to the class name
3. View results inline

---

## Frontend Testing

### Running Frontend Tests

#### Run All Tests

```bash
cd /home/user/Wms/frontend
npm test
```

#### Run Tests in Watch Mode

```bash
npm run test:watch
```

This will re-run tests automatically when you change code.

#### Run Tests with Coverage

```bash
npm test -- --coverage
```

Coverage report will be displayed in the terminal and saved to:
`frontend/coverage/lcov-report/index.html`

#### Run Specific Test File

```bash
npm test -- api.test.ts
```

### Frontend Test Structure

```
frontend/src/
├── services/
│   └── __tests__/
│       └── api.test.ts              # API service tests
├── components/
│   └── __tests__/
│       ├── QuickOrderDialog.test.tsx  # (Create this)
│       └── CustomerAutocomplete.test.tsx  # (Create this)
└── pages/
    └── __tests__/
        └── ReturnsPage.test.tsx     # (Create this)
```

### What's Being Tested - Frontend

#### API Service Tests (Current)

**Authentication:**
- ✅ Login stores token
- ✅ Logout clears token
- ✅ Token retrieval from localStorage

**Order Service:**
- ✅ Get order by ID
- ✅ Create quick order
- ✅ Get completed orders by customer

**Return Service:**
- ✅ Create return
- ✅ Approve return
- ✅ Inspect return

**Error Handling:**
- ✅ 401 Unauthorized errors
- ✅ 404 Not Found errors
- ✅ Network errors

#### Component Tests (To Be Created)

**QuickOrderDialog:**
- ⏳ Render component
- ⏳ Customer selection
- ⏳ Product code input
- ⏳ Add/remove lines
- ⏳ Submit order

**ReturnsPage:**
- ⏳ Display returns list
- ⏳ Filter by status
- ⏳ Return approval/rejection
- ⏳ Inspection workflow

### Running Frontend Tests in VS Code

1. Install Jest extension
2. Open test file
3. Click "Run Test" above each test function
4. View results inline

---

## Manual Testing Guide

This section provides step-by-step instructions for manually testing all functionality of the application.

### Setup for Manual Testing

#### 1. Start PostgreSQL Database

```bash
# Create database
createdb wms_db

# Create user
psql -d wms_db -c "CREATE USER wms_user WITH PASSWORD 'wms_password';"
psql -d wms_db -c "GRANT ALL PRIVILEGES ON DATABASE wms_db TO wms_user;"
```

#### 2. Configure Database Connection

Edit `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/wms_db
spring.datasource.username=wms_user
spring.datasource.password=wms_password
```

#### 3. Start Backend Server

```bash
cd /home/user/Wms/backend
mvn spring-boot:run
```

Backend will start on: **http://localhost:8080**

#### 4. Start Frontend Development Server

```bash
cd /home/user/Wms/frontend
npm run dev
```

Frontend will start on: **http://localhost:3000**

### Manual Test Scenarios

#### Scenario 1: Order Management

**Test Case 1.1: Create Quick Order**

1. Navigate to: `http://localhost:3000/pos`
2. Click **"Quick Order"** button
3. Select customer: Type customer name, select from dropdown
4. Add product lines:
   - Enter product code (e.g., "PROD-001")
   - Enter quantity (e.g., "5")
   - Click **"Add Line"**
5. Repeat for multiple products
6. Click **"Create Order"**
7. **Verify:** Order is created with status DRAFT
8. **Verify:** Order number is generated (e.g., ORD-20250131-0001)
9. **Verify:** Totals are calculated correctly

**Test Case 1.2: View Customer Orders**

1. Navigate to: `http://localhost:3000/pos`
2. Click **"View Orders"**
3. Select a customer
4. **Verify:** All orders for that customer are displayed
5. **Verify:** Orders are paginated (20 per page)
6. **Verify:** Order details show: number, date, status, total

**Test Case 1.3: Confirm Order**

1. Open an order in DRAFT status
2. Click **"Confirm Order"**
3. **Verify:** Status changes to CONFIRMED
4. **Verify:** Timestamp is recorded

**Test Case 1.4: Cancel Order**

1. Open an order in DRAFT or CONFIRMED status
2. Click **"Cancel Order"**
3. Enter cancellation reason
4. **Verify:** Status changes to CANCELLED
5. **Verify:** Reason is saved in notes

#### Scenario 2: Customer Returns Management

**Test Case 2.1: Create Return**

1. Navigate to: `http://localhost:3000/returns`
2. Click **"Create Return"**
3. Select customer
4. Select completed order from dropdown
5. Select items to return:
   - Check product checkbox
   - Enter quantity to return (≤ ordered quantity)
   - Select return reason (e.g., "Defective")
   - Add notes
6. Click **"Submit Return"**
7. **Verify:** Return is created with status PENDING
8. **Verify:** Return number is generated (e.g., RET-20250131-0001)
9. **Verify:** Return type is FULL or PARTIAL

**Test Case 2.2: Approve Return**

1. Open a return in PENDING status
2. Click **"Approve"**
3. Add approval notes (optional)
4. **Verify:** Status changes to APPROVED
5. **Verify:** Expected date is set (7 days from now)

**Test Case 2.3: Reject Return**

1. Open a return in PENDING status
2. Click **"Reject"**
3. Enter rejection reason
4. **Verify:** Status changes to REJECTED
5. **Verify:** Rejection reason is saved

**Test Case 2.4: Receive Return**

1. Open an APPROVED return
2. Click **"Mark as Received"**
3. **Verify:** Status changes to RECEIVED
4. **Verify:** Received date is recorded

**Test Case 2.5: Inspect Return**

1. Open a RECEIVED return
2. Click **"Inspect"**
3. For each return line:
   - Set product condition (PERFECT, GOOD, DAMAGED, DEFECTIVE, MISSING_PARTS)
   - Enter accepted quantity
   - Enter rejected quantity
   - **Verify:** Accepted + Rejected = Returned quantity
   - Add inspection notes
   - Set warehouse location (optional)
4. Click **"Complete Inspection"**
5. **Verify:** Status changes to INSPECTED
6. **Verify:** Restock eligibility is determined correctly
7. **Verify:** Refund amount is calculated

**Test Case 2.6: Restock Return**

1. Open an INSPECTED return
2. Click **"Restock"**
3. **Verify:** Eligible items are added back to inventory
4. **Verify:** Status changes to COMPLETED
5. **Verify:** Restock date is recorded

**Test Case 2.7: Process Refund**

1. Open a COMPLETED return
2. Click **"Process Refund"**
3. Select refund method (BANK_TRANSFER, CREDIT_CARD, CASH, STORE_CREDIT)
4. Enter refund amount (≤ calculated refund amount)
5. Enter refund reference number
6. Add notes
7. Click **"Process"**
8. **Verify:** Refund status changes to COMPLETED
9. **Verify:** Refund date is recorded

#### Scenario 3: Product Search and Selection

**Test Case 3.1: Product Code Input**

1. Navigate to Quick Order dialog
2. Type product code in the input field
3. Press Enter
4. **Verify:** Product is found and details are displayed
5. **Verify:** Price and tax rate are shown

**Test Case 3.2: Product Autocomplete**

1. Start typing product name or SKU
2. **Verify:** Suggestions appear in dropdown
3. Select product from dropdown
4. **Verify:** Product details are populated

#### Scenario 4: Customer Search and Selection

**Test Case 4.1: Customer Autocomplete**

1. Navigate to Create Order
2. Start typing customer name or code
3. **Verify:** Suggestions appear in dropdown
4. Select customer from dropdown
5. **Verify:** Customer details are loaded

**Test Case 4.2: Customer Order History**

1. Select a customer
2. Click **"View Order History"**
3. **Verify:** All customer orders are displayed
4. **Verify:** Completed orders are highlighted

### Testing Programs and Tools

#### Recommended Testing Tools

1. **Backend Testing:**
   - **IntelliJ IDEA** (Best for Java development)
   - **Eclipse** (Alternative IDE)
   - **Maven Command Line** (CI/CD)

2. **Frontend Testing:**
   - **VS Code** (Best for React/TypeScript)
   - **WebStorm** (Alternative IDE)
   - **npm Command Line** (CI/CD)

3. **API Testing:**
   - **Postman** - https://www.postman.com/downloads/
   - **Insomnia** - https://insomnia.rest/download
   - **curl** - Command line tool

4. **Database Testing:**
   - **pgAdmin** - PostgreSQL GUI
   - **DBeaver** - Universal database tool
   - **psql** - PostgreSQL command line

5. **Browser Testing:**
   - **Chrome DevTools** - F12 in Chrome
   - **React Developer Tools** - Chrome extension
   - **Redux DevTools** - Chrome extension

#### Setting Up Postman for API Testing

1. Download and install Postman
2. Import the API collection (create one):

**Sample Postman Collection:**

```json
{
  "info": {
    "name": "WMS API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Authentication",
      "item": [
        {
          "name": "Login",
          "request": {
            "method": "POST",
            "header": [{"key": "Content-Type", "value": "application/json"}],
            "body": {
              "mode": "raw",
              "raw": "{\"username\":\"admin\",\"password\":\"password\"}"
            },
            "url": {"raw": "http://localhost:8080/api/auth/login"}
          }
        }
      ]
    },
    {
      "name": "Orders",
      "item": [
        {
          "name": "Get All Orders",
          "request": {
            "method": "GET",
            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
            "url": {"raw": "http://localhost:8080/api/orders?page=0&size=20"}
          }
        },
        {
          "name": "Create Quick Order",
          "request": {
            "method": "POST",
            "header": [
              {"key": "Authorization", "value": "Bearer {{token}}"},
              {"key": "Content-Type", "value": "application/json"}
            ],
            "body": {
              "mode": "raw",
              "raw": "{\"customerId\":\"customer-uuid-here\",\"lines\":[{\"productCode\":\"PROD-001\",\"quantity\":5}]}"
            },
            "url": {"raw": "http://localhost:8080/api/orders/quick"}
          }
        }
      ]
    }
  ]
}
```

---

## Test Coverage

### Current Coverage

**Backend:**
- OrderService: ~90% coverage
- ReturnService: ~85% coverage
- Controllers: ~70% coverage
- Overall Target: 80%+

**Frontend:**
- API Services: ~80% coverage
- Components: ~40% (needs more tests)
- Overall Target: 70%+

### Viewing Coverage Reports

**Backend:**
```bash
cd /home/user/Wms/backend
mvn test jacoco:report
open target/site/jacoco/index.html
```

**Frontend:**
```bash
cd /home/user/Wms/frontend
npm test -- --coverage
open coverage/lcov-report/index.html
```

---

## Continuous Integration

### GitHub Actions (Recommended)

Create `.github/workflows/test.yml`:

```yaml
name: Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  backend-tests:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Run backend tests
      run: |
        cd backend
        mvn clean test jacoco:report

    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: backend/target/site/jacoco/jacoco.xml

  frontend-tests:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Setup Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'

    - name: Install dependencies
      run: |
        cd frontend
        npm ci

    - name: Run frontend tests
      run: |
        cd frontend
        npm test -- --coverage

    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: frontend/coverage/lcov.info
```

---

## Troubleshooting

### Common Issues

#### Backend Tests Fail: "Cannot connect to database"

**Solution:** Tests use H2 in-memory database, not PostgreSQL. Ensure `application-test.properties` is configured correctly.

#### Frontend Tests Fail: "Cannot find module"

**Solution:**
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
```

#### Tests Pass Locally But Fail in CI

**Solution:** Check Java/Node versions match between local and CI environments.

#### JaCoCo Report Not Generated

**Solution:**
```bash
mvn clean test  # Clean first
mvn jacoco:report  # Then generate report
```

#### Jest Tests Timeout

**Solution:** Increase timeout in `jest.config.js`:
```javascript
testTimeout: 10000  // 10 seconds
```

### Getting Help

- Backend Issues: Check Spring Boot logs in console
- Frontend Issues: Check browser console (F12)
- Database Issues: Check PostgreSQL logs
- Build Issues: Run with verbose flag: `mvn test -X` or `npm test -- --verbose`

---

## What's Not Done Yet (TODO)

### Backend Tests (To Create)

1. **ProductService Tests**
   - Product search tests
   - Cable and modular product tests
   - Pricing calculation tests

2. **CustomerService Tests**
   - Customer search tests
   - Credit limit validation tests
   - Customer type handling tests

3. **Integration Tests**
   - Return API endpoints
   - Product API endpoints
   - Customer API endpoints

4. **Security Tests**
   - Authentication tests
   - Authorization tests
   - JWT token validation tests

### Frontend Tests (To Create)

1. **Component Tests**
   - QuickOrderDialog.test.tsx
   - CustomerAutocomplete.test.tsx
   - ProductCodeInput.test.tsx
   - ReturnsPage.test.tsx

2. **Redux Store Tests**
   - Action creators
   - Reducers
   - Selectors

3. **Integration Tests**
   - Complete user flows
   - Form validation
   - Error handling

### End-to-End Tests (To Create)

Consider adding Playwright or Cypress for E2E tests:

```bash
# Install Playwright
npm install -D @playwright/test

# Create E2E tests
# tests/e2e/order-workflow.spec.ts
# tests/e2e/return-workflow.spec.ts
```

---

## Summary

**You can start testing by:**

1. **Run backend tests:** `cd backend && mvn test`
2. **Run frontend tests:** `cd frontend && npm test`
3. **Start servers for manual testing:** Follow "Setup for Manual Testing" section
4. **Use Postman for API testing:** Import collection and test endpoints
5. **View coverage reports:** Run tests with coverage flags

**Programs to use:**
- **IntelliJ IDEA / Eclipse** - Backend development and testing
- **VS Code / WebStorm** - Frontend development and testing
- **Postman / Insomnia** - API testing
- **Chrome DevTools** - Frontend debugging
- **pgAdmin / DBeaver** - Database inspection

**What's missing:**
- Additional component tests for frontend
- E2E tests with Playwright/Cypress
- Performance tests
- Security/penetration tests

This testing infrastructure provides a solid foundation for ensuring code quality and catching bugs early!
