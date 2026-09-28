import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Alert } from '../components/Alert';
import { Shield, Lock, User, KeyRound } from 'lucide-react';

export const LoginPage = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const { login, isAuthenticated, user, isLogistics } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    if (isAuthenticated && user) {
      if (isLogistics) {
        navigate('/purchases', { replace: true });
      } else {
        navigate('/dashboard', { replace: true });
      }
    }
  }, [isAuthenticated, user, isLogistics, navigate]);

  const handleLogin = async (e) => {
    e.preventDefault();
    if (!username || !password) {
      setError('Please enter both username and password');
      return;
    }

    setError(null);
    setSubmitting(true);

    try {
      const response = await login(username, password);
      setSubmitting(false);

      if (response.success && response.data) {
        const userRole = response.data.role;
        const from = location.state?.from?.pathname;

        if (from && from !== '/login') {
          navigate(from, { replace: true });
        } else if (userRole === 'LOGISTICS_OFFICER') {
          navigate('/purchases', { replace: true });
        } else {
          navigate('/dashboard', { replace: true });
        }
      } else {
        setError(response.message || 'Authentication failed');
      }
    } catch (err) {
      setSubmitting(false);
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response?.status === 401) {
        setError('Invalid username or password');
      } else {
        setError('Unable to connect to security server. Please check backend status.');
      }
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      backgroundColor: 'var(--bg-main)',
      padding: '1.5rem'
    }}>
      <div style={{ width: '100%', maxWidth: '420px' }}>
        <div style={{ textAlign: 'center', marginBottom: '1.75rem' }}>
          <div style={{ display: 'inline-flex', padding: '0.75rem', borderRadius: '50%', backgroundColor: 'var(--bg-card)', border: '1px solid var(--border-color)', marginBottom: '0.75rem' }}>
            <Shield size={36} color="var(--primary)" />
          </div>
          <h1 style={{ fontSize: '1.35rem', fontWeight: 700, letterSpacing: '0.04em', color: 'var(--text-main)', marginBottom: '0.25rem' }}>
            MILITARY ASSET MANAGEMENT
          </h1>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            Restricted System • Authorized Access Only
          </p>
        </div>

        <div className="card" style={{ padding: '1.75rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
            <Lock size={16} color="var(--text-muted)" />
            <h2 style={{ fontSize: '0.95rem', fontWeight: 600, color: 'var(--text-main)' }}>Authentication Credentials</h2>
          </div>

          <Alert type="error" message={error} onClose={() => setError(null)} />

          <form onSubmit={handleLogin}>
            <div className="form-group">
              <label className="form-label" htmlFor="username">Username / Service ID</label>
              <div style={{ position: 'relative' }}>
                <User size={16} color="var(--text-dim)" style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)' }} />
                <input
                  id="username"
                  type="text"
                  className="form-input"
                  style={{ width: '100%', paddingLeft: '2.25rem' }}
                  placeholder="Enter username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  disabled={submitting}
                  autoComplete="username"
                />
              </div>
            </div>

            <div className="form-group" style={{ marginBottom: '1.5rem' }}>
              <label className="form-label" htmlFor="password">Security Password</label>
              <div style={{ position: 'relative' }}>
                <KeyRound size={16} color="var(--text-dim)" style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)' }} />
                <input
                  id="password"
                  type="password"
                  className="form-input"
                  style={{ width: '100%', paddingLeft: '2.25rem' }}
                  placeholder="Enter password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  disabled={submitting}
                  autoComplete="current-password"
                />
              </div>
            </div>

            <button
              type="submit"
              className="btn btn-primary"
              style={{ width: '100%', padding: '0.65rem', fontSize: '0.9rem', fontWeight: 600 }}
              disabled={submitting}
            >
              {submitting ? 'Authenticating...' : 'Sign In to Terminal'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
