import api from './api';

export const getDashboardMetrics = async (params = {}) => {
  const response = await api.get('/api/dashboard', { params });
  return response.data;
};

export const getNetMovementDetails = async (params = {}) => {
  const response = await api.get('/api/dashboard/net-movement-details', { params });
  return response.data;
};
