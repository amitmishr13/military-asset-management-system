import api from './api';

export const getBases = async () => {
  const response = await api.get('/api/bases');
  return response.data;
};

export const getEquipmentTypes = async () => {
  const response = await api.get('/api/equipment-types');
  return response.data;
};
