import React from 'react';
import {
  Box,
  Paper,
  Typography,
  Grid,
  Card,
  CardContent,
  Button,
  Chip,
} from '@mui/material';
import {
  Warehouse as WarehouseIcon,
  Inventory as InventoryIcon,
  LocalShipping as ShippingIcon,
  AssignmentReturn as ReturnIcon,
  Warning as WarningIcon,
  Add as AddIcon,
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';

const WMSPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const quickActions = [
    { label: 'Prekių priėmimas', icon: <AddIcon />, color: 'primary', path: '/wms/receive' },
    { label: 'Prekių išdavimas', icon: <ShippingIcon />, color: 'success', path: '/wms/issue' },
    { label: 'Grąžinimų apdorojimas', icon: <ReturnIcon />, color: 'warning', path: '/returns' },
    { label: 'Inventorizacija', icon: <InventoryIcon />, color: 'info', path: '/wms/inventory' },
  ];

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#f5f5f5' }}>
      {/* Header */}
      <Paper
        elevation={2}
        sx={{
          p: 3,
          borderRadius: 0,
          background: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)',
          color: 'white',
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <WarehouseIcon sx={{ fontSize: 48 }} />
          <Box>
            <Typography variant="h4" fontWeight={600}>
              Sandėlio valdymas (WMS)
            </Typography>
            <Typography variant="body1">
              Sveiki, {user?.fullName || user?.username}!
            </Typography>
          </Box>
        </Box>
      </Paper>

      <Box sx={{ p: 3 }}>
        {/* Quick Stats */}
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid item xs={12} md={3}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Box
                    sx={{
                      bgcolor: 'primary.light',
                      borderRadius: 2,
                      p: 1.5,
                      display: 'flex',
                    }}
                  >
                    <InventoryIcon sx={{ fontSize: 32, color: 'primary.main' }} />
                  </Box>
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Šiandien gauta
                    </Typography>
                    <Typography variant="h5" fontWeight={700}>
                      0
                    </Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={3}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Box
                    sx={{
                      bgcolor: 'success.light',
                      borderRadius: 2,
                      p: 1.5,
                      display: 'flex',
                    }}
                  >
                    <ShippingIcon sx={{ fontSize: 32, color: 'success.main' }} />
                  </Box>
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Šiandien išduota
                    </Typography>
                    <Typography variant="h5" fontWeight={700}>
                      0
                    </Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={3}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Box
                    sx={{
                      bgcolor: 'warning.light',
                      borderRadius: 2,
                      p: 1.5,
                      display: 'flex',
                    }}
                  >
                    <ReturnIcon sx={{ fontSize: 32, color: 'warning.main' }} />
                  </Box>
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Grąžinimai
                    </Typography>
                    <Typography variant="h5" fontWeight={700}>
                      0
                    </Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={3}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Box
                    sx={{
                      bgcolor: 'error.light',
                      borderRadius: 2,
                      p: 1.5,
                      display: 'flex',
                    }}
                  >
                    <WarningIcon sx={{ fontSize: 32, color: 'error.main' }} />
                  </Box>
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Žemos atsargos
                    </Typography>
                    <Typography variant="h5" fontWeight={700}>
                      0
                    </Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        <Grid container spacing={3}>
          {/* Quick Actions */}
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom fontWeight={600}>
                Greiti veiksmai
              </Typography>
              <Grid container spacing={2} sx={{ mt: 1 }}>
                {quickActions.map((action) => (
                  <Grid item xs={6} key={action.label}>
                    <Button
                      variant="outlined"
                      fullWidth
                      startIcon={action.icon}
                      onClick={() => action.path && navigate(action.path)}
                      sx={{
                        py: 3,
                        flexDirection: 'column',
                        height: '100%',
                        gap: 1,
                      }}
                    >
                      {action.label}
                    </Button>
                  </Grid>
                ))}
              </Grid>
            </Paper>
          </Grid>

          {/* Recent Activity */}
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom fontWeight={600}>
                Naujausios operacijos
              </Typography>
              <Box
                sx={{
                  display: 'flex',
                  justifyContent: 'center',
                  alignItems: 'center',
                  minHeight: 200,
                  bgcolor: '#fafafa',
                  borderRadius: 2,
                  mt: 2,
                }}
              >
                <Typography color="text.secondary">Šiandien dar nebuvo operacijų</Typography>
              </Box>
            </Paper>
          </Grid>

          {/* Warehouse Locations */}
          <Grid item xs={12}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom fontWeight={600}>
                Sandėlio zonos
              </Typography>
              <Grid container spacing={2} sx={{ mt: 1 }}>
                {['A1', 'A2', 'B1', 'B2', 'C1', 'C2'].map((zone) => (
                  <Grid item xs={6} sm={4} md={2} key={zone}>
                    <Card variant="outlined" sx={{ textAlign: 'center', p: 2 }}>
                      <Typography variant="h5" fontWeight={600}>
                        {zone}
                      </Typography>
                      <Chip
                        label="Laisva"
                        size="small"
                        color="success"
                        sx={{ mt: 1 }}
                      />
                    </Card>
                  </Grid>
                ))}
              </Grid>
            </Paper>
          </Grid>
        </Grid>
      </Box>
    </Box>
  );
};

export default WMSPage;
