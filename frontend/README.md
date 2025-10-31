# WMS Frontend - React TypeScript Application

## Quick Start

### Install Dependencies

```bash
npm install
```

### Run Development Server

```bash
npm run dev
```

Application opens at: http://localhost:3000

### Run Tests

```bash
# Run all tests
npm test

# Run in watch mode
npm run test:watch

# Run with coverage
npm test -- --coverage
```

### View Coverage Report

```bash
npm test -- --coverage
open coverage/lcov-report/index.html
```

## Test Structure

```
src/
├── services/
│   └── __tests__/
│       └── api.test.ts           # API service tests
├── components/
│   └── __tests__/
│       └── (component tests)
└── pages/
    └── __tests__/
        └── (page tests)
```

## Test Features

- ✅ API service unit tests
- ✅ Jest + React Testing Library
- ✅ TypeScript support
- ✅ Mock localStorage
- ✅ Mock axios
- ✅ Coverage reporting

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm test` - Run tests
- `npm run test:watch` - Run tests in watch mode
- `npm run lint` - Run ESLint
- `npm run type-check` - Check TypeScript types

## Application Routes

- `/` - Home page
- `/pos` - Point of Sale
- `/wms` - Warehouse Management
- `/admin` - Admin panel
- `/b2b` - B2B Portal
- `/returns` - Returns Management
- `/login` - Authentication

## Key Features

### Quick Order Entry
- Fast order creation with product codes
- Customer autocomplete
- Real-time total calculation

### Returns Management
- Complete returns workflow
- Return approval/rejection
- Item inspection
- Refund processing

### Customer Selection
- Ultra-fast customer search
- Order history view
- Completed orders for returns

## Dependencies

- React 18.2
- TypeScript 5.3
- Material-UI 5.14
- Redux Toolkit 1.9
- React Query 5.8
- React Router 6.20
- Axios 1.6
- Vite 5.0

## Dev Dependencies

- Jest 29.7
- React Testing Library 14.1
- ts-jest 29.1
- @testing-library/jest-dom 6.1
- @testing-library/user-event 14.5

## Configuration Files

- `vite.config.ts` - Vite configuration
- `tsconfig.json` - TypeScript configuration
- `jest.config.js` - Jest configuration
- `package.json` - Dependencies and scripts

## API Integration

Backend API base URL: `http://localhost:8080/api`

API proxy configured in Vite to forward `/api/*` requests to backend.

### API Services

```typescript
import { orderService, returnService, customerService } from './services/api';

// Get orders
const orders = await orderService.getOrdersByCustomer(customerId);

// Create return
const return = await returnService.createReturn(returnData);
```

## Building for Production

```bash
# Build
npm run build

# Output in dist/ folder
# Deploy dist/ to web server
```

## Environment Variables

Create `.env` file:

```env
VITE_API_URL=http://localhost:8080
```

## Troubleshooting

**Tests fail with "Cannot find module":**
```bash
rm -rf node_modules package-lock.json
npm install
```

**Type errors:**
```bash
npm run type-check
```

**Linting errors:**
```bash
npm run lint
```

**Port 3000 already in use:**
Edit `vite.config.ts` and change `server.port`

## Testing Best Practices

1. **Test user interactions, not implementation**
2. **Use data-testid for complex queries**
3. **Mock external dependencies (axios, localStorage)**
4. **Write tests before fixing bugs**
5. **Aim for 70%+ code coverage**

## Component Testing Example

```typescript
import { render, screen, fireEvent } from '@testing-library/react';
import QuickOrderDialog from './QuickOrderDialog';

test('should add product line', () => {
  render(<QuickOrderDialog />);

  const input = screen.getByLabelText('Product Code');
  fireEvent.change(input, { target: { value: 'PROD-001' } });

  const addButton = screen.getByText('Add Line');
  fireEvent.click(addButton);

  expect(screen.getByText('PROD-001')).toBeInTheDocument();
});
```

## Redux Store

State management with Redux Toolkit:

```typescript
// store/orderSlice.ts
// store/returnSlice.ts
// store/customerSlice.ts
```

## Material-UI Theming

Theme configuration in `App.tsx`:

```typescript
const theme = createTheme({
  palette: {
    primary: { main: '#1976d2' },
    secondary: { main: '#dc004e' },
  },
});
```

## Code Style

- Use TypeScript for all new files
- Follow ESLint rules
- Use functional components with hooks
- Prefer const over let
- Use arrow functions
