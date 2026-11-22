import { apiClient } from '../api/client'

export const transactionsApi = {
  transfer: (data) => apiClient.post('/transactions/transfer', data),
  getAll: (params) => apiClient.get('/transactions', { params }),
  getById: (id) => apiClient.get(`/transactions/${id}`)
}
