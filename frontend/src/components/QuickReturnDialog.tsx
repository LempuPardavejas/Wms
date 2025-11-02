import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Checkbox,
  TextField,
  Alert,
  Chip,
  CircularProgress,
} from '@mui/material';
import UndoIcon from '@mui/icons-material/Undo';
import SaveIcon from '@mui/icons-material/Save';
import { getCreditTransactionById } from '../services/creditTransactionService';

interface PickupTransaction {
  id: string;
  transactionNumber: string;
  customerName: string;
  lines: Array<{
    id: string;
    productCode: string;
    productName: string;
    quantity: number;
    unitPrice: number;
  }>;
  totalAmount: number;
  createdAt: string;
}

interface QuickReturnDialogProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (
    customerCode: string,
    selectedLines: Array<{ productCode: string; quantity: number }>,
    originalTransactionNumber: string
  ) => void;
  transactionId: string | null;
}

/**
 * QuickReturnDialog - Ultra-fast return from pickup transaction
 *
 * Features:
 * - Shows all items from original pickup
 * - Select items to return (checkbox)
 * - Adjust return quantities
 * - One-click partial or full return
 * - Visual diff (picked vs returning)
 */
const QuickReturnDialog: React.FC<QuickReturnDialogProps> = ({
  open,
  onClose,
  onSubmit,
  transactionId,
}) => {
  const [transaction, setTransaction] = useState<PickupTransaction | null>(null);
  const [loading, setLoading] = useState(false);
  const [selectedLines, setSelectedLines] = useState<{ [key: string]: boolean }>({});
  const [quantities, setQuantities] = useState<{ [key: string]: number }>({});

  useEffect(() => {
    if (transactionId && open) {
      loadTransaction();
    }
  }, [transactionId, open]);

  const loadTransaction = async () => {
    if (!transactionId) return;

    setLoading(true);
    try {
      const data = await getCreditTransactionById(transactionId);
      const pickupTx: PickupTransaction = {
        id: data.id,
        transactionNumber: data.transactionNumber,
        customerName: data.customerName,
        lines: data.lines,
        totalAmount: data.totalAmount,
        createdAt: data.createdAt,
      };
      setTransaction(pickupTx);

      // Initialize: select all lines, full quantities
      const selected: { [key: string]: boolean } = {};
      const qtys: { [key: string]: number } = {};

      pickupTx.lines.forEach(line => {
        selected[line.id] = true;
        qtys[line.id] = line.quantity;
      });

      setSelectedLines(selected);
      setQuantities(qtys);
    } catch (error) {
      console.error('Failed to load transaction:', error);
      alert('Nepavyko įkelti transakcijos');
    } finally {
      setLoading(false);
    }
  };

  const handleToggleLine = (lineId: string) => {
    setSelectedLines(prev => ({
      ...prev,
      [lineId]: !prev[lineId],
    }));
  };

  const handleQuantityChange = (lineId: string, newQty: number) => {
    setQuantities(prev => ({
      ...prev,
      [lineId]: newQty,
    }));
  };

  const handleSubmit = () => {
    if (!transaction) return;

    const returnLines = transaction.lines
      .filter(line => selectedLines[line.id])
      .map(line => ({
        productCode: line.productCode,
        quantity: quantities[line.id] || line.quantity,
      }));

    if (returnLines.length === 0) {
      alert('Pasirinkite bent vieną prekę grąžinimui');
      return;
    }

    // Extract customer code from customerName (if format is "Name (CODE)")
    // Or just use transaction number as reference
    const customerCode = transaction.customerName; // TODO: proper customer code extraction

    onSubmit(customerCode, returnLines, transaction.transactionNumber);
    onClose();
  };

  const getReturnTotal = (): number => {
    if (!transaction) return 0;

    return transaction.lines
      .filter(line => selectedLines[line.id])
      .reduce((sum, line) => {
        const qty = quantities[line.id] || line.quantity;
        return sum + (line.unitPrice * qty);
      }, 0);
  };

  const getSelectedCount = (): number => {
    return Object.values(selectedLines).filter(Boolean).length;
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box display="flex" alignItems="center" gap={1}>
          <UndoIcon color="secondary" />
          <Box>
            <Typography variant="h6">
              Greitas Grąžinimas
            </Typography>
            {transaction && (
              <Typography variant="caption" color="text.secondary">
                Iš paėmimo: {transaction.transactionNumber}
              </Typography>
            )}
          </Box>
        </Box>
      </DialogTitle>

      <DialogContent dividers>
        {loading && (
          <Box display="flex" justifyContent="center" alignItems="center" py={4}>
            <CircularProgress />
          </Box>
        )}

        {!loading && !transaction && (
          <Alert severity="info">Pasirinkite operaciją grąžinimui</Alert>
        )}

        {!loading && transaction && (
          <>
        <Box mb={2}>
          <Paper sx={{ p: 2, bgcolor: 'background.default' }}>
            <Typography variant="body2" color="text.secondary">
              Klientas
            </Typography>
            <Typography variant="h6" gutterBottom>
              {transaction.customerName}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              Paėmimo data: {new Date(transaction.createdAt).toLocaleString('lt-LT')}
            </Typography>
          </Paper>
        </Box>

        <Alert severity="info" sx={{ mb: 2 }}>
          Pasirinkite prekės, kurias klientas grąžina. Galite koreguoti kiekius.
        </Alert>

        <TableContainer component={Paper} variant="outlined">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell padding="checkbox"></TableCell>
                <TableCell>Kodas</TableCell>
                <TableCell>Pavadinimas</TableCell>
                <TableCell align="right">Paėmta</TableCell>
                <TableCell align="right">Grąžinama</TableCell>
                <TableCell align="right">Kaina</TableCell>
                <TableCell align="right">Suma</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {transaction.lines.map((line) => {
                const isSelected = selectedLines[line.id];
                const returnQty = quantities[line.id] || line.quantity;

                return (
                  <TableRow
                    key={line.id}
                    hover
                    selected={isSelected}
                    sx={{
                      bgcolor: isSelected ? 'action.selected' : 'inherit',
                    }}
                  >
                    <TableCell padding="checkbox">
                      <Checkbox
                        checked={isSelected}
                        onChange={() => handleToggleLine(line.id)}
                        color="secondary"
                      />
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" fontWeight="bold">
                        {line.productCode}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        {line.productName}
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Chip
                        label={`${line.quantity}`}
                        size="small"
                        color="primary"
                        variant="outlined"
                      />
                    </TableCell>
                    <TableCell align="right">
                      <TextField
                        type="number"
                        value={returnQty}
                        onChange={(e) => handleQuantityChange(line.id, parseFloat(e.target.value))}
                        disabled={!isSelected}
                        size="small"
                        inputProps={{
                          min: 0.001,
                          max: line.quantity,
                          step: 0.001,
                          style: { textAlign: 'right', width: 80 },
                        }}
                      />
                    </TableCell>
                    <TableCell align="right">
                      €{line.unitPrice.toFixed(2)}
                    </TableCell>
                    <TableCell align="right">
                      <Typography
                        variant="body2"
                        fontWeight="bold"
                        color={isSelected ? 'secondary' : 'text.secondary'}
                      >
                        €{isSelected ? (line.unitPrice * returnQty).toFixed(2) : '-'}
                      </Typography>
                    </TableCell>
                  </TableRow>
                );
              })}
              <TableRow>
                <TableCell colSpan={6} align="right">
                  <Typography variant="h6">
                    Grąžinama ({getSelectedCount()} prekės):
                  </Typography>
                </TableCell>
                <TableCell align="right">
                  <Typography variant="h6" color="secondary">
                    €{getReturnTotal().toFixed(2)}
                  </Typography>
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </TableContainer>

        <Box mt={2}>
          <Alert severity="success">
            Grąžinimas sumažins kliento skolą €{getReturnTotal().toFixed(2)}
          </Alert>
        </Box>
        </>
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Atšaukti</Button>
        <Button
          variant="contained"
          color="secondary"
          startIcon={<SaveIcon />}
          onClick={handleSubmit}
          disabled={getSelectedCount() === 0}
        >
          Grąžinti ({getSelectedCount()})
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default QuickReturnDialog;
