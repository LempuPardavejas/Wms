import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Box,
  Typography,
  Divider,
  Alert,
  Grid,
  Paper,
  Chip,
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import SignaturePad from './SignaturePad';
import PhotoCapture from './PhotoCapture';

interface Transaction {
  id: string;
  transactionNumber: string;
  transactionType: 'PICKUP' | 'RETURN';
  customerName: string;
  totalAmount: number;
  totalItems: number;
  performedBy: string;
  createdAt: string;
}

interface ConfirmTransactionDialogProps {
  open: boolean;
  onClose: () => void;
  onConfirm: (confirmedBy: string, signatureData: string, photoData: string, notes: string) => void;
  transaction: Transaction | null;
  currentUser?: string;
}

/**
 * ConfirmTransactionDialog - Confirmation dialog with signature and photo capture
 *
 * Features:
 * - Transaction summary display
 * - Signature capture (required)
 * - Photo capture (optional)
 * - Confirmation notes
 * - Clear visual feedback
 * - Validation before confirm
 */
const ConfirmTransactionDialog: React.FC<ConfirmTransactionDialogProps> = ({
  open,
  onClose,
  onConfirm,
  transaction,
  currentUser = 'Vadybininkas',
}) => {
  const [confirmedBy, setConfirmedBy] = useState<string>(currentUser);
  const [signatureData, setSignatureData] = useState<string>('');
  const [photoData, setPhotoData] = useState<string>('');
  const [notes, setNotes] = useState<string>('');
  const [error, setError] = useState<string>('');

  const handleConfirm = () => {
    if (!confirmedBy.trim()) {
      setError('Įveskite patvirtinusio asmens vardą');
      return;
    }

    if (!signatureData) {
      setError('Prašome pasirašyti');
      return;
    }

    onConfirm(confirmedBy, signatureData, photoData, notes);
    handleClose();
  };

  const handleClose = () => {
    setConfirmedBy(currentUser);
    setSignatureData('');
    setPhotoData('');
    setNotes('');
    setError('');
    onClose();
  };

  if (!transaction) return null;

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box display="flex" alignItems="center" gap={1}>
          <CheckCircleIcon color="success" />
          <Typography variant="h6">Patvirtinti operaciją</Typography>
        </Box>
      </DialogTitle>

      <DialogContent dividers>
        <Grid container spacing={3}>
          {/* Transaction Summary */}
          <Grid item xs={12}>
            <Paper sx={{ p: 2, bgcolor: 'background.default' }}>
              <Typography variant="h6" gutterBottom>
                Operacijos informacija
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <Typography variant="caption" color="text.secondary">
                    Operacijos numeris
                  </Typography>
                  <Typography variant="body1" fontWeight="bold">
                    {transaction.transactionNumber}
                  </Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="caption" color="text.secondary">
                    Tipas
                  </Typography>
                  <Box>
                    <Chip
                      label={transaction.transactionType === 'PICKUP' ? 'Paėmimas' : 'Grąžinimas'}
                      color={transaction.transactionType === 'PICKUP' ? 'primary' : 'secondary'}
                      size="small"
                    />
                  </Box>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="caption" color="text.secondary">
                    Klientas
                  </Typography>
                  <Typography variant="body1">
                    {transaction.customerName}
                  </Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="caption" color="text.secondary">
                    Atliko
                  </Typography>
                  <Typography variant="body1">
                    {transaction.performedBy}
                  </Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="caption" color="text.secondary">
                    Suma
                  </Typography>
                  <Typography variant="h6" color="primary">
                    €{transaction.totalAmount.toFixed(2)}
                  </Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="caption" color="text.secondary">
                    Prekių kiekis
                  </Typography>
                  <Typography variant="h6">
                    {transaction.totalItems} vnt.
                  </Typography>
                </Grid>
                <Grid item xs={12}>
                  <Typography variant="caption" color="text.secondary">
                    Data
                  </Typography>
                  <Typography variant="body2">
                    {new Date(transaction.createdAt).toLocaleString('lt-LT')}
                  </Typography>
                </Grid>
              </Grid>
            </Paper>
          </Grid>

          {error && (
            <Grid item xs={12}>
              <Alert severity="error" onClose={() => setError('')}>
                {error}
              </Alert>
            </Grid>
          )}

          {/* Confirmed By */}
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Patvirtino (vardas, pavardė) *"
              value={confirmedBy}
              onChange={(e) => setConfirmedBy(e.target.value)}
              required
              helperText="Įveskite patvirtinusio asmens vardą ir pavardę"
            />
          </Grid>

          <Grid item xs={12}>
            <Divider />
          </Grid>

          {/* Signature Capture */}
          <Grid item xs={12}>
            <SignaturePad
              onSave={setSignatureData}
              width={600}
              height={150}
            />
            <Alert severity="info" sx={{ mt: 1 }}>
              <strong>Parašas privalomas.</strong> Klientas ar darbuotojas turi pasirašyti patvirtindamas operaciją.
            </Alert>
          </Grid>

          <Grid item xs={12}>
            <Divider />
          </Grid>

          {/* Photo Capture */}
          <Grid item xs={12}>
            <PhotoCapture
              onCapture={setPhotoData}
              width={300}
              height={225}
            />
            <Alert severity="info" sx={{ mt: 1 }}>
              <strong>Nuotrauka neprivaloma.</strong> Galite įkelti patvirtinusio asmens nuotrauką papildomam patvirtinimui.
            </Alert>
          </Grid>

          <Grid item xs={12}>
            <Divider />
          </Grid>

          {/* Notes */}
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Papildomos pastabos"
              multiline
              rows={3}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Įveskite papildomas pastabas apie patvirtinimą..."
            />
          </Grid>
        </Grid>
      </DialogContent>

      <DialogActions>
        <Button onClick={handleClose}>Atšaukti</Button>
        <Button
          variant="contained"
          color="success"
          startIcon={<CheckCircleIcon />}
          onClick={handleConfirm}
          disabled={!confirmedBy.trim() || !signatureData}
        >
          Patvirtinti operaciją
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ConfirmTransactionDialog;
