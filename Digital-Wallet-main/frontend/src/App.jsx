import { Route, Routes } from 'react-router-dom';
import Layout from './components/Layout.jsx';
import Dashboard from './pages/Dashboard.jsx';
import UsersPage from './pages/UsersPage.jsx';
import WalletsPage from './pages/WalletsPage.jsx';
import TransferPage from './pages/TransferPage.jsx';
import TransactionsPage from './pages/TransactionsPage.jsx';
import AboveAveragePage from './pages/AboveAveragePage.jsx';

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Dashboard />} />
        <Route path="/users" element={<UsersPage />} />
        <Route path="/wallets" element={<WalletsPage />} />
        <Route path="/transfer" element={<TransferPage />} />
        <Route path="/transactions" element={<TransactionsPage />} />
        <Route path="/above-average" element={<AboveAveragePage />} />
        <Route path="*" element={<div className="alert alert-warning">Page not found.</div>} />
      </Route>
    </Routes>
  );
}
