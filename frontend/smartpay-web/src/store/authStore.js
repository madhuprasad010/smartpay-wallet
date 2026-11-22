import { create } from 'zustand'

const getStoredItem = (key) => {
  if (typeof window !== 'undefined' && window.localStorage) {
    return window.localStorage.getItem(key)
  }
  return null
}

const setStoredItem = (key, value) => {
  if (typeof window !== 'undefined' && window.localStorage) {
    window.localStorage.setItem(key, value)
  }
}

const removeStoredItem = (key) => {
  if (typeof window !== 'undefined' && window.localStorage) {
    window.localStorage.removeItem(key)
  }
}

export const useAuthStore = create((set) => ({
  user: JSON.parse(getStoredItem('smartpay_user') || 'null'),
  token: getStoredItem('smartpay_token') || null,
  isAuthenticated: !!getStoredItem('smartpay_token'),

  setAuth: (user, token) => {
    setStoredItem('smartpay_user', JSON.stringify(user))
    setStoredItem('smartpay_token', token)
    set({ user, token, isAuthenticated: true })
  },

  logout: () => {
    removeStoredItem('smartpay_user')
    removeStoredItem('smartpay_token')
    set({ user: null, token: null, isAuthenticated: false })
  }
}))
