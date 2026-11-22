import { apiClient } from '../api/client'

export const authApi = {
  login: (data) => apiClient.post('/auth/login', data),
  register: (data) => apiClient.post('/auth/register', data),
  refresh: (refreshToken) => apiClient.post('/auth/refresh', { refreshToken })
}
