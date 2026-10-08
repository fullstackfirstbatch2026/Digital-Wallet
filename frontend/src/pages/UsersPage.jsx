import { useEffect, useState } from 'react';
import { createUser, deleteUser, getErrorMessage, getUsers, getWallets, updateUser } from '../services/api.js';
import Alert from '../components/Alert.jsx';
import Loading from '../components/Loading.jsx';
import { dateTime, money } from '../utils/format.js';

const emptyForm = { name: '', email: '', phone: '' };
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function UsersPage() {
  const [users, setUsers] = useState([]);
  const [wallets, setWallets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [alert, setAlert] = useState(null); // { type, message }

  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [errors, setErrors] = useState({});
  const [saving, setSaving] = useState(false);

  const [viewUser, setViewUser] = useState(null);

  const load = async () => {
    setLoading(true);
    try {
      const [u, w] = await Promise.all([getUsers(), getWallets()]);
      setUsers(u);
      setWallets(w);
    } catch (err) {
      setAlert({ type: 'danger', message: getErrorMessage(err) });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const openAdd = () => { setEditingId(null); setForm(emptyForm); setErrors({}); setFormOpen(true); };
  const openEdit = (u) => {
    setEditingId(u.id);
    setForm({ name: u.name, email: u.email, phone: u.phone || '' });
    setErrors({});
    setFormOpen(true);
  };

  const validate = () => {
    const e = {};
    if (!form.name.trim()) e.name = 'Name is required';
    if (!form.email.trim()) e.email = 'Email is required';
    else if (!EMAIL_REGEX.test(form.email.trim())) e.email = 'Enter a valid email address';
    if (form.phone && !/^[0-9+\-\s]{7,20}$/.test(form.phone)) e.phone = 'Phone must be 7-20 digits';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async (ev) => {
    ev.preventDefault();
    if (!validate()) return;
    setSaving(true);
    try {
      if (editingId) {
        await updateUser(editingId, form);
        setAlert({ type: 'success', message: 'User updated successfully' });
      } else {
        await createUser(form);
        setAlert({ type: 'success', message: 'User created successfully (wallet created automatically)' });
      }
      setFormOpen(false);
      load();
    } catch (err) {
      setErrors({ server: getErrorMessage(err) });
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (u) => {
    if (!window.confirm(`Delete user "${u.name}"?`)) return;
    try {
      await deleteUser(u.id);
      setAlert({ type: 'success', message: 'User deleted successfully' });
      load();
    } catch (err) {
      setAlert({ type: 'danger', message: getErrorMessage(err) });
    }
  };

  const walletOf = (userId) => wallets.find((w) => w.userId === userId);

  return (
    <>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h3 className="mb-0">Users</h3>
        <button className="btn btn-primary" onClick={openAdd}>+ Add User</button>
      </div>

      <Alert type={alert?.type} message={alert?.message} onClose={() => setAlert(null)} />

      {loading ? <Loading /> : (
        <div className="card">
          <div className="table-responsive">
            <table className="table table-hover mb-0 align-middle">
              <thead>
                <tr><th>ID</th><th>Name</th><th>Email</th><th>Phone</th><th>Created</th><th className="text-end">Actions</th></tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td>{u.id}</td>
                    <td>{u.name}</td>
                    <td>{u.email}</td>
                    <td>{u.phone}</td>
                    <td>{dateTime(u.createdAt)}</td>
                    <td className="text-end text-nowrap">
                      <button className="btn btn-sm btn-outline-secondary me-1" onClick={() => setViewUser(u)}>View</button>
                      <button className="btn btn-sm btn-outline-primary me-1" onClick={() => openEdit(u)}>Edit</button>
                      <button className="btn btn-sm btn-outline-danger" onClick={() => handleDelete(u)}>Delete</button>
                    </td>
                  </tr>
                ))}
                {users.length === 0 && <tr><td colSpan="6" className="text-center text-muted py-4">No users found</td></tr>}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Add / Edit modal */}
      {formOpen && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <form className="modal-content" onSubmit={handleSubmit} noValidate>
              <div className="modal-header">
                <h5 className="modal-title">{editingId ? 'Edit User' : 'Add User'}</h5>
                <button type="button" className="btn-close" onClick={() => setFormOpen(false)} />
              </div>
              <div className="modal-body">
                <Alert type="danger" message={errors.server} />
                <div className="mb-3">
                  <label className="form-label">Name *</label>
                  <input className={`form-control ${errors.name ? 'is-invalid' : ''}`} value={form.name}
                         onChange={(e) => setForm({ ...form, name: e.target.value })} />
                  <div className="invalid-feedback">{errors.name}</div>
                </div>
                <div className="mb-3">
                  <label className="form-label">Email *</label>
                  <input type="email" className={`form-control ${errors.email ? 'is-invalid' : ''}`} value={form.email}
                         onChange={(e) => setForm({ ...form, email: e.target.value })} />
                  <div className="invalid-feedback">{errors.email}</div>
                </div>
                <div className="mb-3">
                  <label className="form-label">Phone</label>
                  <input className={`form-control ${errors.phone ? 'is-invalid' : ''}`} value={form.phone}
                         onChange={(e) => setForm({ ...form, phone: e.target.value })} />
                  <div className="invalid-feedback">{errors.phone}</div>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-light" onClick={() => setFormOpen(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Saving...' : 'Save'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* View details modal */}
      {viewUser && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">User Details</h5>
                <button type="button" className="btn-close" onClick={() => setViewUser(null)} />
              </div>
              <div className="modal-body">
                <dl className="row mb-0">
                  <dt className="col-4">ID</dt><dd className="col-8">{viewUser.id}</dd>
                  <dt className="col-4">Name</dt><dd className="col-8">{viewUser.name}</dd>
                  <dt className="col-4">Email</dt><dd className="col-8">{viewUser.email}</dd>
                  <dt className="col-4">Phone</dt><dd className="col-8">{viewUser.phone || '-'}</dd>
                  <dt className="col-4">Created</dt><dd className="col-8">{dateTime(viewUser.createdAt)}</dd>
                  <dt className="col-4">Wallet ID</dt><dd className="col-8">{walletOf(viewUser.id)?.id ?? 'No wallet'}</dd>
                  <dt className="col-4">Balance</dt>
                  <dd className="col-8 fw-bold">{walletOf(viewUser.id) ? money(walletOf(viewUser.id).balance) : '-'}</dd>
                </dl>
              </div>
              <div className="modal-footer">
                <button className="btn btn-light" onClick={() => setViewUser(null)}>Close</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
