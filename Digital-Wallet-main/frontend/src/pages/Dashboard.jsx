import { useEffect, useState } from 'react';
import { getTransactionDetails, getUsers, getWallets, getErrorMessage } from '../services/api.js';
import Alert from '../components/Alert.jsx';
import Loading from '../components/Loading.jsx';
import { dateTime, money, typeBadge } from '../utils/format.js';

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([getUsers(), getWallets(), getTransactionDetails()])
      .then(([users, wallets, transactions]) => setData({ users, wallets, transactions }))
      .catch((err) => setError(getErrorMessage(err)));
  }, []);

  if (error) return <Alert type="danger" message={error} />;
  if (!data) return <Loading />;

  const totalBalance = data.wallets.reduce((sum, w) => sum + Number(w.balance), 0);
  const stats = [
    { label: 'Total Users', value: data.users.length },
    { label: 'Total Wallets', value: data.wallets.length },
    { label: 'Total Transactions', value: data.transactions.length },
    { label: 'Money in Wallets', value: money(totalBalance) },
  ];

  return (
    <>
      <h3 className="mb-4">Dashboard</h3>
      <div className="row g-3 mb-4">
        {stats.map((s) => (
          <div className="col-12 col-sm-6 col-xl-3" key={s.label}>
            <div className="card stat-card">
              <div className="card-body">
                <div className="text-muted small">{s.label}</div>
                <div className="value">{s.value}</div>
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="card">
        <div className="card-header bg-white fw-semibold">Recent Transactions</div>
        <div className="table-responsive">
          <table className="table table-hover mb-0 align-middle">
            <thead>
              <tr><th>ID</th><th>User</th><th>Type</th><th>Amount</th><th>Description</th><th>Date</th></tr>
            </thead>
            <tbody>
              {data.transactions.slice(0, 5).map((t) => (
                <tr key={t.transactionId}>
                  <td>{t.transactionId}</td>
                  <td>{t.userName}</td>
                  <td><span className={`badge ${typeBadge(t.transactionType)}`}>{t.transactionType}</span></td>
                  <td>{money(t.amount)}</td>
                  <td>{t.description}</td>
                  <td>{dateTime(t.transactionDate)}</td>
                </tr>
              ))}
              {data.transactions.length === 0 && (
                <tr><td colSpan="6" className="text-center text-muted py-4">No transactions yet</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </>
  );
}
