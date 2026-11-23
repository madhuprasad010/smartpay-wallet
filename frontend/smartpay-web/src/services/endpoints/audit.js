import { apiClient } from '../api/client'

export const auditApi = {
  getAll: (params) => apiClient.get('/audit', { params }),
  getByActor: (actorId, params) => apiClient.get(`/audit/actor/${actorId}`, { params }),
  record: (data) => apiClient.post('/audit', data)
}
