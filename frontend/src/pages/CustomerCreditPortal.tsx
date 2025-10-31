import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Container,
  Paper,
  Typography,
  Grid,
  Card,
  CardContent,
  CardActions,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  IconButton,
  Alert,
  Tabs,
  Tab,
  Divider,
  LinearProgress,
} from '@mui/material';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import UndoIcon from '@mui/icons-material/Undo';
import HistoryIcon from '@mui/icons-material/History';
import ReceiptIcon from '@mui/icons-material/Receipt';
import VisibilityIcon from '@mui/icons-material/Visibility';
import WarningIcon from '@mui/icons-material/Warning';
import QuickCreditPickupDialog from '../components/QuickCreditPickupDialog';
import QuickCreditReturnDialog from '../components/QuickCreditReturnDialog';
import MonthlyStatementDialog from '../components/MonthlyStatementDialog';

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

interface Transaction {
  id: string;
  transactionNumber: string;
  transactionType: 'PICKUP' | 'RETURN';
  status: string;
  totalAmount: number;
  totalItems: number;
  createdAt: string;
}

/**
 * CustomerCreditPortal - Self-service portal for customers
 *
 * Features:
 * - View credit balance and limit
 * - Quick pickup items
 * - Quick return items
 * - View transaction history
 * - Generate monthly statements
 * - Real-time balance updates
 */
const CustomerCreditPortal: React.FC = () => {
  const [currentCustomer, setCurrentCustomer] = useState<Customer | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [pickupDialogOpen, setPickupDialogOpen] = useState(false);
  const [returnDialogOpen, setReturnDialogOpen] = useState(false);
  const [statementDialogOpen, setStatementDialogOpen] = useState(false);
  const [selectedTab, setSelectedTab] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadCustomerData();
  }, []);

  const loadCustomerData = async () => {
    try {
      setLoading(true);
      // TODO: Replace with actual API call to get current logged-in customer
      // const response = await fetch('/api/customers/me');
      // const customerData = await response.json();

      // Mock data for now
      const mockCustomer: Customer = {
        id: '1',
        code: 'B001',
        customerType: 'BUSINESS',
        companyName: 'UAB Elektros Darbai',
        creditLimit: 5000.0,
        currentBalance: 2350.50,
      };

      setCurrentCustomer(mockCustomer);
      loadTransactions(mockCustomer.id);
    } catch (error) {
      console.error('Failed to load customer data:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadTransactions = async (customerId: string) => {
    try {
      // TODO: Replace with actual API call
      // const response = await fetch(`/api/credit-transactions/customer/${customerId}/recent?limit=20`);
      // const data = await response.json();

      // Mock data for now
      const mockTransactions: Transaction[] = [
        {
          id: '1',
          transactionNumber: 'P1730000001',
          transactionType: 'PICKUP',
          status: 'CONFIRMED',
          totalAmount: 250.50,
          totalItems: 5,
          createdAt: new Date(Date.now() - 86400000).toISOString(),
        },
        {
          id: '2',
          transactionNumber: 'R1730000002',
          transactionType: 'RETURN',
          status: 'CONFIRMED',
          totalAmount: 50.00,
          totalItems: 2,
          createdAt: new Date(Date.now() - 172800000).toISOString(),
        },
      ];

      setTransactions(mockTransactions);
    } catch (error) {
      console.error('Failed to load transactions:', error);
    }
  };

  const handlePickupSubmit = async (
    customerId: string,
    lines: Array<{ productCode: string; quantity: number; notes?: string }>,
    transactionType: 'PICKUP' | 'RETURN',
    performedBy: string,
    performedByRole: string,
    notes?: string
  ) => {
    try {
      // TODO: Replace with actual API call
      alert('Paėmimas sėkmingai užregistruotas!');
      loadCustomerData();
    } catch (error) {
      console.error('Failed to create pickup:', error);
      alert('Klaida registruojant paėmimą');
    }
  };

  const handleReturnSubmit = async (
    customerId: string,
    lines: Array<{ productCode: string; quantity: number; notes?: string }>,
    performedBy: string,
    performedByRole: string,
    notes?: string
  ) => {
    try {
      // TODO: Replace with actual API call
      alert('Grąžinimas sėkmingai užregistruotas!');
      loadCustomerData();
    } catch (error) {
      console.error('Failed to create return:', error);
      alert('Klaida registruojant grąžinimą');
    }
  };

  const getCustomerDisplayName = (): string => {
    if (!currentCustomer) return '';
    if (currentCustomer.customerType === 'BUSINESS') {
      return currentCustomer.companyName || currentCustomer.code;
    }
    if (currentCustomer.firstName && currentCustomer.lastName) {
      return `${currentCustomer.firstName} ${currentCustomer.lastName}`;
    }
    return currentCustomer.code;
  };

  const getCreditUsagePercentage = (): number => {
    if (!currentCustomer || currentCustomer.creditLimit === 0) return 0;
    return (currentCustomer.currentBalance / currentCustomer.creditLimit) * 100;
  };

  const getStatusColor = (status: string): 'default' | 'warning' | 'success' | 'error' | 'info' => {
    switch (status) {
      case 'PENDING': return 'warning';
      case 'CONFIRMED': return 'success';
      case 'CANCELLED': return 'error';
      default: return 'default';
    }
  };

  const getStatusLabel = (status: string): string => {
    switch (status) {
      case 'PENDING': return 'Laukiama';
      case 'CONFIRMED': return 'Patvirtinta';
      case 'CANCELLED': return 'Atšaukta';
      default: return status;
    }
  };

  const creditUsagePercentage = getCreditUsagePercentage();
  const isOverLimit = currentCustomer && currentCustomer.currentBalance > currentCustomer.creditLimit;

  if (loading) {
    return (
      <Container maxWidth="xl" sx={{ mt: 4 }}>
        <LinearProgress />
      </Container>
    );
  }

  if (!currentCustomer) {
    return (
      <Container maxWidth="xl" sx={{ mt: 4 }}>
        <Alert severity="error">
          Nepavyko užkrauti kliento duomenų
        </Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      {/* Header */}
      <Box mb={4}>
        <Typography variant="h4" component="h1" gutterBottom>
          Kredito savitarna
        </Typography>
        <Typography variant="h6" color="text.secondary">
          {getCustomerDisplayName()} ({currentCustomer.code})
        </Typography>
      </Box>

      {/* Credit Summary Card */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Grid container spacing={3}>
          <Grid item xs={12} md={4}>
            <Typography variant="caption" color="text.secondary">
              Dabartinė skola
            </Typography>
            <Typography
              variant="h3"
              color={isOverLimit ? 'error' : 'inherit'}
              fontWeight="bold"
            >
              €{currentCustomer.currentBalance.toFixed(2)}
            </Typography>
          </Grid>
          <Grid item xs={12} md={4}>
            <Typography variant="caption" color="text.secondary">
              Kredito limitas
            </Typography>
            <Typography variant="h3" fontWeight="bold">
              €{currentCustomer.creditLimit.toFixed(2)}
            </Typography>
          </Grid>
          <Grid item xs={12} md={4}>
            <Typography variant="caption" color="text.secondary">
              Likutis
            </Typography>
            <Typography variant="h3" color="success.main" fontWeight="bold">
              €{(currentCustomer.creditLimit - currentCustomer.currentBalance).toFixed(2)}
            </Typography>
          </Grid>
          <Grid item xs={12}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Box sx={{ flex: 1 }}>
                <LinearProgress
                  variant="determinate"
                  value={Math.min(creditUsagePercentage, 100)}
                  color={isOverLimit ? 'error' : creditUsagePercentage > 80 ? 'warning' : 'primary'}
                  sx={{ height: 10, borderRadius: 5 }}
                />
              </Box>
              <Typography variant="body2" color="text.secondary">
                {creditUsagePercentage.toFixed(0)}%
              </Typography>
            </Box>
          </Grid>
          {isOverLimit && (
            <Grid item xs={12}>
              <Alert severity="error" icon={<WarningIcon />}>
                Viršytas kredito limitas! Prašome susisiekti su mumis.
              </Alert>
            </Grid>
          )}
        </Grid>
      </Paper>

      {/* Action Buttons */}
      <Grid container spacing={2} mb={3}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" gap={2} mb={2}>
                <ShoppingCartIcon color="primary" fontSize="large" />
                <Typography variant="h6">
                  Pasiimti prekes
                </Typography>
              </Box>
              <Typography variant="body2" color="text.secondary">
                Greitai užregistruokite paimtas prekes
              </Typography>
            </CardContent>
            <CardActions>
              <Button
                fullWidth
                variant="contained"
                color="primary"
                startIcon={<ShoppingCartIcon />}
                onClick={() => setPickupDialogOpen(true)}
                disabled={isOverLimit}
              >
                Pasiimti
              </Button>
            </CardActions>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" gap={2} mb={2}>
                <UndoIcon color="secondary" fontSize="large" />
                <Typography variant="h6">
                  Grąžinti prekes
                </Typography>
              </Box>
              <Typography variant="body2" color="text.secondary">
                Greitai užregistruokite grąžintas prekes
              </Typography>
            </CardContent>
            <CardActions>
              <Button
                fullWidth
                variant="contained"
                color="secondary"
                startIcon={<UndoIcon />}
                onClick={() => setReturnDialogOpen(true)}
              >
                Grąžinti
              </Button>
            </CardActions>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" gap={2} mb={2}>
                <HistoryIcon color="info" fontSize="large" />
                <Typography variant="h6">
                  Istorija
                </Typography>
              </Box>
              <Typography variant="body2" color="text.secondary">
                Peržiūrėkite visų operacijų istoriją
              </Typography>
            </CardContent>
            <CardActions>
              <Button
                fullWidth
                variant="outlined"
                color="info"
                startIcon={<HistoryIcon />}
                onClick={() => setSelectedTab(1)}
              >
                Žiūrėti
              </Button>
            </CardActions>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" gap={2} mb={2}>
                <ReceiptIcon color="success" fontSize="large" />
                <Typography variant="h6">
                  Mėnesio išrašas
                </Typography>
              </Box>
              <Typography variant="body2" color="text.secondary">
                Generuokite mėnesio ataskaitas
              </Typography>
            </CardContent>
            <CardActions>
              <Button
                fullWidth
                variant="outlined"
                color="success"
                startIcon={<ReceiptIcon />}
                onClick={() => setStatementDialogOpen(true)}
              >
                Generuoti
              </Button>
            </CardActions>
          </Card>
        </Grid>
      </Grid>

      {/* Tabs */}
      <Paper sx={{ mb: 3 }}>
        <Tabs
          value={selectedTab}
          onChange={(e, newValue) => setSelectedTab(newValue)}
          variant="fullWidth"
        >
          <Tab label="Apžvalga" />
          <Tab label="Transakcijos" />
        </Tabs>
      </Paper>

      {/* Tab Content */}
      {selectedTab === 0 && (
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom>
                Paskutinės operacijos
              </Typography>
              <Divider sx={{ mb: 2 }} />
              {transactions.slice(0, 5).map((transaction) => (
                <Box key={transaction.id} sx={{ mb: 2, pb: 2, borderBottom: '1px solid #eee' }}>
                  <Box display="flex" justifyContent="space-between" alignItems="center">
                    <Box>
                      <Typography variant="body2" fontWeight="bold">
                        {transaction.transactionNumber}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {new Date(transaction.createdAt).toLocaleDateString('lt-LT')}
                      </Typography>
                    </Box>
                    <Box textAlign="right">
                      <Chip
                        label={transaction.transactionType === 'PICKUP' ? 'Paėmimas' : 'Grąžinimas'}
                        size="small"
                        color={transaction.transactionType === 'PICKUP' ? 'primary' : 'secondary'}
                      />
                      <Typography variant="body2" fontWeight="bold" sx={{ mt: 0.5 }}>
                        €{transaction.totalAmount.toFixed(2)}
                      </Typography>
                    </Box>
                  </Box>
                </Box>
              ))}
            </Paper>
          </Grid>

          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom>
                Instrukcijos
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Box sx={{ '& > div': { mb: 2 } }}>
                <Box>
                  <Typography variant="subtitle2" fontWeight="bold">
                    1. Kaip pasiimti prekes?
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Spauskite "Pasiimti" mygtuką, įveskite prekių kodus ir kiekius, patvirtinkite.
                  </Typography>
                </Box>
                <Box>
                  <Typography variant="subtitle2" fontWeight="bold">
                    2. Kaip grąžinti prekes?
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Spauskite "Grąžinti" mygtuką, įveskite grąžinamų prekių kodus, pasirinki te priežastį.
                  </Typography>
                </Box>
                <Box>
                  <Typography variant="subtitle2" fontWeight="bold">
                    3. Kredito limitas
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Jūsų kredito limitas yra €{currentCustomer.creditLimit.toFixed(2)}. Viršijus limitą paėmimas bus užblokuotas.
                  </Typography>
                </Box>
                <Box>
                  <Typography variant="subtitle2" fontWeight="bold">
                    4. Pagalba
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Kilus klausimams susisiekite su mumis telefonu arba el. paštu.
                  </Typography>
                </Box>
              </Box>
            </Paper>
          </Grid>
        </Grid>
      )}

      {selectedTab === 1 && (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Numeris</TableCell>
                <TableCell>Tipas</TableCell>
                <TableCell>Data</TableCell>
                <TableCell>Statusas</TableCell>
                <TableCell align="right">Suma</TableCell>
                <TableCell align="right">Prekių</TableCell>
                <TableCell align="center">Veiksmai</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {transactions.map((transaction) => (
                <TableRow key={transaction.id} hover>
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
                    {new Date(transaction.createdAt).toLocaleDateString('lt-LT')}
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={getStatusLabel(transaction.status)}
                      size="small"
                      color={getStatusColor(transaction.status)}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Typography variant="body2" fontWeight="bold">
                      €{transaction.totalAmount.toFixed(2)}
                    </Typography>
                  </TableCell>
                  <TableCell align="right">{transaction.totalItems}</TableCell>
                  <TableCell align="center">
                    <IconButton size="small">
                      <VisibilityIcon fontSize="small" />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Dialogs */}
      <QuickCreditPickupDialog
        open={pickupDialogOpen}
        onClose={() => setPickupDialogOpen(false)}
        onSubmit={handlePickupSubmit}
        currentUser={getCustomerDisplayName()}
      />

      <QuickCreditReturnDialog
        open={returnDialogOpen}
        onClose={() => setReturnDialogOpen(false)}
        onSubmit={handleReturnSubmit}
        currentUser={getCustomerDisplayName()}
        preSelectedCustomer={currentCustomer}
      />

      <MonthlyStatementDialog
        open={statementDialogOpen}
        onClose={() => setStatementDialogOpen(false)}
        preSelectedCustomer={currentCustomer}
      />
    </Container>
  );
};

export default CustomerCreditPortal;
