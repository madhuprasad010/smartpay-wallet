import { apiClient } from '../api/client'

export const walletsApi = {
  getWalletsByUser: (userId) => apiClient.get(`/wallets/user/${userId}`),
  getWalletById: (id) => apiClient.get(`/wallets/${id}`),
  getLedger: (id) => apiClient.get(`/wallets/${id}/ledger`),
  createWallet: (data) => apiClient.post('/wallets', data)
}
