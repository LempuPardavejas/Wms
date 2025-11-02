import React, { useState } from 'react';
import {
  Box,
  Grid,
  Paper,
  Typography,
  Button,
  Card,
  CardContent,
  Chip,
  IconButton,
  Divider,
  List,
  ListItem,
  ListItemText,
  TextField,
} from '@mui/material';
import {
  Add as AddIcon,
  Search as SearchIcon,
  ShoppingCart as CartIcon,
  Receipt as ReceiptIcon,
  Keyboard as KeyboardIcon,
  Delete as DeleteIcon,
  Print as PrintIcon,
  AssessmentOutlined as ReportIcon,
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';

interface CartItem {
  id: string;
  productCode: string;
  productName: string;
  quantity: number;
  price: number;
  subtotal: number;
}

const POSPage: React.FC = () => {
  const { user } = useAuth();
  const [cart, _setCart] = useState<CartItem[]>([]);
  const [_showQuickOrder, _setShowQuickOrder] = useState(false);

  // Calculate totals
  const subtotal = cart.reduce((sum, item) => sum + item.subtotal, 0);
  const tax = subtotal * 0.21; // PVM 21%
  const total = subtotal + tax;

  const handleNewOrder = () => {
    _setShowQuickOrder(true);
  };

  const handleKeyboardShortcut = (e: React.KeyboardEvent) => {
    if (e.key === 'F1') {
      e.preventDefault();
      handleNewOrder();
    }
  };

  return (
    <Box sx={{ height: '100vh', display: 'flex', flexDirection: 'column', bgcolor: '#f5f5f5' }}>
      {/* Top Bar */}
      <Paper
        elevation={2}
        sx={{
          p: 2,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          borderRadius: 0,
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <CartIcon sx={{ fontSize: 32, color: 'primary.main' }} />
          <Box>
            <Typography variant="h5" fontWeight={600}>
              Kasos Sistema (POS)
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Sveiki, {user?.fullName || user?.username}!
            </Typography>
          </Box>
        </Box>

        <Box sx={{ display: 'flex', gap: 1 }}>
          <Chip
            icon={<KeyboardIcon />}
            label="F1 - Naujas"
            size="small"
            color="primary"
            variant="outlined"
          />
          <Chip
            icon={<KeyboardIcon />}
            label="F3 - Paieška"
            size="small"
            color="primary"
            variant="outlined"
          />
          <Chip
            icon={<KeyboardIcon />}
            label="F8 - Ataskaitos"
            size="small"
            color="primary"
            variant="outlined"
          />
        </Box>
      </Paper>

      {/* Main Content */}
      <Grid container sx={{ flex: 1, p: 2, gap: 2 }} onKeyDown={handleKeyboardShortcut}>
        {/* Left Side - Product Search & Selection */}
        <Grid item xs={12} md={7}>
          <Paper sx={{ p: 3, height: '100%', display: 'flex', flexDirection: 'column' }}>
            <Typography variant="h6" gutterBottom fontWeight={600}>
              Produktų paieška
            </Typography>

            <TextField
              fullWidth
              placeholder="Įveskite produkto kodą, pavadinimą arba skanuokite"
              variant="outlined"
              autoFocus
              InputProps={{
                startAdornment: <SearchIcon sx={{ mr: 1, color: 'text.secondary' }} />,
              }}
              sx={{ mb: 3 }}
            />

            {/* Quick Actions */}
            <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
              <Button
                variant="contained"
                size="large"
                startIcon={<AddIcon />}
                onClick={handleNewOrder}
                fullWidth
                sx={{ py: 2, fontSize: '1.1rem' }}
              >
                Naujas užsakymas (F1)
              </Button>
              <Button
                variant="outlined"
                size="large"
                startIcon={<SearchIcon />}
                fullWidth
                sx={{ py: 2, fontSize: '1.1rem' }}
              >
                Klientų paieška (F2)
              </Button>
            </Box>

            {/* Product Grid Placeholder */}
            <Box
              sx={{
                flex: 1,
                bgcolor: '#fafafa',
                borderRadius: 2,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                border: '2px dashed #e0e0e0',
              }}
            >
              <Typography color="text.secondary">
                Įveskite produkto kodą arba naudokite spartųjį užsakymo langą
              </Typography>
            </Box>
          </Paper>
        </Grid>

        {/* Right Side - Shopping Cart */}
        <Grid item xs={12} md={4.8}>
          <Paper sx={{ p: 3, height: '100%', display: 'flex', flexDirection: 'column' }}>
            <Typography variant="h6" gutterBottom fontWeight={600}>
              Krepšelis
            </Typography>

            {cart.length === 0 ? (
              <Box
                sx={{
                  flex: 1,
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'text.secondary',
                }}
              >
                <CartIcon sx={{ fontSize: 64, mb: 2, opacity: 0.3 }} />
                <Typography>Krepšelis tuščias</Typography>
                <Typography variant="caption">
                  Pradėkite naudodami spartųjį užsakymo langą
                </Typography>
              </Box>
            ) : (
              <>
                <List sx={{ flex: 1, overflow: 'auto', mb: 2 }}>
                  {cart.map((item) => (
                    <ListItem
                      key={item.id}
                      secondaryAction={
                        <IconButton edge="end" size="small">
                          <DeleteIcon />
                        </IconButton>
                      }
                      sx={{ bgcolor: '#fafafa', mb: 1, borderRadius: 1 }}
                    >
                      <ListItemText
                        primary={item.productName}
                        secondary={`${item.quantity} x ${item.price.toFixed(2)} €`}
                      />
                      <Typography fontWeight={600}>{item.subtotal.toFixed(2)} €</Typography>
                    </ListItem>
                  ))}
                </List>

                <Divider sx={{ my: 2 }} />

                {/* Totals */}
                <Box sx={{ mb: 2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography>Tarpinė suma:</Typography>
                    <Typography>{subtotal.toFixed(2)} €</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography>PVM (21%):</Typography>
                    <Typography>{tax.toFixed(2)} €</Typography>
                  </Box>
                  <Divider sx={{ my: 1 }} />
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="h6" fontWeight={600}>
                      Viso:
                    </Typography>
                    <Typography variant="h6" fontWeight={600} color="primary.main">
                      {total.toFixed(2)} €
                    </Typography>
                  </Box>
                </Box>

                {/* Payment Buttons */}
                <Button
                  variant="contained"
                  size="large"
                  fullWidth
                  startIcon={<ReceiptIcon />}
                  sx={{ py: 2, mb: 1 }}
                >
                  Mokėjimas
                </Button>
                <Button
                  variant="outlined"
                  size="large"
                  fullWidth
                  startIcon={<PrintIcon />}
                  sx={{ py: 1.5 }}
                >
                  Spausdinti
                </Button>
              </>
            )}
          </Paper>
        </Grid>
      </Grid>

      {/* Bottom Stats Bar */}
      <Paper
        elevation={3}
        sx={{
          p: 2,
          display: 'flex',
          justifyContent: 'space-around',
          borderRadius: 0,
        }}
      >
        <Card sx={{ minWidth: 180 }}>
          <CardContent>
            <Typography color="text.secondary" gutterBottom variant="body2">
              Šiandien pardavimai
            </Typography>
            <Typography variant="h5" fontWeight={600}>
              0 €
            </Typography>
          </CardContent>
        </Card>
        <Card sx={{ minWidth: 180 }}>
          <CardContent>
            <Typography color="text.secondary" gutterBottom variant="body2">
              Užsakymų skaičius
            </Typography>
            <Typography variant="h5" fontWeight={600}>
              0
            </Typography>
          </CardContent>
        </Card>
        <Card sx={{ minWidth: 180 }}>
          <CardContent>
            <Typography color="text.secondary" gutterBottom variant="body2">
              Vidutinis čekis
            </Typography>
            <Typography variant="h5" fontWeight={600}>
              0 €
            </Typography>
          </CardContent>
        </Card>
        <Button
          variant="outlined"
          startIcon={<ReportIcon />}
          sx={{ alignSelf: 'center' }}
        >
          Dienos ataskaita
        </Button>
      </Paper>
    </Box>
  );
};

export default POSPage;
