import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { getBases, getEquipmentTypes } from '../services/referenceService';
import { createExpenditure, getExpenditures } from '../services/expenditureService';
import { getAssignments } from '../services/assignmentService';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { Alert } from '../components/Alert';
import { formatDateIN } from '../utils/formatters';
import { exportToCSV } from '../utils/csvExport';
import { Flame, AlertTriangle, RefreshCw, Link2, Search, Download } from 'lucide-react';

export const ExpendituresPage = () => {
  const { user, isAdmin, isCommander } = useAuth();

  // Reference Data State
  const [bases, setBases] = useState([]);
  const [equipmentTypes, setEquipmentTypes] = useState([]);
  const [activeAssignments, setActiveAssignments] = useState([]);

  // Form State
  const [formBaseId, setFormBaseId] = useState('');
  const [formEquipmentTypeId, setFormEquipmentTypeId] = useState('');
  const [formAssignmentId, setFormAssignmentId] = useState('');
  const [formExpendedQuantity, setFormExpendedQuantity] = useState('1');
  const [formReason, setFormReason] = useState('');
  const [formExpendedDate, setFormExpendedDate] = useState(() => {
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    return now.toISOString().slice(0, 16);
  });
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState(null);
  const [formSuccess, setFormSuccess] = useState(null);

  // Table Filters & Search State
  const [filterBaseId, setFilterBaseId] = useState('');
  const [filterEquipmentTypeId, setFilterEquipmentTypeId] = useState('');
  const [searchTerm, setSearchTerm] = useState('');

  // Table Data State
  const [expenditures, setExpenditures] = useState([]);
  const [loadingExpenditures, setLoadingExpenditures] = useState(true);
  const [tableError, setTableError] = useState(null);

  // Load Base & Equipment References
  useEffect(() => {
    const loadReferences = async () => {
      try {
        const [baseRes, equipRes] = await Promise.all([getBases(), getEquipmentTypes()]);
        if (baseRes.success) setBases(baseRes.data || []);
        if (equipRes.success) setEquipmentTypes(equipRes.data || []);
      } catch (err) {
        console.error('Failed to load reference data', err);
      }
    };
    loadReferences();
  }, []);

  // Set default base for commander
  useEffect(() => {
    if (user?.baseId) {
      setFormBaseId(user.baseId);
      setFilterBaseId(user.baseId);
    } else if (bases.length > 0 && !formBaseId) {
      setFormBaseId(bases[0].id);
    }
  }, [user, bases, formBaseId]);

  // Dynamically load active assignments for selected Base and Equipment Type
  useEffect(() => {
    const fetchActiveAssignments = async () => {
      if (!formBaseId || !formEquipmentTypeId) {
        setActiveAssignments([]);
        return;
      }
      try {
        const res = await getAssignments({
          baseId: formBaseId,
          equipmentTypeId: formEquipmentTypeId,
          status: 'ACTIVE'
        });
        if (res.success) {
          setActiveAssignments(res.data || []);
        }
      } catch (err) {
        console.error('Failed to load active assignments for expenditure form', err);
      }
    };
    fetchActiveAssignments();
  }, [formBaseId, formEquipmentTypeId]);

  // Fetch Expenditures List
  const fetchExpenditures = useCallback(async () => {
    setLoadingExpenditures(true);
    setTableError(null);
    try {
      const params = {};
      if (filterBaseId) params.baseId = filterBaseId;
      if (filterEquipmentTypeId) params.equipmentTypeId = filterEquipmentTypeId;

      const response = await getExpenditures(params);
      if (response.success) {
        setExpenditures(response.data || []);
      } else {
        setTableError(response.message || 'Failed to fetch expenditures');
      }
    } catch (err) {
      setTableError(err.response?.data?.message || 'Server error loading expenditure history');
    } finally {
      setLoadingExpenditures(false);
    }
  }, [filterBaseId, filterEquipmentTypeId]);

  useEffect(() => {
    fetchExpenditures();
  }, [fetchExpenditures]);

  // Client-side search filter
  const filteredExpenditures = expenditures.filter(e => {
    if (!searchTerm) return true;
    const term = searchTerm.toLowerCase();
    return (
      e.expenditureReference?.toLowerCase().includes(term) ||
      e.reason?.toLowerCase().includes(term) ||
      e.equipmentName?.toLowerCase().includes(term) ||
      e.baseName?.toLowerCase().includes(term) ||
      e.assignmentReference?.toLowerCase().includes(term) ||
      e.recordedByUsername?.toLowerCase().includes(term)
    );
  });

  // Submit Expenditure Form
  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError(null);
    setFormSuccess(null);

    const baseIdNum = Number(formBaseId);
    const equipIdNum = Number(formEquipmentTypeId);
    const qtyNum = parseInt(formExpendedQuantity, 10);
    const reasonText = formReason.trim();
    const assignmentIdNum = formAssignmentId ? Number(formAssignmentId) : null;

    if (!baseIdNum) {
      setFormError('Please select a military base');
      return;
    }
    if (!equipIdNum) {
      setFormError('Please select an equipment category');
      return;
    }
    if (isNaN(qtyNum) || qtyNum < 1) {
      setFormError('Expended quantity must be a positive integer (minimum 1)');
      return;
    }
    if (!reasonText) {
      setFormError('Reason for expenditure is required');
      return;
    }
    if (!formExpendedDate) {
      setFormError('Expended date and time are required');
      return;
    }

    setSubmitting(true);

    try {
      const payload = {
        baseId: baseIdNum,
        equipmentTypeId: equipIdNum,
        assignmentId: assignmentIdNum,
        expendedQuantity: qtyNum,
        reason: reasonText,
        expendedDate: new Date(formExpendedDate).toISOString()
      };

      const response = await createExpenditure(payload);
      setSubmitting(false);

      if (response.success && response.data) {
        setFormSuccess(`Asset expenditure recorded successfully! Reference: ${response.data.expenditureReference}`);
        setFormAssignmentId('');
        setFormExpendedQuantity('1');
        setFormReason('');
        fetchExpenditures();
      } else {
        setFormError(response.message || 'Failed to record expenditure');
      }
    } catch (err) {
      setSubmitting(false);
      setFormError(err.response?.data?.message || 'Error executing expenditure transaction on server');
    }
  };

  const handleExportCSV = () => {
    const columns = [
      { key: 'expenditureReference', label: 'Reference' },
      { key: 'expendedDate', label: 'Expended Date' },
      { key: 'baseName', label: 'Base' },
      { key: 'equipmentName', label: 'Equipment' },
      { key: 'expendedQuantity', label: 'Expended Quantity' },
      { key: 'reason', label: 'Reason / Report' },
      { key: 'assignmentReference', label: 'Linked Assignment Ref' },
      { key: 'recordedByUsername', label: 'Recorded By' }
    ];

    const exportData = filteredExpenditures.map(e => ({
      ...e,
      expendedDate: formatDateIN(e.expendedDate)
    }));

    exportToCSV(exportData, columns, 'expenditures_log');
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
            <Flame size={24} color="var(--accent-military)" />
            <h1 style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--text-main)', margin: 0 }}>Expended Asset Records</h1>
          </div>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>
            Record consumed, decommissioned, or written-off equipment and inspect expenditure audit logs.
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <button className="btn btn-export" onClick={handleExportCSV} disabled={!filteredExpenditures.length}>
            <Download size={15} />
            Export CSV
          </button>
          <button className="btn btn-secondary" onClick={fetchExpenditures} disabled={loadingExpenditures}>
            <RefreshCw size={15} className={loadingExpenditures ? 'spin' : ''} />
            Refresh
          </button>
        </div>
      </div>

      {/* Record Expenditure Form Card */}
      <div className="card">
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
          <AlertTriangle size={18} color="var(--low-border)" />
          <h2 style={{ fontSize: '1rem', fontWeight: 600, color: 'var(--text-main)', margin: 0 }}>Record Expended Equipment</h2>
        </div>

        <Alert type="error" message={formError} onClose={() => setFormError(null)} />
        <Alert type="success" message={formSuccess} onClose={() => setFormSuccess(null)} />

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem', marginBottom: '1rem' }}>

            {/* Base Selection */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="expend-base">Target Base *</label>
              <select
                id="expend-base"
                className="form-select"
                value={formBaseId}
                onChange={(e) => setFormBaseId(e.target.value)}
                disabled={isCommander}
              >
                <option value="">Select Base</option>
                {bases.map(b => (
                  <option key={b.id} value={b.id}>{b.name} ({b.code})</option>
                ))}
              </select>
            </div>

            {/* Equipment Type Selection */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="expend-equip">Equipment Category *</label>
              <select
                id="expend-equip"
                className="form-select"
                value={formEquipmentTypeId}
                onChange={(e) => {
                  setFormEquipmentTypeId(e.target.value);
                  setFormAssignmentId('');
                }}
              >
                <option value="">Select Equipment Type</option>
                {equipmentTypes.map(eq => (
                  <option key={eq.id} value={eq.id}>{eq.name} ({eq.code})</option>
                ))}
              </select>
            </div>

            {/* Linked Active Personnel Assignment (Optional) */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="expend-assign">Linked Personnel Assignment (Optional)</label>
              <select
                id="expend-assign"
                className="form-select"
                value={formAssignmentId}
                onChange={(e) => setFormAssignmentId(e.target.value)}
                disabled={!formBaseId || !formEquipmentTypeId}
              >
                <option value="">General Base Stock (Unlinked)</option>
                {activeAssignments.map(a => (
                  <option key={a.id} value={a.id}>
                    {a.assignmentReference} - {a.personnelName} ({a.personnelId}) [Qty: {a.assignedQuantity}]
                  </option>
                ))}
              </select>
            </div>

            {/* Expended Quantity */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="expend-qty">Expended Quantity *</label>
              <input
                id="expend-qty"
                type="number"
                min="1"
                className="form-input"
                placeholder="Quantity"
                value={formExpendedQuantity}
                onChange={(e) => setFormExpendedQuantity(e.target.value)}
              />
            </div>

            {/* Expended Date */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="expend-date">Expended Date & Time *</label>
              <input
                id="expend-date"
                type="datetime-local"
                className="form-input"
                value={formExpendedDate}
                onChange={(e) => setFormExpendedDate(e.target.value)}
              />
            </div>

            {/* Reason */}
            <div className="form-group" style={{ margin: 0, gridColumn: 'span 2' }}>
              <label className="form-label" htmlFor="expend-reason">Reason for Expenditure / Incident Report *</label>
              <input
                id="expend-reason"
                type="text"
                className="form-input"
                placeholder="e.g. Expended during field exercise / Equipment decommissioned"
                value={formReason}
                onChange={(e) => setFormReason(e.target.value)}
              />
            </div>

          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={submitting}
            >
              {submitting ? 'Recording Expenditure...' : 'Record Expended Asset'}
            </button>
          </div>
        </form>
      </div>

      {/* Historical Expenditure Records Table */}
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.75rem', marginBottom: '1rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Flame size={18} color="var(--text-muted)" />
            <h2 style={{ fontSize: '1rem', fontWeight: 600, color: 'var(--text-main)', margin: 0 }}>Expenditure Logs</h2>
          </div>

          <div className="filter-inputs">
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', backgroundColor: '#070b10', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)', padding: '0.35rem 0.65rem' }}>
              <Search size={15} color="var(--text-muted)" />
              <input
                type="text"
                placeholder="Search ref, reason, equipment..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                style={{ background: 'none', border: 'none', color: 'var(--text-main)', fontSize: '0.8125rem', outline: 'none', width: '200px' }}
              />
            </div>

            {isAdmin && (
              <select
                className="form-select"
                style={{ padding: '0.35rem 0.6rem', fontSize: '0.8125rem' }}
                value={filterBaseId}
                onChange={(e) => setFilterBaseId(e.target.value)}
              >
                <option value="">All Bases</option>
                {bases.map(b => (
                  <option key={b.id} value={b.id}>{b.name}</option>
                ))}
              </select>
            )}

            <select
              className="form-select"
              style={{ padding: '0.35rem 0.6rem', fontSize: '0.8125rem' }}
              value={filterEquipmentTypeId}
              onChange={(e) => setFilterEquipmentTypeId(e.target.value)}
            >
              <option value="">All Equipment</option>
              {equipmentTypes.map(eq => (
                <option key={eq.id} value={eq.id}>{eq.name}</option>
              ))}
            </select>
          </div>
        </div>

        <Alert type="error" message={tableError} onClose={() => setTableError(null)} />

        {loadingExpenditures ? (
          <LoadingSpinner message="Fetching expenditure audit records..." />
        ) : filteredExpenditures.length > 0 ? (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Reference</th>
                  <th>Date & Time</th>
                  <th>Base</th>
                  <th>Equipment Category</th>
                  <th>Expended Qty</th>
                  <th>Reason / Cause</th>
                  <th>Linked Assignment Ref</th>
                  <th>Recorded By</th>
                </tr>
              </thead>
              <tbody>
                {filteredExpenditures.map(ex => (
                  <tr key={ex.id}>
                    <td style={{ fontFamily: 'monospace', fontWeight: 600, fontSize: '0.8125rem', color: 'var(--low-text)' }}>
                      {ex.expenditureReference}
                    </td>
                    <td style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>
                      {formatDateIN(ex.expendedDate)}
                    </td>
                    <td style={{ fontWeight: 500 }}>{ex.baseName} ({ex.baseCode})</td>
                    <td>{ex.equipmentName} <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>({ex.equipmentCode})</span></td>
                    <td style={{ fontWeight: 700, color: 'var(--low-text)' }}>{ex.expendedQuantity} {ex.unitOfMeasure}</td>
                    <td style={{ fontSize: '0.8125rem', color: 'var(--text-main)' }}>{ex.reason}</td>
                    <td style={{ fontSize: '0.8125rem' }}>
                      {ex.assignmentReference ? (
                        <span style={{ fontFamily: 'monospace', color: '#c4b5fd', display: 'inline-flex', alignItems: 'center', gap: '0.25rem' }}>
                          <Link2 size={12} /> {ex.assignmentReference}
                        </span>
                      ) : (
                        <span style={{ color: 'var(--text-dim)' }}>Unlinked Stock</span>
                      )}
                    </td>
                    <td style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>{ex.recordedByUsername || '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state">
            <Flame size={36} color="var(--text-dim)" style={{ marginBottom: '0.5rem' }} />
            <h3>No Expenditure Records Found</h3>
            <p>No expenditure audit entries match your active search or filter criteria.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default ExpendituresPage;
