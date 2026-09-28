import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { getBases, getEquipmentTypes } from '../services/referenceService';
import { createPurchase, getPurchases } from '../services/purchaseService';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { Alert } from '../components/Alert';
import { formatINR, formatDateIN } from '../utils/formatters';
import { exportToCSV } from '../utils/csvExport';
import { ShoppingCart, PlusCircle, RefreshCw, Search, Download } from 'lucide-react';

export const PurchasesPage = () => {
  const { user, isAdmin } = useAuth();

  // Reference Data State
  const [bases, setBases] = useState([]);
  const [equipmentTypes, setEquipmentTypes] = useState([]);

  // Form State
  const [formBaseId, setFormBaseId] = useState('');
  const [formEquipmentTypeId, setFormEquipmentTypeId] = useState('');
  const [formQuantity, setFormQuantity] = useState('1');
  const [formUnitCost, setFormUnitCost] = useState('0.00');
  const [formPurchaseDate, setFormPurchaseDate] = useState(() => {
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    return now.toISOString().slice(0, 16);
  });
  const [formSupplierDetails, setFormSupplierDetails] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState(null);
  const [formSuccess, setFormSuccess] = useState(null);

  // Table Filters & Search State
  const [filterBaseId, setFilterBaseId] = useState('');
  const [filterEquipmentTypeId, setFilterEquipmentTypeId] = useState('');
  const [searchTerm, setSearchTerm] = useState('');

  // Table Data State
  const [purchases, setPurchases] = useState([]);
  const [loadingPurchases, setLoadingPurchases] = useState(true);
  const [tableError, setTableError] = useState(null);

  // Dynamic Total Cost Calculation
  const calculatedTotalCost = (Number(formQuantity) || 0) * (Number(formUnitCost) || 0);

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

  // Set default base for form & filter if user has baseId
  useEffect(() => {
    if (user?.baseId) {
      setFormBaseId(user.baseId);
      setFilterBaseId(user.baseId);
    } else if (bases.length > 0 && !formBaseId) {
      setFormBaseId(bases[0].id);
    }
  }, [user, bases, formBaseId]);

  // Fetch Purchases List
  const fetchPurchases = useCallback(async () => {
    setLoadingPurchases(true);
    setTableError(null);
    try {
      const params = {};
      if (filterBaseId) params.baseId = filterBaseId;
      if (filterEquipmentTypeId) params.equipmentTypeId = filterEquipmentTypeId;

      const response = await getPurchases(params);
      if (response.success) {
        setPurchases(response.data || []);
      } else {
        setTableError(response.message || 'Failed to fetch purchases');
      }
    } catch (err) {
      setTableError(err.response?.data?.message || 'Server error loading purchase history');
    } finally {
      setLoadingPurchases(false);
    }
  }, [filterBaseId, filterEquipmentTypeId]);

  useEffect(() => {
    fetchPurchases();
  }, [fetchPurchases]);

  // Filter purchases client-side by search term
  const filteredPurchases = purchases.filter(p => {
    if (!searchTerm) return true;
    const term = searchTerm.toLowerCase();
    return (
      p.purchaseReference?.toLowerCase().includes(term) ||
      p.equipmentName?.toLowerCase().includes(term) ||
      p.baseName?.toLowerCase().includes(term) ||
      p.supplierDetails?.toLowerCase().includes(term) ||
      p.recordedByUsername?.toLowerCase().includes(term)
    );
  });

  // Submit Purchase Form
  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError(null);
    setFormSuccess(null);

    const baseIdNum = Number(formBaseId);
    const equipIdNum = Number(formEquipmentTypeId);
    const qtyNum = parseInt(formQuantity, 10);
    const costNum = parseFloat(formUnitCost);

    if (!baseIdNum) {
      setFormError('Please select a military base');
      return;
    }
    if (!equipIdNum) {
      setFormError('Please select an equipment category');
      return;
    }
    if (isNaN(qtyNum) || qtyNum < 1) {
      setFormError('Quantity must be a positive integer (minimum 1)');
      return;
    }
    if (isNaN(costNum) || costNum < 0) {
      setFormError('Unit cost must be a non-negative number');
      return;
    }
    if (!formPurchaseDate) {
      setFormError('Purchase date and time are required');
      return;
    }

    setSubmitting(true);

    try {
      const payload = {
        baseId: baseIdNum,
        equipmentTypeId: equipIdNum,
        quantity: qtyNum,
        unitCost: costNum,
        purchaseDate: new Date(formPurchaseDate).toISOString(),
        supplierDetails: formSupplierDetails.trim() || null
      };

      const response = await createPurchase(payload);
      setSubmitting(false);

      if (response.success && response.data) {
        setFormSuccess(`Procurement recorded successfully! Reference: ${response.data.purchaseReference}`);
        setFormQuantity('1');
        setFormUnitCost('0.00');
        setFormSupplierDetails('');
        fetchPurchases();
      } else {
        setFormError(response.message || 'Failed to record purchase');
      }
    } catch (err) {
      setSubmitting(false);
      setFormError(err.response?.data?.message || 'Error executing purchase transaction on server');
    }
  };

  const handleExportCSV = () => {
    const columns = [
      { key: 'purchaseReference', label: 'Reference' },
      { key: 'purchaseDate', label: 'Purchase Date' },
      { key: 'baseName', label: 'Base Name' },
      { key: 'equipmentName', label: 'Equipment' },
      { key: 'quantity', label: 'Quantity' },
      { key: 'unitCost', label: 'Unit Cost (INR)' },
      { key: 'totalCost', label: 'Total Cost (INR)' },
      { key: 'supplierDetails', label: 'Supplier' },
      { key: 'recordedByUsername', label: 'Recorded By' }
    ];

    const exportData = filteredPurchases.map(p => ({
      ...p,
      purchaseDate: formatDateIN(p.purchaseDate),
      unitCost: formatINR(p.unitCost),
      totalCost: formatINR((p.unitCost || 0) * (p.quantity || 0))
    }));

    exportToCSV(exportData, columns, 'purchases_log');
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
            <ShoppingCart size={24} color="var(--accent-military)" />
            <h1 style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--text-main)', margin: 0 }}>Procurement & Purchases</h1>
          </div>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>
            Record asset procurements, track supplier contracts, and maintain transaction logs.
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <button className="btn btn-export" onClick={handleExportCSV} disabled={!filteredPurchases.length}>
            <Download size={15} />
            Export CSV
          </button>
          <button className="btn btn-secondary" onClick={fetchPurchases} disabled={loadingPurchases}>
            <RefreshCw size={15} className={loadingPurchases ? 'spin' : ''} />
            Refresh
          </button>
        </div>
      </div>

      {/* Record Purchase Form Card */}
      <div className="card">
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
          <PlusCircle size={18} color="var(--accent-military)" />
          <h2 style={{ fontSize: '1rem', fontWeight: 600, color: 'var(--text-main)', margin: 0 }}>Record Asset Procurement</h2>
        </div>

        <Alert type="error" message={formError} onClose={() => setFormError(null)} />
        <Alert type="success" message={formSuccess} onClose={() => setFormSuccess(null)} />

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem', marginBottom: '1rem' }}>

            {/* Base Selection */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="purchase-base">Military Base *</label>
              <select
                id="purchase-base"
                className="form-select"
                value={formBaseId}
                onChange={(e) => setFormBaseId(e.target.value)}
                disabled={!isAdmin && Boolean(user?.baseId)}
              >
                <option value="">Select Base</option>
                {bases.map(b => (
                  <option key={b.id} value={b.id}>{b.name} ({b.code})</option>
                ))}
              </select>
            </div>

            {/* Equipment Category Selection */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="purchase-equipment">Equipment Category *</label>
              <select
                id="purchase-equipment"
                className="form-select"
                value={formEquipmentTypeId}
                onChange={(e) => setFormEquipmentTypeId(e.target.value)}
              >
                <option value="">Select Equipment Type</option>
                {equipmentTypes.map(eq => (
                  <option key={eq.id} value={eq.id}>{eq.name} ({eq.code}) - {eq.unitOfMeasure}</option>
                ))}
              </select>
            </div>

            {/* Quantity */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="purchase-qty">Quantity *</label>
              <input
                id="purchase-qty"
                type="number"
                min="1"
                className="form-input"
                placeholder="Quantity"
                value={formQuantity}
                onChange={(e) => setFormQuantity(e.target.value)}
              />
            </div>

            {/* Unit Cost in INR */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="purchase-cost">Unit Cost (₹ INR)</label>
              <input
                id="purchase-cost"
                type="number"
                step="0.01"
                min="0"
                className="form-input"
                placeholder="0.00"
                value={formUnitCost}
                onChange={(e) => setFormUnitCost(e.target.value)}
              />
            </div>

            {/* Purchase Date */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="purchase-date">Purchase Date & Time *</label>
              <input
                id="purchase-date"
                type="datetime-local"
                className="form-input"
                value={formPurchaseDate}
                onChange={(e) => setFormPurchaseDate(e.target.value)}
              />
            </div>

            {/* Supplier Details */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label" htmlFor="purchase-supplier">Supplier / Vendor Details</label>
              <input
                id="purchase-supplier"
                type="text"
                className="form-input"
                placeholder="Vendor name or contract code"
                value={formSupplierDetails}
                onChange={(e) => setFormSupplierDetails(e.target.value)}
              />
            </div>

          </div>

          {/* Dynamic Total Cost Summary Banner */}
          <div className="info-banner" style={{ justifyContent: 'space-between', marginTop: '0.5rem', marginBottom: '1rem' }}>
            <div>
              <span className="info-label">Quantity × Unit Cost:</span>
              <span style={{ fontSize: '0.9rem', color: 'var(--text-main)', fontWeight: 600 }}>
                {formQuantity || 0} × {formatINR(formUnitCost)}
              </span>
            </div>
            <div>
              <span className="info-label">Estimated Total Procurement Value:</span>
              <span className="info-val">{formatINR(calculatedTotalCost)}</span>
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={submitting}
            >
              {submitting ? 'Recording Purchase...' : 'Submit Procurement Record'}
            </button>
          </div>
        </form>
      </div>

      {/* Historical Purchases Table */}
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.75rem', marginBottom: '1rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <ShoppingCart size={18} color="var(--text-muted)" />
            <h2 style={{ fontSize: '1rem', fontWeight: 600, color: 'var(--text-main)', margin: 0 }}>Procurement Logs</h2>
          </div>

          {/* Table Search & Filter Bar */}
          <div className="filter-inputs">
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', backgroundColor: '#070b10', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)', padding: '0.35rem 0.65rem' }}>
              <Search size={15} color="var(--text-muted)" />
              <input
                type="text"
                placeholder="Search reference, vendor, equipment..."
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

        {loadingPurchases ? (
          <LoadingSpinner message="Fetching procurement records..." />
        ) : filteredPurchases.length > 0 ? (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Reference</th>
                  <th>Date</th>
                  <th>Base</th>
                  <th>Equipment Category</th>
                  <th>Quantity</th>
                  <th>Unit Cost</th>
                  <th>Total Cost</th>
                  <th>Supplier</th>
                  <th>Recorded By</th>
                </tr>
              </thead>
              <tbody>
                {filteredPurchases.map(p => (
                  <tr key={p.id}>
                    <td style={{ fontFamily: 'monospace', fontWeight: 600, fontSize: '0.8125rem', color: '#86efac' }}>
                      {p.purchaseReference}
                    </td>
                    <td style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>
                      {formatDateIN(p.purchaseDate)}
                    </td>
                    <td style={{ fontWeight: 500 }}>{p.baseName} ({p.baseCode})</td>
                    <td>{p.equipmentName} <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>({p.equipmentCode})</span></td>
                    <td style={{ fontWeight: 700 }}>{p.quantity} {p.unitOfMeasure}</td>
                    <td style={{ fontSize: '0.8125rem' }}>{formatINR(p.unitCost)}</td>
                    <td style={{ fontWeight: 600, color: 'var(--text-main)' }}>
                      {formatINR((p.unitCost || 0) * (p.quantity || 0))}
                    </td>
                    <td style={{ fontSize: '0.8125rem', color: 'var(--text-dim)' }}>{p.supplierDetails || '-'}</td>
                    <td style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>{p.recordedByUsername || '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state">
            <ShoppingCart size={36} color="var(--text-dim)" style={{ marginBottom: '0.5rem' }} />
            <h3>No Procurement Records Found</h3>
            <p>No procurement records match your active search or filter selection.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default PurchasesPage;
