import api from './api';

export const createExpenditure = async (expenditureData) => {
  const response = await api.post('/api/expenditures', expenditureData);
  return response.data;
};

export const getExpenditures = async (params = {}) => {
  const response = await api.get('/api/expenditures', { params });
  return response.data;
};
