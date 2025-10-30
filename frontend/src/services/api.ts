import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || '/api';

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

// API Services
export const authService = {
  login: (username: string, password: string) =>
    api.post('/auth/login', { username, password }),
};

export const productService = {
  getAll: (params?: any) => api.get('/products', { params }),
  getById: (id: string) => api.get(`/products/${id}`),
  search: (query: string, params?: any) =>
    api.get('/products/search', { params: { q: query, ...params } }),
  create: (data: any) => api.post('/products', data),
  update: (id: string, data: any) => api.put(`/products/${id}`, data),
};

export const orderService = {
  getAll: (params?: any) => api.get('/orders', { params }),
  getById: (id: string) => api.get(`/orders/${id}`),
  create: (data: any) => api.post('/orders', data),
  confirm: (id: string) => api.post(`/orders/${id}/confirm`),
  cancel: (id: string) => api.post(`/orders/${id}/cancel`),
  complete: (id: string) => api.post(`/orders/${id}/complete`),
};

export const customerService = {
  getAll: (params?: any) => api.get('/customers', { params }),
  getById: (id: string) => api.get(`/customers/${id}`),
  search: (query: string, params?: any) =>
    api.get('/customers/search', { params: { q: query, ...params } }),
  create: (data: any) => api.post('/customers', data),
  update: (id: string, data: any) => api.put(`/customers/${id}`, data),
};

export const returnService = {
  getAll: (params?: any) => api.get('/returns', { params }),
  getById: (id: string) => api.get(`/returns/${id}`),
  getByNumber: (returnNumber: string) => api.get(`/returns/number/${returnNumber}`),
  getByStatus: (status: string, params?: any) =>
    api.get(`/returns/status/${status}`, { params }),
  getByCustomer: (customerId: string, params?: any) =>
    api.get(`/returns/customer/${customerId}`, { params }),
  create: (data: any) => api.post('/returns', data),
  approve: (id: string, notes?: string) =>
    api.post(`/returns/${id}/approve`, { notes }),
  reject: (id: string, rejectionReason: string) =>
    api.post(`/returns/${id}/reject`, { rejectionReason }),
  markAsReceived: (id: string) => api.post(`/returns/${id}/receive`),
  inspect: (id: string, inspections: any[]) =>
    api.post(`/returns/${id}/inspect`, inspections),
  restock: (id: string) => api.post(`/returns/${id}/restock`),
  processRefund: (id: string, data: any) =>
    api.post(`/returns/${id}/refund`, data),
  getReturnReasons: () => api.get('/returns/reasons'),
};
