import React, { useEffect, useState } from 'react'
import { useAuthStore } from '@/store/authStore'
import { walletsApi } from '@/services/endpoints/wallets'
import { paymentsApi } from '@/services/endpoints/payments'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { formatCurrency } from '@/utils/cn'
import { ArrowUpRight, CheckCircle2, AlertCircle, ShieldAlert } from 'lucide-react'

export function WithdrawalPage() {
  const user = useAuthStore((s) => s.user)
  const [wallets, setWallets] = useState([])
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')

  const [form, setForm] = useState({
    walletId: '',
    amount: '',
    bankAccountNumber: '',
    ifscCode: 'SBIN0001234',
    description: 'Simulated payout to bank'
  })

  useEffect(() => {
    async function load() {
      if (!user?.id) return
      try {
        const res = await walletsApi.getWalletsByUser(user.id)
        if (res.success && res.data?.length > 0) {
          setWallets(res.data)
          setForm((f) => ({ ...f, walletId: res.data[0].id }))
        }
      } catch (err) {
        console.error(err)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [user?.id])

  const selectedWallet = wallets.find((w) => String(w.id) === String(form.walletId))

  const handleWithdrawal = async (e) => {
    e.preventDefault()
    setError('')
    setResult(null)
    setSubmitting(true)

    try {
      const payload = {
        walletId: Number(form.walletId),
        amount: parseFloat(form.amount),
        currency: 'INR',
        bankAccountNumber: form.bankAccountNumber,
        ifscCode: form.ifscCode,
        idempotencyKey: crypto.randomUUID(),
        description: form.description
      }

      const res = await paymentsApi.withdrawal(payload)
      if (res.success) {
        setResult(res.data)
        const wRes = await walletsApi.getWalletsByUser(user.id)
        if (wRes.success) setWallets(wRes.data)
        setForm((f) => ({ ...f, amount: '', bankAccountNumber: '' }))
      }
    } catch (err) {
      setError(err.message || 'Withdrawal failed')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Withdraw Funds</h1>
        <p className="text-sm text-slate-500 dark:text-slate-400">
          Simulate a real-time IMPS / NEFT payout from your SmartPay wallet to your external bank account.
        </p>
      </div>

      <div className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-xs text-amber-800 dark:border-amber-900 dark:bg-amber-950/40 dark:text-amber-300 flex items-start gap-2">
        <ShieldAlert className="h-4 w-4 shrink-0 mt-0.5" />
        <span>
          <strong>Simulated Payout:</strong> Payouts debit the internal wallet balance and record a simulated banking settlement reference.
        </span>
      </div>

      {result && (
        <div className="rounded-xl border border-emerald-200 bg-emerald-50 p-6 dark:border-emerald-900 dark:bg-emerald-950/30">
          <div className="flex items-center gap-3">
            <CheckCircle2 className="h-8 w-8 text-emerald-600 dark:text-emerald-400 shrink-0" />
            <div>
              <h3 className="font-semibold text-emerald-900 dark:text-emerald-200">Withdrawal Dispatched (Simulated)!</h3>
              <p className="text-sm text-emerald-700 dark:text-emerald-300">
                Debited {formatCurrency(result.amount, result.currency)}. Payout Reference: <span className="font-mono font-bold">{result.paymentRef}</span>
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
            <ArrowUpRight className="h-5 w-5 text-rose-600" />
            Simulated Bank Withdrawal (IMPS/NEFT)
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleWithdrawal} className="space-y-4">
            <div>
              <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Debit From Wallet</label>
              <select
                value={form.walletId}
                onChange={(e) => setForm({ ...form, walletId: e.target.value })}
                className="mt-1 block w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100"
              >
                {wallets.map((w) => (
                  <option key={w.id} value={w.id}>
                    {w.walletNumber} — Available: {formatCurrency(w.balance, w.currency)} ({w.status})
                  </option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Recipient Bank Account Number</label>
                <Input
                  required
                  placeholder="e.g. 987654321098"
                  value={form.bankAccountNumber}
                  onChange={(e) => setForm({ ...form, bankAccountNumber: e.target.value })}
                  className="mt-1 font-mono"
                />
              </div>
              <div>
                <label className="text-xs font-medium text-slate-600 dark:text-slate-400">IFSC Code</label>
                <Input
                  required
                  placeholder="e.g. HDFC0001234"
                  value={form.ifscCode}
                  onChange={(e) => setForm({ ...form, ifscCode: e.target.value })}
                  className="mt-1 uppercase font-mono"
                />
              </div>
            </div>

            <div>
              <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Withdrawal Amount (INR)</label>
              <Input
                required
                type="number"
                step="0.01"
                min="1"
                placeholder="500.00"
                value={form.amount}
                onChange={(e) => setForm({ ...form, amount: e.target.value })}
                className="mt-1"
              />
              {selectedWallet && (
                <p className="text-xs text-slate-400 mt-1">Available balance: {formatCurrency(selectedWallet.balance, selectedWallet.currency)}</p>
              )}
            </div>

            <div>
              <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Remarks</label>
              <Input
                value={form.description}
                onChange={(e) => setForm({ ...form, description: e.target.value })}
                className="mt-1"
              />
            </div>

            <div className="pt-2">
              <Button type="submit" disabled={submitting || !form.walletId || !form.amount} className="w-full">
                {submitting ? 'Executing Payout...' : 'Confirm Withdrawal'}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
