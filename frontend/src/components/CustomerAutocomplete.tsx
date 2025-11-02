import React, { useState, useCallback } from 'react';
import {
  Autocomplete,
  TextField,
  CircularProgress,
  Box,
  Typography,
  Chip,
  Tooltip,
} from '@mui/material';
import { debounce } from 'lodash';
import { customerService } from '../services/api';

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

interface CustomerAutocompleteProps {
  value: Customer | null;
  onChange: (customer: Customer | null) => void;
  label?: string;
  error?: boolean;
  helperText?: string;
  required?: boolean;
  autoFocus?: boolean;
}

/**
 * FAST CustomerAutocomplete component with optimized search
 * Features:
 * - Debounced search (300ms) for performance
 * - Searches by code, name, email, phone, VAT
 * - Shows customer type badge
 * - Displays credit limit and balance
 * - Keyboard navigation friendly
 * - Clear hints for better UX
 */
const CustomerAutocomplete: React.FC<CustomerAutocompleteProps> = ({
  value,
  onChange,
  label = 'Klientas',
  error = false,
  helperText,
  required = false,
  autoFocus = false,
}) => {
  const [open, setOpen] = useState(false);
  const [options, setOptions] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(false);
  const [inputValue, setInputValue] = useState('');

  // Debounced search function - PERFORMANCE OPTIMIZATION
  const searchCustomers = useCallback(
    debounce(async (query: string) => {
      if (!query || query.length < 2) {
        setOptions([]);
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        const response = await customerService.search(query);
        setOptions(response.data);
      } catch (error) {
        console.error('Error searching customers:', error);
        setOptions([]);
      } finally {
        setLoading(false);
      }
    }, 300), // 300ms debounce for optimal UX
    []
  );

  const handleInputChange = (_event: any, newInputValue: string) => {
    setInputValue(newInputValue);
    if (newInputValue.length >= 2) {
      searchCustomers(newInputValue);
    } else {
      setOptions([]);
    }
  };

  const getDisplayName = (customer: Customer): string => {
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

  const getCustomerTypeLabel = (type: string): string => {
    switch (type) {
      case 'BUSINESS':
        return 'Įmonė';
      case 'CONTRACTOR':
        return 'Rangovas';
      case 'RETAIL':
        return 'Fizinis';
      default:
        return type;
    }
  };

  const getCustomerTypeColor = (type: string): 'primary' | 'secondary' | 'default' => {
    switch (type) {
      case 'BUSINESS':
        return 'primary';
      case 'CONTRACTOR':
        return 'secondary';
      default:
        return 'default';
    }
  };

  return (
    <Autocomplete
      fullWidth
      open={open}
      onOpen={() => setOpen(true)}
      onClose={() => setOpen(false)}
      value={value}
      onChange={(_event, newValue) => onChange(newValue)}
      inputValue={inputValue}
      onInputChange={handleInputChange}
      options={options}
      loading={loading}
      getOptionLabel={(option) => `${option.code} - ${getDisplayName(option)}`}
      isOptionEqualToValue={(option, value) => option.id === value.id}
      noOptionsText={
        inputValue.length < 2
          ? 'Įveskite bent 2 simbolius...'
          : 'Klientų nerasta'
      }
      renderInput={(params) => (
        <TextField
          {...params}
          label={label}
          required={required}
          error={error}
          helperText={
            helperText ||
            'Ieškokite pagal kodą, pavadinimą, el. paštą arba tel. numerį'
          }
          autoFocus={autoFocus}
          InputProps={{
            ...params.InputProps,
            endAdornment: (
              <>
                {loading ? <CircularProgress color="inherit" size={20} /> : null}
                {params.InputProps.endAdornment}
              </>
            ),
          }}
        />
      )}
      renderOption={(props, option) => (
        <li {...props} key={option.id}>
          <Box sx={{ width: '100%' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
              <Typography variant="body1" sx={{ fontWeight: 500 }}>
                {option.code}
              </Typography>
              <Chip
                label={getCustomerTypeLabel(option.customerType)}
                size="small"
                color={getCustomerTypeColor(option.customerType)}
              />
              <Typography variant="body1" sx={{ flexGrow: 1 }}>
                {getDisplayName(option)}
              </Typography>
            </Box>
            <Box sx={{ display: 'flex', gap: 2 }}>
              {option.city && (
                <Typography variant="caption" color="text.secondary">
                  📍 {option.city}
                </Typography>
              )}
              {option.phone && (
                <Typography variant="caption" color="text.secondary">
                  📞 {option.phone}
                </Typography>
              )}
              {option.creditLimit > 0 && (
                <Tooltip title="Kredito limitas">
                  <Typography variant="caption" color="text.secondary">
                    💳 €{option.creditLimit.toFixed(0)}
                  </Typography>
                </Tooltip>
              )}
              {option.currentBalance > 0 && (
                <Tooltip title="Dabartinis balansas">
                  <Typography variant="caption" color="warning.main">
                    ⚠️ Balansas: €{option.currentBalance.toFixed(2)}
                  </Typography>
                </Tooltip>
              )}
            </Box>
          </Box>
        </li>
      )}
    />
  );
};

export default CustomerAutocomplete;
