import { useEffect, useState } from 'react';
import { getAboveAverageTransactions, getErrorMessage, getTransactions } from '../services/api.js';
import Alert from '../components/Alert.jsx';
import Loading from '../components/Loading.jsx';
import { money } from '../utils/format.js';

export default function AboveAveragePage() {
  const [rows, setRows] = useState([]);
  const [average, setAverage] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    // GET /api/users/above-average-transactions  (SQL subquery)
    Promise.all([getAboveAverageTransactions(), getTransactions()])
      .then(([above, all]) => {
        setRows(above);
        if (all.length) setAverage(all.reduce((s, t) => s + Number(t.amount), 0) / all.length);
      })
      .catch((err) => setError(getErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  return (
    <>
      <h3 className="mb-1">Above Average Transactions</h3>
      <p className="text-muted">
        Transactions larger than the average of all transactions
        {average !== null && <> (average = <strong>{money(average)}</strong>)</>}.
      </p>
      <Alert type="danger" message={error} />

      {loading ? <Loading /> : (
        <div className="card">
          <div className="table-responsive">
            <table className="table table-hover mb-0 align-middle">
              <thead><tr><th>User ID</th><th>Name</th><th>Email</th><th>Transaction Amount</th></tr></thead>
              <tbody>
                {rows.map((r, i) => (
                  <tr key={`${r.userId}-${i}`}>
                    <td>{r.userId}</td>
                    <td>{r.userName}</td>
                    <td>{r.email}</td>
                    <td className="fw-semibold">{money(r.transactionAmount)}</td>
                  </tr>
                ))}
                {rows.length === 0 && <tr><td colSpan="4" className="text-center text-muted py-4">No results</td></tr>}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </>
  );
}
