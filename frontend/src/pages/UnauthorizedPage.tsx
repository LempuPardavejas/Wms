import React from 'react';
import { Box, Typography, Button, Paper } from '@mui/material';
import { Block as BlockIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { authService } from '../services/authService';

const UnauthorizedPage: React.FC = () => {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const handleGoHome = () => {
    if (user) {
      const defaultRoute = authService.getDefaultRoute(user);
      navigate(defaultRoute);
    } else {
      navigate('/login');
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: '#f5f5f5',
        p: 3,
      }}
    >
      <Paper
        sx={{
          p: 6,
          textAlign: 'center',
          maxWidth: 500,
        }}
      >
        <BlockIcon
          sx={{
            fontSize: 100,
            color: 'error.main',
            mb: 3,
          }}
        />
        <Typography variant="h4" gutterBottom fontWeight={600}>
          Prieiga uždrausta
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
          Jūs neturite teisės pasiekti šį puslapį. Jei manote, kad tai klaida, susisiekite su
          administratoriumi.
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center' }}>
          <Button variant="contained" onClick={handleGoHome}>
            Grįžti į pagrindinį
          </Button>
          <Button variant="outlined" onClick={logout}>
            Atsijungti
          </Button>
        </Box>
      </Paper>
    </Box>
  );
};

export default UnauthorizedPage;
