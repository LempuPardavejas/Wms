import { Box, AppBar, Toolbar, Typography, Container } from '@mui/material';

export default function AdminPage() {
  return (
    <Box>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6">ELEKTRO MEISTRAS - Admin</Typography>
        </Toolbar>
      </AppBar>
      <Container sx={{ mt: 4 }}>
        <Typography variant="h4">Admin Panel</Typography>
        <Typography variant="body1" sx={{ mt: 2 }}>
          Administration interface - Coming soon
        </Typography>
      </Container>
    </Box>
  );
}
