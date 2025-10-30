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
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import SaveIcon from '@mui/icons-material/Save';
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

interface OrderLine {
  tempId: string;
  product: Product;
  quantity: number;
  notes?: string;
}

interface QuickOrderDialogProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (customerId: string, lines: Array<{ productCode: string; quantity: number }>) => void;
}

/**
 * QuickOrderDialog - ULTRA FAST order entry window
 *
 * Features:
 * - Customer selection with autocomplete
 * - Sequential product entry (type code, enter quantity, press Enter to add next)
 * - Live order total calculation
 * - Product list management (add, edit quantity, delete)
 * - Clear hints and tooltips for better UX
 * - Keyboard shortcuts for speed (Ctrl+Enter to save, Esc to close)
 * - Visual feedback and validation
 * - Modern Material-UI design
 */
const QuickOrderDialog: React.FC<QuickOrderDialogProps> = ({
  open,
  onClose,
  onSubmit,
}) => {
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [quantity, setQuantity] = useState<string>('1');
  const [orderLines, setOrderLines] = useState<OrderLine[]>([]);
  const [notes, setNotes] = useState<string>('');

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
      setSelectedProduct(null);
      setQuantity('1');
      setOrderLines([]);
      setNotes('');
    }
  }, [open]);

  const handleAddLine = () => {
    if (!selectedProduct) {
      return;
    }

    const qty = parseFloat(quantity);
    if (isNaN(qty) || qty <= 0) {
      alert('Įveskite teisingą kiekį');
      return;
    }

    const newLine: OrderLine = {
      tempId: Date.now().toString(),
      product: selectedProduct,
      quantity: qty,
    };

    setOrderLines([...orderLines, newLine]);
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
    setOrderLines(orderLines.filter(line => line.tempId !== tempId));
  };

  const handleQuantityChange = (tempId: string, newQuantity: number) => {
    setOrderLines(orderLines.map(line =>
      line.tempId === tempId ? { ...line, quantity: newQuantity } : line
    ));
  };

  const handleSubmit = () => {
    if (!customer) {
      alert('Pasirinkite klientą');
      return;
    }

    if (orderLines.length === 0) {
      alert('Pridėkite bent vieną prekę');
      return;
    }

    const lines = orderLines.map(line => ({
      productCode: line.product.code,
      quantity: line.quantity,
    }));

    onSubmit(customer.id, lines);
    onClose();
  };

  const handleKeyDown = (event: React.KeyboardEvent) => {
    // Ctrl+Enter or Cmd+Enter to save
    if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
      event.preventDefault();
      handleSubmit();
    }
    // Esc to close
    if (event.key === 'Escape') {
      onClose();
    }
  };

  const calculateTotal = (): number => {
    return orderLines.reduce((sum, line) => {
      return sum + (line.product.basePrice * line.quantity);
    }, 0);
  };

  const calculateTotalWithTax = (): number => {
    return calculateTotal() * 1.21; // Assuming 21% VAT
  };

  const getCustomerDisplay = (customer: Customer): string => {
    if (customer.customerType === 'BUSINESS' || customer.customerType === 'CONTRACTOR') {
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

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="lg"
      fullWidth
      onKeyDown={handleKeyDown}
    >
      <DialogTitle>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Typography variant="h5">⚡ Greitas užsakymo įvedimas</Typography>
          <Box>
            <Chip
              label={`${orderLines.length} prekių`}
              color="primary"
              sx={{ mr: 1 }}
            />
            <Chip
              label={`€${calculateTotalWithTax().toFixed(2)}`}
              color="success"
              variant="outlined"
            />
          </Box>
        </Box>
      </DialogTitle>

      <DialogContent dividers>
        <Grid container spacing={3}>
          {/* Customer Selection */}
          <Grid item xs={12}>
            <Alert severity="info" sx={{ mb: 2 }}>
              💡 <strong>Patarimas:</strong> Naudokite klaviatūrą greitam įvedimui -
              įveskite prekės kodą, paspauskite Enter, įveskite kiekį, vėl Enter pridėti kitą prekę.
              Ctrl+Enter išsaugoti užsakymą.
            </Alert>
            <CustomerAutocomplete
              value={customer}
              onChange={setCustomer}
              label="Pasirinkite klientą *"
              required
              autoFocus
            />
            {customer && (
              <Box sx={{ mt: 1, p: 2, bgcolor: 'background.default', borderRadius: 1 }}>
                <Typography variant="body2" color="text.secondary">
                  <strong>{customer.code}</strong> - {getCustomerDisplay(customer)}
                  {customer.city && ` | 📍 ${customer.city}`}
                  {customer.creditLimit > 0 && ` | 💳 Kreditas: €${customer.creditLimit.toFixed(0)}`}
                  {customer.currentBalance > 0 && (
                    <span style={{ color: '#ff9800' }}>
                      {' '}| ⚠️ Balansas: €{customer.currentBalance.toFixed(2)}
                    </span>
                  )}
                </Typography>
              </Box>
            )}
          </Grid>

          <Grid item xs={12}>
            <Divider sx={{ my: 1 }} />
            <Typography variant="h6" gutterBottom>
              Prekės
            </Typography>
          </Grid>

          {/* Product Entry */}
          <Grid item xs={12} md={6}>
            <ProductCodeInput
              value={selectedProduct}
              onChange={setSelectedProduct}
              label="Prekės kodas arba pavadinimas *"
              required
              onEnterPress={() => {
                if (quantityInputRef.current) {
                  quantityInputRef.current.focus();
                }
              }}
            />
            {selectedProduct && (
              <Box sx={{ mt: 1, p: 2, bgcolor: 'success.light', borderRadius: 1 }}>
                <Typography variant="body2">
                  ✓ <strong>{selectedProduct.code}</strong> - {selectedProduct.name}
                  <br />
                  Kaina: €{selectedProduct.basePrice.toFixed(2)} / {selectedProduct.unitOfMeasure}
                </Typography>
              </Box>
            )}
          </Grid>

          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              label="Kiekis *"
              type="number"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              inputRef={quantityInputRef}
              required
              inputProps={{
                min: 0.001,
                step: selectedProduct?.isCable ? 0.1 : 1,
              }}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  e.preventDefault();
                  handleAddLine();
                }
              }}
              helperText={
                selectedProduct?.isCable
                  ? 'Kabeliams galite įvesti dešimtaines'
                  : 'Paspauskite Enter pridėti'
              }
            />
          </Grid>

          <Grid item xs={12} md={2}>
            <Button
              fullWidth
              variant="contained"
              color="primary"
              onClick={handleAddLine}
              disabled={!selectedProduct || !quantity}
              startIcon={<AddIcon />}
              sx={{ height: '56px' }}
            >
              Pridėti
            </Button>
          </Grid>

          {/* Order Lines Table */}
          {orderLines.length > 0 && (
            <Grid item xs={12}>
              <TableContainer component={Paper} variant="outlined">
                <Table size="small">
                  <TableHead>
                    <TableRow sx={{ bgcolor: 'primary.light' }}>
                      <TableCell><strong>Kodas</strong></TableCell>
                      <TableCell><strong>Pavadinimas</strong></TableCell>
                      <TableCell align="right"><strong>Kaina</strong></TableCell>
                      <TableCell align="right"><strong>Kiekis</strong></TableCell>
                      <TableCell align="right"><strong>Vnt.</strong></TableCell>
                      <TableCell align="right"><strong>Suma</strong></TableCell>
                      <TableCell align="center"><strong>Veiksmai</strong></TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {orderLines.map((line) => (
                      <TableRow key={line.tempId} hover>
                        <TableCell>
                          <Typography variant="body2" sx={{ fontWeight: 600 }}>
                            {line.product.code}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2">{line.product.name}</Typography>
                        </TableCell>
                        <TableCell align="right">
                          <Typography variant="body2">
                            €{line.product.basePrice.toFixed(2)}
                          </Typography>
                        </TableCell>
                        <TableCell align="right">
                          <TextField
                            type="number"
                            value={line.quantity}
                            onChange={(e) =>
                              handleQuantityChange(line.tempId, parseFloat(e.target.value))
                            }
                            size="small"
                            sx={{ width: '80px' }}
                            inputProps={{
                              min: 0.001,
                              step: line.product.isCable ? 0.1 : 1,
                            }}
                          />
                        </TableCell>
                        <TableCell align="right">
                          <Typography variant="body2">
                            {line.product.unitOfMeasure}
                          </Typography>
                        </TableCell>
                        <TableCell align="right">
                          <Typography variant="body2" sx={{ fontWeight: 600 }}>
                            €{(line.product.basePrice * line.quantity).toFixed(2)}
                          </Typography>
                        </TableCell>
                        <TableCell align="center">
                          <Tooltip title="Ištrinti">
                            <IconButton
                              size="small"
                              color="error"
                              onClick={() => handleDeleteLine(line.tempId)}
                            >
                              <DeleteIcon />
                            </IconButton>
                          </Tooltip>
                        </TableCell>
                      </TableRow>
                    ))}
                    <TableRow sx={{ bgcolor: 'background.default' }}>
                      <TableCell colSpan={5} align="right">
                        <Typography variant="body1" sx={{ fontWeight: 600 }}>
                          Suma be PVM:
                        </Typography>
                      </TableCell>
                      <TableCell align="right" colSpan={2}>
                        <Typography variant="body1" sx={{ fontWeight: 600 }}>
                          €{calculateTotal().toFixed(2)}
                        </Typography>
                      </TableCell>
                    </TableRow>
                    <TableRow sx={{ bgcolor: 'success.light' }}>
                      <TableCell colSpan={5} align="right">
                        <Typography variant="h6" sx={{ fontWeight: 700 }}>
                          Viso su PVM (21%):
                        </Typography>
                      </TableCell>
                      <TableCell align="right" colSpan={2}>
                        <Typography variant="h6" sx={{ fontWeight: 700, color: 'success.dark' }}>
                          €{calculateTotalWithTax().toFixed(2)}
                        </Typography>
                      </TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </TableContainer>
            </Grid>
          )}

          {/* Notes */}
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Pastabos (neprivaloma)"
              multiline
              rows={2}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Papildomos užsakymo pastabos..."
            />
          </Grid>
        </Grid>
      </DialogContent>

      <DialogActions sx={{ px: 3, py: 2 }}>
        <Button onClick={onClose} size="large">
          Atšaukti (Esc)
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          color="primary"
          size="large"
          startIcon={<SaveIcon />}
          disabled={!customer || orderLines.length === 0}
        >
          Sukurti užsakymą (Ctrl+Enter)
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default QuickOrderDialog;
