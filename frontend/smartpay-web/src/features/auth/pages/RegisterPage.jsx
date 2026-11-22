import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { FormField } from '@/components/forms/FormField'
import { PasswordInput } from '@/components/forms/PasswordInput'
import { authApi } from '@/services/endpoints/auth'
import { useAuthStore } from '@/store/authStore'

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
      if (res.success && res.data) {
        setAuth(res.data.user, res.data.accessToken)
        navigate('/dashboard')
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
          {error && <div className="rounded-lg bg-rose-50 p-3 text-xs text-rose-600 dark:bg-rose-950/40">{error}</div>}
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
          <div className="text-center text-sm text-slate-500">
            Already registered?{' '}
            <Link to="/login" className="text-blue-600 hover:underline">
              Sign In
            </Link>
          </div>
        </form>
      </CardContent>
    </Card>
  )
}
