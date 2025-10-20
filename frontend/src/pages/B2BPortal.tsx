import { Box, AppBar, Toolbar, Typography, Container } from '@mui/material';

export default function B2BPortal() {
  return (
    <Box>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6">ELEKTRO MEISTRAS - B2B Portal</Typography>
        </Toolbar>
      </AppBar>
      <Container sx={{ mt: 4 }}>
        <Typography variant="h4">B2B Customer Portal</Typography>
        <Typography variant="body1" sx={{ mt: 2 }}>
          B2B portal interface - Coming soon
        </Typography>
      </Container>
    </Box>
  );
}
