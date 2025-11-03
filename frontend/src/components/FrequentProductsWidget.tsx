import React, { useState, useEffect } from 'react';
import {
  Paper,
  Typography,
  Box,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  Chip,
  IconButton,
  Tooltip,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import StarIcon from '@mui/icons-material/Star';
import RefreshIcon from '@mui/icons-material/Refresh';

interface Product {
  id: string;
  code: string;
  name: string;
  basePrice: number;
  unitOfMeasure: string;
  usageCount?: number;
}

interface FrequentProductsWidgetProps {
  onProductSelect: (product: Product) => void;
  limit?: number;
}

/**
 * FrequentProductsWidget - Quick access to frequently used products
 *
 * Features:
 * - Shows top N most used products
 * - One-click add to cart
 * - Visual product cards
 * - Usage count indicator
 * - Refresh to update
 *
 * Future: Could track user-specific or warehouse-specific frequent products
 */
const FrequentProductsWidget: React.FC<FrequentProductsWidgetProps> = ({
  onProductSelect,
  limit = 6,
}) => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadFrequentProducts();
    // Note: loadFrequentProducts is stable and doesn't need to be in deps
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadFrequentProducts = async () => {
    setLoading(true);
    try {
      // TODO: Implement API endpoint /api/products/frequent
      // For now, using mock data

      // Mock frequent products (would come from API based on usage statistics)
      const mockProducts: Product[] = [
        { id: '1', code: 'CABLE-2X1.5', name: 'Kabelis 2x1.5mm²', basePrice: 1.20, unitOfMeasure: 'm', usageCount: 145 },
        { id: '2', code: 'SOCKET-16A', name: 'Lizdas 16A', basePrice: 3.50, unitOfMeasure: 'vnt', usageCount: 98 },
        { id: '3', code: 'SWITCH-ON', name: 'Jungiklis viengubas', basePrice: 2.80, unitOfMeasure: 'vnt', usageCount: 87 },
        { id: '4', code: 'CABLE-3X2.5', name: 'Kabelis 3x2.5mm²', basePrice: 2.10, unitOfMeasure: 'm', usageCount: 76 },
        { id: '5', code: 'BOX-DISTRIB', name: 'Paskirstymo dėžutė', basePrice: 5.40, unitOfMeasure: 'vnt', usageCount: 65 },
        { id: '6', code: 'BREAKER-16A', name: 'Automatinis jungiklis 16A', basePrice: 8.90, unitOfMeasure: 'vnt', usageCount: 54 },
      ];

      setProducts(mockProducts.slice(0, limit));
    } catch (error) {
      console.error('Failed to load frequent products:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Paper sx={{ p: 3 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Box display="flex" alignItems="center" gap={1}>
          <StarIcon color="warning" />
          <Typography variant="h6" fontWeight="bold">
            Dažnai Naudojamos Prekės
          </Typography>
        </Box>
        <Tooltip title="Atnaujinti">
          <IconButton size="small" onClick={loadFrequentProducts} disabled={loading}>
            <RefreshIcon />
          </IconButton>
        </Tooltip>
      </Box>

      <Typography variant="body2" color="text.secondary" gutterBottom>
        Greitas pridėjimas dažniausiai naudojamų prekių
      </Typography>

      <Grid container spacing={2} mt={1}>
        {products.map((product) => (
          <Grid item xs={12} sm={6} md={4} key={product.id}>
            <Card
              variant="outlined"
              sx={{
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                '&:hover': {
                  boxShadow: 2,
                  borderColor: 'primary.main',
                },
              }}
            >
              <CardContent sx={{ flexGrow: 1, pb: 1 }}>
                <Box display="flex" justifyContent="space-between" alignItems="start" mb={1}>
                  <Chip
                    label={product.code}
                    size="small"
                    color="primary"
                    variant="outlined"
                  />
                  {product.usageCount && (
                    <Chip
                      label={`${product.usageCount}x`}
                      size="small"
                      color="warning"
                      icon={<StarIcon />}
                    />
                  )}
                </Box>

                <Typography variant="body1" fontWeight="medium" gutterBottom>
                  {product.name}
                </Typography>

                <Box display="flex" justifyContent="space-between" alignItems="center" mt={1}>
                  <Typography variant="h6" color="primary">
                    €{product.basePrice.toFixed(2)}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    /{product.unitOfMeasure}
                  </Typography>
                </Box>
              </CardContent>

              <CardActions sx={{ pt: 0 }}>
                <Button
                  fullWidth
                  variant="contained"
                  size="small"
                  startIcon={<AddIcon />}
                  onClick={() => onProductSelect(product)}
                >
                  Pridėti
                </Button>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>

      {products.length === 0 && !loading && (
        <Typography variant="body2" color="text.secondary" align="center" sx={{ py: 4 }}>
          Dažnai naudojamų prekių sąrašas tuščias
        </Typography>
      )}
    </Paper>
  );
};

export default FrequentProductsWidget;
