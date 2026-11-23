import React, { useEffect, useState } from 'react'
import { useAuthStore } from '@/store/authStore'
import { walletsApi } from '@/services/endpoints/wallets'
import { transactionsApi } from '@/services/endpoints/transactions'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { formatCurrency } from '@/utils/cn'
import { ArrowLeftRight, CheckCircle2, AlertCircle, RefreshCw, ShieldCheck } from 'lucide-react'

export function TransferPage() {
  const user = useAuthStore((s) => s.user)
  const [wallets, setWallets] = useState([])
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')

  const [form, setForm] = useState({
    sourceWalletId: '',
    destWalletNumber: '',
    amount: '',
    currency: 'INR',
    description: '',
    idempotencyKey: crypto.randomUUID()
  })

  useEffect(() => {
    async function fetchWallets() {
      if (!user?.id) return
      try {
        const res = await walletsApi.getWalletsByUser(user.id)
        if (res.success && res.data?.length > 0) {
          setWallets(res.data)
          setForm((f) => ({ ...f, sourceWalletId: res.data[0].id }))
        }
      } catch (err) {
        console.error(err)
      } finally {
        setLoading(false)
      }
    }
    fetchWallets()
  }, [user?.id])

  const selectedWallet = wallets.find((w) => String(w.id) === String(form.sourceWalletId))

  const handleRefreshKey = () => {
    setForm((f) => ({ ...f, idempotencyKey: crypto.randomUUID() }))
  }

  const handleTransfer = async (e) => {
    e.preventDefault()
    setError('')
    setResult(null)
    setSubmitting(true)

    try {
      // Find destination wallet by number
      const destRes = await walletsApi.getWalletByNumber(form.destWalletNumber.trim())
      if (!destRes.success || !destRes.data) {
        throw new Error('Destination wallet not found. Please verify the wallet number.')
      }

      const destWallet = destRes.data

      const transferPayload = {
        idempotencyKey: form.idempotencyKey,
        sourceWalletId: Number(form.sourceWalletId),
        destWalletId: destWallet.id,
        amount: parseFloat(form.amount),
        currency: form.currency,
        description: form.description || 'Wallet Transfer'
      }

      const res = await transactionsApi.transfer(transferPayload)
      if (res.success) {
        setResult(res.data)
        // Refresh wallet balance
        const wRes = await walletsApi.getWalletsByUser(user.id)
        if (wRes.success) setWallets(wRes.data)
        // Reset form amount & new idempotency key
        setForm((f) => ({
          ...f,
          amount: '',
          description: '',
          destWalletNumber: '',
          idempotencyKey: crypto.randomUUID()
        }))
      }
    } catch (err) {
      setError(err.message || 'Transfer failed')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Transfer Money</h1>
        <p className="text-sm text-slate-500 dark:text-slate-400">
          Instant wallet-to-wallet transfer secured by double-entry ledger and atomic database transactions.
        </p>
      </div>

      {result && (
        <div className="rounded-xl border border-emerald-200 bg-emerald-50 p-6 dark:border-emerald-900 dark:bg-emerald-950/30">
          <div className="flex items-center gap-3">
            <CheckCircle2 className="h-8 w-8 text-emerald-600 dark:text-emerald-400 shrink-0" />
            <div>
              <h3 className="font-semibold text-emerald-900 dark:text-emerald-200">Transfer Successful!</h3>
              <p className="text-sm text-emerald-700 dark:text-emerald-300">
                Transferred {formatCurrency(result.amount, result.currency)} successfully. Reference: <span className="font-mono font-bold">{result.transactionRef}</span>
              </p>
            </div>
          </div>
        </div>
      )}

      {error && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800 dark:border-rose-900 dark:bg-rose-950/30 dark:text-rose-300 flex items-center gap-2">
          <AlertCircle className="h-5 w-5 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <ArrowLeftRight className="h-5 w-5 text-blue-600" />
            Peer-to-Peer Wallet Transfer
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleTransfer} className="space-y-4">
            <div>
              <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Source Wallet</label>
              <select
                value={form.sourceWalletId}
                onChange={(e) => setForm({ ...form, sourceWalletId: e.target.value })}
                className="mt-1 block w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100"
              >
                {wallets.map((w) => (
                  <option key={w.id} value={w.id}>
                    {w.walletNumber} — Available: {formatCurrency(w.balance, w.currency)} ({w.status})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Recipient Wallet Number</label>
              <Input
                required
                placeholder="e.g. WLT-ABC12345"
                value={form.destWalletNumber}
                onChange={(e) => setForm({ ...form, destWalletNumber: e.target.value })}
                className="mt-1 font-mono uppercase"
              />
              <p className="text-xs text-slate-400 mt-1">Enter the 12-character SmartPay wallet identifier.</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Amount (INR)</label>
                <Input
                  required
                  type="number"
                  step="0.01"
                  min="1"
                  placeholder="0.00"
                  value={form.amount}
                  onChange={(e) => setForm({ ...form, amount: e.target.value })}
                  className="mt-1"
                />
              </div>
              <div>
                <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Currency</label>
                <Input value={form.currency} disabled className="mt-1 bg-slate-50 dark:bg-slate-800/50" />
              </div>
            </div>

            <div>
              <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Transfer Note (Optional)</label>
              <Input
                placeholder="e.g. Dinner share, rent, invoice"
                value={form.description}
                onChange={(e) => setForm({ ...form, description: e.target.value })}
                className="mt-1"
              />
            </div>

            <div className="rounded-lg bg-slate-50 p-3 dark:bg-slate-800/50">
              <div className="flex items-center justify-between">
                <span className="text-xs font-medium text-slate-500">Idempotency Key (Audit Safe):</span>
                <button
                  type="button"
                  onClick={handleRefreshKey}
                  className="text-xs text-blue-600 hover:underline flex items-center gap-1 dark:text-blue-400"
                >
                  <RefreshCw className="h-3 w-3" /> Regenerate
                </button>
              </div>
              <p className="mt-1 font-mono text-xs text-slate-600 dark:text-slate-300 break-all">{form.idempotencyKey}</p>
            </div>

            <div className="pt-2">
              <Button type="submit" disabled={submitting || !form.sourceWalletId || !form.amount} className="w-full">
                {submitting ? 'Processing Ledger Transfer...' : 'Confirm & Send Money'}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
