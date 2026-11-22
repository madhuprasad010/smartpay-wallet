import { apiClient } from '../api/client'

export const beneficiariesApi = {
  getByUser: (userId) => apiClient.get(`/beneficiaries/user/${userId}`),
  create: (data) => apiClient.post('/beneficiaries', data),
  delete: (id) => apiClient.delete(`/beneficiaries/${id}`)
}
