import api from './api';

export const getAuditLogs = async (baseId) => {
  const params = baseId ? { baseId } : {};
  const response = await api.get('/api/audit-logs', { params });
  return response.data;
};
