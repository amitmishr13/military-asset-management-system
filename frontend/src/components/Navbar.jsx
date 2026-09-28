import React from 'react';
import { useAuth } from '../context/AuthContext';
import { LogOut, Shield, Menu } from 'lucide-react';

export const Navbar = ({ onToggleSidebar }) => {
  const { user, logout } = useAuth();

  const getRoleBadgeClass = (role) => {
    switch (role) {
      case 'ADMIN': return 'badge badge-admin';
      case 'BASE_COMMANDER': return 'badge badge-commander';
      case 'LOGISTICS_OFFICER': return 'badge badge-logistics';
      default: return 'badge';
    }
  };

  const getRoleLabel = (role) => {
    switch (role) {
      case 'ADMIN': return 'System Administrator';
      case 'BASE_COMMANDER': return 'Base Commander';
      case 'LOGISTICS_OFFICER': return 'Logistics Officer';
      default: return role;
    }
  };

  return (
    <header style={{
      height: '60px',
      backgroundColor: 'var(--bg-header)',
      borderBottom: '1px solid var(--border-color)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '0 1.25rem',
      position: 'sticky',
      top: 0,
      zIndex: 40
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <button
          onClick={onToggleSidebar}
          style={{
            background: 'none',
            border: 'none',
            color: 'var(--text-muted)',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            padding: '0.25rem'
          }}
          className="mobile-menu-btn"
          title="Toggle Navigation Menu"
        >
          <Menu size={20} />
        </button>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Shield size={20} color="var(--primary)" />
          <span style={{ fontWeight: 700, fontSize: '0.95rem', letterSpacing: '0.04em', color: 'var(--text-main)' }}>
            MILITARY ASSET MANAGEMENT
          </span>
        </div>
      </div>

      {user && (
        <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
          <div style={{ textAlign: 'right', display: 'flex', flexDirection: 'column', gap: '0.1rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', justifyContent: 'flex-end' }}>
              <span style={{ fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-main)' }}>
                {user.fullName || user.username}
              </span>
              <span className={getRoleBadgeClass(user.role)}>
                {getRoleLabel(user.role)}
              </span>
            </div>
            {user.baseName && (
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                Assigned Base: <strong style={{ color: '#6ee7b7' }}>{user.baseName}</strong>
              </span>
            )}
          </div>

          <button
            onClick={logout}
            className="btn btn-secondary"
            style={{ padding: '0.4rem 0.75rem', fontSize: '0.8rem', gap: '0.35rem' }}
            title="Sign Out"
          >
            <LogOut size={14} />
            <span>Logout</span>
          </button>
        </div>
      )}
    </header>
  );
};
