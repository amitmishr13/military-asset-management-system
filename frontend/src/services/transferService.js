import api from './api';

export const createTransfer = async (transferData) => {
  const response = await api.post('/api/transfers', transferData);
  return response.data;
};

export const getTransfers = async (params = {}) => {
  const response = await api.get('/api/transfers', { params });
  return response.data;
};
