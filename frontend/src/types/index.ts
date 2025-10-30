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

// Returns System Types

export interface ReturnReason {
  id: string;
  code: string;
  name: string;
  description?: string;
  requiresInspection: boolean;
  allowsRestock: boolean;
  active: boolean;
}

export interface ReturnLine {
  id: string;
  returnId: string;
  orderLineId: string;
  productId: string;
  productSku: string;
  productName: string;
  returnReasonId: string;
  returnReasonName: string;
  quantityOrdered: number;
  quantityReturned: number;
  quantityAccepted: number;
  quantityRejected: number;
  condition: ProductCondition;
  unitPrice: number;
  discountPercentage: number;
  taxRate: number;
  lineTotal: number;
  refundAmount: number;
  restockEligible: boolean;
  restocked: boolean;
  restockedDate?: Date;
  warehouseLocationId?: string;
  notes?: string;
  inspectionNotes?: string;
}

export interface Return {
  id: string;
  returnNumber: string;
  orderId: string;
  orderNumber: string;
  customerId: string;
  customerName: string;
  warehouseId: string;
  warehouseName: string;
  status: ReturnStatus;
  returnType: ReturnType;
  returnDate: Date;
  expectedDate?: Date;
  receivedDate?: Date;
  inspectedDate?: Date;
  completedDate?: Date;
  subtotalAmount: number;
  taxAmount: number;
  totalAmount: number;
  refundAmount: number;
  refundMethod?: string;
  refundStatus: RefundStatus;
  refundDate?: Date;
  refundReference?: string;
  notes?: string;
  internalNotes?: string;
  rejectionReason?: string;
  requestedById?: string;
  requestedByName?: string;
  approvedById?: string;
  approvedByName?: string;
  inspectedById?: string;
  inspectedByName?: string;
  lines: ReturnLine[];
}

export enum ReturnStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  IN_TRANSIT = 'IN_TRANSIT',
  RECEIVED = 'RECEIVED',
  INSPECTED = 'INSPECTED',
  COMPLETED = 'COMPLETED',
  REJECTED = 'REJECTED',
}

export enum ReturnType {
  FULL = 'FULL',
  PARTIAL = 'PARTIAL',
}

export enum RefundStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
}

export enum ProductCondition {
  UNKNOWN = 'UNKNOWN',
  PERFECT = 'PERFECT',
  GOOD = 'GOOD',
  DAMAGED = 'DAMAGED',
  DEFECTIVE = 'DEFECTIVE',
  MISSING_PARTS = 'MISSING_PARTS',
}

export interface CreateReturnRequest {
  orderId: string;
  customerId: string;
  warehouseId: string;
  lines: CreateReturnLineRequest[];
  notes?: string;
}

export interface CreateReturnLineRequest {
  orderLineId: string;
  productId: string;
  returnReasonId: string;
  quantityReturned: number;
  notes?: string;
}

export interface UpdateReturnStatusRequest {
  status: string;
  notes?: string;
  rejectionReason?: string;
}

export interface InspectReturnLineRequest {
  returnLineId: string;
  condition: ProductCondition;
  quantityAccepted: number;
  quantityRejected: number;
  warehouseLocationId?: string;
  inspectionNotes?: string;
}

export interface ProcessRefundRequest {
  refundAmount: number;
  refundMethod: string;
  refundReference?: string;
  notes?: string;
}
