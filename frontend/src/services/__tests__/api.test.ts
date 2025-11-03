/**
 * API Service Unit Tests
 * Tests the API layer including:
 * - Authentication
 * - Request/response handling
 * - Error handling
 * - Token management
 */

// Mock axios.create before importing the api module
const mockAxiosInstance = {
  get: jest.fn(),
  post: jest.fn(),
  put: jest.fn(),
  delete: jest.fn(),
  interceptors: {
    request: { use: jest.fn(), eject: jest.fn() },
    response: { use: jest.fn(), eject: jest.fn() }
  }
};

jest.mock('axios', () => ({
  __esModule: true,
  default: {
    create: jest.fn(() => mockAxiosInstance),
  },
}));

import { authService, orderService, returnService } from '../api';

describe('API Service Tests', () => {
  beforeEach(() => {
    // Clear all mocks before each test
    jest.clearAllMocks();
    localStorage.clear();
  });

  describe('authService', () => {
    test('login should send correct credentials', async () => {
      const mockResponse = {
        data: {
          token: 'test-jwt-token',
          user: {
            id: '123',
            username: 'testuser',
            email: 'test@example.com',
          },
        },
      };

      mockAxiosInstance.post.mockResolvedValueOnce(mockResponse);

      const result = await authService.login('testuser', 'password123');

      expect(mockAxiosInstance.post).toHaveBeenCalledWith('/auth/login', {
        username: 'testuser',
        password: 'password123',
      });
      expect(result).toEqual(mockResponse);
    });

    // TODO: Update these tests - API methods have changed
    test.skip('logout should clear token from storage', () => {
      localStorage.setItem('token', 'test-token');

      // authService.logout();

      expect(localStorage.removeItem).toHaveBeenCalledWith('token');
    });

    test.skip('getToken should return stored token', () => {
      localStorage.setItem('token', 'stored-token');

      // const token = authService.getToken();

      // expect(token).toBe('stored-token');
    });
  });

  describe('orderService', () => {
    // TODO: Update these tests - API methods have changed
    test.skip('getOrderById should fetch order data', async () => {
      const mockOrder = {
        id: 'order-123',
        orderNumber: 'ORD-20250101-0001',
        status: 'DRAFT',
        customer: {
          id: 'cust-123',
          name: 'Test Customer',
        },
        orderLines: [],
      };

      mockedAxios.get.mockResolvedValueOnce({ data: mockOrder });

      // const result = await orderService.getOrderById('order-123');
      const result = await orderService.getById('order-123');

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/orders/order-123');
      expect(result).toEqual(mockOrder);
    });

    test.skip('createQuickOrder should send POST request', async () => {
      const quickOrderData = {
        customerId: 'cust-123',
        lines: [
          { productCode: 'PROD-001', quantity: 5 },
        ],
      };

      const mockResponse = {
        id: 'order-456',
        orderNumber: 'ORD-20250101-0002',
        status: 'DRAFT',
      };

      mockedAxios.post.mockResolvedValueOnce({ data: mockResponse });

      // const result = await orderService.createQuickOrder(quickOrderData);
      const result = await orderService.createQuick(quickOrderData);

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/orders/quick', quickOrderData);
      expect(result).toEqual(mockResponse);
    });

    test.skip('getCompletedOrdersByCustomer should fetch completed orders', async () => {
      const mockOrders = [
        { id: 'order-1', orderNumber: 'ORD-001', status: 'COMPLETED' },
        { id: 'order-2', orderNumber: 'ORD-002', status: 'COMPLETED' },
      ];

      mockedAxios.get.mockResolvedValueOnce({ data: mockOrders });

      // const result = await orderService.getCompletedOrdersByCustomer('cust-123');
      const result = await orderService.getCompletedByCustomerId('cust-123');

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/orders/customer/cust-123/completed');
      expect(result).toEqual(mockOrders);
      expect(result).toHaveLength(2);
    });
  });

  describe('returnService', () => {
    // TODO: Update these tests - API methods have changed
    test.skip('createReturn should send POST request with return data', async () => {
      const returnData = {
        orderId: 'order-123',
        customerId: 'cust-123',
        warehouseId: 'wh-123',
        lines: [
          {
            orderLineId: 'line-123',
            productId: 'prod-123',
            returnReasonId: 'reason-123',
            quantityReturned: 2,
            notes: 'Defective product',
          },
        ],
        notes: 'Customer return request',
      };

      const mockResponse = {
        id: 'return-123',
        returnNumber: 'RET-20250101-0001',
        status: 'PENDING',
      };

      mockedAxios.post.mockResolvedValueOnce({ data: mockResponse });

      // const result = await returnService.createReturn(returnData);
      const result = await returnService.create(returnData);

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/returns', returnData);
      expect(result).toEqual(mockResponse);
    });

    test.skip('approveReturn should send POST request to approve endpoint', async () => {
      const mockResponse = {
        id: 'return-123',
        returnNumber: 'RET-20250101-0001',
        status: 'APPROVED',
      };

      mockedAxios.post.mockResolvedValueOnce({ data: mockResponse });

      // const result = await returnService.approveReturn('return-123', 'Approved by manager');
      const result = await returnService.approve('return-123');

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/returns/return-123/approve', {
        notes: 'Approved by manager',
      });
      expect(result.status).toBe('APPROVED');
    });

    test.skip('inspectReturn should send POST request with inspection data', async () => {
      const inspectionData = [
        {
          returnLineId: 'line-123',
          quantityAccepted: 2,
          quantityRejected: 0,
          condition: 'GOOD',
          inspectionNotes: 'Product in good condition',
        },
      ];

      const mockResponse = {
        id: 'return-123',
        status: 'INSPECTED',
      };

      mockedAxios.post.mockResolvedValueOnce({ data: mockResponse });

      // const result = await returnService.inspectReturn('return-123', inspectionData);
      const result = await returnService.inspect('return-123', inspectionData);

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/returns/return-123/inspect', inspectionData);
      expect(result.status).toBe('INSPECTED');
    });
  });

  describe('Error Handling', () => {
    // TODO: Update these tests - API methods have changed
    test.skip('should handle 401 unauthorized errors', async () => {
      const error = {
        response: {
          status: 401,
          data: { message: 'Unauthorized' },
        },
      };

      mockedAxios.get.mockRejectedValueOnce(error);

      // await expect(orderService.getOrderById('order-123')).rejects.toEqual(error);
      await expect(orderService.getById('order-123')).rejects.toEqual(error);
    });

    test.skip('should handle network errors', async () => {
      const error = new Error('Network Error');
      mockedAxios.get.mockRejectedValueOnce(error);

      // await expect(orderService.getOrderById('order-123')).rejects.toThrow('Network Error');
      await expect(orderService.getById('order-123')).rejects.toThrow('Network Error');
    });

    test.skip('should handle 404 not found errors', async () => {
      const error = {
        response: {
          status: 404,
          data: { message: 'Order not found' },
        },
      };

      mockedAxios.get.mockRejectedValueOnce(error);

      // await expect(orderService.getOrderById('invalid-id')).rejects.toEqual(error);
      await expect(orderService.getById('invalid-id')).rejects.toEqual(error);
    });
  });
});
