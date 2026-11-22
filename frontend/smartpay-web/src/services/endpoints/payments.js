import { apiClient } from '../api/client'

export const paymentsApi = {
  initiate: (data) => apiClient.post('/payments', data),
  getByUser: (userId) => apiClient.get(`/payments/user/${userId}`)
}
