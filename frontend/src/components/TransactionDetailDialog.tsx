import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Grid,
  Paper,
  Typography,
  Box,
  Divider,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  Avatar,
} from '@mui/material';
import PrintIcon from '@mui/icons-material/Print';
import CloseIcon from '@mui/icons-material/Close';
import PersonIcon from '@mui/icons-material/Person';
import InventoryIcon from '@mui/icons-material/Inventory';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';

interface Transaction {
  id: string;
  transactionNumber: string;
  transactionType: 'PICKUP' | 'RETURN';
  status: 'PENDING' | 'CONFIRMED' | 'INVOICED' | 'CANCELLED';
  customerCode: string;
  customerName: string;
  lines: Array<{
    id: string;
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
  signatureData?: string;
  photoData?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

interface TransactionDetailDialogProps {
  open: boolean;
  onClose: () => void;
  transaction: Transaction | null;
}

/**
 * TransactionDetailDialog - Comprehensive transaction details view
 *
 * Features:
 * - Complete transaction information
 * - Line items breakdown
 * - Signature and photo display
 * - Confirmation details
 * - Timeline of changes
 * - Print-friendly format
 */
const TransactionDetailDialog: React.FC<TransactionDetailDialogProps> = ({
  open,
  onClose,
  transaction,
}) => {
  const handlePrint = () => {
    window.print();
  };

  if (!transaction) return null;

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING': return 'warning';
      case 'CONFIRMED': return 'success';
      case 'INVOICED': return 'info';
      case 'CANCELLED': return 'error';
      default: return 'default';
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'PENDING': return 'Laukiama patvirtinimo';
      case 'CONFIRMED': return 'Patvirtinta';
      case 'INVOICED': return 'Į sąskaitą įtraukta';
      case 'CANCELLED': return 'Atšaukta';
      default: return status;
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="lg" fullWidth>
      <DialogTitle>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box>
            <Typography variant="h5" fontWeight="bold">
              Operacijos Detalės
            </Typography>
            <Typography variant="subtitle2" color="text.secondary">
              {transaction.transactionNumber}
            </Typography>
          </Box>
          <Chip
            label={getStatusLabel(transaction.status)}
            color={getStatusColor(transaction.status)}
            size="medium"
          />
        </Box>
      </DialogTitle>

      <DialogContent dividers>
        <Grid container spacing={3}>
          {/* Header Information */}
          <Grid item xs={12}>
            <Paper sx={{ p: 3, bgcolor: 'background.default' }}>
              <Grid container spacing={3}>
                <Grid item xs={12} md={6}>
                  <Box display="flex" alignItems="start" gap={2}>
                    <Avatar sx={{ bgcolor: 'primary.main' }}>
                      <PersonIcon />
                    </Avatar>
                    <Box>
                      <Typography variant="caption" color="text.secondary">
                        Klientas
                      </Typography>
                      <Typography variant="h6" fontWeight="bold">
                        {transaction.customerName}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Kodas: {transaction.customerCode}
                      </Typography>
                    </Box>
                  </Box>
                </Grid>

                <Grid item xs={12} md={6}>
                  <Box display="flex" alignItems="start" gap={2}>
                    <Avatar sx={{ bgcolor: 'secondary.main' }}>
                      <InventoryIcon />
                    </Avatar>
                    <Box>
                      <Typography variant="caption" color="text.secondary">
                        Operacijos Tipas
                      </Typography>
                      <Typography variant="h6" fontWeight="bold">
                        {transaction.transactionType === 'PICKUP' ? 'Paėmimas' : 'Grąžinimas'}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {transaction.totalItems} prekės
                      </Typography>
                    </Box>
                  </Box>
                </Grid>

                <Grid item xs={12} md={6}>
                  <Box display="flex" alignItems="start" gap={2}>
                    <Avatar sx={{ bgcolor: 'info.main' }}>
                      <CalendarTodayIcon />
                    </Avatar>
                    <Box>
                      <Typography variant="caption" color="text.secondary">
                        Sukurta
                      </Typography>
                      <Typography variant="body1" fontWeight="medium">
                        {new Date(transaction.createdAt).toLocaleString('lt-LT')}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Atliko: {transaction.performedBy}
                      </Typography>
                    </Box>
                  </Box>
                </Grid>

                {transaction.confirmedAt && (
                  <Grid item xs={12} md={6}>
                    <Box display="flex" alignItems="start" gap={2}>
                      <Avatar sx={{ bgcolor: 'success.main' }}>
                        <CheckCircleIcon />
                      </Avatar>
                      <Box>
                        <Typography variant="caption" color="text.secondary">
                          Patvirtinta
                        </Typography>
                        <Typography variant="body1" fontWeight="medium">
                          {new Date(transaction.confirmedAt).toLocaleString('lt-LT')}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Patvirtino: {transaction.confirmedBy}
                        </Typography>
                      </Box>
                    </Box>
                  </Grid>
                )}
              </Grid>
            </Paper>
          </Grid>

          {/* Line Items */}
          <Grid item xs={12}>
            <Typography variant="h6" fontWeight="bold" gutterBottom>
              Prekių Sąrašas
            </Typography>
            <TableContainer component={Paper} variant="outlined">
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Kodas</TableCell>
                    <TableCell>Pavadinimas</TableCell>
                    <TableCell align="right">Kiekis</TableCell>
                    <TableCell align="right">Vnt. Kaina</TableCell>
                    <TableCell align="right">Suma</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {transaction.lines.map((line) => (
                    <TableRow key={line.id} hover>
                      <TableCell>
                        <Chip label={line.productCode} size="small" variant="outlined" />
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">{line.productName}</Typography>
                        {line.notes && (
                          <Typography variant="caption" color="text.secondary">
                            Pastaba: {line.notes}
                          </Typography>
                        )}
                      </TableCell>
                      <TableCell align="right">
                        <Typography variant="body2" fontWeight="medium">
                          {line.quantity}
                        </Typography>
                      </TableCell>
                      <TableCell align="right">
                        <Typography variant="body2">
                          €{line.unitPrice.toFixed(2)}
                        </Typography>
                      </TableCell>
                      <TableCell align="right">
                        <Typography variant="body2" fontWeight="bold" color="primary">
                          €{line.lineTotal.toFixed(2)}
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ))}
                  <TableRow>
                    <TableCell colSpan={4} align="right">
                      <Typography variant="h6">IŠ VISO:</Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="h5" color="primary" fontWeight="bold">
                        €{transaction.totalAmount.toFixed(2)}
                      </Typography>
                    </TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </TableContainer>
          </Grid>

          {/* Signature and Photo */}
          {(transaction.signatureData || transaction.photoData) && (
            <Grid item xs={12}>
              <Divider sx={{ mb: 2 }} />
              <Typography variant="h6" fontWeight="bold" gutterBottom>
                Patvirtinimo Dokumentacija
              </Typography>
              <Grid container spacing={2}>
                {transaction.signatureData && (
                  <Grid item xs={12} md={6}>
                    <Paper variant="outlined" sx={{ p: 2 }}>
                      <Typography variant="subtitle1" fontWeight="medium" gutterBottom>
                        Parašas
                      </Typography>
                      <Box
                        component="img"
                        src={transaction.signatureData}
                        alt="Parašas"
                        sx={{
                          width: '100%',
                          maxWidth: 500,
                          border: '2px solid',
                          borderColor: 'divider',
                          borderRadius: 1,
                          bgcolor: 'white',
                        }}
                      />
                      {transaction.confirmedBy && (
                        <Typography variant="caption" color="text.secondary" display="block" mt={1}>
                          Pasirašė: {transaction.confirmedBy}
                        </Typography>
                      )}
                    </Paper>
                  </Grid>
                )}

                {transaction.photoData && (
                  <Grid item xs={12} md={6}>
                    <Paper variant="outlined" sx={{ p: 2 }}>
                      <Typography variant="subtitle1" fontWeight="medium" gutterBottom>
                        Patvirtinusio Asmens Nuotrauka
                      </Typography>
                      <Box
                        component="img"
                        src={transaction.photoData}
                        alt="Nuotrauka"
                        sx={{
                          width: '100%',
                          maxWidth: 400,
                          border: '2px solid',
                          borderColor: 'divider',
                          borderRadius: 1,
                        }}
                      />
                      {transaction.confirmedBy && (
                        <Typography variant="caption" color="text.secondary" display="block" mt={1}>
                          Asmuo: {transaction.confirmedBy}
                        </Typography>
                      )}
                    </Paper>
                  </Grid>
                )}
              </Grid>
            </Grid>
          )}

          {/* Notes */}
          {transaction.notes && (
            <Grid item xs={12}>
              <Paper variant="outlined" sx={{ p: 2, bgcolor: 'background.default' }}>
                <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                  Pastabos
                </Typography>
                <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                  {transaction.notes}
                </Typography>
              </Paper>
            </Grid>
          )}

          {/* Footer Info */}
          <Grid item xs={12}>
            <Divider />
            <Box mt={2} display="flex" justifyContent="space-between">
              <Typography variant="caption" color="text.secondary">
                Sukurta: {new Date(transaction.createdAt).toLocaleString('lt-LT')}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Atnaujinta: {new Date(transaction.updatedAt).toLocaleString('lt-LT')}
              </Typography>
            </Box>
          </Grid>
        </Grid>
      </DialogContent>

      <DialogActions>
        <Button startIcon={<PrintIcon />} onClick={handlePrint}>
          Spausdinti
        </Button>
        <Button variant="contained" startIcon={<CloseIcon />} onClick={onClose}>
          Uždaryti
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default TransactionDetailDialog;
