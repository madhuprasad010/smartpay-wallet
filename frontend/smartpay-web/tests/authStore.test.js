import { describe, it, expect, beforeEach } from 'vitest'
import { useAuthStore } from '@/store/authStore'

describe('Auth Store', () => {
  beforeEach(() => {
    useAuthStore.getState().logout()
  })

  it('sets user and token on setAuth', () => {
    const user = { id: 1, email: 'test@smartpay.com', role: 'ROLE_USER' }
    useAuthStore.getState().setAuth(user, 'token-123')

    expect(useAuthStore.getState().isAuthenticated).toBe(true)
    expect(useAuthStore.getState().user.email).toBe('test@smartpay.com')
  })

  it('clears state on logout', () => {
    useAuthStore.getState().setAuth({ id: 1 }, 'token-123')
    useAuthStore.getState().logout()

    expect(useAuthStore.getState().isAuthenticated).toBe(false)
    expect(useAuthStore.getState().user).toBeNull()
  })
})
