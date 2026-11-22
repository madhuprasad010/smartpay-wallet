import { apiClient } from '../api/client'

export const notificationsApi = {
  getByUser: (userId) => apiClient.get(`/notifications/user/${userId}`),
  markRead: (id) => apiClient.put(`/notifications/${id}/read`)
}
