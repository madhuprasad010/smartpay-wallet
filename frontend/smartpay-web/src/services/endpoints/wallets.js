import { apiClient } from '../api/client'

export const walletsApi = {
  getWalletsByUser: (userId) => apiClient.get(`/wallets/user/${userId}`),
  getWalletById: (id) => apiClient.get(`/wallets/${id}`),
  getWalletByNumber: (walletNumber) => apiClient.get(`/wallets/number/${walletNumber}`),
  getLedger: (id) => apiClient.get(`/wallets/${id}/ledger`),
  createWallet: (data) => apiClient.post('/wallets', data),
  creditWallet: (id, data) => apiClient.post(`/wallets/${id}/credit`, data),
  debitWallet: (id, data) => apiClient.post(`/wallets/${id}/debit`, data),
  freeze: (id) => apiClient.post(`/wallets/${id}/freeze`),
  unfreeze: (id) => apiClient.post(`/wallets/${id}/unfreeze`),
  activate: (id) => apiClient.post(`/wallets/${id}/activate`),
  getLimits: (id) => apiClient.get(`/wallets/${id}/limits`),
  updateLimits: (id, data) => apiClient.put(`/wallets/${id}/limits`, data),
  reconcile: (id) => apiClient.get(`/wallets/${id}/reconcile`),
  reconcileAll: () => apiClient.get('/wallets/reconcile-all')
}
