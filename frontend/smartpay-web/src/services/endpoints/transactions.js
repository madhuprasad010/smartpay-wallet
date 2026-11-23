import { apiClient } from '../api/client'

export const transactionsApi = {
  transfer: (data) => apiClient.post('/transactions/transfer', data),
  reverse: (ref, data) => apiClient.post(`/transactions/${ref}/reverse`, data),
  getAll: (params) => apiClient.get('/transactions', { params }),
  getById: (id) => apiClient.get(`/transactions/${id}`),
  getByRef: (ref) => apiClient.get(`/transactions/ref/${ref}`)
}
