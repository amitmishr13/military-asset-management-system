import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { getBases, getEquipmentTypes } from '../services/referenceService';
import { createAssignment, getAssignments } from '../services/assignmentService';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { Alert } from '../components/Alert';
import { formatDateIN } from '../utils/formatters';
import { exportToCSV } from '../utils/csvExport';
import { UserCheck, UserPlus, RefreshCw, Search, Download } from 'lucide-react';

export const AssignmentsPage = () => {
  const { user, isAdmin, isCommander } = useAuth();

  // Reference Data State
  const [bases, setBases] = useState([]);
  const [equipmentTypes, setEquipmentTypes] = useState([]);

  // Form State
  const [formBaseId, setFormBaseId] = useState('');
  const [formEquipmentTypeId, setFormEquipmentTypeId] = useState('');
  const [formPersonnelName, setFormPersonnelName] = useState('');
  const [formPersonnelRank, setFormPersonnelRank] = useState('');
  const [formPersonnelId, setFormPersonnelId] = useState('');
  const [formAssignedQuantity, setFormAssignedQuantity] = useState('1');
  const [formAssignedDate, setFormAssignedDate] = useState(() => {
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
  const [filterStatus, setFilterStatus] = useState('');
  const [searchTerm, setSearchTerm] = useState('');

  // Table Data State
  const [assignments, setAssignments] = useState([]);
  const [loadingAssignments, setLoadingAssignments] = useState(true);
  const [tableError, setTableError] = useState(null);

  // Load Reference Data
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

  // Fetch Assignments List
  const fetchAssignments = useCallback(async () => {
    setLoadingAssignments(true);
    setTableError(null);
    try {
      const params = {};
      if (filterBaseId) params.baseId = filterBaseId;
      if (filterEquipmentTypeId) params.equipmentTypeId = filterEquipmentTypeId;
      if (filterStatus) params.status = filterStatus;

      const response = await getAssignments(params);
      if (response.success) {
        setAssignments(response.data || []);
      } else {
        setTableError(response.message || 'Failed to fetch assignments');
      }
    } catch (err) {
      setTableError(err.response?.data?.message || 'Server error loading personnel assignments');
    } finally {
      setLoadingAssignments(false);
    }
  }, [filterBaseId, filterEquipmentTypeId, filterStatus]);

  useEffect(() => {
    fetchAssignments();
  }, [fetchAssignments]);

  // Client-side search filter
  const filteredAssignments = assignments.filter(a => {
    if (!searchTerm) return true;
    const term = searchTerm.toLowerCase();
    return (
      a.assignmentReference?.toLowerCase().includes(term) ||
      a.personnelName?.toLowerCase().includes(term) ||
      a.personnelId?.toLowerCase().includes(term) ||
      a.personnelRank?.toLowerCase().includes(term) ||
      a.equipmentName?.toLowerCase().includes(term) ||
      a.baseName?.toLowerCase().includes(term)
    );
  });

  // Submit Assignment Form
  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError(null);
    setFormSuccess(null);

    const baseIdNum = Number(formBaseId);
    const equipIdNum = Number(formEquipmentTypeId);
    const qtyNum = parseInt(formAssignedQuantity, 10);
    const pName = formPersonnelName.trim();
    const pId = formPersonnelId.trim();

    if (!baseIdNum) {
      setFormError('Please select a military base');
      return;
    }
    if (!equipIdNum) {
      setFormError('Please select an equipment category');
      return;
    }
    if (!pName) {
      setFormError('Personnel full name is required');
      return;
    }
    if (!pId) {
      setFormError('Personnel Service ID / Service Number is required');
      return;
    }
    if (isNaN(qtyNum) || qtyNum < 1) {
      setFormError('Assigned quantity must be a positive integer (minimum 1)');
      return;
    }
    if (!formAssignedDate) {
      setFormError('Assigned date and time are required');
      return;
    }

    setSubmitting(true);

    try {
      const payload = {
        baseId: baseIdNum,
        equipmentTypeId: equipIdNum,
        personnelName: pName,
        personnelRank: formPersonnelRank.trim() || null,
        personnelId: pId,
        assignedQuantity: qtyNum,
        assignedDate: new Date(formAssignedDate).toISOString()
      };

      const response = await createAssignment(payload);
      setSubmitting(false);

      if (response.success && response.data) {
        setFormSuccess(`Asset assigned successfully! Reference: ${response.data.assignmentReference}`);
        setFormPersonnelName('');
        setFormPersonnelRank('');
        setFormPersonnelId('');
        setFormAssignedQuantity('1');
        fetchAssignments();
      } else {
        setFormError(response.message || 'Failed to assign asset');
      }
    } catch (err) {
      setSubmitting(false);
      setFormError(err.response?.data?.message || 'Error executing assignment transaction on server');
    }
  };

  const handleExportCSV = () => {
    const columns = [
      { key: 'assignmentReference', label: 'Reference' },
      { key: 'assignedDate', label: 'Assigned Date' },
      { key: 'personnelRank', label: 'Rank' },
      { key: 'personnelName', label: 'Personnel Name' },
      { key: 'personnelId', label: 'Service ID' },
      { key: 'baseName', label: 'Base' },
      { key: 'equipmentName', label: 'Equipment' },
      { key: 'assignedQuantity', label: 'Quantity' },
      { key: 'status', label: 'Status' },
      { key: 'assignedByUsername', label: 'Assigned By' }
    ];

    const exportData = filteredAssignments.map(a => ({
      ...a,
      assignedDate: formatDateIN(a.assignedDate)
    }));

    exportToCSV(exportData, columns, 'personnel_assignments');
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
            <UserCheck size={24} color="var(--accent-military)" />
            <h1 style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--text-main)', margin: 0 }}>Personnel Asset Assignments</h1>
          </div>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>
            Assign assets to active personnel by rank and Service ID, and manage active deployment status.
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <button className="btn btn-export" onClick={handleExportCSV} disabled={!filteredAssignments.length}>
            <Download size={15} />
            Export CSV
          </button>
          <button className="btn btn-secondary" onClick={fetchAssignments} disabled={loadingAssignments}>
            <RefreshCw size={15} className={loadingAssignments ? 'spin' : ''} />
            Refresh
          </button>
        </div>
      </div>

      {/* Assign Asset Form Card */}
      <div className="card">
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
          <UserPlus size={18} color="var(--accent-military)" />
          <h2 style={{ fontSize: '1rem', fontWeight: 600, color: 'var(--text-main)', margin: 0 }}>Assign Asset to Personnel</h2>
        </div>

        <Alert type="error" message={formError} onClose={() => setFormError(null)} />
        <Alert type="success" message={formSuccess} onClose={() => setFormSuccess(null)} />

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem', marginBottom: '1rem' }}>

            {/* Base Selection */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="assign-base">Assigned Base *</label>
              <select
                id="assign-base"
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

            {/* Equipment Type */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="assign-equip">Equipment Category *</label>
              <select
                id="assign-equip"
                className="form-select"
                value={formEquipmentTypeId}
                onChange={(e) => setFormEquipmentTypeId(e.target.value)}
              >
                <option value="">Select Equipment Type</option>
                {equipmentTypes.map(eq => (
                  <option key={eq.id} value={eq.id}>{eq.name} ({eq.code})</option>
                ))}
              </select>
            </div>

            {/* Personnel Name */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="assign-name">Personnel Full Name *</label>
              <input
                id="assign-name"
                type="text"
                className="form-input"
                placeholder="e.g. Sgt. Rajesh Kumar"
                value={formPersonnelName}
                onChange={(e) => setFormPersonnelName(e.target.value)}
              />
            </div>

            {/* Personnel Rank */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="assign-rank">Military Rank</label>
              <input
                id="assign-rank"
                type="text"
                className="form-input"
                placeholder="e.g. Captain, Sergeant, Lance Naik"
                value={formPersonnelRank}
                onChange={(e) => setFormPersonnelRank(e.target.value)}
              />
            </div>

            {/* Service ID */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="assign-pid">Service ID / Personnel Number *</label>
              <input
                id="assign-pid"
                type="text"
                className="form-input"
                placeholder="e.g. IC-90241"
                value={formPersonnelId}
                onChange={(e) => setFormPersonnelId(e.target.value)}
              />
            </div>

            {/* Quantity */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="assign-qty">Assigned Quantity *</label>
              <input
                id="assign-qty"
                type="number"
                min="1"
                className="form-input"
                placeholder="Quantity"
                value={formAssignedQuantity}
                onChange={(e) => setFormAssignedQuantity(e.target.value)}
              />
            </div>

            {/* Assigned Date */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="assign-date">Assigned Date & Time *</label>
              <input
                id="assign-date"
                type="datetime-local"
                className="form-input"
                value={formAssignedDate}
                onChange={(e) => setFormAssignedDate(e.target.value)}
              />
            </div>

          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={submitting}
            >
              {submitting ? 'Assigning Asset...' : 'Confirm Personnel Assignment'}
            </button>
          </div>
        </form>
      </div>

      {/* Personnel Assignment Registry Table */}
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.75rem', marginBottom: '1rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <UserCheck size={18} color="var(--text-muted)" />
            <h2 style={{ fontSize: '1rem', fontWeight: 600, color: 'var(--text-main)', margin: 0 }}>Assignment Registry</h2>
          </div>

          <div className="filter-inputs">
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', backgroundColor: '#070b10', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)', padding: '0.35rem 0.65rem' }}>
              <Search size={15} color="var(--text-muted)" />
              <input
                type="text"
                placeholder="Search name, Service ID, equipment..."
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

            <select
              className="form-select"
              style={{ padding: '0.35rem 0.6rem', fontSize: '0.8125rem' }}
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
            >
              <option value="">All Statuses</option>
              <option value="ACTIVE">ACTIVE</option>
              <option value="RETURNED">RETURNED</option>
              <option value="EXPENDED">EXPENDED</option>
            </select>
          </div>
        </div>

        <Alert type="error" message={tableError} onClose={() => setTableError(null)} />

        {loadingAssignments ? (
          <LoadingSpinner message="Fetching assignment registry..." />
        ) : filteredAssignments.length > 0 ? (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Reference</th>
                  <th>Assigned Personnel</th>
                  <th>Service ID</th>
                  <th>Base</th>
                  <th>Equipment Category</th>
                  <th>Assigned Qty</th>
                  <th>Status</th>
                  <th>Assigned Date</th>
                  <th>Returned Date</th>
                  <th>Assigned By</th>
                </tr>
              </thead>
              <tbody>
                {filteredAssignments.map(a => (
                  <tr key={a.id}>
                    <td style={{ fontFamily: 'monospace', fontWeight: 600, fontSize: '0.8125rem', color: '#86efac' }}>
                      {a.assignmentReference}
                    </td>
                    <td style={{ fontWeight: 600, color: 'var(--text-main)' }}>
                      {a.personnelRank ? `${a.personnelRank} ${a.personnelName}` : a.personnelName}
                    </td>
                    <td style={{ fontFamily: 'monospace', fontSize: '0.8125rem', color: 'var(--text-muted)' }}>
                      {a.personnelId}
                    </td>
                    <td style={{ fontWeight: 500 }}>{a.baseName} ({a.baseCode})</td>
                    <td>{a.equipmentName} <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>({a.equipmentCode})</span></td>
                    <td style={{ fontWeight: 700 }}>{a.assignedQuantity} {a.unitOfMeasure}</td>
                    <td>
                      {a.status === 'ACTIVE' && (
                        <span className="badge badge-commander">ACTIVE</span>
                      )}
                      {a.status === 'RETURNED' && (
                        <span className="badge badge-healthy">RETURNED</span>
                      )}
                      {a.status === 'EXPENDED' && (
                        <span className="badge badge-low">EXPENDED</span>
                      )}
                    </td>
                    <td style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>
                      {formatDateIN(a.assignedDate)}
                    </td>
                    <td style={{ fontSize: '0.8125rem', color: 'var(--text-dim)' }}>
                      {formatDateIN(a.returnedDate)}
                    </td>
                    <td style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>{a.assignedByUsername || '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state">
            <UserCheck size={36} color="var(--text-dim)" style={{ marginBottom: '0.5rem' }} />
            <h3>No Assignment Records Found</h3>
            <p>No assignment entries match your active search or filter selection.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default AssignmentsPage;
