import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { getBases, getEquipmentTypes } from '../services/referenceService';
import { getDashboardMetrics, getNetMovementDetails } from '../services/dashboardService';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { Alert } from '../components/Alert';
import { formatDateIN, formatNumber, getInventoryHealthStatus } from '../utils/formatters';
import { exportToCSV } from '../utils/csvExport';
import {
  LayoutDashboard,
  Filter,
  PackageCheck,
  ShoppingBag,
  ArrowDownRight,
  ArrowUpRight,
  Activity,
  Flame,
  ShieldCheck,
  UserCheck,
  Layers,
  X,
  ExternalLink,
  RefreshCw,
  AlertTriangle,
  Download
} from 'lucide-react';

export const DashboardPage = () => {
  const { user, isCommander } = useAuth();

  // Filters State
  const [selectedDate, setSelectedDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [selectedBaseId, setSelectedBaseId] = useState(isCommander ? user?.baseId || '' : '');
  const [selectedEquipmentTypeId, setSelectedEquipmentTypeId] = useState('');

  // Data State
  const [bases, setBases] = useState([]);
  const [equipmentTypes, setEquipmentTypes] = useState([]);
  const [metrics, setMetrics] = useState(null);
  const [loadingMetrics, setLoadingMetrics] = useState(true);
  const [metricsError, setMetricsError] = useState(null);

  // Modal State
  const [modalOpen, setModalOpen] = useState(false);
  const [breakdown, setBreakdown] = useState(null);
  const [loadingBreakdown, setLoadingBreakdown] = useState(false);
  const [breakdownError, setBreakdownError] = useState(null);

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

  // Set Commander default base when available
  useEffect(() => {
    if (isCommander && user?.baseId) {
      setSelectedBaseId(user.baseId);
    }
  }, [isCommander, user]);

  // Fetch Metrics
  const fetchMetrics = useCallback(async () => {
    setLoadingMetrics(true);
    setMetricsError(null);
    try {
      const params = {};
      if (selectedDate) params.date = selectedDate;
      if (selectedBaseId) params.baseId = selectedBaseId;
      if (selectedEquipmentTypeId) params.equipmentTypeId = selectedEquipmentTypeId;

      const response = await getDashboardMetrics(params);
      if (response.success) {
        setMetrics(response.data);
      } else {
        setMetricsError(response.message || 'Failed to load dashboard metrics');
      }
    } catch (err) {
      setMetricsError(err.response?.data?.message || 'Server error loading metrics');
    } finally {
      setLoadingMetrics(false);
    }
  }, [selectedDate, selectedBaseId, selectedEquipmentTypeId]);

  useEffect(() => {
    fetchMetrics();
  }, [fetchMetrics]);

  // Open Net Movement Breakdown Modal
  const handleOpenNetMovementModal = async () => {
    setModalOpen(true);
    setLoadingBreakdown(true);
    setBreakdownError(null);
    try {
      const params = {};
      if (selectedDate) params.date = selectedDate;
      if (selectedBaseId) params.baseId = selectedBaseId;
      if (selectedEquipmentTypeId) params.equipmentTypeId = selectedEquipmentTypeId;

      const response = await getNetMovementDetails(params);
      if (response.success) {
        setBreakdown(response.data);
      } else {
        setBreakdownError(response.message || 'Failed to load net movement details');
      }
    } catch (err) {
      setBreakdownError(err.response?.data?.message || 'Server error loading net movement details');
    } finally {
      setLoadingBreakdown(false);
    }
  };

  const handleExportDashboardCSV = () => {
    if (!metrics) return;
    const data = [{
      SelectedDate: formatDateIN(metrics.selectedDate),
      Base: metrics.baseName || 'All Bases',
      Equipment: metrics.equipmentName || 'All Types',
      OpeningBalance: metrics.openingBalance,
      Purchases: metrics.purchases,
      TransferIn: metrics.transferIn,
      TransferOut: metrics.transferOut,
      NetMovement: metrics.netMovement,
      ExpendedAssets: metrics.expendedAssets,
      ClosingBalance: metrics.closingBalance,
      ActiveAssigned: metrics.activeAssignedAssets,
      AvailableStock: metrics.availableStock
    }];

    const columns = [
      { key: 'SelectedDate', label: 'As Of Date' },
      { key: 'Base', label: 'Base' },
      { key: 'Equipment', label: 'Equipment' },
      { key: 'OpeningBalance', label: 'Opening Balance' },
      { key: 'Purchases', label: 'Purchases' },
      { key: 'TransferIn', label: 'Transfer In' },
      { key: 'TransferOut', label: 'Transfer Out' },
      { key: 'NetMovement', label: 'Net Movement' },
      { key: 'ExpendedAssets', label: 'Expended' },
      { key: 'ClosingBalance', label: 'Closing Balance' },
      { key: 'ActiveAssigned', label: 'Active Assigned' },
      { key: 'AvailableStock', label: 'Available Stock' }
    ];

    exportToCSV(data, columns, 'dashboard_summary');
  };

  const stockHealth = metrics ? getInventoryHealthStatus(metrics.availableStock) : null;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
            <LayoutDashboard size={24} color="var(--accent-military)" />
            <h1 style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--text-main)', margin: 0 }}>Operational Dashboard</h1>
          </div>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>
            Real-time balance reconstruction, historical inventory movements, and stock operational health metrics.
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <button className="btn btn-export" onClick={handleExportDashboardCSV} disabled={!metrics} title="Export CSV Summary">
            <Download size={15} />
            Export CSV
          </button>
          <button className="btn btn-secondary" onClick={fetchMetrics} disabled={loadingMetrics} title="Refresh Data">
            <RefreshCw size={15} className={loadingMetrics ? 'spin' : ''} />
            Refresh
          </button>
        </div>
      </div>

      {/* Filter Controls Card */}
      <div className="card" style={{ padding: '1rem 1.25rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.75rem', color: 'var(--text-muted)', fontSize: '0.8125rem', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.04em' }}>
          <Filter size={15} />
          <span>Operational Context Filters</span>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem', alignItems: 'end' }}>
          <div className="form-group" style={{ margin: 0 }}>
            <label className="form-label">As Of Date</label>
            <input
              type="date"
              className="form-input"
              value={selectedDate}
              onChange={(e) => setSelectedDate(e.target.value)}
            />
          </div>

          <div className="form-group" style={{ margin: 0 }}>
            <label className="form-label">Military Base</label>
            <select
              className="form-select"
              value={selectedBaseId}
              onChange={(e) => setSelectedBaseId(e.target.value)}
              disabled={isCommander}
            >
              {!isCommander && <option value="">All Bases (Global View)</option>}
              {bases.map(b => (
                <option key={b.id} value={b.id}>{b.name} ({b.code})</option>
              ))}
            </select>
          </div>

          <div className="form-group" style={{ margin: 0 }}>
            <label className="form-label">Equipment Category</label>
            <select
              className="form-select"
              value={selectedEquipmentTypeId}
              onChange={(e) => setSelectedEquipmentTypeId(e.target.value)}
            >
              <option value="">All Equipment Types</option>
              {equipmentTypes.map(eq => (
                <option key={eq.id} value={eq.id}>{eq.name} ({eq.code})</option>
              ))}
            </select>
          </div>

          {(!isCommander && (selectedBaseId || selectedEquipmentTypeId)) && (
            <div>
              <button
                className="btn btn-secondary"
                onClick={() => {
                  setSelectedBaseId('');
                  setSelectedEquipmentTypeId('');
                }}
                style={{ height: '38px', width: '100%' }}
              >
                Clear Filters
              </button>
            </div>
          )}
        </div>
      </div>

      <Alert type="error" message={metricsError} onClose={() => setMetricsError(null)} />

      {/* Main Metrics Grid */}
      {loadingMetrics ? (
        <LoadingSpinner message="Calculating inventory stock metrics..." />
      ) : metrics ? (
        <>
          {/* SECTION 1: OPERATIONAL OVERVIEW */}
          <div style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-dim)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
            OVERVIEW & STOCK HEALTH
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1rem' }}>

            {/* Opening Balance */}
            <div className="card" style={{ borderLeft: '4px solid #64748b' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                <span style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.03em' }}>Opening Balance</span>
                <PackageCheck size={18} color="var(--text-muted)" />
              </div>
              <div style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-main)' }}>
                {formatNumber(metrics.openingBalance)}
              </div>
              <p style={{ fontSize: '0.78rem', color: 'var(--text-dim)', marginTop: '0.25rem' }}>
                Reconstructed prior to {formatDateIN(metrics.selectedDate)}
              </p>
            </div>

            {/* Net Movement Card (Clickable) */}
            <div
              className="card"
              onClick={handleOpenNetMovementModal}
              style={{
                borderLeft: '4px solid var(--accent-military)',
                backgroundColor: 'rgba(74, 103, 65, 0.1)',
                cursor: 'pointer',
                transition: 'transform 0.15s ease, border-color 0.15s ease'
              }}
              title="Click to view detailed Net Movement breakdown"
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                <span style={{ fontSize: '0.8125rem', fontWeight: 600, color: '#86efac', textTransform: 'uppercase', letterSpacing: '0.03em', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                  Net Movement <ExternalLink size={13} />
                </span>
                <Activity size={18} color="#86efac" />
              </div>
              <div style={{ fontSize: '1.75rem', fontWeight: 700, color: metrics.netMovement >= 0 ? '#86efac' : 'var(--critical-text)' }}>
                {metrics.netMovement > 0 ? `+${formatNumber(metrics.netMovement)}` : formatNumber(metrics.netMovement)}
              </div>
              <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '0.25rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span>Purchases + In - Out</span>
                <span style={{ textDecoration: 'underline', color: '#86efac', fontWeight: 500 }}>Itemized Audit &rarr;</span>
              </p>
            </div>

            {/* Expended Assets */}
            <div className="card" style={{ borderLeft: '4px solid var(--low-border)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                <span style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--low-text)', textTransform: 'uppercase', letterSpacing: '0.03em' }}>Expended Assets</span>
                <Flame size={18} color="var(--low-text)" />
              </div>
              <div style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-main)' }}>
                {formatNumber(metrics.expendedAssets)}
              </div>
              <p style={{ fontSize: '0.78rem', color: 'var(--text-dim)', marginTop: '0.25rem' }}>
                Consumed or decommissioned
              </p>
            </div>

            {/* Closing Balance & Health Badge */}
            <div className="card" style={{ borderLeft: '4px solid var(--healthy-border)', backgroundColor: 'rgba(34, 197, 94, 0.05)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                <span style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--healthy-text)', textTransform: 'uppercase', letterSpacing: '0.03em' }}>Closing Balance</span>
                <ShieldCheck size={18} color="var(--healthy-text)" />
              </div>
              <div style={{ display: 'flex', alignItems: 'center', justifyBetween: 'space-between', gap: '0.5rem' }}>
                <div style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--healthy-text)' }}>
                  {formatNumber(metrics.closingBalance)}
                </div>
                {stockHealth && (
                  <span className={`badge ${stockHealth.badgeClass}`} style={{ marginLeft: 'auto' }}>
                    {stockHealth.label}
                  </span>
                )}
              </div>
              <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                Total owned stock as of {formatDateIN(metrics.selectedDate)}
              </p>
            </div>

          </div>

          {/* SECTION 2: MOVEMENT & DEPLOYMENT METRICS */}
          <div style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-dim)', textTransform: 'uppercase', letterSpacing: '0.06em', marginTop: '0.5rem' }}>
            LOGISTICS & PERSONNEL DEPLOYMENT BREAKDOWN
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>

            <div className="card" style={{ padding: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.35rem', color: 'var(--text-muted)', fontSize: '0.75rem', textTransform: 'uppercase', fontWeight: 600 }}>
                <ShoppingBag size={14} color="var(--text-muted)" />
                <span>Purchases</span>
              </div>
              <div style={{ fontSize: '1.35rem', fontWeight: 700, color: 'var(--text-main)' }}>
                +{formatNumber(metrics.purchases)}
              </div>
            </div>

            <div className="card" style={{ padding: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.35rem', color: 'var(--text-muted)', fontSize: '0.75rem', textTransform: 'uppercase', fontWeight: 600 }}>
                <ArrowDownRight size={14} color="var(--healthy-text)" />
                <span>Transfers In</span>
              </div>
              <div style={{ fontSize: '1.35rem', fontWeight: 700, color: 'var(--healthy-text)' }}>
                +{formatNumber(metrics.transferIn)}
              </div>
            </div>

            <div className="card" style={{ padding: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.35rem', color: 'var(--text-muted)', fontSize: '0.75rem', textTransform: 'uppercase', fontWeight: 600 }}>
                <ArrowUpRight size={14} color="var(--critical-text)" />
                <span>Transfers Out</span>
              </div>
              <div style={{ fontSize: '1.35rem', fontWeight: 700, color: 'var(--critical-text)' }}>
                -{formatNumber(metrics.transferOut)}
              </div>
            </div>

            <div className="card" style={{ padding: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.35rem', color: 'var(--text-muted)', fontSize: '0.75rem', textTransform: 'uppercase', fontWeight: 600 }}>
                <UserCheck size={14} color="#c4b5fd" />
                <span>Active Assigned</span>
              </div>
              <div style={{ fontSize: '1.35rem', fontWeight: 700, color: '#c4b5fd' }}>
                {formatNumber(metrics.activeAssignedAssets)}
              </div>
            </div>

            <div className="card" style={{ padding: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.35rem', color: 'var(--text-muted)', fontSize: '0.75rem', textTransform: 'uppercase', fontWeight: 600 }}>
                <Layers size={14} color="#38bdf8" />
                <span>Available Depot Stock</span>
              </div>
              <div style={{ fontSize: '1.35rem', fontWeight: 700, color: '#38bdf8' }}>
                {formatNumber(metrics.availableStock)}
              </div>
            </div>

          </div>

          {/* Reconciled Formula Footer Notice */}
          <div className="card" style={{ padding: '0.85rem 1.25rem', backgroundColor: '#070b10', fontSize: '0.8125rem', color: 'var(--text-muted)', display: 'flex', flexWrap: 'wrap', justifyContent: 'space-between', gap: '0.75rem', alignItems: 'center' }}>
            <div>
              <strong style={{ color: 'var(--text-main)' }}>Inventory Balance Formula:</strong> Closing Balance ({metrics.closingBalance}) = Opening ({metrics.openingBalance}) + Net Movement ({metrics.netMovement}) - Expended ({metrics.expendedAssets})
            </div>
            <div>
              <strong style={{ color: 'var(--text-main)' }}>Stock Readiness:</strong> Closing ({metrics.closingBalance}) = Assigned ({metrics.activeAssignedAssets}) + Available ({metrics.availableStock})
            </div>
          </div>
        </>
      ) : (
        <div className="card" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
          No metrics available for selected filters.
        </div>
      )}

      {/* Net Movement Breakdown Modal */}
      {modalOpen && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 100,
          padding: '1rem'
        }}>
          <div className="card" style={{
            width: '100%',
            maxWidth: '920px',
            maxHeight: '90vh',
            display: 'flex',
            flexDirection: 'column',
            padding: '1.5rem',
            backgroundColor: 'var(--bg-card)',
            boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.5)'
          }}>
            {/* Modal Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: '1rem', borderBottom: '1px solid var(--border-color)', marginBottom: '1rem' }}>
              <div>
                <h2 style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '0.5rem', margin: 0 }}>
                  <Activity size={20} color="var(--accent-military)" />
                  Net Movement Detailed Itemized Audit
                </h2>
                <p style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', marginTop: '0.2rem', margin: 0 }}>
                  Base: {breakdown?.baseName || (selectedBaseId ? 'Selected Base' : 'All Bases')} • Equipment: {breakdown?.equipmentName || 'All Categories'} • As of: {formatDateIN(selectedDate)}
                </p>
              </div>
              <button className="btn btn-secondary" onClick={() => setModalOpen(false)} style={{ padding: '0.4rem' }}>
                <X size={18} />
              </button>
            </div>

            {/* Modal Content */}
            <div style={{ flex: 1, overflowY: 'auto', paddingRight: '0.25rem' }}>
              <Alert type="error" message={breakdownError} onClose={() => setBreakdownError(null)} />

              {loadingBreakdown ? (
                <LoadingSpinner message="Fetching itemized transaction audit records..." />
              ) : breakdown ? (
                <>
                  {/* Summary Reconciliation Row */}
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: '0.75rem', marginBottom: '1.25rem', padding: '0.85rem', backgroundColor: '#070b10', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-color)' }}>
                    <div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)', textTransform: 'uppercase' }}>Total Purchases</div>
                      <div style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--text-main)' }}>+{formatNumber(breakdown.totalPurchases)}</div>
                    </div>
                    <div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)', textTransform: 'uppercase' }}>Total Transfer In</div>
                      <div style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--healthy-text)' }}>+{formatNumber(breakdown.totalTransferIn)}</div>
                    </div>
                    <div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)', textTransform: 'uppercase' }}>Total Transfer Out</div>
                      <div style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--critical-text)' }}>-{formatNumber(breakdown.totalTransferOut)}</div>
                    </div>
                    <div>
                      <div style={{ fontSize: '0.75rem', color: '#86efac', textTransform: 'uppercase', fontWeight: 600 }}>Reconciled Net</div>
                      <div style={{ fontSize: '1.1rem', fontWeight: 700, color: breakdown.netMovement >= 0 ? 'var(--healthy-text)' : 'var(--critical-text)' }}>
                        {breakdown.netMovement > 0 ? `+${formatNumber(breakdown.netMovement)}` : formatNumber(breakdown.netMovement)}
                      </div>
                    </div>
                  </div>

                  {/* Itemized Transactions Table */}
                  {breakdown.items && breakdown.items.length > 0 ? (
                    <div className="table-responsive">
                      <table className="data-table">
                        <thead>
                          <tr>
                            <th>Reference</th>
                            <th>Date</th>
                            <th>Type</th>
                            <th>Source / Destination</th>
                            <th>Equipment</th>
                            <th>Qty</th>
                            <th>Net Impact</th>
                            <th>Recorded By</th>
                          </tr>
                        </thead>
                        <tbody>
                          {breakdown.items.map((item, idx) => (
                            <tr key={idx}>
                              <td style={{ fontFamily: 'monospace', fontWeight: 600, fontSize: '0.8rem' }}>{item.reference}</td>
                              <td style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                                {formatDateIN(item.transactionDate)}
                              </td>
                              <td>
                                {item.transactionType === 'PURCHASE' && (
                                  <span className="badge" style={{ backgroundColor: 'rgba(37, 99, 235, 0.15)', color: '#60a5fa', border: '1px solid #2563eb' }}>PURCHASE</span>
                                )}
                                {item.transactionType === 'TRANSFER_IN' && (
                                  <span className="badge badge-healthy">TRANSFER IN</span>
                                )}
                                {item.transactionType === 'TRANSFER_OUT' && (
                                  <span className="badge badge-critical">TRANSFER OUT</span>
                                )}
                              </td>
                              <td style={{ fontSize: '0.8125rem' }}>
                                {item.transactionType === 'PURCHASE' && (item.baseName || 'Depot')}
                                {item.transactionType === 'TRANSFER_IN' && `${item.relatedBaseName || 'Source'} → ${item.baseName}`}
                                {item.transactionType === 'TRANSFER_OUT' && `${item.baseName} → ${item.relatedBaseName || 'Dest'}`}
                              </td>
                              <td style={{ fontSize: '0.8125rem' }}>{item.equipmentName} ({item.equipmentCode})</td>
                              <td style={{ fontWeight: 600 }}>{item.quantity}</td>
                              <td style={{ fontWeight: 700, color: item.netImpact >= 0 ? 'var(--healthy-text)' : 'var(--critical-text)' }}>
                                {item.netImpact > 0 ? `+${item.netImpact}` : item.netImpact}
                              </td>
                              <td style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{item.recordedByUsername || '-'}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  ) : (
                    <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)', fontSize: '0.875rem' }}>
                      No net movement transaction records found for the selected filters.
                    </div>
                  )}
                </>
              ) : null}
            </div>

            {/* Modal Footer */}
            <div style={{ paddingTop: '1rem', borderTop: '1px solid var(--border-color)', display: 'flex', justifyContent: 'flex-end', marginTop: '1rem' }}>
              <button className="btn btn-secondary" onClick={() => setModalOpen(false)}>
                Close Audit Breakdown
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default DashboardPage;
