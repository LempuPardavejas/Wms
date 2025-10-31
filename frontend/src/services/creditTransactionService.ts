/**
 * Credit Transaction Service - API calls for credit transactions
 */

const API_BASE_URL = '/api/credit-transactions';

export interface QuickCreditPickupRequest {
  customerCode: string;
  items: Array<{
    productCode: string;
    quantity: number;
    notes?: string;
  }>;
  performedBy: string;
  performedByRole: string;
  notes?: string;
}

export interface CreditTransactionResponse {
  id: string;
  transactionNumber: string;
  customerId: string;
  customerCode: string;
  customerName: string;
  transactionType: 'PICKUP' | 'RETURN';
  status: 'PENDING' | 'CONFIRMED' | 'INVOICED' | 'CANCELLED';
  lines: Array<{
    id: string;
    productId: string;
    productCode: string;
    productName: string;
    quantity: number;
    unitPrice: number;
    lineTotal: number;
    notes?: string;
  }>;
  totalAmount: number;
  totalItems: number;
  performedBy: string;
  performedByRole: string;
  confirmedBy?: string;
  confirmedAt?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreditTransactionSummaryResponse {
  id: string;
  transactionNumber: string;
  customerCode: string;
  customerName: string;
  transactionType: 'PICKUP' | 'RETURN';
  status: 'PENDING' | 'CONFIRMED' | 'INVOICED' | 'CANCELLED';
  totalAmount: number;
  totalItems: number;
  performedBy: string;
  createdAt: string;
}

/**
 * Create quick credit pickup
 */
export const createQuickCreditPickup = async (
  request: QuickCreditPickupRequest
): Promise<CreditTransactionResponse> => {
  const response = await fetch(`${API_BASE_URL}/quick-pickup`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    throw new Error('Failed to create credit pickup');
  }

  return response.json();
};

/**
 * Get all credit transactions
 */
export const getAllCreditTransactions = async (
  page: number = 0,
  size: number = 20
): Promise<{ content: CreditTransactionSummaryResponse[]; totalElements: number }> => {
  const response = await fetch(`${API_BASE_URL}?page=${page}&size=${size}`);

  if (!response.ok) {
    throw new Error('Failed to fetch credit transactions');
  }

  return response.json();
};

/**
 * Get credit transaction by ID
 */
export const getCreditTransactionById = async (
  id: string
): Promise<CreditTransactionResponse> => {
  const response = await fetch(`${API_BASE_URL}/${id}`);

  if (!response.ok) {
    throw new Error('Failed to fetch credit transaction');
  }

  return response.json();
};

/**
 * Get credit transactions for a customer
 */
export const getCustomerCreditTransactions = async (
  customerId: string,
  page: number = 0,
  size: number = 20
): Promise<{ content: CreditTransactionSummaryResponse[]; totalElements: number }> => {
  const response = await fetch(`${API_BASE_URL}/customer/${customerId}?page=${page}&size=${size}`);

  if (!response.ok) {
    throw new Error('Failed to fetch customer credit transactions');
  }

  return response.json();
};

/**
 * Get recent credit transactions for a customer
 */
export const getRecentCustomerCreditTransactions = async (
  customerId: string,
  limit: number = 10
): Promise<CreditTransactionSummaryResponse[]> => {
  const response = await fetch(`${API_BASE_URL}/customer/${customerId}/recent?limit=${limit}`);

  if (!response.ok) {
    throw new Error('Failed to fetch recent credit transactions');
  }

  return response.json();
};

/**
 * Get pending credit transactions for a customer
 */
export const getPendingCustomerCreditTransactions = async (
  customerId: string
): Promise<CreditTransactionResponse[]> => {
  const response = await fetch(`${API_BASE_URL}/customer/${customerId}/pending`);

  if (!response.ok) {
    throw new Error('Failed to fetch pending credit transactions');
  }

  return response.json();
};

/**
 * Search credit transactions
 */
export const searchCreditTransactions = async (
  query: string,
  page: number = 0,
  size: number = 20
): Promise<{ content: CreditTransactionSummaryResponse[]; totalElements: number }> => {
  const response = await fetch(`${API_BASE_URL}/search?q=${encodeURIComponent(query)}&page=${page}&size=${size}`);

  if (!response.ok) {
    throw new Error('Failed to search credit transactions');
  }

  return response.json();
};

/**
 * Confirm credit transaction
 */
export const confirmCreditTransaction = async (
  id: string,
  confirmedBy: string,
  signatureData?: string,
  notes?: string
): Promise<CreditTransactionResponse> => {
  const response = await fetch(`${API_BASE_URL}/${id}/confirm`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      confirmedBy,
      signatureData,
      notes,
    }),
  });

  if (!response.ok) {
    throw new Error('Failed to confirm credit transaction');
  }

  return response.json();
};

/**
 * Cancel credit transaction
 */
export const cancelCreditTransaction = async (
  id: string,
  reason: string
): Promise<void> => {
  const response = await fetch(`${API_BASE_URL}/${id}/cancel?reason=${encodeURIComponent(reason)}`, {
    method: 'POST',
  });

  if (!response.ok) {
    throw new Error('Failed to cancel credit transaction');
  }
};

/**
 * Get monthly statement for customer
 */
export const getMonthlyStatement = async (
  customerId: string,
  year: number,
  month: number
): Promise<CreditTransactionResponse[]> => {
  const response = await fetch(`${API_BASE_URL}/customer/${customerId}/statement/${year}/${month}`);

  if (!response.ok) {
    throw new Error('Failed to fetch monthly statement');
  }

  return response.json();
};

/**
 * Generate monthly statement PDF (example)
 */
export const generateMonthlyStatementPDF = async (
  customerId: string,
  year: number,
  month: number
): Promise<Blob> => {
  const response = await fetch(`${API_BASE_URL}/customer/${customerId}/statement/${year}/${month}/pdf`);

  if (!response.ok) {
    throw new Error('Failed to generate monthly statement PDF');
  }

  return response.blob();
};
