import { useEffect, useState } from 'react';
import { getErrorMessage, getTransactionDetails } from '../services/api.js';
import Alert from '../components/Alert.jsx';
import Loading from '../components/Loading.jsx';
import { dateTime, money, typeBadge } from '../utils/format.js';

export default function TransactionsPage() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('ALL');
  const [search, setSearch] = useState('');

  useEffect(() => {
    // GET /api/transactions/user-details  (SQL JOIN of 4 tables)
    getTransactionDetails()
      .then(setRows)
      .catch((err) => setError(getErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  const visible = rows.filter(
    (r) =>
      (filter === 'ALL' || r.transactionType === filter) &&
      (r.userName.toLowerCase().includes(search.toLowerCase()) ||
        (r.description || '').toLowerCase().includes(search.toLowerCase()))
  );

  return (
    <>
      <h3 className="mb-3">Transaction History</h3>
      <Alert type="danger" message={error} />

      <div className="row g-2 mb-3">
        <div className="col-12 col-md-5">
          <input className="form-control" placeholder="Search by user or description..." value={search}
                 onChange={(e) => setSearch(e.target.value)} />
        </div>
        <div className="col-12 col-md-3">
          <select className="form-select" value={filter} onChange={(e) => setFilter(e.target.value)}>
            <option value="ALL">All types</option>
            <option value="DEPOSIT">DEPOSIT</option>
            <option value="WITHDRAW">WITHDRAW</option>
            <option value="TRANSFER">TRANSFER</option>
          </select>
        </div>
      </div>

      {loading ? <Loading /> : (
        <div className="card">
          <div className="table-responsive">
            <table className="table table-hover mb-0 align-middle">
              <thead>
                <tr><th>Transaction ID</th><th>User</th><th>Wallet</th><th>Type</th><th>Amount</th><th>Description</th><th>Date</th></tr>
              </thead>
              <tbody>
                {visible.map((t) => (
                  <tr key={t.transactionId}>
                    <td>{t.transactionId}</td>
                    <td>{t.userName}<div className="text-muted small">{t.userEmail}</div></td>
                    <td>#{t.walletId}</td>
                    <td><span className={`badge ${typeBadge(t.transactionType)}`}>{t.transactionType}</span></td>
                    <td>{money(t.amount)}</td>
                    <td>{t.description}</td>
                    <td className="text-nowrap">{dateTime(t.transactionDate)}</td>
                  </tr>
                ))}
                {visible.length === 0 && <tr><td colSpan="7" className="text-center text-muted py-4">No transactions found</td></tr>}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </>
  );
}
