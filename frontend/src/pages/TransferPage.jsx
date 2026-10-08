import { useEffect, useState } from 'react';
import { getErrorMessage, getWallets, transferMoney } from '../services/api.js';
import Alert from '../components/Alert.jsx';
import Loading from '../components/Loading.jsx';
import { money } from '../utils/format.js';

const emptyForm = { senderWalletId: '', receiverWalletId: '', amount: '', description: '' };

export default function TransferPage() {
  const [wallets, setWallets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState(emptyForm);
  const [errors, setErrors] = useState({});
  const [alert, setAlert] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const loadWallets = async () => {
    try {
      setWallets(await getWallets());
    } catch (err) {
      setAlert({ type: 'danger', message: getErrorMessage(err) });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadWallets(); }, []);

  const sender = wallets.find((w) => String(w.id) === form.senderWalletId);

  const validate = () => {
    const e = {};
    const amount = Number(form.amount);
    if (!form.senderWalletId) e.senderWalletId = 'Select the sender wallet';
    if (!form.receiverWalletId) e.receiverWalletId = 'Select the receiver wallet';
    if (form.senderWalletId && form.senderWalletId === form.receiverWalletId) {
      e.receiverWalletId = 'Sender and receiver wallets cannot be the same';
    }
    if (!form.amount || isNaN(amount) || amount <= 0) e.amount = 'Amount must be greater than 0';
    else if (!/^\d+(\.\d{1,2})?$/.test(form.amount)) e.amount = 'Use at most 2 decimal places';
    else if (sender && amount > Number(sender.balance)) e.amount = `Insufficient balance (available ${money(sender.balance)})`;
    if (form.description.length > 200) e.description = 'Description must be at most 200 characters';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async (ev) => {
    ev.preventDefault();
    setAlert(null);
    if (!validate()) return;
    setSubmitting(true);
    try {
      const res = await transferMoney({
        senderWalletId: Number(form.senderWalletId),
        receiverWalletId: Number(form.receiverWalletId),
        amount: Number(form.amount),
        description: form.description || 'Money transfer',
      });
      setAlert({
        type: 'success',
        message: `${res.message}. ${money(res.amount)} sent. New balances - sender: ${money(res.senderBalance)}, receiver: ${money(res.receiverBalance)}.`,
      });
      setForm(emptyForm);
      setErrors({});
      loadWallets();
    } catch (err) {
      setAlert({ type: 'danger', message: getErrorMessage(err) });
    } finally {
      setSubmitting(false);
    }
  };

  const label = (w) => `#${w.id} - ${w.userName} (${money(w.balance)})`;

  if (loading) return <Loading />;

  return (
    <>
      <h3 className="mb-3">Transfer Money</h3>
      <Alert type={alert?.type} message={alert?.message} onClose={() => setAlert(null)} />

      <div className="row">
        <div className="col-12 col-lg-7 col-xl-6">
          <form className="card" onSubmit={handleSubmit} noValidate>
            <div className="card-body">
              <div className="mb-3">
                <label className="form-label">Sender wallet *</label>
                <select className={`form-select ${errors.senderWalletId ? 'is-invalid' : ''}`} value={form.senderWalletId}
                        onChange={(e) => setForm({ ...form, senderWalletId: e.target.value })}>
                  <option value="">Select sender...</option>
                  {wallets.map((w) => <option key={w.id} value={w.id}>{label(w)}</option>)}
                </select>
                <div className="invalid-feedback">{errors.senderWalletId}</div>
              </div>

              <div className="mb-3">
                <label className="form-label">Receiver wallet *</label>
                <select className={`form-select ${errors.receiverWalletId ? 'is-invalid' : ''}`} value={form.receiverWalletId}
                        onChange={(e) => setForm({ ...form, receiverWalletId: e.target.value })}>
                  <option value="">Select receiver...</option>
                  {wallets.map((w) => <option key={w.id} value={w.id}>{label(w)}</option>)}
                </select>
                <div className="invalid-feedback">{errors.receiverWalletId}</div>
              </div>

              <div className="mb-3">
                <label className="form-label">Amount *</label>
                <input className={`form-control ${errors.amount ? 'is-invalid' : ''}`} placeholder="e.g. 500.00"
                       value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} />
                <div className="invalid-feedback">{errors.amount}</div>
              </div>

              <div className="mb-3">
                <label className="form-label">Description</label>
                <input className={`form-control ${errors.description ? 'is-invalid' : ''}`} value={form.description}
                       onChange={(e) => setForm({ ...form, description: e.target.value })} />
                <div className="invalid-feedback">{errors.description}</div>
              </div>

              <button className="btn btn-primary" disabled={submitting}>
                {submitting ? 'Transferring...' : 'Transfer'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </>
  );
}
