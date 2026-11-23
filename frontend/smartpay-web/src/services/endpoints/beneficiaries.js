import { apiClient } from '../api/client'

export const beneficiariesApi = {
  getByUser: (userId) => apiClient.get(`/beneficiaries/user/${userId}`),
  getById: (id) => apiClient.get(`/beneficiaries/${id}`),
  create: (data) => apiClient.post('/beneficiaries', data),
  update: (id, data) => apiClient.put(`/beneficiaries/${id}`, data),
  delete: (id) => apiClient.delete(`/beneficiaries/${id}`),
  updateStatus: (id, status) => apiClient.patch(`/beneficiaries/${id}/status?status=${status}`),
  transfer: (id, data) => apiClient.post(`/beneficiaries/${id}/transfer`, data)
}
