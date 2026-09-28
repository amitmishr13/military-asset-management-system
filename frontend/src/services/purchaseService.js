import api from './api';

export const createPurchase = async (purchaseData) => {
  const response = await api.post('/api/purchases', purchaseData);
  return response.data;
};

export const getPurchases = async (params = {}) => {
  const response = await api.get('/api/purchases', { params });
  return response.data;
};
