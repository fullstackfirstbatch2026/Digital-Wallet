import axios from 'axios';

// The backend URL comes from the VITE_API_URL environment variable (see .env)
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: { 'Content-Type': 'application/json' },
});

const unwrap = (promise) => promise.then((res) => res.data);

// ---- Users ----
export const getUsers = () => unwrap(api.get('/users'));
export const getUser = (id) => unwrap(api.get(`/users/${id}`));
export const createUser = (data) => unwrap(api.post('/users', data));
export const updateUser = (id, data) => unwrap(api.put(`/users/${id}`, data));
export const deleteUser = (id) => unwrap(api.delete(`/users/${id}`));
export const getAboveAverageTransactions = () => unwrap(api.get('/users/above-average-transactions'));

// ---- Wallets ----
export const getWallets = () => unwrap(api.get('/wallets'));
export const getWallet = (id) => unwrap(api.get(`/wallets/${id}`));
export const createWallet = (data) => unwrap(api.post('/wallets', data));
export const getWalletBalance = (walletId) => unwrap(api.get(`/wallets/${walletId}/balance`));
export const transferMoney = (data) => unwrap(api.post('/wallets/transfer', data));

// ---- Transactions ----
export const getTransactions = () => unwrap(api.get('/transactions'));
export const getTransactionDetails = () => unwrap(api.get('/transactions/user-details'));
export const createTransaction = (data) => unwrap(api.post('/transactions', data));

/** Turns any Axios error into a readable message for the UI. */
export const getErrorMessage = (err) => {
  if (err.response?.data?.message) return err.response.data.message;
  if (err.request) return 'Cannot reach the server. Is the backend running?';
  return err.message || 'Something went wrong';
};

export default api;
