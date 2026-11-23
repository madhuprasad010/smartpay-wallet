import React, { useEffect, useState } from 'react'
import { useAuthStore } from '@/store/authStore'
import { usersApi } from '@/services/endpoints/users'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { formatDate } from '@/utils/cn'
import { User, ShieldCheck, AlertCircle, CheckCircle, Clock } from 'lucide-react'

export function ProfilePage() {
  const user = useAuthStore((s) => s.user)
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState({ type: '', text: '' })

  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    phone: '',
    address: '',
    dateOfBirth: ''
  })

  const [kycForm, setKycForm] = useState({
    documentType: 'PAN',
    documentNumber: ''
  })
  const [submittingKyc, setSubmittingKyc] = useState(false)

  useEffect(() => {
    async function load() {
      if (!user?.id) return
      try {
        const res = await usersApi.getProfile(user.id)
        if (res.success && res.data) {
          setProfile(res.data)
          setForm({
            firstName: res.data.firstName || '',
            lastName: res.data.lastName || '',
            phone: res.data.phone || '',
            address: res.data.address || '',
            dateOfBirth: res.data.dateOfBirth || ''
          })
        }
      } catch (err) {
        console.error(err)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [user?.id])

  const handleProfileSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setMessage({ type: '', text: '' })
    try {
      const res = await usersApi.updateProfile(user.id, form)
      if (res.success) {
        setProfile(res.data)
        setMessage({ type: 'success', text: 'Personal details updated successfully!' })
      }
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Failed to update profile' })
    } finally {
      setSaving(false)
    }
  }

  const handleKycSubmit = async (e) => {
    e.preventDefault()
    setSubmittingKyc(true)
    setMessage({ type: '', text: '' })
    try {
      const res = await usersApi.submitKyc(user.id, kycForm)
      if (res.success) {
        setProfile(res.data)
        setMessage({ type: 'success', text: 'KYC submitted successfully and is pending administrative review!' })
      }
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Failed to submit KYC' })
    } finally {
      setSubmittingKyc(false)
    }
  }

  if (loading) {
    return <div className="p-8 text-center text-slate-500">Loading user profile...</div>
  }

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">My Profile & Account</h1>
        <p className="text-sm text-slate-500 dark:text-slate-400">Manage your personal information and KYC verification</p>
      </div>

      {message.text && (
        <div
          className={`p-4 rounded-lg text-sm font-medium flex items-center gap-2 ${
            message.type === 'success'
              ? 'bg-emerald-50 text-emerald-800 dark:bg-emerald-950/40 dark:text-emerald-300'
              : 'bg-rose-50 text-rose-800 dark:bg-rose-950/40 dark:text-rose-300'
          }`}
        >
          {message.type === 'success' ? <CheckCircle className="h-4 w-4 shrink-0" /> : <AlertCircle className="h-4 w-4 shrink-0" />}
          {message.text}
        </div>
      )}

      {/* KYC Status Banner */}
      <Card className="border-l-4 border-l-blue-600 shadow-sm">
        <CardContent className="p-6">
          <div className="flex items-start justify-between">
            <div className="flex gap-4">
              <div className="rounded-full bg-blue-100 p-3 text-blue-600 dark:bg-blue-900/40 dark:text-blue-400">
                <ShieldCheck className="h-6 w-6" />
              </div>
              <div>
                <h3 className="font-semibold text-slate-900 dark:text-slate-100">KYC Verification Status</h3>
                <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
                  Required to increase wallet transaction limits and access international remittence.
                </p>
                <div className="mt-3 flex items-center gap-2">
                  <span className="text-xs font-semibold text-slate-500">Status:</span>
                  {profile?.kycStatus === 'VERIFIED' ? (
                    <span className="inline-flex items-center gap-1 rounded-full bg-emerald-100 px-2.5 py-0.5 text-xs font-semibold text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300">
                      <CheckCircle className="h-3 w-3" /> Verified ({formatDate(profile.kycVerifiedAt)})
                    </span>
                  ) : profile?.kycStatus === 'PENDING' ? (
                    <span className="inline-flex items-center gap-1 rounded-full bg-amber-100 px-2.5 py-0.5 text-xs font-semibold text-amber-800 dark:bg-amber-900/40 dark:text-amber-300">
                      <Clock className="h-3 w-3" /> Under Review
                    </span>
                  ) : (
                    <span className="inline-flex items-center gap-1 rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-semibold text-slate-700 dark:bg-slate-800 dark:text-slate-300">
                      <AlertCircle className="h-3 w-3" /> Unverified
                    </span>
                  )}
                </div>
              </div>
            </div>
          </div>

          {profile?.kycStatus === 'UNVERIFIED' && (
            <form onSubmit={handleKycSubmit} className="mt-6 border-t border-slate-200 pt-4 dark:border-slate-800 grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Document Type</label>
                <select
                  value={kycForm.documentType}
                  onChange={(e) => setKycForm({ ...kycForm, documentType: e.target.value })}
                  className="mt-1 block w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100"
                >
                  <option value="PAN">PAN Card</option>
                  <option value="AADHAAR">Aadhaar Card</option>
                  <option value="PASSPORT">Passport</option>
                  <option value="DRIVING_LICENSE">Driving License</option>
                </select>
              </div>
              <div>
                <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Document Number</label>
                <Input
                  required
                  placeholder="e.g. ABCDE1234F"
                  value={kycForm.documentNumber}
                  onChange={(e) => setKycForm({ ...kycForm, documentNumber: e.target.value })}
                  className="mt-1"
                />
              </div>
              <div className="flex items-end">
                <Button type="submit" disabled={submittingKyc} className="w-full">
                  {submittingKyc ? 'Submitting...' : 'Submit for Verification'}
                </Button>
              </div>
            </form>
          )}
        </CardContent>
      </Card>

      {/* Personal Details Form */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <User className="h-5 w-5 text-blue-600" />
            Personal Details
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleProfileSubmit} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="text-xs font-medium text-slate-600 dark:text-slate-400">First Name</label>
                <Input
                  value={form.firstName}
                  onChange={(e) => setForm({ ...form, firstName: e.target.value })}
                  placeholder="First name"
                  className="mt-1"
                />
              </div>
              <div>
                <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Last Name</label>
                <Input
                  value={form.lastName}
                  onChange={(e) => setForm({ ...form, lastName: e.target.value })}
                  placeholder="Last name"
                  className="mt-1"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Email Address (Read-only)</label>
                <Input value={user?.email || ''} disabled className="mt-1 bg-slate-50 dark:bg-slate-800/50" />
              </div>
              <div>
                <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Phone Number</label>
                <Input
                  value={form.phone}
                  onChange={(e) => setForm({ ...form, phone: e.target.value })}
                  placeholder="+91 9876543210"
                  className="mt-1"
                />
              </div>
            </div>

            <div>
              <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Residential Address</label>
              <Input
                value={form.address}
                onChange={(e) => setForm({ ...form, address: e.target.value })}
                placeholder="Street address, City, State, PIN"
                className="mt-1"
              />
            </div>

            <div className="pt-2">
              <Button type="submit" disabled={saving}>
                {saving ? 'Saving...' : 'Save Profile Changes'}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
