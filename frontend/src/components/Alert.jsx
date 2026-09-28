import React from 'react';

export const Alert = ({ type = 'info', message, onClose }) => {
  if (!message) return null;

  const styles = {
    error: { bg: 'var(--danger-bg)', border: 'var(--danger-border)', color: 'var(--danger-text)' },
    warning: { bg: 'var(--warning-bg)', border: 'var(--warning-border)', color: 'var(--warning-text)' },
    success: { bg: 'var(--success-bg)', border: 'var(--success-border)', color: 'var(--success-text)' },
    info: { bg: 'rgba(37, 99, 235, 0.12)', border: '#2563eb', color: '#60a5fa' }
  };

  const current = styles[type] || styles.info;

  return (
    <div
      style={{
        backgroundColor: current.bg,
        border: `1px solid ${current.border}`,
        color: current.color,
        padding: '0.75rem 1rem',
        borderRadius: 'var(--radius-md)',
        fontSize: '0.875rem',
        marginBottom: '1rem',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        gap: '0.5rem'
      }}
    >
      <span>{message}</span>
      {onClose && (
        <button
          onClick={onClose}
          style={{ background: 'none', border: 'none', color: 'inherit', cursor: 'pointer', fontSize: '1rem', padding: '0 0.25rem' }}
        >
          &times;
        </button>
      )}
    </div>
  );
};
