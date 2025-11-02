/**
 * Excel Export Utilities
 *
 * Helper functions to export data to Excel format
 * Uses simple CSV generation (opens in Excel)
 * For advanced features, could integrate xlsx library
 */

interface ExportTransaction {
  transactionNumber: string;
  transactionType: 'PICKUP' | 'RETURN';
  date: string;
  customerName: string;
  totalAmount: number;
  totalItems: number;
  performedBy: string;
  confirmedBy?: string;
  status: string;
}

interface ExportTransactionLine {
  transactionNumber: string;
  productCode: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

/**
 * Export monthly statement to CSV/Excel
 */
export const exportMonthlyStatementToExcel = (
  customerName: string,
  year: number,
  month: number,
  transactions: ExportTransaction[]
): void => {
  // Prepare CSV content
  const csvRows = [];

  // Header
  csvRows.push('MĖNESIO IŠRAŠAS');
  csvRows.push(`Klientas: ${customerName}`);
  csvRows.push(`Periodas: ${year}-${String(month).padStart(2, '0')}`);
  csvRows.push(`Sugeneruota: ${new Date().toLocaleString('lt-LT')}`);
  csvRows.push(''); // Empty row

  // Column headers
  csvRows.push([
    'Data',
    'Numeris',
    'Tipas',
    'Prekių kiekis',
    'Suma (€)',
    'Atliko',
    'Patvirtino',
    'Statusas',
  ].join(','));

  // Data rows
  transactions.forEach(transaction => {
    csvRows.push([
      new Date(transaction.date).toLocaleDateString('lt-LT'),
      transaction.transactionNumber,
      transaction.transactionType === 'PICKUP' ? 'Paėmimas' : 'Grąžinimas',
      transaction.totalItems,
      transaction.totalAmount.toFixed(2),
      escapeCSV(transaction.performedBy),
      escapeCSV(transaction.confirmedBy || ''),
      transaction.status,
    ].join(','));
  });

  // Totals
  csvRows.push(''); // Empty row
  const pickupsTotal = transactions
    .filter(t => t.transactionType === 'PICKUP')
    .reduce((sum, t) => sum + t.totalAmount, 0);
  const returnsTotal = transactions
    .filter(t => t.transactionType === 'RETURN')
    .reduce((sum, t) => sum + t.totalAmount, 0);
  const netTotal = pickupsTotal - returnsTotal;

  csvRows.push(`Paėmimai:,€${pickupsTotal.toFixed(2)}`);
  csvRows.push(`Grąžinimai:,€${returnsTotal.toFixed(2)}`);
  csvRows.push(`Grynoji suma:,€${netTotal.toFixed(2)}`);

  // Create and download
  const csvContent = csvRows.join('\n');
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  const url = URL.createObjectURL(blob);

  link.setAttribute('href', url);
  link.setAttribute('download', `israsas_${customerName}_${year}_${month}.csv`);
  link.style.visibility = 'hidden';

  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

/**
 * Export transaction details with line items to Excel
 */
export const exportTransactionDetailsToExcel = (
  transaction: ExportTransaction,
  lines: ExportTransactionLine[]
): void => {
  const csvRows = [];

  // Header
  csvRows.push('OPERACIJOS DETALĖS');
  csvRows.push(`Numeris: ${transaction.transactionNumber}`);
  csvRows.push(`Tipas: ${transaction.transactionType === 'PICKUP' ? 'Paėmimas' : 'Grąžinimas'}`);
  csvRows.push(`Data: ${new Date(transaction.date).toLocaleString('lt-LT')}`);
  csvRows.push(`Klientas: ${transaction.customerName}`);
  csvRows.push(`Atliko: ${transaction.performedBy}`);
  if (transaction.confirmedBy) {
    csvRows.push(`Patvirtino: ${transaction.confirmedBy}`);
  }
  csvRows.push(''); // Empty row

  // Line items
  csvRows.push('PREKIŲ SĄRAŠAS');
  csvRows.push([
    'Kodas',
    'Pavadinimas',
    'Kiekis',
    'Vnt. Kaina (€)',
    'Suma (€)',
  ].join(','));

  lines.forEach(line => {
    csvRows.push([
      line.productCode,
      escapeCSV(line.productName),
      line.quantity,
      line.unitPrice.toFixed(2),
      line.lineTotal.toFixed(2),
    ].join(','));
  });

  // Total
  csvRows.push(''); // Empty row
  csvRows.push(`IŠ VISO:,€${transaction.totalAmount.toFixed(2)}`);

  // Create and download
  const csvContent = csvRows.join('\n');
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  const url = URL.createObjectURL(blob);

  link.setAttribute('href', url);
  link.setAttribute('download', `operacija_${transaction.transactionNumber}.csv`);
  link.style.visibility = 'hidden';

  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

/**
 * Export customer balance overview to Excel
 */
export const exportCustomerBalancesToExcel = (
  customers: Array<{
    code: string;
    name: string;
    creditLimit: number;
    currentBalance: number;
    utilizationPercent: number;
  }>
): void => {
  const csvRows = [];

  // Header
  csvRows.push('KLIENTŲ SKOLŲ APŽVALGA');
  csvRows.push(`Sugeneruota: ${new Date().toLocaleString('lt-LT')}`);
  csvRows.push(''); // Empty row

  // Column headers
  csvRows.push([
    'Kodas',
    'Klientas',
    'Kredito Limitas (€)',
    'Dabartinė Skola (€)',
    'Panaudota (%)',
    'Liko (€)',
  ].join(','));

  // Data rows
  customers.forEach(customer => {
    const remaining = customer.creditLimit - customer.currentBalance;
    csvRows.push([
      customer.code,
      escapeCSV(customer.name),
      customer.creditLimit.toFixed(2),
      customer.currentBalance.toFixed(2),
      customer.utilizationPercent.toFixed(1),
      remaining.toFixed(2),
    ].join(','));
  });

  // Totals
  csvRows.push(''); // Empty row
  const totalLimit = customers.reduce((sum, c) => sum + c.creditLimit, 0);
  const totalBalance = customers.reduce((sum, c) => sum + c.currentBalance, 0);
  const avgUtilization = totalBalance / totalLimit * 100;

  csvRows.push(`Bendras limitas:,€${totalLimit.toFixed(2)}`);
  csvRows.push(`Bendra skola:,€${totalBalance.toFixed(2)}`);
  csvRows.push(`Vidutinis panaudojimas:,${avgUtilization.toFixed(1)}%`);

  // Create and download
  const csvContent = csvRows.join('\n');
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  const url = URL.createObjectURL(blob);

  link.setAttribute('href', url);
  link.setAttribute('download', `klientu_skolos_${new Date().toISOString().split('T')[0]}.csv`);
  link.style.visibility = 'hidden';

  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

/**
 * Export daily summary to Excel
 */
export const exportDailySummaryToExcel = (
  date: Date,
  summary: {
    totalPickups: number;
    totalReturns: number;
    pickupsAmount: number;
    returnsAmount: number;
    netAmount: number;
    transactionsCount: number;
    uniqueCustomers: number;
  }
): void => {
  const csvRows = [];

  // Header
  csvRows.push('DIENOS SUVESTINĖ');
  csvRows.push(`Data: ${date.toLocaleDateString('lt-LT')}`);
  csvRows.push(`Sugeneruota: ${new Date().toLocaleString('lt-LT')}`);
  csvRows.push(''); // Empty row

  // Summary
  csvRows.push('STATISTIKA');
  csvRows.push(`Paėmimų skaičius:,${summary.totalPickups}`);
  csvRows.push(`Paėmimų suma:,€${summary.pickupsAmount.toFixed(2)}`);
  csvRows.push(''); // Empty row

  csvRows.push(`Grąžinimų skaičius:,${summary.totalReturns}`);
  csvRows.push(`Grąžinimų suma:,€${summary.returnsAmount.toFixed(2)}`);
  csvRows.push(''); // Empty row

  csvRows.push(`Grynoji suma:,€${summary.netAmount.toFixed(2)}`);
  csvRows.push(`Viso operacijų:,${summary.transactionsCount}`);
  csvRows.push(`Unikalių klientų:,${summary.uniqueCustomers}`);

  // Create and download
  const csvContent = csvRows.join('\n');
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  const url = URL.createObjectURL(blob);

  link.setAttribute('href', url);
  link.setAttribute('download', `dienos_suvestine_${date.toISOString().split('T')[0]}.csv`);
  link.style.visibility = 'hidden';

  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

/**
 * Escape CSV special characters
 */
function escapeCSV(value: string | undefined): string {
  if (!value) return '';

  // If value contains comma, quotes, or newline, wrap in quotes and escape internal quotes
  if (value.includes(',') || value.includes('"') || value.includes('\n')) {
    return `"${value.replace(/"/g, '""')}"`;
  }

  return value;
}

/**
 * Format date for Excel (ISO format is Excel-friendly)
 */
export function formatDateForExcel(date: Date | string): string {
  const d = typeof date === 'string' ? new Date(date) : date;
  return d.toISOString().split('T')[0]; // YYYY-MM-DD
}

/**
 * Format currency for Excel (no currency symbol, just number)
 */
export function formatCurrencyForExcel(amount: number): string {
  return amount.toFixed(2);
}
