import { useState } from 'react';
import {
  Box,
  Container,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Grid,
  Card,
  CardContent,
  Tabs,
  Tab,
  Alert,
  CircularProgress,
} from '@mui/material';
import {
  Add as AddIcon,
  Visibility as ViewIcon,
  Check as ApproveIcon,
  Close as RejectIcon,
  Inventory as RestockIcon,
  Payment as RefundIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { returnService } from '../services/api';
import {
  Return,
  ReturnStatus,
} from '../types';

export default function ReturnsPage() {
  const [selectedTab, setSelectedTab] = useState(0);
  const [selectedReturn, setSelectedReturn] = useState<Return | null>(null);
  const [openCreateDialog, setOpenCreateDialog] = useState(false);
  const [openDetailsDialog, setOpenDetailsDialog] = useState(false);
  const [_openInspectionDialog, _setOpenInspectionDialog] = useState(false);
  const [_openRefundDialog, _setOpenRefundDialog] = useState(false);

  const queryClient = useQueryClient();

  // Fetch returns based on status
  const statusFilters = [
    { label: 'Visi', status: null },
    { label: 'Laukiama', status: ReturnStatus.PENDING },
    { label: 'Patvirtinta', status: ReturnStatus.APPROVED },
    { label: 'Gauta', status: ReturnStatus.RECEIVED },
    { label: 'Patikrinta', status: ReturnStatus.INSPECTED },
    { label: 'Užbaigta', status: ReturnStatus.COMPLETED },
    { label: 'Atmesta', status: ReturnStatus.REJECTED },
  ];

  const currentFilter = statusFilters[selectedTab];
  const { data: returns, isLoading } = useQuery({
    queryKey: ['returns', currentFilter.status],
    queryFn: async () => {
      const response = currentFilter.status
        ? await returnService.getByStatus(currentFilter.status)
        : await returnService.getAll();
      return response.data.content || response.data;
    },
  });

  const { data: _returnReasons } = useQuery({
    queryKey: ['returnReasons'],
    queryFn: async () => {
      const response = await returnService.getReturnReasons();
      return response.data;
    },
  });

  // Mutations
  const approveMutation = useMutation({
    mutationFn: (id: string) => returnService.approve(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['returns'] });
      setOpenDetailsDialog(false);
    },
  });

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) =>
      returnService.reject(id, reason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['returns'] });
      setOpenDetailsDialog(false);
    },
  });

  const receiveMutation = useMutation({
    mutationFn: (id: string) => returnService.markAsReceived(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['returns'] });
      setOpenDetailsDialog(false);
    },
  });

  const restockMutation = useMutation({
    mutationFn: (id: string) => returnService.restock(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['returns'] });
      setOpenDetailsDialog(false);
    },
  });

  const getStatusColor = (status: ReturnStatus) => {
    const colors: Record<ReturnStatus, 'default' | 'primary' | 'secondary' | 'success' | 'error' | 'warning'> = {
      [ReturnStatus.PENDING]: 'warning',
      [ReturnStatus.APPROVED]: 'primary',
      [ReturnStatus.IN_TRANSIT]: 'secondary',
      [ReturnStatus.RECEIVED]: 'secondary',
      [ReturnStatus.INSPECTED]: 'primary',
      [ReturnStatus.COMPLETED]: 'success',
      [ReturnStatus.REJECTED]: 'error',
    };
    return colors[status] || 'default';
  };

  const getStatusLabel = (status: ReturnStatus) => {
    const labels: Record<ReturnStatus, string> = {
      [ReturnStatus.PENDING]: 'Laukiama',
      [ReturnStatus.APPROVED]: 'Patvirtinta',
      [ReturnStatus.IN_TRANSIT]: 'Siunčiama',
      [ReturnStatus.RECEIVED]: 'Gauta',
      [ReturnStatus.INSPECTED]: 'Patikrinta',
      [ReturnStatus.COMPLETED]: 'Užbaigta',
      [ReturnStatus.REJECTED]: 'Atmesta',
    };
    return labels[status] || status;
  };

  const handleViewReturn = async (returnId: string) => {
    const response = await returnService.getById(returnId);
    setSelectedReturn(response.data);
    setOpenDetailsDialog(true);
  };

  const handleApprove = () => {
    if (selectedReturn) {
      approveMutation.mutate(selectedReturn.id);
    }
  };

  const handleReject = () => {
    const reason = prompt('Įveskite atmetimo priežastį:');
    if (reason && selectedReturn) {
      rejectMutation.mutate({ id: selectedReturn.id, reason });
    }
  };

  const handleReceive = () => {
    if (selectedReturn) {
      receiveMutation.mutate(selectedReturn.id);
    }
  };

  const handleRestock = () => {
    if (selectedReturn) {
      restockMutation.mutate(selectedReturn.id);
    }
  };

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          Grąžinimai
        </Typography>
        <Button
          variant="contained"
          color="primary"
          startIcon={<AddIcon />}
          onClick={() => setOpenCreateDialog(true)}
        >
          Naujas grąžinimas
        </Button>
      </Box>

      <Paper sx={{ mb: 3 }}>
        <Tabs
          value={selectedTab}
          onChange={(_, newValue) => setSelectedTab(newValue)}
          variant="scrollable"
          scrollButtons="auto"
        >
          {statusFilters.map((filter, index) => (
            <Tab key={index} label={filter.label} />
          ))}
        </Tabs>
      </Paper>

      {isLoading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Grąžinimo Nr.</TableCell>
                <TableCell>Užsakymo Nr.</TableCell>
                <TableCell>Klientas</TableCell>
                <TableCell>Data</TableCell>
                <TableCell>Būsena</TableCell>
                <TableCell>Suma</TableCell>
                <TableCell>Grąžinimo suma</TableCell>
                <TableCell>Grąžinimo būsena</TableCell>
                <TableCell align="right">Veiksmai</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {returns?.map((returnItem: Return) => (
                <TableRow key={returnItem.id}>
                  <TableCell>{returnItem.returnNumber}</TableCell>
                  <TableCell>{returnItem.orderNumber}</TableCell>
                  <TableCell>{returnItem.customerName}</TableCell>
                  <TableCell>
                    {new Date(returnItem.returnDate).toLocaleDateString('lt-LT')}
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={getStatusLabel(returnItem.status)}
                      color={getStatusColor(returnItem.status)}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    €{returnItem.totalAmount.toFixed(2)}
                  </TableCell>
                  <TableCell>
                    €{returnItem.refundAmount.toFixed(2)}
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={returnItem.refundStatus}
                      size="small"
                      variant="outlined"
                    />
                  </TableCell>
                  <TableCell align="right">
                    <IconButton
                      size="small"
                      onClick={() => handleViewReturn(returnItem.id)}
                    >
                      <ViewIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
              {returns?.length === 0 && (
                <TableRow>
                  <TableCell colSpan={9} align="center">
                    Grąžinimų nerasta
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Return Details Dialog */}
      <Dialog
        open={openDetailsDialog}
        onClose={() => setOpenDetailsDialog(false)}
        maxWidth="lg"
        fullWidth
      >
        <DialogTitle>
          Grąžinimo detalės: {selectedReturn?.returnNumber}
        </DialogTitle>
        <DialogContent>
          {selectedReturn && (
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Pagrindinė informacija
                    </Typography>
                    <Typography variant="body2">
                      <strong>Užsakymas:</strong> {selectedReturn.orderNumber}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Klientas:</strong> {selectedReturn.customerName}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Sandėlis:</strong> {selectedReturn.warehouseName}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Tipas:</strong> {selectedReturn.returnType}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Data:</strong>{' '}
                      {new Date(selectedReturn.returnDate).toLocaleDateString('lt-LT')}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} md={6}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Finansinė informacija
                    </Typography>
                    <Typography variant="body2">
                      <strong>Suma be PVM:</strong> €{selectedReturn.subtotalAmount.toFixed(2)}
                    </Typography>
                    <Typography variant="body2">
                      <strong>PVM:</strong> €{selectedReturn.taxAmount.toFixed(2)}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Viso:</strong> €{selectedReturn.totalAmount.toFixed(2)}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Grąžinama suma:</strong> €{selectedReturn.refundAmount.toFixed(2)}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Grąžinimo būsena:</strong>{' '}
                      <Chip label={selectedReturn.refundStatus} size="small" />
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Grąžinamos prekės
                </Typography>
                <TableContainer component={Paper}>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>SKU</TableCell>
                        <TableCell>Pavadinimas</TableCell>
                        <TableCell>Priežastis</TableCell>
                        <TableCell align="right">Grąžinama</TableCell>
                        <TableCell align="right">Priimta</TableCell>
                        <TableCell align="right">Atmesta</TableCell>
                        <TableCell>Būklė</TableCell>
                        <TableCell align="right">Suma</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {selectedReturn.lines.map((line) => (
                        <TableRow key={line.id}>
                          <TableCell>{line.productSku}</TableCell>
                          <TableCell>{line.productName}</TableCell>
                          <TableCell>{line.returnReasonName}</TableCell>
                          <TableCell align="right">{line.quantityReturned}</TableCell>
                          <TableCell align="right">{line.quantityAccepted}</TableCell>
                          <TableCell align="right">{line.quantityRejected}</TableCell>
                          <TableCell>
                            <Chip label={line.condition} size="small" />
                          </TableCell>
                          <TableCell align="right">
                            €{line.refundAmount.toFixed(2)}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Grid>

              {selectedReturn.notes && (
                <Grid item xs={12}>
                  <Alert severity="info">
                    <strong>Pastabos:</strong> {selectedReturn.notes}
                  </Alert>
                </Grid>
              )}
            </Grid>
          )}
        </DialogContent>
        <DialogActions>
          {selectedReturn?.status === ReturnStatus.PENDING && (
            <>
              <Button
                onClick={handleApprove}
                color="success"
                startIcon={<ApproveIcon />}
              >
                Patvirtinti
              </Button>
              <Button
                onClick={handleReject}
                color="error"
                startIcon={<RejectIcon />}
              >
                Atmesti
              </Button>
            </>
          )}
          {selectedReturn?.status === ReturnStatus.APPROVED && (
            <Button
              onClick={handleReceive}
              color="primary"
            >
              Pažymėti kaip gautą
            </Button>
          )}
          {selectedReturn?.status === ReturnStatus.RECEIVED && (
            <Button
              onClick={() => _setOpenInspectionDialog(true)}
              color="primary"
            >
              Patikrinti
            </Button>
          )}
          {selectedReturn?.status === ReturnStatus.INSPECTED && (
            <>
              <Button
                onClick={handleRestock}
                color="primary"
                startIcon={<RestockIcon />}
              >
                Grąžinti į sandėlį
              </Button>
              <Button
                onClick={() => _setOpenRefundDialog(true)}
                color="success"
                startIcon={<RefundIcon />}
              >
                Grąžinti pinigus
              </Button>
            </>
          )}
          <Button onClick={() => setOpenDetailsDialog(false)}>
            Uždaryti
          </Button>
        </DialogActions>
      </Dialog>

      {/* Create Return Dialog - Simplified placeholder */}
      <Dialog
        open={openCreateDialog}
        onClose={() => setOpenCreateDialog(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>Naujas grąžinimas</DialogTitle>
        <DialogContent>
          <Alert severity="info" sx={{ mt: 2 }}>
            Grąžinimo kūrimo forma - reikia įgyvendinti pilną funkcionalumą su užsakymo pasirinkimu ir prekių sąrašu.
          </Alert>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenCreateDialog(false)}>
            Atšaukti
          </Button>
          <Button variant="contained" color="primary">
            Sukurti
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
}
