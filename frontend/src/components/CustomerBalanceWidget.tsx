import React, { useState, useEffect } from 'react';
import {
  Paper,
  Typography,
  Box,
  List,
  ListItem,
  ListItemText,
  ListItemAvatar,
  Avatar,
  Chip,
  IconButton,
  Tooltip,
  Alert,
  LinearProgress,
  Divider,
} from '@mui/material';
import PersonIcon from '@mui/icons-material/Person';
import BusinessIcon from '@mui/icons-material/Business';
import WarningIcon from '@mui/icons-material/Warning';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import RefreshIcon from '@mui/icons-material/Refresh';
import ReceiptIcon from '@mui/icons-material/Receipt';

interface Customer {
  id: string;
  code: string;
  customerType: 'RETAIL' | 'BUSINESS' | 'CONTRACTOR';
  companyName?: string;
  firstName?: string;
  lastName?: string;
  creditLimit: number;
  currentBalance: number;
  pendingAmount?: number;
}

interface CustomerBalanceWidgetProps {
  onCustomerClick?: (customer: Customer) => void;
  limit?: number;
}

/**
 * CustomerBalanceWidget - Quick overview of customer balances
 *
 * Features:
 * - Shows top N customers with highest balances
 * - Credit limit progress bars
 * - Over-limit warnings
 * - Click to view statement
 * - Color-coded risk levels
 */
const CustomerBalanceWidget: React.FC<CustomerBalanceWidgetProps> = ({
  onCustomerClick,
  limit = 10,
}) => {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(false);
  const [totalDebt, setTotalDebt] = useState(0);
  const [overLimitCount, setOverLimitCount] = useState(0);

  useEffect(() => {
    loadCustomerBalances();
    // Note: loadCustomerBalances is stable and doesn't need to be in deps
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadCustomerBalances = async () => {
    setLoading(true);
    try {
      // TODO: Implement API endpoint /api/customers/balances
      // For now, using mock data

      // Mock customer balances (would come from API sorted by currentBalance DESC)
      const mockCustomers: Customer[] = [
        {
          id: '1',
          code: 'C001',
          customerType: 'BUSINESS',
          companyName: 'UAB Elektros Darbai',
          creditLimit: 5000,
          currentBalance: 4850,
          pendingAmount: 200,
        },
        {
          id: '2',
          code: 'C002',
          customerType: 'CONTRACTOR',
          firstName: 'Petras',
          lastName: 'Petraitis',
          creditLimit: 3000,
          currentBalance: 2750,
          pendingAmount: 150,
        },
        {
          id: '3',
          code: 'C003',
          customerType: 'BUSINESS',
          companyName: 'MB Elektrika Plus',
          creditLimit: 4000,
          currentBalance: 2100,
          pendingAmount: 0,
        },
        {
          id: '4',
          code: 'C004',
          customerType: 'RETAIL',
          firstName: 'Jonas',
          lastName: 'Jonaitis',
          creditLimit: 1000,
          currentBalance: 850,
          pendingAmount: 50,
        },
        {
          id: '5',
          code: 'C005',
          customerType: 'BUSINESS',
          companyName: 'UAB Šviesos Projektai',
          creditLimit: 6000,
          currentBalance: 4200,
          pendingAmount: 300,
        },
      ];

      const sorted = mockCustomers.sort((a, b) => b.currentBalance - a.currentBalance).slice(0, limit);
      setCustomers(sorted);

      // Calculate statistics
      const total = sorted.reduce((sum, c) => sum + c.currentBalance, 0);
      const overLimit = sorted.filter(c => c.currentBalance > c.creditLimit).length;

      setTotalDebt(total);
      setOverLimitCount(overLimit);
    } catch (error) {
      console.error('Failed to load customer balances:', error);
    } finally {
      setLoading(false);
    }
  };

  const getCustomerName = (customer: Customer): string => {
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

  const getUtilizationPercent = (customer: Customer): number => {
    return (customer.currentBalance / customer.creditLimit) * 100;
  };

  const getUtilizationColor = (percent: number): 'success' | 'warning' | 'error' => {
    if (percent >= 100) return 'error';
    if (percent >= 80) return 'warning';
    return 'success';
  };

  const isOverLimit = (customer: Customer): boolean => {
    return customer.currentBalance > customer.creditLimit;
  };

  return (
    <Paper sx={{ p: 3 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Box>
          <Typography variant="h6" fontWeight="bold">
            Klientų Skolos
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Top {limit} pagal skolą
          </Typography>
        </Box>
        <Tooltip title="Atnaujinti">
          <IconButton size="small" onClick={loadCustomerBalances} disabled={loading}>
            <RefreshIcon />
          </IconButton>
        </Tooltip>
      </Box>

      {loading && <LinearProgress sx={{ mb: 2 }} />}

      {/* Statistics Summary */}
      <Box display="flex" gap={2} mb={2}>
        <Paper variant="outlined" sx={{ p: 2, flexGrow: 1 }}>
          <Box display="flex" alignItems="center" gap={1}>
            <TrendingUpIcon color="primary" />
            <Box>
              <Typography variant="caption" color="text.secondary">
                Bendra skola
              </Typography>
              <Typography variant="h6" fontWeight="bold" color="primary">
                €{totalDebt.toFixed(2)}
              </Typography>
            </Box>
          </Box>
        </Paper>

        {overLimitCount > 0 && (
          <Paper variant="outlined" sx={{ p: 2, bgcolor: 'error.light' }}>
            <Box display="flex" alignItems="center" gap={1}>
              <WarningIcon sx={{ color: 'white' }} />
              <Box>
                <Typography variant="caption" sx={{ color: 'white' }}>
                  Viršyta
                </Typography>
                <Typography variant="h6" fontWeight="bold" sx={{ color: 'white' }}>
                  {overLimitCount}
                </Typography>
              </Box>
            </Box>
          </Paper>
        )}
      </Box>

      <Divider sx={{ mb: 2 }} />

      {/* Customer List */}
      {customers.length === 0 && !loading ? (
        <Alert severity="info">Nėra klientų su skolom</Alert>
      ) : (
        <List sx={{ maxHeight: 400, overflow: 'auto' }}>
          {customers.map((customer, index) => {
            const utilizationPercent = getUtilizationPercent(customer);
            const utilizationColor = getUtilizationColor(utilizationPercent);
            const overLimit = isOverLimit(customer);

            return (
              <ListItem
                key={customer.id}
                sx={{
                  border: 1,
                  borderColor: overLimit ? 'error.main' : 'divider',
                  borderRadius: 1,
                  mb: 1,
                  bgcolor: overLimit ? 'error.lighter' : 'background.paper',
                  '&:hover': {
                    bgcolor: 'action.hover',
                    cursor: onCustomerClick ? 'pointer' : 'default',
                  },
                }}
                onClick={() => onCustomerClick && onCustomerClick(customer)}
                secondaryAction={
                  onCustomerClick && (
                    <Tooltip title="Mėnesio išrašas">
                      <IconButton edge="end">
                        <ReceiptIcon />
                      </IconButton>
                    </Tooltip>
                  )
                }
              >
                <ListItemAvatar>
                  <Avatar sx={{ bgcolor: overLimit ? 'error.main' : 'primary.main' }}>
                    {customer.customerType === 'BUSINESS' ? <BusinessIcon /> : <PersonIcon />}
                  </Avatar>
                </ListItemAvatar>

                <ListItemText
                  primary={
                    <Box display="flex" alignItems="center" gap={1}>
                      <Typography variant="body1" fontWeight="medium">
                        #{index + 1} {getCustomerName(customer)}
                      </Typography>
                      {overLimit && (
                        <Chip
                          label="VIRŠYTA"
                          size="small"
                          color="error"
                          icon={<WarningIcon />}
                        />
                      )}
                    </Box>
                  }
                  secondary={
                    <Box mt={1}>
                      <Box display="flex" justifyContent="space-between" mb={0.5}>
                        <Typography variant="caption" color="text.secondary">
                          Skola: €{customer.currentBalance.toFixed(2)}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          Limitas: €{customer.creditLimit.toFixed(2)}
                        </Typography>
                      </Box>
                      <LinearProgress
                        variant="determinate"
                        value={Math.min(utilizationPercent, 100)}
                        color={utilizationColor}
                        sx={{ height: 6, borderRadius: 1 }}
                      />
                      <Box display="flex" justifyContent="space-between" mt={0.5}>
                        <Typography variant="caption" color="text.secondary">
                          {utilizationPercent.toFixed(1)}% panaudota
                        </Typography>
                        {customer.pendingAmount && customer.pendingAmount > 0 && (
                          <Typography variant="caption" color="warning.main">
                            +€{customer.pendingAmount.toFixed(2)} laukia
                          </Typography>
                        )}
                      </Box>
                    </Box>
                  }
                />
              </ListItem>
            );
          })}
        </List>
      )}
    </Paper>
  );
};

export default CustomerBalanceWidget;
