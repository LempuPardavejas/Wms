import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import MainLayout from './components/MainLayout';
import LoginPage from './pages/LoginPage';
import POSPage from './pages/POSPage';
import WMSPage from './pages/WMSPage';
import AdminPage from './pages/AdminPage';
import B2BPortalPage from './pages/B2BPortalPage';
import ReturnsPage from './pages/ReturnsPage';
import CreditTransactionsPage from './pages/CreditTransactionsPage';
import UnauthorizedPage from './pages/UnauthorizedPage';

function App() {
  return (
    <AuthProvider>
      <Routes>
        {/* Public routes */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/unauthorized" element={<UnauthorizedPage />} />

        {/* Protected routes with layout */}
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Navigate to="/pos" replace />
            </ProtectedRoute>
          }
        />

        <Route
          path="/pos"
          element={
            <ProtectedRoute requiredRoles={['ADMIN', 'SALES', 'SALES_MANAGER']}>
              <MainLayout>
                <POSPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/wms"
          element={
            <ProtectedRoute requiredRoles={['ADMIN', 'WAREHOUSE']}>
              <MainLayout>
                <WMSPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin"
          element={
            <ProtectedRoute requiredRoles={['ADMIN']}>
              <MainLayout>
                <AdminPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/b2b"
          element={
            <ProtectedRoute requiredRoles={['CLIENT', 'ADMIN']}>
              <MainLayout>
                <B2BPortalPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/returns"
          element={
            <ProtectedRoute requiredRoles={['ADMIN', 'SALES', 'WAREHOUSE']}>
              <MainLayout>
                <ReturnsPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/credit"
          element={
            <ProtectedRoute requiredRoles={['ADMIN', 'SALES', 'SALES_MANAGER']}>
              <MainLayout>
                <CreditTransactionsPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  );
}

export default App;
