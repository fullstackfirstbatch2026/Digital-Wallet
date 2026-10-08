import { useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';

const links = [
  { to: '/', label: 'Dashboard', icon: '📊', end: true },
  { to: '/users', label: 'Users', icon: '👤' },
  { to: '/wallets', label: 'Wallets', icon: '👛' },
  { to: '/transfer', label: 'Transfer Money', icon: '💸' },
  { to: '/transactions', label: 'Transactions', icon: '🧾' },
  { to: '/above-average', label: 'Above Average', icon: '📈' },
];

export default function Layout() {
  const [open, setOpen] = useState(false);

  return (
    <div className="d-flex">
      <aside className={`sidebar ${open ? 'open' : ''}`}>
        <div className="brand">💳 Digital Wallet</div>
        <nav className="nav flex-column mt-2">
          {links.map((l) => (
            <NavLink key={l.to} to={l.to} end={l.end} className="nav-link" onClick={() => setOpen(false)}>
              <span className="me-2">{l.icon}</span>
              {l.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="content-area">
        <header className="bg-white border-bottom px-3 py-2 d-md-none d-flex align-items-center">
          <button className="btn btn-outline-secondary btn-sm me-2" onClick={() => setOpen(!open)}>☰</button>
          <strong>Digital Wallet</strong>
        </header>
        <main className="p-3 p-md-4">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
