import React, { useState, useCallback, useRef, useEffect } from 'react';
import {
  Autocomplete,
  TextField,
  CircularProgress,
  Box,
  Typography,
  Chip,
  InputAdornment,
  Tooltip,
} from '@mui/material';
import { debounce } from 'lodash';
import { productService } from '../services/api';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CableIcon from '@mui/icons-material/Cable';
import ViewModuleIcon from '@mui/icons-material/ViewModule';

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

interface ProductCodeInputProps {
  value: Product | null;
  onChange: (product: Product | null) => void;
  label?: string;
  error?: boolean;
  helperText?: string;
  required?: boolean;
  autoFocus?: boolean;
  disabled?: boolean;
  onEnterPress?: () => void; // Callback for Enter key - for quick sequential entry
}

/**
 * ULTRA FAST ProductCodeInput component optimized for rapid entry
 * Features:
 * - Instant code lookup (type "0010006" and get result immediately)
 * - Debounced search (200ms) for partial matches
 * - Searches by code, SKU, and name
 * - Shows product type icons (cable, modular)
 * - Displays price and unit
 * - Keyboard optimized (Enter to confirm and move to next field)
 * - Visual feedback for valid code entry
 * - Clear hints for better UX
 */
const ProductCodeInput: React.FC<ProductCodeInputProps> = ({
  value,
  onChange,
  label = 'Prekės kodas',
  error = false,
  helperText,
  required = false,
  autoFocus = false,
  disabled = false,
  onEnterPress,
}) => {
  const [open, setOpen] = useState(false);
  const [options, setOptions] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [inputValue, setInputValue] = useState('');
  const [validCode, setValidCode] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  // Debounced search function - PERFORMANCE OPTIMIZATION
  const searchProducts = useCallback(
    debounce(async (query: string) => {
      if (!query || query.length < 2) {
        setOptions([]);
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        const response = await productService.search(query);
        setOptions(response.data);

        // Auto-select if exact code match
        if (response.data.length === 1 &&
            response.data[0].code.toLowerCase() === query.toLowerCase()) {
          onChange(response.data[0]);
          setValidCode(true);
          setOpen(false);
        }
      } catch (error) {
        console.error('Error searching products:', error);
        setOptions([]);
      } finally {
        setLoading(false);
      }
    }, 200), // 200ms debounce - faster than customer search for rapid entry
    [onChange]
  );

  const handleInputChange = (_event: any, newInputValue: string) => {
    setInputValue(newInputValue);
    setValidCode(false);

    if (newInputValue.length >= 2) {
      searchProducts(newInputValue);
    } else {
      setOptions([]);
    }
  };

  const handleChange = (_event: any, newValue: Product | null) => {
    onChange(newValue);
    if (newValue) {
      setValidCode(true);
      setInputValue(newValue.code);
      setOpen(false);
    } else {
      setValidCode(false);
    }
  };

  const handleKeyDown = (event: React.KeyboardEvent) => {
    if (event.key === 'Enter' && validCode && onEnterPress) {
      event.preventDefault();
      onEnterPress();
    }
  };

  // Auto-focus support
  useEffect(() => {
    if (autoFocus && inputRef.current) {
      setTimeout(() => inputRef.current?.focus(), 100);
    }
  }, [autoFocus]);

  return (
    <Autocomplete
      fullWidth
      disabled={disabled}
      open={open}
      onOpen={() => setOpen(true)}
      onClose={() => setOpen(false)}
      value={value}
      onChange={handleChange}
      inputValue={inputValue}
      onInputChange={handleInputChange}
      options={options}
      loading={loading}
      getOptionLabel={(option) => option.code}
      isOptionEqualToValue={(option, value) => option.id === value.id}
      noOptionsText={
        inputValue.length < 2
          ? 'Įveskite kodą arba pavadinimą...'
          : 'Prekių nerasta'
      }
      renderInput={(params) => (
        <TextField
          {...params}
          inputRef={inputRef}
          label={label}
          required={required}
          error={error}
          helperText={
            helperText ||
            'Įveskite prekės kodą (pvz. 0010006) arba ieškokite pagal pavadinimą'
          }
          onKeyDown={handleKeyDown}
          InputProps={{
            ...params.InputProps,
            startAdornment: validCode ? (
              <InputAdornment position="start">
                <Tooltip title="Galiojantis kodas">
                  <CheckCircleIcon color="success" />
                </Tooltip>
              </InputAdornment>
            ) : null,
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
              <Typography variant="body1" sx={{ fontWeight: 600, color: 'primary.main' }}>
                {option.code}
              </Typography>
              {option.isCable && (
                <Tooltip title="Kabelis">
                  <CableIcon fontSize="small" color="action" />
                </Tooltip>
              )}
              {option.isModular && (
                <Tooltip title="Modulinė įranga">
                  <ViewModuleIcon fontSize="small" color="action" />
                </Tooltip>
              )}
            </Box>
            <Typography variant="body2" sx={{ mb: 0.5 }}>
              {option.name}
            </Typography>
            <Box sx={{ display: 'flex', gap: 2 }}>
              <Typography variant="caption" color="text.secondary">
                SKU: {option.sku}
              </Typography>
              <Chip
                label={`€${option.basePrice.toFixed(2)} / ${option.unitOfMeasure}`}
                size="small"
                color="primary"
                variant="outlined"
              />
            </Box>
          </Box>
        </li>
      )}
    />
  );
};

export default ProductCodeInput;
