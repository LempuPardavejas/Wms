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
import UndoIcon from '@mui/icons-material/Undo';
import WarningIcon from '@mui/icons-material/Warning';
import CustomerAutocomplete from './CustomerAutocomplete';
import ProductCodeInput from './ProductCodeInput';

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

interface Product {
  id: string;
  code: string;
  sku: string;
  name: string;
  unitOfMeasure: string;
  basePrice: number;
  isCable: boolean;
  isModular: boolean;
}

interface ReturnLine {
  tempId: string;
  product: Product;
  quantity: number;
  notes?: string;
  reason?: string;
}

interface QuickCreditReturnDialogProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (
    customerId: string,
    lines: Array<{ productCode: string; quantity: number; notes?: string }>,
    performedBy: string,
    performedByRole: string,
    notes?: string
  ) => void;
  currentUser?: string;
  preSelectedCustomer?: Customer;
}

/**
 * QuickCreditReturnDialog - ULTRA FAST credit return entry
 *
 * Optimized for customers to quickly return items:
 * - Pre-select customer if coming from customer portal
 * - Fast product entry by code
 * - Optional return reasons
 * - Automatic balance calculation
 * - Visual feedback
 */
const QuickCreditReturnDialog: React.FC<QuickCreditReturnDialogProps> = ({
  open,
  onClose,
  onSubmit,
  currentUser = 'Klientas',
  preSelectedCustomer,
}) => {
  const [customer, setCustomer] = useState<Customer | null>(preSelectedCustomer || null);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [quantity, setQuantity] = useState<string>('1');
  const [returnReason, setReturnReason] = useState<string>('');
  const [lines, setLines] = useState<ReturnLine[]>([]);
  const [notes, setNotes] = useState<string>('');
  const [performedBy, setPerformedBy] = useState<string>(currentUser);
  const [performedByRole, setPerformedByRole] = useState<string>(
    preSelectedCustomer ? 'CUSTOMER' : 'EMPLOYEE'
  );

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
      setCustomer(preSelectedCustomer || null);
      setSelectedProduct(null);
      setQuantity('1');
      setReturnReason('');
      setLines([]);
      setNotes('');
      setPerformedBy(currentUser);
      setPerformedByRole(preSelectedCustomer ? 'CUSTOMER' : 'EMPLOYEE');
    }
  }, [open, currentUser, preSelectedCustomer]);

  const handleAddLine = () => {
    if (!selectedProduct) {
      return;
    }

    const qty = parseFloat(quantity);
    if (isNaN(qty) || qty <= 0) {
      alert('Įveskite teisingą kiekį');
      return;
    }

    const newLine: ReturnLine = {
      tempId: Date.now().toString(),
      product: selectedProduct,
      quantity: qty,
      reason: returnReason || undefined,
    };

    setLines([...lines, newLine]);
    setSelectedProduct(null);
    setQuantity('1');
    setReturnReason('');

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
      alert('Pridėkite prekių į grąžinimo sąrašą');
      return;
    }

    const lineRequests = lines.map(line => ({
      productCode: line.product.code,
      quantity: line.quantity,
      notes: line.reason || line.notes,
    }));

    onSubmit(customer.id, lineRequests, performedBy, performedByRole, notes);
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
    return customer.currentBalance - calculateTotal();
  };

  const returnReasons = [
    'Klaidingas užsakymas',
    'Prekė su defektu',
    'Netinka',
    'Perteklius',
    'Kita',
  ];

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
            <UndoIcon color="secondary" />
            <Typography variant="h6">
              Greitas grąžinimas
            </Typography>
          </Box>
          <Chip
            label={`Eilučių: ${lines.length} | Suma: €${calculateTotal().toFixed(2)}`}
            color="secondary"
            variant="outlined"
          />
        </Box>
      </DialogTitle>

      <DialogContent dividers>
        <Grid container spacing={3}>
          {/* Customer Selection */}
          {!preSelectedCustomer && (
            <Grid item xs={12}>
              <CustomerAutocomplete
                value={customer}
                onChange={setCustomer}
                label="Klientas *"
                autoFocus
              />
            </Grid>
          )}

          {/* Customer Info */}
          {customer && (
            <Grid item xs={12}>
              <Paper sx={{ p: 2, bgcolor: 'background.default' }}>
                <Grid container spacing={2}>
                  <Grid item xs={12} sm={4}>
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
                  <Grid item xs={12} sm={4}>
                    <Typography variant="caption" color="text.secondary">
                      Dabartinė skola
                    </Typography>
                    <Typography variant="h6">
                      €{customer.currentBalance.toFixed(2)}
                    </Typography>
                  </Grid>
                  <Grid item xs={12} sm={4}>
                    <Typography variant="caption" color="text.secondary">
                      Nauja skola (po grąžinimo)
                    </Typography>
                    <Typography variant="h6" color="success.main" fontWeight="bold">
                      €{getNewBalance().toFixed(2)}
                    </Typography>
                  </Grid>
                </Grid>
                <Alert severity="info" sx={{ mt: 2 }}>
                  <Box display="flex" alignItems="center" gap={1}>
                    <WarningIcon fontSize="small" />
                    <Typography variant="body2">
                      Grąžinimas sumažins kliento skolą
                    </Typography>
                  </Box>
                </Alert>
              </Paper>
            </Grid>
          )}

          <Grid item xs={12}>
            <Divider />
          </Grid>

          {/* Product Selection */}
          <Grid item xs={12} sm={6}>
            <ProductCodeInput
              ref={productInputRef}
              value={selectedProduct}
              onChange={setSelectedProduct}
              label="Prekės kodas"
              disabled={!customer}
              autoFocus={!!preSelectedCustomer}
            />
          </Grid>

          <Grid item xs={12} sm={2}>
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

          <Grid item xs={12} sm={3}>
            <FormControl fullWidth disabled={!selectedProduct}>
              <InputLabel>Priežastis</InputLabel>
              <Select
                value={returnReason}
                label="Priežastis"
                onChange={(e) => setReturnReason(e.target.value)}
              >
                <MenuItem value="">
                  <em>Nepasirinkta</em>
                </MenuItem>
                {returnReasons.map(reason => (
                  <MenuItem key={reason} value={reason}>{reason}</MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>

          <Grid item xs={12} sm={1}>
            <Tooltip title="Pridėti (Enter)">
              <span>
                <IconButton
                  color="secondary"
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
                    <TableCell>Priežastis</TableCell>
                    <TableCell align="right">Kaina</TableCell>
                    <TableCell align="right">Kiekis</TableCell>
                    <TableCell align="right">Suma</TableCell>
                    <TableCell align="center" width={60}></TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {lines.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={7} align="center">
                        <Typography variant="body2" color="text.secondary" sx={{ py: 4 }}>
                          Nėra grąžinamų prekių. Pradėkite įvesti prekės kodą.
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
                        <TableCell>
                          {line.reason ? (
                            <Chip label={line.reason} size="small" variant="outlined" />
                          ) : (
                            <Typography variant="caption" color="text.secondary">-</Typography>
                          )}
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
                      <TableCell colSpan={5} align="right">
                        <Typography variant="h6">Viso:</Typography>
                      </TableCell>
                      <TableCell align="right">
                        <Typography variant="h6" color="secondary">
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
              placeholder="Papildomos pastabos apie grąžinimą..."
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
              color="secondary"
              startIcon={<SaveIcon />}
              onClick={handleSubmit}
              disabled={!customer || lines.length === 0}
            >
              Grąžinti
            </Button>
          </Box>
        </Box>
      </DialogActions>
    </Dialog>
  );
};

export default QuickCreditReturnDialog;
