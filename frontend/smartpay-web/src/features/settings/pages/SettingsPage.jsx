import React, { useState } from 'react'
import { useAuthStore } from '@/store/authStore'
import { authApi } from '@/services/endpoints/auth'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Lock, Shield, Bell, CheckCircle2, AlertCircle } from 'lucide-react'

export function SettingsPage() {
  const user = useAuthStore((s) => s.user)
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState({ type: '', text: '' })

  const [notificationsEnabled, setNotificationsEnabled] = useState(true)
  const [emailAlerts, setEmailAlerts] = useState(true)

  const handlePasswordChange = async (e) => {
    e.preventDefault()
    setMessage({ type: '', text: '' })

    if (newPassword !== confirmPassword) {
      setMessage({ type: 'error', text: 'New password and confirmation do not match.' })
      return
    }

    if (newPassword.length < 8) {
      setMessage({ type: 'error', text: 'New password must be at least 8 characters long.' })
      return
    }

    setSaving(true)
    try {
      const res = await authApi.changePassword(user.id, {
        currentPassword,
        newPassword
      })
      if (res.success) {
        setMessage({ type: 'success', text: 'Password successfully changed! All other sessions have been invalidated.' })
        setCurrentPassword('')
        setNewPassword('')
        setConfirmPassword('')
      }
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Failed to change password.' })
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Security & Settings</h1>
        <p className="text-sm text-slate-500 dark:text-slate-400">
          Manage your credentials, authentication methods, and notification preferences.
        </p>
      </div>

      {message.text && (
        <div
          className={`p-4 rounded-lg text-sm font-medium flex items-center gap-2 ${
            message.type === 'success'
              ? 'bg-emerald-50 text-emerald-800 dark:bg-emerald-950/40 dark:text-emerald-300'
              : 'bg-rose-50 text-rose-800 dark:bg-rose-950/40 dark:text-rose-300'
          }`}
        >
          {message.type === 'success' ? <CheckCircle2 className="h-4 w-4 shrink-0" /> : <AlertCircle className="h-4 w-4 shrink-0" />}
          {message.text}
        </div>
      )}

      {/* Change Password Card */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Lock className="h-5 w-5 text-blue-600" />
            Change Password
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handlePasswordChange} className="space-y-4">
            <div>
              <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Current Password</label>
              <Input
                required
                type="password"
                placeholder="••••••••"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                className="mt-1"
              />
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="text-xs font-medium text-slate-600 dark:text-slate-400">New Password (min 8 chars)</label>
                <Input
                  required
                  type="password"
                  placeholder="••••••••"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className="mt-1"
                />
              </div>
              <div>
                <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Confirm New Password</label>
                <Input
                  required
                  type="password"
                  placeholder="••••••••"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="mt-1"
                />
              </div>
            </div>
            <div className="pt-2">
              <Button type="submit" disabled={saving}>
                {saving ? 'Updating Password...' : 'Update Password'}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {/* Notification Preferences */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Bell className="h-5 w-5 text-blue-600" />
            Communication & Alerts
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between py-2 border-b border-slate-100 dark:border-slate-800">
            <div>
              <p className="text-sm font-medium text-slate-900 dark:text-slate-100">In-App Transaction Alerts</p>
              <p className="text-xs text-slate-500">Receive immediate in-app notifications on all wallet debits and credits.</p>
            </div>
            <input
              type="checkbox"
              checked={notificationsEnabled}
              onChange={(e) => setNotificationsEnabled(e.target.checked)}
              className="h-4 w-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
            />
          </div>

          <div className="flex items-center justify-between py-2">
            <div>
              <p className="text-sm font-medium text-slate-900 dark:text-slate-100">Email Summaries (Simulated)</p>
              <p className="text-xs text-slate-500">Receive daily statements and critical security alerts via simulated email queue.</p>
            </div>
            <input
              type="checkbox"
              checked={emailAlerts}
              onChange={(e) => setEmailAlerts(e.target.checked)}
              className="h-4 w-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
            />
          </div>
        </CardContent>
      </Card>

      {/* Security Architecture Notice */}
      <Card className="bg-slate-50 border-slate-200 dark:bg-slate-900 dark:border-slate-800">
        <CardContent className="p-6">
          <div className="flex items-start gap-3">
            <Shield className="h-5 w-5 text-blue-600 shrink-0 mt-0.5" />
            <div className="space-y-1 text-xs text-slate-600 dark:text-slate-400">
              <p className="font-semibold text-slate-900 dark:text-slate-100 text-sm">Security & Token Architecture</p>
              <p>• Passwords stored using adaptive BCrypt (cost factor 10) salt and hash.</p>
              <p>• Stateless authentication via short-lived JWT access tokens (15-min expiration) with refresh token rotation.</p>
              <p>• Automatic token reuse detection with immediate multi-device session revocation.</p>
              <p>• All financial transactions enforce idempotency keys and deterministic locking.</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
