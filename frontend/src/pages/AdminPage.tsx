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
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  ListItemButton,
  Divider,
} from '@mui/material';
import {
  Settings as SettingsIcon,
  People as PeopleIcon,
  Assessment as ReportIcon,
  Security as SecurityIcon,
  Business as BusinessIcon,
  Category as CategoryIcon,
  Inventory as InventoryIcon,
  LocalShipping as ShippingIcon,
  ChevronRight as ChevronIcon,
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';

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

const AdminPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [currentTab, setCurrentTab] = useState(0);

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setCurrentTab(newValue);
  };

  const stats = [
    { label: 'Vartotojai', value: '1', icon: <PeopleIcon />, color: '#667eea' },
    { label: 'Produktai', value: '0', icon: <CategoryIcon />, color: '#38ef7d' },
    { label: 'Užsakymai', value: '0', icon: <ShippingIcon />, color: '#fa709a' },
    { label: 'Klientai', value: '0', icon: <BusinessIcon />, color: '#f093fb' },
  ];

  const userManagementItems = [
    { label: 'Vartotojų sąrašas', path: '/admin/users' },
    { label: 'Vaidmenų valdymas', path: '/admin/roles' },
    { label: 'Teisių valdymas', path: '/admin/permissions' },
    { label: 'Audito žurnalas', path: '/admin/audit-log' },
  ];

  const systemSettingsItems = [
    { label: 'Bendri nustatymai', path: '/admin/settings/general' },
    { label: 'Įmonės informacija', path: '/admin/settings/company' },
    { label: 'Mokėjimo būdai', path: '/admin/settings/payment' },
    { label: 'El. pašto nustatymai', path: '/admin/settings/email' },
    { label: 'PVM tarifai', path: '/admin/settings/tax' },
  ];

  const reportsItems = [
    { label: 'Pardavimų ataskaita', path: '/admin/reports/sales' },
    { label: 'Kreditų ataskaita', path: '/admin/reports/credit' },
    { label: 'Grąžinimų ataskaita', path: '/admin/reports/returns' },
    { label: 'Sandėlio ataskaita', path: '/admin/reports/inventory' },
    { label: 'Klientų ataskaita', path: '/admin/reports/customers' },
  ];

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
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <SettingsIcon sx={{ fontSize: 48 }} />
          <Box>
            <Typography variant="h4" fontWeight={600}>
              Administravimas
            </Typography>
            <Typography variant="body1">
              Sveiki, {user?.fullName || user?.username}!
            </Typography>
          </Box>
        </Box>
      </Paper>

      <Box sx={{ p: 3 }}>
        {/* Stats */}
        <Grid container spacing={3} sx={{ mb: 4 }}>
          {stats.map((stat) => (
            <Grid item xs={12} sm={6} md={3} key={stat.label}>
              <Card>
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    <Box
                      sx={{
                        bgcolor: stat.color,
                        borderRadius: 2,
                        p: 1.5,
                        display: 'flex',
                        color: 'white',
                      }}
                    >
                      {stat.icon}
                    </Box>
                    <Box>
                      <Typography variant="body2" color="text.secondary">
                        {stat.label}
                      </Typography>
                      <Typography variant="h4" fontWeight={700}>
                        {stat.value}
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>

        {/* Main Content */}
        <Paper sx={{ borderRadius: 2 }}>
          <Tabs
            value={currentTab}
            onChange={handleTabChange}
            sx={{ borderBottom: 1, borderColor: 'divider', px: 2 }}
          >
            <Tab icon={<PeopleIcon />} label="Vartotojai" iconPosition="start" />
            <Tab icon={<SettingsIcon />} label="Sistemos nustatymai" iconPosition="start" />
            <Tab icon={<ReportIcon />} label="Ataskaitos" iconPosition="start" />
          </Tabs>

          {/* User Management Tab */}
          <TabPanel value={currentTab} index={0}>
            <Box sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom fontWeight={600}>
                Vartotojų valdymas
              </Typography>
              <List>
                {userManagementItems.map((item, index) => (
                  <React.Fragment key={item.label}>
                    <ListItem disablePadding>
                      <ListItemButton onClick={() => navigate(item.path)}>
                        <ListItemIcon>
                          <SecurityIcon />
                        </ListItemIcon>
                        <ListItemText primary={item.label} />
                        <ChevronIcon />
                      </ListItemButton>
                    </ListItem>
                    {index < userManagementItems.length - 1 && <Divider />}
                  </React.Fragment>
                ))}
              </List>
            </Box>
          </TabPanel>

          {/* System Settings Tab */}
          <TabPanel value={currentTab} index={1}>
            <Box sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom fontWeight={600}>
                Sistemos nustatymai
              </Typography>
              <List>
                {systemSettingsItems.map((item, index) => (
                  <React.Fragment key={item.label}>
                    <ListItem disablePadding>
                      <ListItemButton onClick={() => navigate(item.path)}>
                        <ListItemIcon>
                          <SettingsIcon />
                        </ListItemIcon>
                        <ListItemText primary={item.label} />
                        <ChevronIcon />
                      </ListItemButton>
                    </ListItem>
                    {index < systemSettingsItems.length - 1 && <Divider />}
                  </React.Fragment>
                ))}
              </List>
            </Box>
          </TabPanel>

          {/* Reports Tab */}
          <TabPanel value={currentTab} index={2}>
            <Box sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom fontWeight={600}>
                Ataskaitos
              </Typography>
              <List>
                {reportsItems.map((item, index) => (
                  <React.Fragment key={item.label}>
                    <ListItem disablePadding>
                      <ListItemButton onClick={() => navigate(item.path)}>
                        <ListItemIcon>
                          <ReportIcon />
                        </ListItemIcon>
                        <ListItemText primary={item.label} />
                        <ChevronIcon />
                      </ListItemButton>
                    </ListItem>
                    {index < reportsItems.length - 1 && <Divider />}
                  </React.Fragment>
                ))}
              </List>
            </Box>
          </TabPanel>
        </Paper>
      </Box>
    </Box>
  );
};

export default AdminPage;
