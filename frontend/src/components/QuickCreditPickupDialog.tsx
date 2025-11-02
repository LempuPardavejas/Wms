import React, { useState, useRef, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Grid,
  TextField,
  IconButton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Typography,
  Box,
  Chip,
  Alert,
  Tooltip,
  Divider,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import SaveIcon from '@mui/icons-material/Save';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import UndoIcon from '@mui/icons-material/Undo';
import CustomerAutocomplete from './CustomerAutocomplete';
import ProductCodeInput from './ProductCodeInput';

interface Customer {
  id: string;
  code: string;
  customerType: string;
  companyName?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  city?: string;
  creditLimit: number;
  currentBalance: number;
}

interface Product {
  id: string;
  code: string;
  sku: string;
  name: string;
  unitOfMeasure: string;
  basePrice: number;
  isCable: boolean;
  isModular: boolean;
  imageUrl?: string;
}

interface CreditLine {
  tempId: string;
  product: Product;
  quantity: number;
  notes?: string;
}

interface QuickCreditPickupDialogProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (
    customerId: string,
    lines: Array<{ productCode: string; quantity: number; notes?: string }>,
    transactionType: 'PICKUP' | 'RETURN',
    performedBy: string,
    performedByRole: string,
    notes?: string
  ) => void;
  currentUser?: string; // Current logged in user name
}

/**
 * QuickCreditPickupDialog - ULTRA FAST credit pickup/return entry
 *
 * Features:
 * - Customer selection with autocomplete
 * - Transaction type selection (Pickup or Return)
 * - Sequential product entry (type code, enter quantity, press Enter)
 * - Live total calculation
 * - Shows customer credit info (limit, current balance)
 * - Keyboard shortcuts (Ctrl+Enter to save, Esc to close)
 * - Visual feedback and validation
 * - Modern Material-UI design
 */
const QuickCreditPickupDialog: React.FC<QuickCreditPickupDialogProps> = ({
  open,
  onClose,
  onSubmit,
  currentUser = 'Darbuotojas',
}) => {
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [transactionType, setTransactionType] = useState<'PICKUP' | 'RETURN'>('PICKUP');
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [quantity, setQuantity] = useState<string>('1');
  const [lines, setLines] = useState<CreditLine[]>([]);
  const [notes, setNotes] = useState<string>('');
  const [performedBy, setPerformedBy] = useState<string>(currentUser);
  const [performedByRole, setPerformedByRole] = useState<string>('EMPLOYEE');

  const quantityInputRef = useRef<HTMLInputElement>(null);
  const productInputRef = useRef<any>(null);

  // Auto-focus quantity when product selected
  useEffect(() => {
    if (selectedProduct && quantityInputRef.current) {
      quantityInputRef.current.focus();
      quantityInputRef.current.select();
    }
  }, [selectedProduct]);

  // Reset form when dialog opens/closes
  useEffect(() => {
    if (open) {
      setCustomer(null);
      setTransactionType('PICKUP');
      setSelectedProduct(null);
      setQuantity('1');
      setLines([]);
      setNotes('');
      setPerformedBy(currentUser);
      setPerformedByRole('EMPLOYEE');
    }
  }, [open, currentUser]);

  const handleAddLine = () => {
    if (!selectedProduct) {
      return;
    }

    const qty = parseFloat(quantity);
    if (isNaN(qty) || qty <= 0) {
      alert('Įveskite teisingą kiekį');
      return;
    }

    const newLine: CreditLine = {
      tempId: Date.now().toString(),
      product: selectedProduct,
      quantity: qty,
    };

    setLines([...lines, newLine]);
    setSelectedProduct(null);
    setQuantity('1');

    // Focus back on product input for next entry
    setTimeout(() => {
      if (productInputRef.current) {
        productInputRef.current.focus();
      }
    }, 100);
  };

  const handleDeleteLine = (tempId: string) => {
    setLines(lines.filter(line => line.tempId !== tempId));
  };

  const handleQuantityChange = (tempId: string, newQuantity: number) => {
    setLines(lines.map(line =>
      line.tempId === tempId ? { ...line, quantity: newQuantity } : line
    ));
  };

  const calculateTotal = (): number => {
    return lines.reduce((sum, line) => sum + (line.product.basePrice * line.quantity), 0);
  };

  const handleSubmit = () => {
    if (!customer) {
      alert('Pasirinkite klientą');
      return;
    }

    if (lines.length === 0) {
      alert('Pridėkite prekių į sąrašą');
      return;
    }

    const lineRequests = lines.map(line => ({
      productCode: line.product.code,
      quantity: line.quantity,
      notes: line.notes,
    }));

    onSubmit(customer.id, lineRequests, transactionType, performedBy, performedByRole, notes);
    onClose();
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && selectedProduct) {
      handleAddLine();
    }
  };

  const handleDialogKeyDown = (e: React.KeyboardEvent) => {
    if (e.ctrlKey && e.key === 'Enter') {
      handleSubmit();
    }
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

  const getNewBalance = (): number => {
    if (!customer) return 0;
    const total = calculateTotal();
    if (transactionType === 'PICKUP') {
      return customer.currentBalance + total;
    } else {
      return customer.currentBalance - total;
    }
  };

  const isOverCreditLimit = (): boolean => {
    if (!customer) return false;
    return getNewBalance() > customer.creditLimit;
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="lg"
      fullWidth
      onKeyDown={handleDialogKeyDown}
    >
      <DialogTitle>
        <Box display="flex" alignItems="center" justifyContent="space-between">
          <Box display="flex" alignItems="center" gap={1}>
            {transactionType === 'PICKUP' ? (
              <ShoppingCartIcon color="primary" />
            ) : (
              <UndoIcon color="secondary" />
            )}
            <Typography variant="h6">
              Greitas {transactionType === 'PICKUP' ? 'Paėmimas' : 'Grąžinimas'}
            </Typography>
          </Box>
          <Chip
            label={`Eilučių: ${lines.length} | Suma: €${calculateTotal().toFixed(2)}`}
            color="primary"
            variant="outlined"
          />
        </Box>
      </DialogTitle>

      <DialogContent dividers>
        <Grid container spacing={3}>
          {/* Customer Selection */}
          <Grid item xs={12}>
            <CustomerAutocomplete
              value={customer}
              onChange={setCustomer}
              label="Klientas *"
              autoFocus
            />
          </Grid>

          {/* Customer Credit Info */}
          {customer && (
            <Grid item xs={12}>
              <Paper sx={{ p: 2, bgcolor: 'background.default' }}>
                <Grid container spacing={2}>
                  <Grid item xs={12} sm={3}>
                    <Typography variant="caption" color="text.secondary">
                      Klientas
                    </Typography>
                    <Typography variant="body1" fontWeight="bold">
                      {getCustomerDisplayName(customer)}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {customer.code}
                    </Typography>
                  </Grid>
                  <Grid item xs={12} sm={3}>
                    <Typography variant="caption" color="text.secondary">
                      Dabartinė skola
                    </Typography>
                    <Typography
                      variant="h6"
                      color={customer.currentBalance > customer.creditLimit ? 'error' : 'inherit'}
                    >
                      €{customer.currentBalance.toFixed(2)}
                    </Typography>
                  </Grid>
                  <Grid item xs={12} sm={3}>
                    <Typography variant="caption" color="text.secondary">
                      Kredito limitas
                    </Typography>
                    <Typography variant="h6">
                      €{customer.creditLimit.toFixed(2)}
                    </Typography>
                  </Grid>
                  <Grid item xs={12} sm={3}>
                    <Typography variant="caption" color="text.secondary">
                      Nauja skola (po operacijos)
                    </Typography>
                    <Typography
                      variant="h6"
                      color={isOverCreditLimit() ? 'error' : 'success.main'}
                      fontWeight="bold"
                    >
                      €{getNewBalance().toFixed(2)}
                    </Typography>
                  </Grid>
                </Grid>
                {isOverCreditLimit() && (
                  <Alert severity="warning" sx={{ mt: 2 }}>
                    Dėmesio! Operacija viršys kredito limitą!
                  </Alert>
                )}
              </Paper>
            </Grid>
          )}

          {/* Transaction Type */}
          <Grid item xs={12} sm={6}>
            <FormControl fullWidth>
              <InputLabel>Operacijos tipas</InputLabel>
              <Select
                value={transactionType}
                label="Operacijos tipas"
                onChange={(e) => setTransactionType(e.target.value as 'PICKUP' | 'RETURN')}
              >
                <MenuItem value="PICKUP">Paėmimas (į skolą)</MenuItem>
                <MenuItem value="RETURN">Grąžinimas</MenuItem>
              </Select>
            </FormControl>
          </Grid>

          {/* Performed By */}
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Atliko"
              value={performedBy}
              onChange={(e) => setPerformedBy(e.target.value)}
            />
          </Grid>

          <Grid item xs={12}>
            <Divider />
          </Grid>

          {/* Product Selection */}
          <Grid item xs={12} sm={8}>
            <ProductCodeInput
              value={selectedProduct}
              onChange={setSelectedProduct}
              label="Prekės kodas"
              disabled={!customer}
            />
          </Grid>

          <Grid item xs={12} sm={3}>
            <TextField
              fullWidth
              label="Kiekis"
              type="number"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              onKeyPress={handleKeyPress}
              inputRef={quantityInputRef}
              disabled={!selectedProduct}
              inputProps={{ min: 0.001, step: 0.001 }}
            />
          </Grid>

          <Grid item xs={12} sm={1}>
            <Tooltip title="Pridėti (Enter)">
              <span>
                <IconButton
                  color="primary"
                  onClick={handleAddLine}
                  disabled={!selectedProduct}
                  size="large"
                >
                  <AddIcon />
                </IconButton>
              </span>
            </Tooltip>
          </Grid>

          {/* Product List */}
          <Grid item xs={12}>
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Kodas</TableCell>
                    <TableCell>Pavadinimas</TableCell>
                    <TableCell align="right">Kaina</TableCell>
                    <TableCell align="right">Kiekis</TableCell>
                    <TableCell align="right">Suma</TableCell>
                    <TableCell align="center" width={60}></TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {lines.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6} align="center">
                        <Typography variant="body2" color="text.secondary" sx={{ py: 4 }}>
                          Nėra pridėtų prekių. Pradėkite įvesti prekės kodą.
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    lines.map((line) => (
                      <TableRow key={line.tempId}>
                        <TableCell>{line.product.code}</TableCell>
                        <TableCell>
                          <Typography variant="body2">{line.product.name}</Typography>
                        </TableCell>
                        <TableCell align="right">
                          €{line.product.basePrice.toFixed(2)}
                        </TableCell>
                        <TableCell align="right">
                          <TextField
                            type="number"
                            value={line.quantity}
                            onChange={(e) =>
                              handleQuantityChange(line.tempId, parseFloat(e.target.value))
                            }
                            size="small"
                            inputProps={{ min: 0.001, step: 0.001, style: { textAlign: 'right' } }}
                            sx={{ width: 100 }}
                          />
                        </TableCell>
                        <TableCell align="right">
                          <Typography variant="body2" fontWeight="bold">
                            €{(line.product.basePrice * line.quantity).toFixed(2)}
                          </Typography>
                        </TableCell>
                        <TableCell align="center">
                          <IconButton
                            size="small"
                            color="error"
                            onClick={() => handleDeleteLine(line.tempId)}
                          >
                            <DeleteIcon fontSize="small" />
                          </IconButton>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                  {lines.length > 0 && (
                    <TableRow>
                      <TableCell colSpan={4} align="right">
                        <Typography variant="h6">Viso:</Typography>
                      </TableCell>
                      <TableCell align="right">
                        <Typography variant="h6" color="primary">
                          €{calculateTotal().toFixed(2)}
                        </Typography>
                      </TableCell>
                      <TableCell />
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Grid>

          {/* Notes */}
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Pastabos"
              multiline
              rows={2}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Papildomos pastabos apie operaciją..."
            />
          </Grid>
        </Grid>
      </DialogContent>

      <DialogActions>
        <Box display="flex" justifyContent="space-between" width="100%" px={1}>
          <Typography variant="caption" color="text.secondary">
            Klavišai: Ctrl+Enter - išsaugoti | Esc - uždaryti
          </Typography>
          <Box>
            <Button onClick={onClose}>Atšaukti</Button>
            <Button
              variant="contained"
              color="primary"
              startIcon={<SaveIcon />}
              onClick={handleSubmit}
              disabled={!customer || lines.length === 0}
            >
              Išsaugoti
            </Button>
          </Box>
        </Box>
      </DialogActions>
    </Dialog>
  );
};

export default QuickCreditPickupDialog;
