import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { getBases, getEquipmentTypes } from '../services/referenceService';
import { createTransfer, getTransfers } from '../services/transferService';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { Alert } from '../components/Alert';
import { formatDateIN } from '../utils/formatters';
import { exportToCSV } from '../utils/csvExport';
import { ArrowLeftRight, Send, RefreshCw, ArrowRight, Search, Download, CheckCircle2 } from 'lucide-react';

export const TransfersPage = () => {
  const { user, isAdmin, isCommander } = useAuth();

  // Reference Data State
  const [bases, setBases] = useState([]);
  const [equipmentTypes, setEquipmentTypes] = useState([]);

  // Form State
  const [formSourceBaseId, setFormSourceBaseId] = useState('');
  const [formDestinationBaseId, setFormDestinationBaseId] = useState('');
  const [formEquipmentTypeId, setFormEquipmentTypeId] = useState('');
  const [formQuantity, setFormQuantity] = useState('1');
  const [formTransferDate, setFormTransferDate] = useState(() => {
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    return now.toISOString().slice(0, 16);
  });
  const [formRemarks, setFormRemarks] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState(null);
  const [formSuccess, setFormSuccess] = useState(null);

  // Table Filters & Search State
  const [filterBaseId, setFilterBaseId] = useState('');
  const [filterEquipmentTypeId, setFilterEquipmentTypeId] = useState('');
  const [searchTerm, setSearchTerm] = useState('');

  // Table Data State
  const [transfers, setTransfers] = useState([]);
  const [loadingTransfers, setLoadingTransfers] = useState(true);
  const [tableError, setTableError] = useState(null);

  // Selected names for confirmation card
  const sourceBaseObj = bases.find(b => String(b.id) === String(formSourceBaseId));
  const destBaseObj = bases.find(b => String(b.id) === String(formDestinationBaseId));
  const equipObj = equipmentTypes.find(e => String(e.id) === String(formEquipmentTypeId));

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
      setFormSourceBaseId(user.baseId);
      setFilterBaseId(user.baseId);
    } else if (bases.length > 0 && !formSourceBaseId) {
      setFormSourceBaseId(bases[0].id);
    }
  }, [user, bases, formSourceBaseId]);

  // Fetch Transfers List
  const fetchTransfers = useCallback(async () => {
    setLoadingTransfers(true);
    setTableError(null);
    try {
      const params = {};
      if (filterBaseId) params.baseId = filterBaseId;
      if (filterEquipmentTypeId) params.equipmentTypeId = filterEquipmentTypeId;

      const response = await getTransfers(params);
      if (response.success) {
        setTransfers(response.data || []);
      } else {
        setTableError(response.message || 'Failed to fetch transfers');
      }
    } catch (err) {
      setTableError(err.response?.data?.message || 'Server error loading transfer history');
    } finally {
      setLoadingTransfers(false);
    }
  }, [filterBaseId, filterEquipmentTypeId]);

  useEffect(() => {
    fetchTransfers();
  }, [fetchTransfers]);

  // Client-side search filter
  const filteredTransfers = transfers.filter(t => {
    if (!searchTerm) return true;
    const term = searchTerm.toLowerCase();
    return (
      t.transferReference?.toLowerCase().includes(term) ||
      t.sourceBaseName?.toLowerCase().includes(term) ||
      t.destinationBaseName?.toLowerCase().includes(term) ||
      t.equipmentName?.toLowerCase().includes(term) ||
      t.initiatedByUsername?.toLowerCase().includes(term) ||
      t.remarks?.toLowerCase().includes(term)
    );
  });

  // Submit Transfer Form
  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError(null);
    setFormSuccess(null);

    const sourceId = Number(formSourceBaseId);
    const destId = Number(formDestinationBaseId);
    const equipId = Number(formEquipmentTypeId);
    const qty = parseInt(formQuantity, 10);

    if (!sourceId) {
      setFormError('Please select a source military base');
      return;
    }
    if (!destId) {
      setFormError('Please select a destination military base');
      return;
    }
    if (sourceId === destId) {
      setFormError('Source base and destination base cannot be the same base');
      return;
    }
    if (!equipId) {
      setFormError('Please select an equipment category');
      return;
    }
    if (isNaN(qty) || qty < 1) {
      setFormError('Transfer quantity must be a positive integer (minimum 1)');
      return;
    }
    if (!formTransferDate) {
      setFormError('Transfer date and time are required');
      return;
    }

    setSubmitting(true);

    try {
      const payload = {
        sourceBaseId: sourceId,
        destinationBaseId: destId,
        equipmentTypeId: equipId,
        quantity: qty,
        transferDate: new Date(formTransferDate).toISOString(),
        remarks: formRemarks.trim() || null
      };

      const response = await createTransfer(payload);
      setSubmitting(false);

      if (response.success && response.data) {
        setFormSuccess(`Inter-base transfer completed! Reference: ${response.data.transferReference}`);
        setFormDestinationBaseId('');
        setFormQuantity('1');
        setFormRemarks('');
        fetchTransfers();
      } else {
        setFormError(response.message || 'Failed to initiate transfer');
      }
    } catch (err) {
      setSubmitting(false);
      setFormError(err.response?.data?.message || 'Error executing transfer transaction on server');
    }
  };

  const handleExportCSV = () => {
    const columns = [
      { key: 'transferReference', label: 'Reference' },
      { key: 'transferDate', label: 'Transfer Date' },
      { key: 'sourceBaseName', label: 'Source Base' },
      { key: 'destinationBaseName', label: 'Destination Base' },
      { key: 'equipmentName', label: 'Equipment' },
      { key: 'quantity', label: 'Quantity' },
      { key: 'initiatedByUsername', label: 'Initiated By' },
      { key: 'remarks', label: 'Remarks' }
    ];

    const exportData = filteredTransfers.map(t => ({
      ...t,
      transferDate: formatDateIN(t.transferDate)
    }));

    exportToCSV(exportData, columns, 'transfers_log');
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
            <ArrowLeftRight size={24} color="var(--accent-military)" />
            <h1 style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--text-main)', margin: 0 }}>Inter-Base Asset Transfers</h1>
          </div>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>
            Initiate atomic inter-base asset relocations and inspect transfer accounting logs.
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <button className="btn btn-export" onClick={handleExportCSV} disabled={!filteredTransfers.length}>
            <Download size={15} />
            Export CSV
          </button>
          <button className="btn btn-secondary" onClick={fetchTransfers} disabled={loadingTransfers}>
            <RefreshCw size={15} className={loadingTransfers ? 'spin' : ''} />
            Refresh
          </button>
        </div>
      </div>

      {/* Transfer Form Card */}
      <div className="card">
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
          <Send size={18} color="var(--accent-military)" />
          <h2 style={{ fontSize: '1rem', fontWeight: 600, color: 'var(--text-main)', margin: 0 }}>Initiate Inter-Base Transfer</h2>
        </div>

        <Alert type="error" message={formError} onClose={() => setFormError(null)} />
        <Alert type="success" message={formSuccess} onClose={() => setFormSuccess(null)} />

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem', marginBottom: '1rem' }}>

            {/* Source Base */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="transfer-source">Source Base (Transfer Out) *</label>
              <select
                id="transfer-source"
                className="form-select"
                value={formSourceBaseId}
                onChange={(e) => setFormSourceBaseId(e.target.value)}
                disabled={isCommander}
              >
                <option value="">Select Source Base</option>
                {bases.map(b => (
                  <option key={b.id} value={b.id}>{b.name} ({b.code})</option>
                ))}
              </select>
            </div>

            {/* Destination Base */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="transfer-dest">Destination Base (Transfer In) *</label>
              <select
                id="transfer-dest"
                className="form-select"
                value={formDestinationBaseId}
                onChange={(e) => setFormDestinationBaseId(e.target.value)}
              >
                <option value="">Select Destination Base</option>
                {bases
                  .filter(b => String(b.id) !== String(formSourceBaseId))
                  .map(b => (
                    <option key={b.id} value={b.id}>{b.name} ({b.code})</option>
                  ))}
              </select>
            </div>

            {/* Equipment Type */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="transfer-equip">Equipment Category *</label>
              <select
                id="transfer-equip"
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

            {/* Quantity */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="transfer-qty">Transfer Quantity *</label>
              <input
                id="transfer-qty"
                type="number"
                min="1"
                className="form-input"
                placeholder="Quantity"
                value={formQuantity}
                onChange={(e) => setFormQuantity(e.target.value)}
              />
            </div>

            {/* Transfer Date */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="transfer-date">Transfer Date & Time *</label>
              <input
                id="transfer-date"
                type="datetime-local"
                className="form-input"
                value={formTransferDate}
                onChange={(e) => setFormTransferDate(e.target.value)}
              />
            </div>

            {/* Remarks */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="transfer-remarks">Transfer Remarks / Order Ref</label>
              <input
                id="transfer-remarks"
                type="text"
                className="form-input"
                placeholder="e.g. Operational reallocation order"
                value={formRemarks}
                onChange={(e) => setFormRemarks(e.target.value)}
              />
            </div>

          </div>

          {/* Pre-Submission Confirmation Summary Box */}
          {sourceBaseObj && destBaseObj && equipObj && Number(formQuantity) > 0 && (
            <div className="confirmation-card">
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#86efac', fontWeight: 600, fontSize: '0.85rem', marginBottom: '0.5rem' }}>
                <CheckCircle2 size={16} />
                <span>Pre-Submission Reallocation Summary</span>
              </div>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1.5rem', fontSize: '0.875rem' }}>
                <div>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem', textTransform: 'uppercase', display: 'block' }}>Relocating Equipment</span>
                  <strong>{equipObj.name} ({equipObj.code})</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem', textTransform: 'uppercase', display: 'block' }}>Movement Vector</span>
                  <span style={{ color: 'var(--critical-text)', fontWeight: 600 }}>{sourceBaseObj.name}</span>
                  <ArrowRight size={13} style={{ margin: '0 0.35rem', verticalAlign: 'middle' }} />
                  <span style={{ color: 'var(--healthy-text)', fontWeight: 600 }}>{destBaseObj.name}</span>
                </div>
                <div>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem', textTransform: 'uppercase', display: 'block' }}>Transfer Quantity</span>
                  <strong style={{ color: 'var(--text-main)' }}>{formQuantity} {equipObj.unitOfMeasure}</strong>
                </div>
              </div>
            </div>
          )}

          <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '1rem' }}>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={submitting}
            >
              {submitting ? 'Processing Transfer...' : 'Initiate Inter-Base Transfer'}
            </button>
          </div>
        </form>
      </div>

      {/* Historical Transfer Log Table */}
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.75rem', marginBottom: '1rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <ArrowLeftRight size={18} color="var(--text-muted)" />
            <h2 style={{ fontSize: '1rem', fontWeight: 600, color: 'var(--text-main)', margin: 0 }}>Transfer Accounting Logs</h2>
          </div>

          <div className="filter-inputs">
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', backgroundColor: '#070b10', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)', padding: '0.35rem 0.65rem' }}>
              <Search size={15} color="var(--text-muted)" />
              <input
                type="text"
                placeholder="Search ref, base, equipment..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                style={{ background: 'none', border: 'none', color: 'var(--text-main)', fontSize: '0.8125rem', outline: 'none', width: '190px' }}
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

        {loadingTransfers ? (
          <LoadingSpinner message="Fetching transfer logs..." />
        ) : filteredTransfers.length > 0 ? (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Reference</th>
                  <th>Date & Time</th>
                  <th>Source Base (Out)</th>
                  <th></th>
                  <th>Destination Base (In)</th>
                  <th>Equipment Category</th>
                  <th>Quantity</th>
                  <th>Initiated By</th>
                  <th>Remarks</th>
                </tr>
              </thead>
              <tbody>
                {filteredTransfers.map(t => (
                  <tr key={t.id}>
                    <td style={{ fontFamily: 'monospace', fontWeight: 600, fontSize: '0.8125rem', color: '#86efac' }}>
                      {t.transferReference}
                    </td>
                    <td style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>
                      {formatDateIN(t.transferDate)}
                    </td>
                    <td style={{ fontWeight: 500, color: 'var(--critical-text)' }}>
                      {t.sourceBaseName} <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>({t.sourceBaseCode})</span>
                    </td>
                    <td style={{ textAlign: 'center' }}>
                      <ArrowRight size={14} color="var(--text-dim)" />
                    </td>
                    <td style={{ fontWeight: 500, color: 'var(--healthy-text)' }}>
                      {t.destinationBaseName} <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>({t.destinationBaseCode})</span>
                    </td>
                    <td>{t.equipmentName} <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>({t.equipmentCode})</span></td>
                    <td style={{ fontWeight: 700 }}>{t.quantity} {t.unitOfMeasure}</td>
                    <td style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>{t.initiatedByUsername || '-'}</td>
                    <td style={{ fontSize: '0.8125rem', color: 'var(--text-dim)' }}>{t.remarks || '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state">
            <ArrowLeftRight size={36} color="var(--text-dim)" style={{ marginBottom: '0.5rem' }} />
            <h3>No Transfer Logs Found</h3>
            <p>No transfer records match your active search or filter criteria.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default TransfersPage;
