import React, { useState, useEffect } from 'react';
import {
  Container,
  Paper,
  Typography,
  Box,
  Button,
  Grid,
  Card,
  CardContent,
  CardActions,
  Chip,
  Alert,
  Divider,
  IconButton,
  Tooltip,
  CircularProgress,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import RefreshIcon from '@mui/icons-material/Refresh';
import ReceiptIcon from '@mui/icons-material/Receipt';
import QuickCreditPickupDialog from '../components/QuickCreditPickupDialog';
import ConfirmTransactionDialog from '../components/ConfirmTransactionDialog';
import MonthlyStatementDialog from '../components/MonthlyStatementDialog';
import {
  createQuickCreditPickup,
  confirmCreditTransaction,
  getAllCreditTransactions,
  CreditTransactionResponse,
  CreditTransactionSummaryResponse,
  QuickCreditPickupRequest,
} from '../services/creditTransactionService';

/**
 * SimpleCreditPortal - Paprastas paėmimo į skolą portalas elektrikams
 *
 * Features:
 * - Greitas paėmimas (Quick Pickup)
 * - Greitas grąžinimas (Quick Return)
 * - Pending transactions list su confirmation
 * - Confirmed transactions history
 * - Monthly statements
 * - Signature ir photo capture per confirmation dialog
 * - Top-notch UX - aiškus ir paprastas
 */
const SimpleCreditPortal: React.FC = () => {
  const [quickDialogOpen, setQuickDialogOpen] = useState(false);
  const [confirmDialogOpen, setConfirmDialogOpen] = useState(false);
  const [statementDialogOpen, setStatementDialogOpen] = useState(false);
  const [selectedTransaction, setSelectedTransaction] = useState<CreditTransactionResponse | null>(null);
  const [pendingTransactions, setPendingTransactions] = useState<CreditTransactionSummaryResponse[]>([]);
  const [recentTransactions, setRecentTransactions] = useState<CreditTransactionSummaryResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string>('');
  const [success, setSuccess] = useState<string>('');

  useEffect(() => {
    loadTransactions();
  }, []);

  const loadTransactions = async () => {
    setLoading(true);
    try {
      const response = await getAllCreditTransactions(0, 50);
      const pending = response.content.filter(t => t.status === 'PENDING');
      const recent = response.content.filter(t => t.status !== 'PENDING').slice(0, 10);

      setPendingTransactions(pending);
      setRecentTransactions(recent);
      setError('');
    } catch (err) {
      console.error('Failed to load transactions:', err);
      setError('Nepavyko įkelti operacijų');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickPickup = async (
    customerId: string,
    lines: Array<{ productCode: string; quantity: number; notes?: string }>,
    _transactionType: 'PICKUP' | 'RETURN',
    performedBy: string,
    performedByRole: string,
    notes?: string
  ) => {
    try {
      const request: QuickCreditPickupRequest = {
        customerCode: customerId, // Note: Using customer code not ID
        items: lines,
        performedBy,
        performedByRole,
        notes,
      };

      const result = await createQuickCreditPickup(request);
      setSuccess(`Operacija sukurta: ${result.transactionNumber}`);

      // Immediately show confirmation dialog
      setSelectedTransaction(result);
      setConfirmDialogOpen(true);

      loadTransactions();
    } catch (err) {
      console.error('Failed to create transaction:', err);
      setError('Nepavyko sukurti operacijos');
    }
  };

  const handleConfirmTransaction = async (
    confirmedBy: string,
    signatureData: string,
    photoData: string,
    notes: string
  ) => {
    if (!selectedTransaction) return;

    try {
      await confirmCreditTransaction(
        selectedTransaction.id,
        confirmedBy,
        signatureData,
        photoData,
        notes
      );

      setSuccess(`Operacija patvirtinta: ${selectedTransaction.transactionNumber}`);
      setConfirmDialogOpen(false);
      setSelectedTransaction(null);
      loadTransactions();
    } catch (err) {
      console.error('Failed to confirm transaction:', err);
      setError('Nepavyko patvirtinti operacijos');
    }
  };

  const handleConfirmPending = (transaction: CreditTransactionSummaryResponse) => {
    setSelectedTransaction(transaction as any);
    setConfirmDialogOpen(true);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING':
        return 'warning';
      case 'CONFIRMED':
        return 'success';
      case 'INVOICED':
        return 'info';
      case 'CANCELLED':
        return 'error';
      default:
        return 'default';
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'PENDING':
        return 'Laukiama patvirtinimo';
      case 'CONFIRMED':
        return 'Patvirtinta';
      case 'INVOICED':
        return 'Į sąskaitą įtraukta';
      case 'CANCELLED':
        return 'Atšaukta';
      default:
        return status;
    }
  };

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={4}>
        <Typography variant="h4" fontWeight="bold">
          Paėmimas į Skolą - Elektrikų Portalas
        </Typography>
        <Box display="flex" gap={2}>
          <Button
            variant="contained"
            color="primary"
            startIcon={<AddIcon />}
            onClick={() => setQuickDialogOpen(true)}
            size="large"
          >
            Naujas Paėmimas / Grąžinimas
          </Button>
          <Button
            variant="outlined"
            startIcon={<ReceiptIcon />}
            onClick={() => setStatementDialogOpen(true)}
          >
            Mėnesio Išrašas
          </Button>
          <Tooltip title="Atnaujinti">
            <IconButton onClick={loadTransactions} disabled={loading}>
              <RefreshIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" onClose={() => setError('')} sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" onClose={() => setSuccess('')} sx={{ mb: 2 }}>
          {success}
        </Alert>
      )}

      {loading && (
        <Box display="flex" justifyContent="center" my={4}>
          <CircularProgress />
        </Box>
      )}

      <Grid container spacing={4}>
        {/* Pending Transactions - Laukiančios Patvirtinimo */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Box display="flex" alignItems="center" justifyContent="space-between" mb={2}>
              <Typography variant="h5" fontWeight="bold">
                Laukia Patvirtinimo ({pendingTransactions.length})
              </Typography>
              <Chip label="Reikia Parašo" color="warning" />
            </Box>
            <Divider sx={{ mb: 2 }} />

            {pendingTransactions.length === 0 ? (
              <Alert severity="info">Nėra operacijų laukiančių patvirtinimo</Alert>
            ) : (
              <Grid container spacing={2}>
                {pendingTransactions.map((transaction) => (
                  <Grid item xs={12} sm={6} md={4} key={transaction.id}>
                    <Card variant="outlined" sx={{ height: '100%' }}>
                      <CardContent>
                        <Box display="flex" justifyContent="space-between" alignItems="start" mb={1}>
                          <Typography variant="h6" fontWeight="bold">
                            {transaction.transactionNumber}
                          </Typography>
                          <Chip
                            label={transaction.transactionType === 'PICKUP' ? 'Paėmimas' : 'Grąžinimas'}
                            size="small"
                            color={transaction.transactionType === 'PICKUP' ? 'primary' : 'secondary'}
                          />
                        </Box>
                        <Typography variant="body2" color="text.secondary" gutterBottom>
                          {transaction.customerName}
                        </Typography>
                        <Typography variant="h5" color="primary" gutterBottom>
                          €{transaction.totalAmount.toFixed(2)}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {transaction.totalItems} vnt. | {transaction.performedBy}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {new Date(transaction.createdAt).toLocaleString('lt-LT')}
                        </Typography>
                      </CardContent>
                      <CardActions>
                        <Button
                          fullWidth
                          variant="contained"
                          color="success"
                          startIcon={<CheckCircleIcon />}
                          onClick={() => handleConfirmPending(transaction)}
                        >
                          Patvirtinti su Parašu
                        </Button>
                      </CardActions>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            )}
          </Paper>
        </Grid>

        {/* Recent Confirmed Transactions - Patvirtintos Operacijos */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h5" fontWeight="bold" mb={2}>
              Paskutinės Patvirtintos Operacijos
            </Typography>
            <Divider sx={{ mb: 2 }} />

            {recentTransactions.length === 0 ? (
              <Alert severity="info">Nėra patvirtintų operacijų</Alert>
            ) : (
              <Grid container spacing={2}>
                {recentTransactions.map((transaction) => (
                  <Grid item xs={12} sm={6} md={4} key={transaction.id}>
                    <Card variant="outlined">
                      <CardContent>
                        <Box display="flex" justifyContent="space-between" alignItems="start" mb={1}>
                          <Typography variant="h6" fontWeight="bold">
                            {transaction.transactionNumber}
                          </Typography>
                          <Chip
                            label={getStatusLabel(transaction.status)}
                            size="small"
                            color={getStatusColor(transaction.status)}
                          />
                        </Box>
                        <Typography variant="body2" color="text.secondary" gutterBottom>
                          {transaction.customerName}
                        </Typography>
                        <Typography variant="h5" color="primary" gutterBottom>
                          €{transaction.totalAmount.toFixed(2)}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {transaction.totalItems} vnt. | {transaction.performedBy}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {new Date(transaction.createdAt).toLocaleString('lt-LT')}
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            )}
          </Paper>
        </Grid>
      </Grid>

      {/* Dialogs */}
      <QuickCreditPickupDialog
        open={quickDialogOpen}
        onClose={() => setQuickDialogOpen(false)}
        onSubmit={handleQuickPickup}
      />

      <ConfirmTransactionDialog
        open={confirmDialogOpen}
        onClose={() => {
          setConfirmDialogOpen(false);
          setSelectedTransaction(null);
        }}
        onConfirm={handleConfirmTransaction}
        transaction={selectedTransaction}
      />

      <MonthlyStatementDialog
        open={statementDialogOpen}
        onClose={() => setStatementDialogOpen(false)}
      />
    </Container>
  );
};

export default SimpleCreditPortal;
