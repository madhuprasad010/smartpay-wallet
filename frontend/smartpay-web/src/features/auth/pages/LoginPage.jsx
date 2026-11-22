import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { FormField } from '@/components/forms/FormField'
import { PasswordInput } from '@/components/forms/PasswordInput'
import { authApi } from '@/services/endpoints/auth'
import { useAuthStore } from '@/store/authStore'

export function LoginPage() {
  const navigate = useNavigate()
  const setAuth = useAuthStore((s) => s.setAuth)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await authApi.login({ email, password })
      if (res.success && res.data) {
        setAuth(res.data.user, res.data.accessToken)
        navigate('/dashboard')
      }
    } catch (err) {
      setError(err.message || 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Welcome Back</CardTitle>
        <CardDescription>Sign in to your SmartPay digital banking account</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && <div className="rounded-lg bg-rose-50 p-3 text-xs text-rose-600 dark:bg-rose-950/40">{error}</div>}
          <FormField
            label="Email Address"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            placeholder="you@smartpay.com"
          />
          <PasswordInput
            label="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            placeholder="••••••••"
          />
          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? 'Authenticating...' : 'Sign In'}
          </Button>
          <div className="text-center text-sm text-slate-500">
            Don't have an account?{' '}
            <Link to="/register" className="text-blue-600 hover:underline">
              Create one
            </Link>
          </div>
        </form>
      </CardContent>
    </Card>
  )
}
