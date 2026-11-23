import React, { useEffect, useState } from 'react'
import { useAuthStore } from '@/store/authStore'
import { beneficiariesApi } from '@/services/endpoints/beneficiaries'
import { walletsApi } from '@/services/endpoints/wallets'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { formatCurrency, formatDate } from '@/utils/cn'
import { Users, Plus, Trash2, ArrowUpRight, CheckCircle2, AlertCircle, X } from 'lucide-react'

export function BeneficiariesPage() {
  const user = useAuthStore((s) => s.user)
  const [beneficiaries, setBeneficiaries] = useState([])
  const [wallets, setWallets] = useState([])
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [transferModalOpen, setTransferModalOpen] = useState(false)
  const [selectedBeneficiary, setSelectedBeneficiary] = useState(null)
  const [message, setMessage] = useState({ type: '', text: '' })

  const [form, setForm] = useState({
    name: '',
    accountNumber: '',
    bankCode: 'SBIN0001234',
    bankName: 'State Bank of India',
    email: ''
  })

  const [transferForm, setTransferForm] = useState({
    sourceWalletId: '',
    amount: '',
    description: ''
  })

  useEffect(() => {
    loadData()
  }, [user?.id])

  async function loadData() {
    if (!user?.id) return
    try {
      const [bRes, wRes] = await Promise.all([
        beneficiariesApi.getByUser(user.id),
        walletsApi.getWalletsByUser(user.id)
      ])
      if (bRes.success) setBeneficiaries(bRes.data || [])
      if (wRes.success && wRes.data?.length > 0) {
        setWallets(wRes.data)
        setTransferForm((f) => ({ ...f, sourceWalletId: wRes.data[0].id }))
      }
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleAdd = async (e) => {
    e.preventDefault()
    setMessage({ type: '', text: '' })
    try {
      const res = await beneficiariesApi.create({
        ...form,
        userId: user.id
      })
      if (res.success) {
        setBeneficiaries([res.data, ...beneficiaries])
        setModalOpen(false)
        setForm({ name: '', accountNumber: '', bankCode: 'SBIN0001234', bankName: 'State Bank of India', email: '' })
        setMessage({ type: 'success', text: 'Beneficiary added successfully!' })
      }
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Failed to add beneficiary' })
    }
  }

  const handleDelete = async (id) => {
    if (!confirm('Are you sure you want to remove this beneficiary?')) return
    try {
      await beneficiariesApi.delete(id)
      setBeneficiaries(beneficiaries.filter((b) => b.id !== id))
      setMessage({ type: 'success', text: 'Beneficiary removed.' })
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Failed to delete' })
    }
  }

  const handleTransferSubmit = async (e) => {
    e.preventDefault()
    if (!selectedBeneficiary) return
    try {
      const res = await beneficiariesApi.transfer(selectedBeneficiary.id, {
        sourceWalletId: Number(transferForm.sourceWalletId),
        amount: parseFloat(transferForm.amount),
        currency: 'INR',
        idempotencyKey: crypto.randomUUID(),
        description: transferForm.description || `Payment to ${selectedBeneficiary.name}`
      })
      if (res.success) {
        setTransferModalOpen(false)
        setTransferForm({ sourceWalletId: wallets[0]?.id || '', amount: '', description: '' })
        setMessage({ type: 'success', text: `Successfully sent ${formatCurrency(res.data.amount, res.data.currency)} to ${selectedBeneficiary.name}! Reference: ${res.data.transactionRef}` })
      }
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Beneficiary transfer failed' })
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Beneficiaries</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">Manage saved payout payees and internal wallet contacts</p>
        </div>
        <Button onClick={() => setModalOpen(true)} className="flex items-center gap-2">
          <Plus className="h-4 w-4" />
          Add Beneficiary
        </Button>
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

      {loading ? (
        <div className="p-8 text-center text-slate-500">Loading beneficiaries...</div>
      ) : beneficiaries.length === 0 ? (
        <Card className="text-center p-12">
          <Users className="mx-auto h-12 w-12 text-slate-400" />
          <h3 className="mt-4 text-base font-semibold text-slate-900 dark:text-slate-100">No Beneficiaries Added</h3>
          <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">Add frequent bank accounts or wallet contacts for quick transfers.</p>
          <Button onClick={() => setModalOpen(true)} className="mt-4">Add First Beneficiary</Button>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {beneficiaries.map((b) => (
            <Card key={b.id} className="hover:shadow-md transition-shadow">
              <CardContent className="p-6 space-y-4">
                <div className="flex items-start justify-between">
                  <div>
                    <h3 className="font-semibold text-slate-900 dark:text-slate-100">{b.name}</h3>
                    <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">{b.bankName || 'SmartPay Partner Bank'}</p>
                  </div>
                  <span className="inline-flex rounded-full bg-emerald-100 px-2.5 py-0.5 text-xs font-semibold text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300">
                    {b.status}
                  </span>
                </div>

                <div className="rounded-lg bg-slate-50 p-3 dark:bg-slate-800/50 text-xs space-y-1">
                  <div className="flex justify-between">
                    <span className="text-slate-500">Account / Wallet:</span>
                    <span className="font-mono font-medium text-slate-900 dark:text-slate-100">{b.accountNumber}</span>
                  </div>
                  {b.bankCode && (
                    <div className="flex justify-between">
                      <span className="text-slate-500">IFSC / Code:</span>
                      <span className="font-mono text-slate-700 dark:text-slate-300">{b.bankCode}</span>
                    </div>
                  )}
                  {b.email && (
                    <div className="flex justify-between">
                      <span className="text-slate-500">Email:</span>
                      <span className="text-slate-700 dark:text-slate-300">{b.email}</span>
                    </div>
                  )}
                </div>

                <div className="flex items-center gap-2 pt-2 border-t border-slate-100 dark:border-slate-800">
                  <Button
                    size="sm"
                    className="flex-1 flex items-center justify-center gap-1.5"
                    onClick={() => {
                      setSelectedBeneficiary(b)
                      setTransferModalOpen(true)
                    }}
                  >
                    <ArrowUpRight className="h-4 w-4" />
                    Send Money
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    className="text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/40"
                    onClick={() => handleDelete(b.id)}
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Add Beneficiary Modal */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4">
          <div className="w-full max-w-md rounded-xl bg-white p-6 shadow-xl dark:bg-slate-900">
            <div className="flex items-center justify-between pb-4 border-b border-slate-200 dark:border-slate-800">
              <h3 className="text-lg font-semibold text-slate-900 dark:text-slate-100">Add New Beneficiary</h3>
              <button onClick={() => setModalOpen(false)} className="text-slate-400 hover:text-slate-600">
                <X className="h-5 w-5" />
              </button>
            </div>
            <form onSubmit={handleAdd} className="mt-4 space-y-4">
              <div>
                <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Beneficiary Full Name</label>
                <Input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="e.g. Ramesh Kumar" className="mt-1" />
              </div>
              <div>
                <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Account Number or Wallet ID</label>
                <Input required value={form.accountNumber} onChange={(e) => setForm({ ...form, accountNumber: e.target.value })} placeholder="e.g. WLT-92847192 or Bank A/C Number" className="mt-1 font-mono" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Bank Name</label>
                  <Input value={form.bankName} onChange={(e) => setForm({ ...form, bankName: e.target.value })} placeholder="e.g. HDFC Bank" className="mt-1" />
                </div>
                <div>
                  <label className="text-xs font-medium text-slate-600 dark:text-slate-400">IFSC / Routing</label>
                  <Input value={form.bankCode} onChange={(e) => setForm({ ...form, bankCode: e.target.value })} placeholder="HDFC0001234" className="mt-1 uppercase font-mono" />
                </div>
              </div>
              <div>
                <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Email Address (Optional)</label>
                <Input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} placeholder="payee@example.com" className="mt-1" />
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <Button type="button" variant="outline" onClick={() => setModalOpen(false)}>Cancel</Button>
                <Button type="submit">Save Beneficiary</Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Transfer to Beneficiary Modal */}
      {transferModalOpen && selectedBeneficiary && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4">
          <div className="w-full max-w-md rounded-xl bg-white p-6 shadow-xl dark:bg-slate-900">
            <div className="flex items-center justify-between pb-4 border-b border-slate-200 dark:border-slate-800">
              <div>
                <h3 className="text-lg font-semibold text-slate-900 dark:text-slate-100">Send to {selectedBeneficiary.name}</h3>
                <p className="text-xs text-slate-500 font-mono">{selectedBeneficiary.accountNumber}</p>
              </div>
              <button onClick={() => setTransferModalOpen(false)} className="text-slate-400 hover:text-slate-600">
                <X className="h-5 w-5" />
              </button>
            </div>
            <form onSubmit={handleTransferSubmit} className="mt-4 space-y-4">
              <div>
                <label className="text-xs font-medium text-slate-600 dark:text-slate-400">From Wallet</label>
                <select
                  value={transferForm.sourceWalletId}
                  onChange={(e) => setTransferForm({ ...transferForm, sourceWalletId: e.target.value })}
                  className="mt-1 block w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100"
                >
                  {wallets.map((w) => (
                    <option key={w.id} value={w.id}>
                      {w.walletNumber} — Bal: {formatCurrency(w.balance, w.currency)}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Transfer Amount (INR)</label>
                <Input
                  required
                  type="number"
                  step="0.01"
                  min="1"
                  placeholder="0.00"
                  value={transferForm.amount}
                  onChange={(e) => setTransferForm({ ...transferForm, amount: e.target.value })}
                  className="mt-1"
                />
              </div>
              <div>
                <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Note / Remarks</label>
                <Input
                  placeholder="e.g. Payment for invoice #102"
                  value={transferForm.description}
                  onChange={(e) => setTransferForm({ ...transferForm, description: e.target.value })}
                  className="mt-1"
                />
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <Button type="button" variant="outline" onClick={() => setTransferModalOpen(false)}>Cancel</Button>
                <Button type="submit">Confirm Payout</Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
