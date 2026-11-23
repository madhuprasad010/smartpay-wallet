import { apiClient } from '../api/client'

export const paymentsApi = {
  deposit: (data) => apiClient.post('/payments/deposit', data),
  withdrawal: (data) => apiClient.post('/payments/withdrawal', data),
  initiate: (data) => apiClient.post('/payments', data),
  refund: (id, data) => apiClient.post(`/payments/${id}/refund`, data),
  getByUser: (userId, params) => apiClient.get(`/payments/user/${userId}`, { params }),
  getAll: (params) => apiClient.get('/payments', { params }),
  getById: (id) => apiClient.get(`/payments/${id}`)
}
