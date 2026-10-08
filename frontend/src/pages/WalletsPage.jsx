import { useEffect, useState } from 'react';
import { createTransaction, getErrorMessage, getWalletBalance, getWallets } from '../services/api.js';
import Alert from '../components/Alert.jsx';
import Loading from '../components/Loading.jsx';
import { money } from '../utils/format.js';

const emptyForm = { walletId: '', transactionType: 'DEPOSIT', amount: '', description: '' };

export default function WalletsPage() {
  const [wallets, setWallets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [alert, setAlert] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [errors, setErrors] = useState({});
  const [saving, setSaving] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      setWallets(await getWallets());
    } catch (err) {
      setAlert({ type: 'danger', message: getErrorMessage(err) });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  // Calls GET /api/wallets/{id}/balance  ->  MySQL function get_wallet_balance()
  const checkBalance = async (wallet) => {
    try {
      const res = await getWalletBalance(wallet.id);
      setAlert({
        type: 'info',
        message: `Wallet #${res.walletId} (${wallet.userName}) balance from get_wallet_balance(): ${money(res.balance)}`,
      });
    } catch (err) {
      setAlert({ type: 'danger', message: getErrorMessage(err) });
    }
  };

  const validate = () => {
    const e = {};
    const amount = Number(form.amount);
    if (!form.walletId) e.walletId = 'Select a wallet';
    if (!form.amount || isNaN(amount) || amount <= 0) e.amount = 'Amount must be greater than 0';
    else if (!/^\d+(\.\d{1,2})?$/.test(form.amount)) e.amount = 'Use at most 2 decimal places';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const submit = async (ev) => {
    ev.preventDefault();
    if (!validate()) return;
    setSaving(true);
    try {
      await createTransaction({ ...form, walletId: Number(form.walletId), amount: Number(form.amount) });
      setAlert({ type: 'success', message: `${form.transactionType} recorded - the database trigger updated the balance.` });
      setForm(emptyForm);
      load();
    } catch (err) {
      setAlert({ type: 'danger', message: getErrorMessage(err) });
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <h3 className="mb-3">Wallets</h3>
      <Alert type={alert?.type} message={alert?.message} onClose={() => setAlert(null)} />

      <div className="row g-3">
        <div className="col-12 col-xl-8">
          {loading ? <Loading /> : (
            <div className="card">
              <div className="table-responsive">
                <table className="table table-hover mb-0 align-middle">
                  <thead><tr><th>Wallet ID</th><th>User</th><th>Current Balance</th><th /></tr></thead>
                  <tbody>
                    {wallets.map((w) => (
                      <tr key={w.id}>
                        <td>{w.id}</td>
                        <td>{w.userName} <span className="text-muted small">(user #{w.userId})</span></td>
                        <td className="fw-semibold">{money(w.balance)}</td>
                        <td className="text-end">
                          <button className="btn btn-sm btn-outline-primary" onClick={() => checkBalance(w)}>View balance</button>
                        </td>
                      </tr>
                    ))}
                    {wallets.length === 0 && <tr><td colSpan="4" className="text-center text-muted py-4">No wallets</td></tr>}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>

        <div className="col-12 col-xl-4">
          <form className="card" onSubmit={submit} noValidate>
            <div className="card-header bg-white fw-semibold">Deposit / Withdraw</div>
            <div className="card-body">
              <div className="mb-3">
                <label className="form-label">Wallet</label>
                <select className={`form-select ${errors.walletId ? 'is-invalid' : ''}`} value={form.walletId}
                        onChange={(e) => setForm({ ...form, walletId: e.target.value })}>
                  <option value="">Select wallet...</option>
                  {wallets.map((w) => <option key={w.id} value={w.id}>#{w.id} - {w.userName}</option>)}
                </select>
                <div className="invalid-feedback">{errors.walletId}</div>
              </div>
              <div className="mb-3">
                <label className="form-label">Type</label>
                <select className="form-select" value={form.transactionType}
                        onChange={(e) => setForm({ ...form, transactionType: e.target.value })}>
                  <option value="DEPOSIT">DEPOSIT</option>
                  <option value="WITHDRAW">WITHDRAW</option>
                </select>
              </div>
              <div className="mb-3">
                <label className="form-label">Amount</label>
                <input className={`form-control ${errors.amount ? 'is-invalid' : ''}`} value={form.amount}
                       onChange={(e) => setForm({ ...form, amount: e.target.value })} />
                <div className="invalid-feedback">{errors.amount}</div>
              </div>
              <div className="mb-3">
                <label className="form-label">Description</label>
                <input className="form-control" maxLength={255} value={form.description}
                       onChange={(e) => setForm({ ...form, description: e.target.value })} />
              </div>
              <button className="btn btn-primary w-100" disabled={saving}>{saving ? 'Saving...' : 'Submit'}</button>
            </div>
          </form>
        </div>
      </div>
    </>
  );
}
