// Domain Types
export interface Product {
  id: string;
  sku: string;
  ean?: string;
  name: string;
  description?: string;
  categoryId?: string;
  manufacturerId?: string;
  unitOfMeasure?: string;
  basePrice: number;
  isCable: boolean;
  isModular: boolean;
  moduleWidth?: number;
  isActive: boolean;
  taxRate: number;
  imageUrl?: string;
}

export interface Customer {
  id: string;
  code: string;
  customerType: 'RETAIL' | 'BUSINESS' | 'CONTRACTOR';
  companyName?: string;
  vatCode?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  address?: string;
  city?: string;
  postalCode?: string;
  country: string;
  priceGroupId?: string;
  creditLimit: number;
  currentBalance: number;
  isActive: boolean;
}

export interface OrderLine {
  id?: string;
  lineNumber: number;
  product: Product;
  quantity: number;
  isCable: boolean;
  rollId?: string;
  cutLength?: number;
  unitPrice: number;
  discountPercentage: number;
  taxRate: number;
  lineTotal: number;
}

export interface Order {
  id?: string;
  orderNumber?: string;
  customer: Customer;
  projectId?: string;
  status: OrderStatus;
  orderDate: Date;
  subtotal: number;
  taxAmount: number;
  totalAmount: number;
  paymentMethod?: string;
  paymentStatus: PaymentStatus;
  salesPersonId?: string;
  warehouseId?: string;
  lines: OrderLine[];
  notes?: string;
}

export enum OrderStatus {
  DRAFT = 'DRAFT',
  CONFIRMED = 'CONFIRMED',
  PROCESSING = 'PROCESSING',
  PICKING = 'PICKING',
  PACKED = 'PACKED',
  SHIPPED = 'SHIPPED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}

export enum PaymentStatus {
  UNPAID = 'UNPAID',
  PARTIAL = 'PARTIAL',
  PAID = 'PAID',
  OVERPAID = 'OVERPAID',
  REFUNDED = 'REFUNDED',
}

export interface User {
  id: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  roles: Role[];
  isActive: boolean;
}

export interface Role {
  id: string;
  name: string;
  description?: string;
  permissions: Permission[];
}

export interface Permission {
  id: string;
  name: string;
  description?: string;
  module?: string;
}

export interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
}

export interface CartItem extends OrderLine {}

export interface SearchResult {
  products: Product[];
  customers: Customer[];
}
