export const money = (value) =>
  new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(Number(value ?? 0));

export const dateTime = (value) => (value ? new Date(value).toLocaleString() : '-');

export const typeBadge = (type) =>
  ({ DEPOSIT: 'bg-success', WITHDRAW: 'bg-danger', TRANSFER: 'bg-primary' }[type] || 'bg-secondary');
