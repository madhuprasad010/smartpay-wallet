import React, { useEffect, useState } from 'react'
import { usersApi } from '@/services/endpoints/users'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { formatDate } from '@/utils/cn'
import { UserCheck, ShieldCheck, CheckCircle2, AlertCircle } from 'lucide-react'

export function AdminUsersPage() {
  const [users, setUsers] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState({ type: '', text: '' })

  useEffect(() => {
    loadUsers()
  }, [page])

  async function loadUsers() {
    try {
      setLoading(true)
      const res = await usersApi.getAllUsers({ page, size: 10 })
      if (res.success && res.data) {
        setUsers(res.data.content || [])
        setTotalPages(res.data.totalPages || 1)
      }
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleStatusChange = async (userId, newStatus) => {
    try {
      const res = await usersApi.updateStatus(userId, { status: newStatus })
      if (res.success) {
        setUsers(users.map((u) => (u.id === userId ? { ...u, status: newStatus } : u)))
        setMessage({ type: 'success', text: `Updated user status to ${newStatus}` })
      }
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Status update failed' })
    }
  }

  const handleVerifyKyc = async (userId) => {
    try {
      const res = await usersApi.verifyKyc(userId)
      if (res.success) {
        setUsers(users.map((u) => (u.id === userId ? { ...u, kycStatus: 'VERIFIED' } : u)))
        setMessage({ type: 'success', text: 'KYC verified and approved!' })
      }
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Failed to verify KYC' })
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">User Management</h1>
        <p className="text-sm text-slate-500 dark:text-slate-400">
          Administer customer accounts, suspend/freeze users, and review KYC submissions.
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

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <UserCheck className="h-5 w-5 text-amber-500" />
            Registered Customer Accounts
          </CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="p-8 text-center text-slate-500">Loading user accounts...</div>
          ) : users.length === 0 ? (
            <div className="p-8 text-center text-slate-500">No users found.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="border-b border-slate-200 text-xs font-semibold text-slate-500 dark:border-slate-800">
                  <tr>
                    <th className="py-3 px-4">User ID</th>
                    <th className="py-3 px-4">Email</th>
                    <th className="py-3 px-4">Name</th>
                    <th className="py-3 px-4">Role</th>
                    <th className="py-3 px-4">Status</th>
                    <th className="py-3 px-4">KYC</th>
                    <th className="py-3 px-4">Registered</th>
                    <th className="py-3 px-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                  {users.map((u) => (
                    <tr key={u.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/50">
                      <td className="py-3 px-4 font-mono text-xs">{u.id}</td>
                      <td className="py-3 px-4 font-medium">{u.email}</td>
                      <td className="py-3 px-4 text-slate-600 dark:text-slate-300">
                        {u.firstName || u.lastName ? `${u.firstName || ''} ${u.lastName || ''}`.trim() : '—'}
                      </td>
                      <td className="py-3 px-4">
                        <span className="rounded bg-slate-100 px-2 py-0.5 text-xs font-semibold dark:bg-slate-800">
                          {u.role}
                        </span>
                      </td>
                      <td className="py-3 px-4">
                        <select
                          value={u.status}
                          onChange={(e) => handleStatusChange(u.id, e.target.value)}
                          className={`rounded px-2 py-1 text-xs font-semibold border ${
                            u.status === 'ACTIVE'
                              ? 'bg-emerald-50 text-emerald-800 border-emerald-200 dark:bg-emerald-950/40'
                              : 'bg-rose-50 text-rose-800 border-rose-200 dark:bg-rose-950/40'
                          }`}
                        >
                          <option value="ACTIVE">ACTIVE</option>
                          <option value="SUSPENDED">SUSPENDED</option>
                          <option value="FROZEN">FROZEN</option>
                          <option value="LOCKED">LOCKED</option>
                        </select>
                      </td>
                      <td className="py-3 px-4">
                        <span
                          className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                            u.kycStatus === 'VERIFIED'
                              ? 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300'
                              : u.kycStatus === 'PENDING'
                              ? 'bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-300'
                              : 'bg-slate-100 text-slate-600 dark:bg-slate-800'
                          }`}
                        >
                          {u.kycStatus || 'UNVERIFIED'}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-xs text-slate-400">{formatDate(u.createdAt)}</td>
                      <td className="py-3 px-4 text-right">
                        {u.kycStatus !== 'VERIFIED' && (
                          <Button size="sm" variant="outline" onClick={() => handleVerifyKyc(u.id)} className="text-xs">
                            Approve KYC
                          </Button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {/* Pagination */}
              <div className="mt-4 flex items-center justify-between border-t border-slate-100 pt-4 dark:border-slate-800">
                <Button size="sm" variant="outline" disabled={page === 0} onClick={() => setPage(page - 1)}>
                  Previous
                </Button>
                <span className="text-xs text-slate-500">
                  Page {page + 1} of {totalPages}
                </span>
                <Button size="sm" variant="outline" disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>
                  Next
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
