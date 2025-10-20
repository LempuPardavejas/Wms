import { Box, AppBar, Toolbar, Typography, Container } from '@mui/material';

export default function WMSPage() {
  return (
    <Box>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6">ELEKTRO MEISTRAS - WMS</Typography>
        </Toolbar>
      </AppBar>
      <Container sx={{ mt: 4 }}>
        <Typography variant="h4">Warehouse Management System</Typography>
        <Typography variant="body1" sx={{ mt: 2 }}>
          Mobile WMS interface - Coming soon
        </Typography>
      </Container>
    </Box>
  );
}
