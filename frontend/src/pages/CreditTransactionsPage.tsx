import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Container,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  Chip,
  IconButton,
  TextField,
  Grid,
  Card,
  CardContent,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Tabs,
  Tab,
  Tooltip,
  Menu,
  MenuItem,
  FormControl,
  InputLabel,
  Select,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import SearchIcon from '@mui/icons-material/Search';
import VisibilityIcon from '@mui/icons-material/Visibility';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import ReceiptIcon from '@mui/icons-material/Receipt';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import UndoIcon from '@mui/icons-material/Undo';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import QuickCreditPickupDialog from '../components/QuickCreditPickupDialog';

interface CreditTransaction {
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

interface CreditTransactionDetails extends CreditTransaction {
  customerId: string;
  performedByRole: string;
  confirmedBy?: string;
  confirmedAt?: string;
  notes?: string;
  updatedAt: string;
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
}

/**
 * CreditTransactionsPage - Credit transaction management page
 *
 * Features:
 * - Quick credit pickup/return dialog
 * - Transaction list with filters
 * - Transaction details view
 * - Confirm/Cancel transactions
 * - Search and filter capabilities
 * - Monthly statement generation
 */
const CreditTransactionsPage: React.FC = () => {
  const [transactions, setTransactions] = useState<CreditTransaction[]>([]);
  const [selectedTransaction, setSelectedTransaction] = useState<CreditTransactionDetails | null>(null);
  const [quickDialogOpen, setQuickDialogOpen] = useState(false);
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterStatus, setFilterStatus] = useState<string>('ALL');
  const [filterType, setFilterType] = useState<string>('ALL');
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [selectedTransactionId, setSelectedTransactionId] = useState<string | null>(null);

  useEffect(() => {
    loadTransactions();
  }, []);

  const loadTransactions = async () => {
    try {
      // TODO: Replace with actual API call
      // const response = await fetch('/api/credit-transactions?page=0&size=20');
      // const data = await response.json();
      // setTransactions(data.content);

      // Mock data for now
      setTransactions([
        {
          id: '1',
          transactionNumber: 'P1730000001',
          customerCode: 'B001',
          customerName: 'UAB Elektros Darbai',
          transactionType: 'PICKUP',
          status: 'PENDING',
          totalAmount: 250.50,
          totalItems: 5,
          performedBy: 'Jonas Jonaitis',
          createdAt: new Date().toISOString(),
        },
        {
          id: '2',
          transactionNumber: 'P1730000002',
          customerCode: 'C001',
          customerName: 'MB Elektrikas',
          transactionType: 'PICKUP',
          status: 'CONFIRMED',
          totalAmount: 480.75,
          totalItems: 8,
          performedBy: 'Petras Petraitis',
          createdAt: new Date(Date.now() - 86400000).toISOString(),
        },
      ]);
    } catch (error) {
      console.error('Failed to load transactions:', error);
    }
  };

  const handleQuickPickupSubmit = async (
    customerId: string,
    lines: Array<{ productCode: string; quantity: number; notes?: string }>,
    transactionType: 'PICKUP' | 'RETURN',
    performedBy: string,
    performedByRole: string,
    notes?: string
  ) => {
    try {
      // TODO: Replace with actual API call
      /*
      const response = await fetch('/api/credit-transactions/quick-pickup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          customerCode: customerId, // In real implementation, we'd need to get customer code
          items: lines,
          performedBy,
          performedByRole,
          notes,
        }),
      });
      const data = await response.json();
      */

      alert(`${transactionType === 'PICKUP' ? 'Paėmimas' : 'Grąžinimas'} sėkmingai sukurtas!`);
      loadTransactions();
    } catch (error) {
      console.error('Failed to create transaction:', error);
      alert('Klaida kuriant operaciją');
    }
  };

  const handleViewDetails = async (transactionId: string) => {
    try {
      // TODO: Replace with actual API call
      // const response = await fetch(`/api/credit-transactions/${transactionId}`);
      // const data = await response.json();
      // setSelectedTransaction(data);

      // Mock data for now
      setSelectedTransaction({
        id: transactionId,
        transactionNumber: 'P1730000001',
        customerId: '1',
        customerCode: 'B001',
        customerName: 'UAB Elektros Darbai',
        transactionType: 'PICKUP',
        status: 'PENDING',
        totalAmount: 250.50,
        totalItems: 5,
        performedBy: 'Jonas Jonaitis',
        performedByRole: 'EMPLOYEE',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        lines: [
          {
            id: '1',
            productId: '1',
            productCode: 'CAB-001',
            productName: 'Kabelis NYM 3x1.5',
            quantity: 50,
            unitPrice: 2.50,
            lineTotal: 125.00,
          },
          {
            id: '2',
            productId: '2',
            productCode: 'SW-001',
            productName: 'Jungiklis Schneider',
            quantity: 5,
            unitPrice: 25.10,
            lineTotal: 125.50,
          },
        ],
      });
      setDetailsDialogOpen(true);
    } catch (error) {
      console.error('Failed to load transaction details:', error);
    }
  };

  const handleConfirmTransaction = async (transactionId: string) => {
    try {
      // TODO: Replace with actual API call
      /*
      const response = await fetch(`/api/credit-transactions/${transactionId}/confirm`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          confirmedBy: 'Current User',
        }),
      });
      */

      alert('Operacija patvirtinta!');
      loadTransactions();
    } catch (error) {
      console.error('Failed to confirm transaction:', error);
      alert('Klaida patvirtinant operaciją');
    }
  };

  const handleCancelTransaction = async (transactionId: string) => {
    const reason = prompt('Įveskite atšaukimo priežastį:');
    if (!reason) return;

    try {
      // TODO: Replace with actual API call
      /*
      const response = await fetch(`/api/credit-transactions/${transactionId}/cancel?reason=${reason}`, {
        method: 'POST',
      });
      */

      alert('Operacija atšaukta!');
      loadTransactions();
    } catch (error) {
      console.error('Failed to cancel transaction:', error);
      alert('Klaida atšaukiant operaciją');
    }
  };

  const getStatusColor = (status: string): 'default' | 'warning' | 'success' | 'error' | 'info' => {
    switch (status) {
      case 'PENDING': return 'warning';
      case 'CONFIRMED': return 'success';
      case 'INVOICED': return 'info';
      case 'CANCELLED': return 'error';
      default: return 'default';
    }
  };

  const getStatusLabel = (status: string): string => {
    switch (status) {
      case 'PENDING': return 'Laukiama';
      case 'CONFIRMED': return 'Patvirtinta';
      case 'INVOICED': return 'Į sąskaitą';
      case 'CANCELLED': return 'Atšaukta';
      default: return status;
    }
  };

  const getTypeLabel = (type: string): string => {
    return type === 'PICKUP' ? 'Paėmimas' : 'Grąžinimas';
  };

  const filteredTransactions = transactions.filter(t => {
    const matchesSearch = searchQuery === '' ||
      t.transactionNumber.toLowerCase().includes(searchQuery.toLowerCase()) ||
      t.customerCode.toLowerCase().includes(searchQuery.toLowerCase()) ||
      t.customerName.toLowerCase().includes(searchQuery.toLowerCase());

    const matchesStatus = filterStatus === 'ALL' || t.status === filterStatus;
    const matchesType = filterType === 'ALL' || t.transactionType === filterType;

    return matchesSearch && matchesStatus && matchesType;
  });

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      {/* Header */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" component="h1">
          Kredito transakcijos
        </Typography>
        <Button
          variant="contained"
          color="primary"
          startIcon={<AddIcon />}
          onClick={() => setQuickDialogOpen(true)}
          size="large"
        >
          Greitas paėmimas
        </Button>
      </Box>

      {/* Summary Cards */}
      <Grid container spacing={3} mb={3}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="text.secondary" gutterBottom>
                Laukiančios
              </Typography>
              <Typography variant="h4">
                {transactions.filter(t => t.status === 'PENDING').length}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="text.secondary" gutterBottom>
                Patvirtintos šiandien
              </Typography>
              <Typography variant="h4">
                {transactions.filter(t =>
                  t.status === 'CONFIRMED' &&
                  new Date(t.createdAt).toDateString() === new Date().toDateString()
                ).length}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="text.secondary" gutterBottom>
                Paėmimai šiandien
              </Typography>
              <Typography variant="h4" color="primary">
                €{transactions
                  .filter(t =>
                    t.transactionType === 'PICKUP' &&
                    new Date(t.createdAt).toDateString() === new Date().toDateString()
                  )
                  .reduce((sum, t) => sum + t.totalAmount, 0)
                  .toFixed(2)}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="text.secondary" gutterBottom>
                Grąžinimai šiandien
              </Typography>
              <Typography variant="h4" color="secondary">
                €{transactions
                  .filter(t =>
                    t.transactionType === 'RETURN' &&
                    new Date(t.createdAt).toDateString() === new Date().toDateString()
                  )
                  .reduce((sum, t) => sum + t.totalAmount, 0)
                  .toFixed(2)}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Filters */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Paieška"
              placeholder="Operacijos numeris, kliento kodas ar pavadinimas..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              InputProps={{
                startAdornment: <SearchIcon sx={{ mr: 1, color: 'text.secondary' }} />,
              }}
            />
          </Grid>
          <Grid item xs={12} md={3}>
            <FormControl fullWidth>
              <InputLabel>Statusas</InputLabel>
              <Select
                value={filterStatus}
                label="Statusas"
                onChange={(e) => setFilterStatus(e.target.value)}
              >
                <MenuItem value="ALL">Visi</MenuItem>
                <MenuItem value="PENDING">Laukiančios</MenuItem>
                <MenuItem value="CONFIRMED">Patvirtintos</MenuItem>
                <MenuItem value="INVOICED">Į sąskaitą</MenuItem>
                <MenuItem value="CANCELLED">Atšauktos</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} md={3}>
            <FormControl fullWidth>
              <InputLabel>Tipas</InputLabel>
              <Select
                value={filterType}
                label="Tipas"
                onChange={(e) => setFilterType(e.target.value)}
              >
                <MenuItem value="ALL">Visi</MenuItem>
                <MenuItem value="PICKUP">Paėmimai</MenuItem>
                <MenuItem value="RETURN">Grąžinimai</MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </Paper>

      {/* Transactions Table */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Numeris</TableCell>
              <TableCell>Tipas</TableCell>
              <TableCell>Klientas</TableCell>
              <TableCell>Statusas</TableCell>
              <TableCell align="right">Suma</TableCell>
              <TableCell align="right">Prekių</TableCell>
              <TableCell>Atliko</TableCell>
              <TableCell>Data</TableCell>
              <TableCell align="center">Veiksmai</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredTransactions.length === 0 ? (
              <TableRow>
                <TableCell colSpan={9} align="center">
                  <Typography variant="body2" color="text.secondary" sx={{ py: 4 }}>
                    Nėra transakcijų
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              filteredTransactions.map((transaction) => (
                <TableRow key={transaction.id} hover>
                  <TableCell>
                    <Typography variant="body2" fontWeight="bold">
                      {transaction.transactionNumber}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Chip
                      icon={transaction.transactionType === 'PICKUP' ? <ShoppingCartIcon /> : <UndoIcon />}
                      label={getTypeLabel(transaction.transactionType)}
                      size="small"
                      color={transaction.transactionType === 'PICKUP' ? 'primary' : 'secondary'}
                      variant="outlined"
                    />
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2">{transaction.customerName}</Typography>
                    <Typography variant="caption" color="text.secondary">
                      {transaction.customerCode}
                    </Typography>
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
                  <TableCell>
                    <Typography variant="body2">{transaction.performedBy}</Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2">
                      {new Date(transaction.createdAt).toLocaleDateString('lt-LT')}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {new Date(transaction.createdAt).toLocaleTimeString('lt-LT')}
                    </Typography>
                  </TableCell>
                  <TableCell align="center">
                    <Tooltip title="Peržiūrėti">
                      <IconButton
                        size="small"
                        onClick={() => handleViewDetails(transaction.id)}
                      >
                        <VisibilityIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    {transaction.status === 'PENDING' && (
                      <>
                        <Tooltip title="Patvirtinti">
                          <IconButton
                            size="small"
                            color="success"
                            onClick={() => handleConfirmTransaction(transaction.id)}
                          >
                            <CheckCircleIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Atšaukti">
                          <IconButton
                            size="small"
                            color="error"
                            onClick={() => handleCancelTransaction(transaction.id)}
                          >
                            <CancelIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </>
                    )}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Quick Pickup Dialog */}
      <QuickCreditPickupDialog
        open={quickDialogOpen}
        onClose={() => setQuickDialogOpen(false)}
        onSubmit={handleQuickPickupSubmit}
        currentUser="Jonas Jonaitis"
      />

      {/* Transaction Details Dialog */}
      <Dialog
        open={detailsDialogOpen}
        onClose={() => setDetailsDialogOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          Transakcijos detalės
        </DialogTitle>
        <DialogContent dividers>
          {selectedTransaction && (
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <Typography variant="caption" color="text.secondary">
                  Numeris
                </Typography>
                <Typography variant="body1" fontWeight="bold">
                  {selectedTransaction.transactionNumber}
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="caption" color="text.secondary">
                  Statusas
                </Typography>
                <Box>
                  <Chip
                    label={getStatusLabel(selectedTransaction.status)}
                    color={getStatusColor(selectedTransaction.status)}
                  />
                </Box>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="caption" color="text.secondary">
                  Klientas
                </Typography>
                <Typography variant="body1">
                  {selectedTransaction.customerName}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {selectedTransaction.customerCode}
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="caption" color="text.secondary">
                  Tipas
                </Typography>
                <Typography variant="body1">
                  {getTypeLabel(selectedTransaction.transactionType)}
                </Typography>
              </Grid>
              <Grid item xs={12}>
                <Divider sx={{ my: 2 }} />
                <Typography variant="h6" gutterBottom>
                  Prekės
                </Typography>
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Kodas</TableCell>
                        <TableCell>Pavadinimas</TableCell>
                        <TableCell align="right">Kaina</TableCell>
                        <TableCell align="right">Kiekis</TableCell>
                        <TableCell align="right">Suma</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {selectedTransaction.lines.map((line) => (
                        <TableRow key={line.id}>
                          <TableCell>{line.productCode}</TableCell>
                          <TableCell>{line.productName}</TableCell>
                          <TableCell align="right">€{line.unitPrice.toFixed(2)}</TableCell>
                          <TableCell align="right">{line.quantity}</TableCell>
                          <TableCell align="right">€{line.lineTotal.toFixed(2)}</TableCell>
                        </TableRow>
                      ))}
                      <TableRow>
                        <TableCell colSpan={4} align="right">
                          <Typography variant="h6">Viso:</Typography>
                        </TableCell>
                        <TableCell align="right">
                          <Typography variant="h6" color="primary">
                            €{selectedTransaction.totalAmount.toFixed(2)}
                          </Typography>
                        </TableCell>
                      </TableRow>
                    </TableBody>
                  </Table>
                </TableContainer>
              </Grid>
            </Grid>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDetailsDialogOpen(false)}>Uždaryti</Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default CreditTransactionsPage;
