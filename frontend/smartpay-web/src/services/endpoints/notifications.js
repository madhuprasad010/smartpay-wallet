import { apiClient } from '../api/client'

export const notificationsApi = {
  getByUser: (userId) => apiClient.get(`/notifications/user/${userId}`),
  getByUserPaged: (userId, params) => apiClient.get(`/notifications/user/${userId}/paged`, { params }),
  getUnreadCount: (userId) => apiClient.get(`/notifications/user/${userId}/unread-count`),
  markRead: (id) => apiClient.patch(`/notifications/${id}/read`),
  markAllRead: (userId) => apiClient.post(`/notifications/user/${userId}/read-all`)
}
