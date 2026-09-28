import React, { useState, useEffect } from 'react';
import { getAuditLogs } from '../services/auditLogService';
import { getBases } from '../services/referenceService';
import { useAuth } from '../context/AuthContext';
import { formatDateIN } from '../utils/formatters';
import { exportToCSV } from '../utils/csvExport';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { Alert } from '../components/Alert';
import { FileText, Download, Search, Filter } from 'lucide-react';

export const AuditLogsPage = () => {
  const { user } = useAuth();
  const [logs, setLogs] = useState([]);
  const [bases, setBases] = useState([]);
  const [selectedBaseId, setSelectedBaseId] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [actionFilter, setActionFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadData();
  }, [selectedBaseId]);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);

      const [logsRes, basesRes] = await Promise.all([
        getAuditLogs(selectedBaseId || null),
        user?.role === 'ADMIN' ? getBases() : Promise.resolve({ data: [] })
      ]);

      if (logsRes.success) {
        setLogs(logsRes.data || []);
      } else {
        setError(logsRes.message || 'Failed to load audit logs.');
      }

      if (basesRes.success) {
        setBases(basesRes.data || []);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'An error occurred while loading audit logs.');
    } finally {
      setLoading(false);
    }
  };

  // Unique actions for filter dropdown
  const uniqueActions = Array.from(new Set(logs.map(l => l.action))).filter(Boolean);

  // Filter logs based on search term & action filter
  const filteredLogs = logs.filter(log => {
    const matchesSearch = 
      !searchTerm ||
      log.username?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      log.action?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      log.entityType?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      log.details?.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesAction = !actionFilter || log.action === actionFilter;

    return matchesSearch && matchesAction;
  });

  const handleExportCSV = () => {
    const columns = [
      { key: 'timestamp', label: 'Timestamp' },
      { key: 'username', label: 'User' },
      { key: 'action', label: 'Action' },
      { key: 'entityType', label: 'Entity Type' },
      { key: 'entityId', label: 'Entity ID' },
      { key: 'baseId', label: 'Base ID' },
      { key: 'details', label: 'Details' },
      { key: 'ipAddress', label: 'IP Address' }
    ];

    const exportData = filteredLogs.map(l => ({
      ...l,
      timestamp: formatDateIN(l.timestamp)
    }));

    exportToCSV(exportData, columns, 'audit_logs');
  };

  if (loading && !logs.length) {
    return <LoadingSpinner message="Loading audit log entries..." />;
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 700, margin: 0, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <FileText size={24} color="var(--accent-military)" />
            System Audit Trail
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginTop: '0.25rem' }}>
            Immutable security and operational event logs for compliance and traceability
          </p>
        </div>

        <button
          onClick={handleExportCSV}
          className="btn btn-export"
          disabled={!filteredLogs.length}
        >
          <Download size={16} />
          <span>Export CSV</span>
        </button>
      </div>

      {error && <Alert type="error" message={error} onClose={() => setError(null)} />}

      {/* Filter Bar */}
      <div className="filter-bar">
        <div className="filter-inputs">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', backgroundColor: '#070b10', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)', padding: '0.4rem 0.75rem' }}>
            <Search size={16} color="var(--text-muted)" />
            <input
              type="text"
              placeholder="Search user, action, entity, details..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{ background: 'none', border: 'none', color: 'var(--text-main)', fontSize: '0.875rem', outline: 'none', width: '220px' }}
            />
          </div>

          {user?.role === 'ADMIN' && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Filter size={16} color="var(--text-muted)" />
              <select
                className="form-select"
                value={selectedBaseId}
                onChange={(e) => setSelectedBaseId(e.target.value)}
                style={{ padding: '0.45rem 0.65rem' }}
              >
                <option value="">All Bases</option>
                {bases.map(b => (
                  <option key={b.id} value={b.id}>{b.name} ({b.code})</option>
                ))}
              </select>
            </div>
          )}

          {uniqueActions.length > 0 && (
            <select
              className="form-select"
              value={actionFilter}
              onChange={(e) => setActionFilter(e.target.value)}
              style={{ padding: '0.45rem 0.65rem' }}
            >
              <option value="">All Event Types</option>
              {uniqueActions.map(action => (
                <option key={action} value={action}>{action}</option>
              ))}
            </select>
          )}
        </div>

        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
          Showing <strong>{filteredLogs.length}</strong> of {logs.length} entries
        </div>
      </div>

      {/* Audit Logs Table */}
      {filteredLogs.length === 0 ? (
        <div className="empty-state">
          <FileText size={36} color="var(--text-dim)" style={{ marginBottom: '0.5rem' }} />
          <h3>No Audit Log Records Found</h3>
          <p>No system actions match your active search or filter criteria.</p>
        </div>
      ) : (
        <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Timestamp</th>
                  <th>User</th>
                  <th>Action</th>
                  <th>Entity Type</th>
                  <th>Entity ID</th>
                  <th>Base ID</th>
                  <th>Operational Details</th>
                  <th>IP Address</th>
                </tr>
              </thead>
              <tbody>
                {filteredLogs.map((log) => (
                  <tr key={log.id}>
                    <td style={{ fontSize: '0.8rem', whiteSpace: 'nowrap', color: 'var(--text-muted)' }}>
                      {formatDateIN(log.timestamp)}
                    </td>
                    <td>
                      <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>
                        {log.username}
                      </span>
                    </td>
                    <td>
                      <span className="badge" style={{ backgroundColor: 'rgba(74, 103, 65, 0.15)', color: '#86efac', border: '1px solid #4a6741' }}>
                        {log.action}
                      </span>
                    </td>
                    <td>{log.entityType}</td>
                    <td style={{ fontFamily: 'monospace' }}>#{log.entityId}</td>
                    <td>{log.baseId ? `Base #${log.baseId}` : 'Global'}</td>
                    <td style={{ maxWidth: '300px', whiteSpace: 'normal', fontSize: '0.8125rem', color: 'var(--text-muted)' }}>
                      {log.details || '-'}
                    </td>
                    <td style={{ fontFamily: 'monospace', fontSize: '0.8rem', color: 'var(--text-dim)' }}>
                      {log.ipAddress || '127.0.0.1'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};
