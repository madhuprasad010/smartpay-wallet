import { apiClient } from '../api/client'

export const usersApi = {
  getProfile: (userId) => apiClient.get(`/users/${userId}/profile`),
  updateProfile: (userId, data) => apiClient.put(`/users/${userId}/profile`, data),
  submitKyc: (userId, data) => apiClient.post(`/users/${userId}/kyc`, data),
  verifyKyc: (userId) => apiClient.post(`/users/${userId}/kyc/verify`),
  getAllUsers: (params) => apiClient.get('/users', { params }),
  updateStatus: (userId, data) => apiClient.patch(`/users/${userId}/status`, data)
}
