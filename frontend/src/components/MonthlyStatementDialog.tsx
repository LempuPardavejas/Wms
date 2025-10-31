import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Typography,
  Box,
  Divider,
  Chip,
  CircularProgress,
  Alert,
} from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import PrintIcon from '@mui/icons-material/Print';
import CustomerAutocomplete from './CustomerAutocomplete';
import { getMonthlyStatement } from '../services/creditTransactionService';

interface Customer {
  id: string;
  code: string;
  customerType: string;
  companyName?: string;
  firstName?: string;
  lastName?: string;
  creditLimit: number;
  currentBalance: number;
}

interface MonthlyStatementDialogProps {
  open: boolean;
  onClose: () => void;
  preSelectedCustomer?: Customer;
}

interface StatementTransaction {
  id: string;
  transactionNumber: string;
  transactionType: 'PICKUP' | 'RETURN';
  totalAmount: number;
  totalItems: number;
  performedBy: string;
  createdAt: string;
  lines: Array<{
    productCode: string;
    productName: string;
    quantity: number;
    unitPrice: number;
    lineTotal: number;
  }>;
}

/**
 * MonthlyStatementDialog - Generate and display monthly credit statements
 *
 * Features:
 * - Select customer
 * - Select year and month
 * - View all transactions for the period
 * - Show detailed breakdown by date
 * - Calculate totals (pickups, returns, net)
 * - Export to PDF
 * - Print functionality
 */
const MonthlyStatementDialog: React.FC<MonthlyStatementDialogProps> = ({
  open,
  onClose,
  preSelectedCustomer,
}) => {
  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth() + 1;

  const [customer, setCustomer] = useState<Customer | null>(preSelectedCustomer || null);
  const [year, setYear] = useState<number>(currentYear);
  const [month, setMonth] = useState<number>(currentMonth);
  const [loading, setLoading] = useState(false);
  const [transactions, setTransactions] = useState<StatementTransaction[]>([]);
  const [showStatement, setShowStatement] = useState(false);

  const handleGenerateStatement = async () => {
    if (!customer) {
      alert('Pasirinkite klientą');
      return;
    }

    setLoading(true);
    try {
      const data = await getMonthlyStatement(customer.id, year, month);
      setTransactions(data as any);
      setShowStatement(true);
    } catch (error) {
      console.error('Failed to generate statement:', error);
      alert('Klaida generuojant išrašą');
    } finally {
      setLoading(false);
    }
  };

  const handlePrint = () => {
    window.print();
  };

  const handleExportPDF = async () => {
    // TODO: Implement PDF export
    alert('PDF eksportavimas dar neįdiegtas');
  };

  const getCustomerDisplayName = (customer: Customer): string => {
    if (customer.customerType === 'BUSINESS') {
      return customer.companyName || customer.code;
    } else {
      if (customer.firstName && customer.lastName) {
        return `${customer.firstName} ${customer.lastName}`;
      } else if (customer.lastName) {
        return customer.lastName;
      } else {
        return customer.code;
      }
    }
  };

  const getMonthName = (month: number): string => {
    const months = [
      'Sausis', 'Vasaris', 'Kovas', 'Balandis', 'Gegužė', 'Birželis',
      'Liepa', 'Rugpjūtis', 'Rugsėjis', 'Spalis', 'Lapkritis', 'Gruodis'
    ];
    return months[month - 1];
  };

  const calculateTotals = () => {
    const pickups = transactions
      .filter(t => t.transactionType === 'PICKUP')
      .reduce((sum, t) => sum + t.totalAmount, 0);

    const returns = transactions
      .filter(t => t.transactionType === 'RETURN')
      .reduce((sum, t) => sum + t.totalAmount, 0);

    const net = pickups - returns;

    return { pickups, returns, net };
  };

  const totals = calculateTotals();

  // Generate list of years (current year and 2 previous years)
  const years = Array.from({ length: 3 }, (_, i) => currentYear - i);

  // Generate list of months
  const months = Array.from({ length: 12 }, (_, i) => i + 1);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="lg" fullWidth>
      <DialogTitle>
        Mėnesio išrašas
      </DialogTitle>

      <DialogContent dividers>
        {!showStatement ? (
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <CustomerAutocomplete
                value={customer}
                onChange={setCustomer}
                label="Klientas *"
                autoFocus
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>Metai</InputLabel>
                <Select
                  value={year}
                  label="Metai"
                  onChange={(e) => setYear(e.target.value as number)}
                >
                  {years.map(y => (
                    <MenuItem key={y} value={y}>{y}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>Mėnuo</InputLabel>
                <Select
                  value={month}
                  label="Mėnuo"
                  onChange={(e) => setMonth(e.target.value as number)}
                >
                  {months.map(m => (
                    <MenuItem key={m} value={m}>{getMonthName(m)}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12}>
              <Button
                variant="contained"
                color="primary"
                fullWidth
                onClick={handleGenerateStatement}
                disabled={!customer || loading}
              >
                {loading ? <CircularProgress size={24} /> : 'Generuoti išrašą'}
              </Button>
            </Grid>
          </Grid>
        ) : (
          <Box>
            {/* Statement Header */}
            <Box mb={3}>
              <Typography variant="h5" gutterBottom>
                KREDITO IŠRAŠAS
              </Typography>
              <Typography variant="h6" color="primary" gutterBottom>
                {customer && getCustomerDisplayName(customer)}
              </Typography>
              <Typography variant="body1" color="text.secondary">
                {getMonthName(month)} {year}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Kliento kodas: {customer?.code}
              </Typography>
            </Box>

            <Divider sx={{ mb: 2 }} />

            {/* Summary */}
            <Grid container spacing={2} mb={3}>
              <Grid item xs={12} sm={4}>
                <Paper sx={{ p: 2, bgcolor: 'primary.light', color: 'white' }}>
                  <Typography variant="caption">
                    Paėmimai
                  </Typography>
                  <Typography variant="h5">
                    €{totals.pickups.toFixed(2)}
                  </Typography>
                </Paper>
              </Grid>
              <Grid item xs={12} sm={4}>
                <Paper sx={{ p: 2, bgcolor: 'secondary.light', color: 'white' }}>
                  <Typography variant="caption">
                    Grąžinimai
                  </Typography>
                  <Typography variant="h5">
                    €{totals.returns.toFixed(2)}
                  </Typography>
                </Paper>
              </Grid>
              <Grid item xs={12} sm={4}>
                <Paper sx={{ p: 2, bgcolor: 'success.main', color: 'white' }}>
                  <Typography variant="caption">
                    Grynoji suma
                  </Typography>
                  <Typography variant="h5">
                    €{totals.net.toFixed(2)}
                  </Typography>
                </Paper>
              </Grid>
            </Grid>

            {/* Transactions List */}
            {transactions.length === 0 ? (
              <Alert severity="info">
                Šiuo laikotarpiu nėra transakcijų
              </Alert>
            ) : (
              <TableContainer component={Paper} variant="outlined">
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Data</TableCell>
                      <TableCell>Numeris</TableCell>
                      <TableCell>Tipas</TableCell>
                      <TableCell>Prekės</TableCell>
                      <TableCell align="right">Suma</TableCell>
                      <TableCell>Atliko</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {transactions.map((transaction) => (
                      <React.Fragment key={transaction.id}>
                        <TableRow hover>
                          <TableCell>
                            {new Date(transaction.createdAt).toLocaleDateString('lt-LT')}
                            <br />
                            <Typography variant="caption" color="text.secondary">
                              {new Date(transaction.createdAt).toLocaleTimeString('lt-LT')}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2" fontWeight="bold">
                              {transaction.transactionNumber}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Chip
                              label={transaction.transactionType === 'PICKUP' ? 'Paėmimas' : 'Grąžinimas'}
                              size="small"
                              color={transaction.transactionType === 'PICKUP' ? 'primary' : 'secondary'}
                              variant="outlined"
                            />
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {transaction.totalItems} vnt.
                            </Typography>
                          </TableCell>
                          <TableCell align="right">
                            <Typography variant="body2" fontWeight="bold">
                              €{transaction.totalAmount.toFixed(2)}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {transaction.performedBy}
                            </Typography>
                          </TableCell>
                        </TableRow>
                        {/* Detailed lines (expandable in future) */}
                      </React.Fragment>
                    ))}
                    <TableRow>
                      <TableCell colSpan={4} align="right">
                        <Typography variant="h6">Iš viso:</Typography>
                      </TableCell>
                      <TableCell align="right">
                        <Typography variant="h6" color="primary">
                          €{totals.net.toFixed(2)}
                        </Typography>
                      </TableCell>
                      <TableCell />
                    </TableRow>
                  </TableBody>
                </Table>
              </TableContainer>
            )}

            {/* Footer */}
            <Box mt={4}>
              <Divider sx={{ mb: 2 }} />
              <Typography variant="body2" color="text.secondary">
                Dokumentas sugeneruotas: {new Date().toLocaleString('lt-LT')}
              </Typography>
            </Box>
          </Box>
        )}
      </DialogContent>

      <DialogActions>
        {showStatement && (
          <>
            <Button
              startIcon={<PrintIcon />}
              onClick={handlePrint}
            >
              Spausdinti
            </Button>
            <Button
              startIcon={<DownloadIcon />}
              onClick={handleExportPDF}
            >
              Eksportuoti PDF
            </Button>
            <Button onClick={() => setShowStatement(false)}>
              Naujas išrašas
            </Button>
          </>
        )}
        <Button onClick={onClose}>Uždaryti</Button>
      </DialogActions>
    </Dialog>
  );
};

export default MonthlyStatementDialog;
