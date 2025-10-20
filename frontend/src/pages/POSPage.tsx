import { useState } from 'react';
import {
  Box,
  AppBar,
  Toolbar,
  Typography,
  Container,
  Grid,
  Paper,
  TextField,
  InputAdornment,
  List,
  ListItem,
  ListItemText,
  Button,
  Chip,
  Divider,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { useSelector } from 'react-redux';
import { RootState } from '@/store';

export default function POSPage() {
  const [searchQuery, setSearchQuery] = useState('');
  const cart = useSelector((state: RootState) => state.cart);
  const user = useSelector((state: RootState) => state.auth.user);

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100vh' }}>
      {/* Header */}
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>
            ELEKTRO MEISTRAS - POS
          </Typography>
          <Typography variant="body1">
            {user?.firstName} {user?.lastName}
          </Typography>
        </Toolbar>
      </AppBar>

      {/* Main Content */}
      <Container maxWidth="xl" sx={{ flex: 1, py: 2 }}>
        <Grid container spacing={2} sx={{ height: '100%' }}>
          {/* Left Panel - Search & Products */}
          <Grid item xs={12} md={8}>
            <Paper sx={{ p: 2, height: '100%', display: 'flex', flexDirection: 'column' }}>
              <TextField
                fullWidth
                placeholder="[F2] Ieškoti produktų, klientų, užsakymų..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon />
                    </InputAdornment>
                  ),
                }}
                autoFocus
              />

              <Box sx={{ mt: 2, flex: 1, overflow: 'auto' }}>
                <Typography variant="subtitle2" color="textSecondary">
                  Rezultatai
                </Typography>
                {/* Search results will go here */}
              </Box>
            </Paper>
          </Grid>

          {/* Right Panel - Customer & Cart */}
          <Grid item xs={12} md={4}>
            <Paper sx={{ p: 2, height: '100%', display: 'flex', flexDirection: 'column' }}>
              {/* Customer Section */}
              <Box>
                <Typography variant="subtitle2" gutterBottom>
                  Klientas [F3]
                </Typography>
                {cart.customer ? (
                  <Box>
                    <Typography variant="body2">{cart.customer.companyName}</Typography>
                    <Typography variant="caption" color="textSecondary">
                      Kreditas: {cart.customer.creditLimit}€
                    </Typography>
                  </Box>
                ) : (
                  <Typography variant="body2" color="textSecondary">
                    Pasirinkite klientą
                  </Typography>
                )}
              </Box>

              <Divider sx={{ my: 2 }} />

              {/* Cart Section */}
              <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                <Typography variant="subtitle2" gutterBottom>
                  Krepšelis [F4 EDIT]
                </Typography>

                <List sx={{ flex: 1, overflow: 'auto' }}>
                  {cart.items.map((item, index) => (
                    <ListItem key={index} dense>
                      <ListItemText
                        primary={item.product.name}
                        secondary={`${item.quantity} × ${item.unitPrice.toFixed(2)}€`}
                      />
                      <Typography variant="body2">
                        {item.lineTotal.toFixed(2)}€
                      </Typography>
                    </ListItem>
                  ))}
                </List>

                {cart.items.length === 0 && (
                  <Typography variant="body2" color="textSecondary" align="center">
                    Krepšelis tuščias
                  </Typography>
                )}

                <Divider sx={{ my: 1 }} />

                {/* Totals */}
                <Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="body2">Suma:</Typography>
                    <Typography variant="body2">{cart.subtotal.toFixed(2)}€</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="body2">PVM:</Typography>
                    <Typography variant="body2">{cart.taxAmount.toFixed(2)}€</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                    <Typography variant="h6">Viso:</Typography>
                    <Typography variant="h6">{cart.totalAmount.toFixed(2)}€</Typography>
                  </Box>
                </Box>

                <Button
                  variant="contained"
                  color="primary"
                  size="large"
                  fullWidth
                  disabled={cart.items.length === 0}
                >
                  [F10] Mokėti
                </Button>
              </Box>
            </Paper>
          </Grid>
        </Grid>
      </Container>
    </Box>
  );
}
