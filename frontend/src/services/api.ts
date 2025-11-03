import axios from 'axios';
import { config } from '../config';

const API_URL = config.apiUrl;

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;

// Type definitions for API services
export interface ApiQueryParams {
  [key: string]: string | number | boolean | undefined;
}

export interface ProductData {
  code: string;
  sku: string;
  name: string;
  unitOfMeasure: string;
  basePrice: number;
  isCable: boolean;
  isModular: boolean;
  imageUrl?: string;
}

export interface OrderData {
  customerId: string;
  lines: Array<{
    productCode: string;
    quantity: number;
    notes?: string;
  }>;
  notes?: string;
}

export interface CustomerData {
  code: string;
  customerType: 'RETAIL' | 'BUSINESS' | 'CONTRACTOR';
  companyName?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  address?: string;
  city?: string;
  postalCode?: string;
  vatNumber?: string;
  creditLimit: number;
}

export interface ReturnData {
  orderId: string;
  returnLines: Array<{
    orderLineId: string;
    quantity: number;
    returnReasonId: string;
    notes?: string;
  }>;
  notes?: string;
}

export interface InspectionData {
  returnLineId: string;
  condition: 'GOOD' | 'DAMAGED' | 'DEFECTIVE';
  notes?: string;
}

export interface RefundData {
  refundMethod: 'CASH' | 'CREDIT' | 'BANK_TRANSFER';
  amount: number;
  notes?: string;
}

// API Services
export const authService = {
  login: (username: string, password: string) =>
    api.post('/auth/login', { username, password }),
};

export const productService = {
  getAll: (params?: ApiQueryParams) => api.get('/products', { params }),
  getById: (id: string) => api.get(`/products/${id}`),
  getByCode: (code: string) => api.get(`/products/code/${code}`),
  search: (query: string, params?: ApiQueryParams) =>
    api.get('/products/search', { params: { q: query, ...params } }),
  searchByCodePrefix: (codePrefix: string) =>
    api.get('/products/search', { params: { q: codePrefix } }),
  create: (data: ProductData) => api.post('/products', data),
  update: (id: string, data: Partial<ProductData>) => api.put(`/products/${id}`, data),
  delete: (id: string) => api.delete(`/products/${id}`),
};

export const orderService = {
  getAll: (params?: ApiQueryParams) => api.get('/orders', { params }),
  getById: (id: string) => api.get(`/orders/${id}`),
  getByOrderNumber: (orderNumber: string) => api.get(`/orders/number/${orderNumber}`),
  getByCustomerId: (customerId: string, params?: ApiQueryParams) =>
    api.get(`/orders/customer/${customerId}`, { params }),
  getCompletedByCustomerId: (customerId: string) =>
    api.get(`/orders/customer/${customerId}/completed`),
  search: (query: string, params?: ApiQueryParams) =>
    api.get('/orders/search', { params: { q: query, ...params } }),
  create: (data: OrderData) => api.post('/orders', data),
  createQuick: (data: OrderData) => api.post('/orders/quick', data),
  confirm: (id: string) => api.post(`/orders/${id}/confirm`),
  cancel: (id: string, reason?: string) =>
    api.post(`/orders/${id}/cancel`, reason),
  complete: (id: string) => api.post(`/orders/${id}/complete`),
  update: (id: string, data: Partial<OrderData>) => api.put(`/orders/${id}`, data),
};

export const customerService = {
  getAll: (params?: ApiQueryParams) => api.get('/customers', { params }),
  getById: (id: string) => api.get(`/customers/${id}`),
  getByCode: (code: string) => api.get(`/customers/code/${code}`),
  search: (query: string, params?: ApiQueryParams) =>
    api.get('/customers/search', { params: { q: query, ...params } }),
  searchByCodePrefix: (codePrefix: string) =>
    api.get('/customers/search', { params: { q: codePrefix } }),
  create: (data: CustomerData) => api.post('/customers', data),
  update: (id: string, data: Partial<CustomerData>) => api.put(`/customers/${id}`, data),
  delete: (id: string) => api.delete(`/customers/${id}`),
};

export const returnService = {
  getAll: (params?: ApiQueryParams) => api.get('/returns', { params }),
  getById: (id: string) => api.get(`/returns/${id}`),
  getByNumber: (returnNumber: string) => api.get(`/returns/number/${returnNumber}`),
  getByStatus: (status: string, params?: ApiQueryParams) =>
    api.get(`/returns/status/${status}`, { params }),
  getByCustomer: (customerId: string, params?: ApiQueryParams) =>
    api.get(`/returns/customer/${customerId}`, { params }),
  create: (data: ReturnData) => api.post('/returns', data),
  approve: (id: string, notes?: string) =>
    api.post(`/returns/${id}/approve`, { notes }),
  reject: (id: string, rejectionReason: string) =>
    api.post(`/returns/${id}/reject`, { rejectionReason }),
  markAsReceived: (id: string) => api.post(`/returns/${id}/receive`),
  inspect: (id: string, inspections: InspectionData[]) =>
    api.post(`/returns/${id}/inspect`, inspections),
  restock: (id: string) => api.post(`/returns/${id}/restock`),
  processRefund: (id: string, data: RefundData) =>
    api.post(`/returns/${id}/refund`, data),
  getReturnReasons: () => api.get('/returns/reasons'),
};
