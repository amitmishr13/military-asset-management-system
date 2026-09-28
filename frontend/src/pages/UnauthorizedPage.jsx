import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldAlert, ArrowLeft } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const UnauthorizedPage = () => {
  const navigate = useNavigate();
  const { user, isLogistics } = useAuth();

  const handleReturn = () => {
    if (isLogistics) {
      navigate('/purchases');
    } else {
      navigate('/dashboard');
    }
  };

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '60vh',
      textAlign: 'center',
      padding: '2rem'
    }}>
      <div style={{
        backgroundColor: 'rgba(239, 68, 68, 0.1)',
        border: '1px solid var(--critical-border)',
        borderRadius: '50%',
        padding: '1.5rem',
        marginBottom: '1.5rem',
        color: 'var(--critical-text)'
      }}>
        <ShieldAlert size={48} />
      </div>

      <h1 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '0.5rem', color: 'var(--text-main)' }}>
        403 - Access Restricted
      </h1>

      <p style={{ color: 'var(--text-muted)', maxWidth: '460px', marginBottom: '1.5rem', fontSize: '0.95rem' }}>
        You do not have authorization to access this operation with your active role ({user?.role || 'User'}).
      </p>

      <button
        className="btn btn-primary"
        onClick={handleReturn}
        style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}
      >
        <ArrowLeft size={16} />
        Return to Permitted Navigation
      </button>
    </div>
  );
};

export default UnauthorizedPage;
