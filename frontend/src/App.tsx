import { Routes, Route, Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { RootState } from './store';
import LoginPage from './pages/LoginPage';
import POSPage from './pages/POSPage';
import WMSPage from './pages/WMSPage';
import AdminPage from './pages/AdminPage';
import B2BPortal from './pages/B2BPortal';
import ReturnsPage from './pages/ReturnsPage';
import CreditTransactionsPage from './pages/CreditTransactionsPage';
import CustomerCreditPortal from './pages/CustomerCreditPortal';

function App() {
  const isAuthenticated = useSelector((state: RootState) => state.auth.isAuthenticated);

  if (!isAuthenticated) {
    return (
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }

  return (
    <Routes>
      <Route path="/" element={<Navigate to="/pos" replace />} />
      <Route path="/pos" element={<POSPage />} />
      <Route path="/wms" element={<WMSPage />} />
      <Route path="/admin" element={<AdminPage />} />
      <Route path="/b2b" element={<B2BPortal />} />
      <Route path="/returns" element={<ReturnsPage />} />
      <Route path="/credit" element={<CreditTransactionsPage />} />
      <Route path="/credit-portal" element={<CustomerCreditPortal />} />
      <Route path="*" element={<Navigate to="/pos" replace />} />
    </Routes>
  );
}

export default App;
