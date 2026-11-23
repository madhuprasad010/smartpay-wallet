import axios from 'axios'
import { API_BASE_URL } from '@/constants'
import { useAuthStore } from '@/store/authStore'

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000, // 30 seconds for cloud cold starts
  headers: {
    'Content-Type': 'application/json'
  }
})

apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  config.headers['X-Correlation-ID'] = crypto.randomUUID()
  return config
})

apiClient.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout()
    }
    
    let message = error.response?.data?.message
    if (!message) {
      if (error.code === 'ECONNABORTED' || (error.message && error.message.toLowerCase().includes('timeout'))) {
        message = 'Connection timed out. If the backend is hosted on Render free tier, it may take 45–60 seconds to wake up from cold sleep. Please retry in a moment.'
      } else if (error.message === 'Network Error' || !error.response) {
        message = `Unable to reach backend server (${API_BASE_URL}). Please verify your backend Web Service is running and live on Render.`
      } else {
        message = error.message || 'An unexpected error occurred'
      }
    }
    return Promise.reject(new Error(message))
  }
)
