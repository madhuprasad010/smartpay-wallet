import { apiClient } from '../api/client'

export const authApi = {
  login: (data) => apiClient.post('/auth/login', data),
  register: (data) => apiClient.post('/auth/register', data),
  refresh: (refreshToken) => apiClient.post('/auth/refresh', { refreshToken }),
  logout: (refreshToken) => apiClient.post('/auth/logout', { refreshToken }),
  changePassword: (userId, data) => apiClient.post(`/auth/change-password?userId=${userId}`, data)
}
