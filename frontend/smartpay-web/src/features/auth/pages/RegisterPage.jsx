import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { FormField } from '@/components/forms/FormField'
import { PasswordInput } from '@/components/forms/PasswordInput'
import { authApi } from '@/services/endpoints/auth'
import { useAuthStore } from '@/store/authStore'
import { API_BASE_URL } from '@/constants'

export function RegisterPage() {
  const navigate = useNavigate()
  const setAuth = useAuthStore((s) => s.setAuth)
  const [form, setForm] = useState({ email: '', password: '', firstName: '', lastName: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await authApi.register(form)
      if (res && res.success && res.data) {
        setAuth(res.data.user, res.data.accessToken)
        navigate('/dashboard')
      } else {
        setError(res?.message || 'Registration failed. Please check your details.')
      }
    } catch (err) {
      setError(err.message || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Create Account</CardTitle>
        <CardDescription>Join SmartPay for secure digital banking</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
            <div className="rounded-lg bg-rose-50 p-3 text-xs leading-relaxed text-rose-600 dark:bg-rose-950/40 dark:text-rose-400">
              {error}
            </div>
          )}
          <div className="grid grid-cols-2 gap-3">
            <FormField
              label="First Name"
              value={form.firstName}
              onChange={(e) => setForm({ ...form, firstName: e.target.value })}
              required
            />
            <FormField
              label="Last Name"
              value={form.lastName}
              onChange={(e) => setForm({ ...form, lastName: e.target.value })}
              required
            />
          </div>
          <FormField
            label="Email"
            type="email"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            required
          />
          <PasswordInput
            label="Password (min. 8 characters)"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            required
          />
          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? 'Creating Account...' : 'Register'}
          </Button>
          {loading && (
            <p className="text-center text-xs text-amber-600 dark:text-amber-400 animate-pulse">
              Connecting to backend... If the service is waking up from sleep, this may take up to 45 seconds.
            </p>
          )}
          <div className="text-center text-sm text-slate-500">
            Already registered?{' '}
            <Link to="/login" className="text-blue-600 hover:underline">
              Sign In
            </Link>
          </div>
          <div className="mt-4 border-t border-slate-100 pt-3 text-center text-xs text-slate-400 dark:border-slate-800">
            Backend API: <code className="rounded bg-slate-100 px-1.5 py-0.5 text-[11px] text-slate-600 dark:bg-slate-800 dark:text-slate-300 break-all">{API_BASE_URL}</code>
          </div>
        </form>
      </CardContent>
    </Card>
  )
}
