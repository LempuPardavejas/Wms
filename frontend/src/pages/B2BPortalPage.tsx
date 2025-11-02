import React, { useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Grid,
  Card,
  CardContent,
  Button,
  Tabs,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from '@mui/material';
import {
  Download as DownloadIcon,
  LocalShipping as ShippingIcon,
  Receipt as ReceiptIcon,
  AccountBalance as BalanceIcon,
  History as HistoryIcon,
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => {
  return (
    <div hidden={value !== index} style={{ paddingTop: 24 }}>
      {value === index && children}
    </div>
  );
};

const B2BPortalPage: React.FC = () => {
  const { user } = useAuth();
  const [currentTab, setCurrentTab] = useState(0);

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setCurrentTab(newValue);
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#f5f5f5' }}>
      {/* Header */}
      <Paper
        elevation={2}
        sx={{
          p: 3,
          borderRadius: 0,
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          color: 'white',
        }}
      >
        <Typography variant="h4" fontWeight={600} gutterBottom>
          Klientų portalas
        </Typography>
        <Typography variant="body1">
          Sveiki sugrįžę, <strong>{user?.fullName || user?.username}</strong>!
        </Typography>
      </Paper>

      <Box sx={{ p: 3 }}>
        {/* Stats Cards */}
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid item xs={12} md={4}>
            <Card
              sx={{
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                color: 'white',
              }}
            >
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <BalanceIcon sx={{ fontSize: 40, mr: 2 }} />
                  <Box>
                    <Typography variant="body2" sx={{ opacity: 0.9 }}>
                      Kredito likutis
                    </Typography>
                    <Typography variant="h4" fontWeight={700}>
                      0,00 €
                    </Typography>
                  </Box>
                </Box>
                <Typography variant="caption" sx={{ opacity: 0.8 }}>
                  Kredito limitas: 0,00 €
                </Typography>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <ShippingIcon sx={{ fontSize: 40, mr: 2, color: 'primary.main' }} />
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Aktyvūs užsakymai
                    </Typography>
                    <Typography variant="h4" fontWeight={700}>
                      0
                    </Typography>
                  </Box>
                </Box>
                <Typography variant="caption" color="text.secondary">
                  Laukiantys pristatymo
                </Typography>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <ReceiptIcon sx={{ fontSize: 40, mr: 2, color: 'success.main' }} />
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      Šį mėnesį
                    </Typography>
                    <Typography variant="h4" fontWeight={700}>
                      0,00 €
                    </Typography>
                  </Box>
                </Box>
                <Typography variant="caption" color="text.secondary">
                  Užsakymų suma
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        {/* Tabs Section */}
        <Paper sx={{ borderRadius: 2 }}>
          <Tabs
            value={currentTab}
            onChange={handleTabChange}
            sx={{ borderBottom: 1, borderColor: 'divider', px: 2 }}
          >
            <Tab label="Mano užsakymai" />
            <Tab label="Prekių pasiėmimai" />
            <Tab label="Sąskaitos" />
            <Tab label="Istorija" />
          </Tabs>

          {/* Orders Tab */}
          <TabPanel value={currentTab} index={0}>
            <Box sx={{ p: 3 }}>
              <Box
                sx={{
                  display: 'flex',
                  justifyContent: 'center',
                  alignItems: 'center',
                  minHeight: 300,
                  bgcolor: '#fafafa',
                  borderRadius: 2,
                  flexDirection: 'column',
                }}
              >
                <ShippingIcon sx={{ fontSize: 80, color: 'text.disabled', mb: 2 }} />
                <Typography variant="h6" color="text.secondary" gutterBottom>
                  Jūs dar neturite užsakymų
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                  Visi jūsų užsakymai bus rodomi čia
                </Typography>
                <Button variant="contained" size="large">
                  Prašyti prekių pasiėmimo
                </Button>
              </Box>
            </Box>
          </TabPanel>

          {/* Pickups Tab */}
          <TabPanel value={currentTab} index={1}>
            <Box sx={{ p: 3 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
                <Typography variant="h6">Prekių pasiėmimai</Typography>
                <Button variant="contained" startIcon={<ReceiptIcon />}>
                  Naujas pasiėmimas
                </Button>
              </Box>

              <TableContainer>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Nr.</TableCell>
                      <TableCell>Data</TableCell>
                      <TableCell>Produktų kiekis</TableCell>
                      <TableCell>Suma</TableCell>
                      <TableCell>Būsena</TableCell>
                      <TableCell align="right">Veiksmai</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    <TableRow>
                      <TableCell colSpan={6} align="center">
                        <Box sx={{ py: 4 }}>
                          <Typography color="text.secondary">
                            Nėra prekių pasiėmimų
                          </Typography>
                        </Box>
                      </TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          </TabPanel>

          {/* Invoices Tab */}
          <TabPanel value={currentTab} index={2}>
            <Box sx={{ p: 3 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
                <Typography variant="h6">Sąskaitos faktūros</Typography>
                <Button variant="outlined" startIcon={<DownloadIcon />}>
                  Atsisiųsti mėnesinę suvestinę
                </Button>
              </Box>

              <TableContainer>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Sąskaitos Nr.</TableCell>
                      <TableCell>Data</TableCell>
                      <TableCell>Suma</TableCell>
                      <TableCell>Būsena</TableCell>
                      <TableCell>Terminas</TableCell>
                      <TableCell align="right">Veiksmai</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    <TableRow>
                      <TableCell colSpan={6} align="center">
                        <Box sx={{ py: 4 }}>
                          <Typography color="text.secondary">Nėra sąskaitų</Typography>
                        </Box>
                      </TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          </TabPanel>

          {/* History Tab */}
          <TabPanel value={currentTab} index={3}>
            <Box sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom>
                Operacijų istorija
              </Typography>

              <Box
                sx={{
                  display: 'flex',
                  justifyContent: 'center',
                  alignItems: 'center',
                  minHeight: 300,
                  bgcolor: '#fafafa',
                  borderRadius: 2,
                  flexDirection: 'column',
                }}
              >
                <HistoryIcon sx={{ fontSize: 80, color: 'text.disabled', mb: 2 }} />
                <Typography variant="h6" color="text.secondary">
                  Nėra įrašų istorijoje
                </Typography>
              </Box>
            </Box>
          </TabPanel>
        </Paper>
      </Box>
    </Box>
  );
};

export default B2BPortalPage;
