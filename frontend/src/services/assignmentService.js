import api from './api';

export const createAssignment = async (assignmentData) => {
  const response = await api.post('/api/assignments', assignmentData);
  return response.data;
};

export const getAssignments = async (params = {}) => {
  const response = await api.get('/api/assignments', { params });
  return response.data;
};
