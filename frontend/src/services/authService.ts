import api from './api';
import { LoginRequest, LoginResponse, User } from '../types/auth';

/**
 * Authentication service
 */
export const authService = {
  /**
   * Login user
   */
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const { data } = await api.post<LoginResponse>('/api/auth/login', credentials);

    // Store tokens
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);

    // Store user info
    localStorage.setItem('user', JSON.stringify(data.user));

    return data;
  },

  /**
   * Logout user
   */
  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  },

  /**
   * Get current user from storage
   */
  getCurrentUser(): User | null {
    const userJson = localStorage.getItem('user');
    if (!userJson) return null;

    try {
      return JSON.parse(userJson);
    } catch {
      return null;
    }
  },

  /**
   * Get access token
   */
  getAccessToken(): string | null {
    return localStorage.getItem('accessToken');
  },

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    return !!this.getAccessToken();
  },

  /**
   * Validate token and get current user info from server
   */
  async validateToken(): Promise<User> {
    const { data } = await api.get<User>('/api/auth/me');

    // Update stored user info
    localStorage.setItem('user', JSON.stringify(data));

    return data;
  },

  /**
   * Get default route based on user roles
   */
  getDefaultRoute(user: User): string {
    const roles = user.roles || [];

    // Priority: Admin > Sales > Warehouse > Client
    if (roles.includes('ADMIN')) {
      return '/admin';
    }
    if (roles.includes('SALES') || roles.includes('SALES_MANAGER')) {
      return '/pos';
    }
    if (roles.includes('WAREHOUSE')) {
      return '/wms';
    }
    if (roles.includes('CLIENT')) {
      return '/b2b';
    }

    // Default fallback
    return '/';
  },
};
