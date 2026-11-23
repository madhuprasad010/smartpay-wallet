import React, { useEffect, useState } from 'react'
import { useAuthStore } from '@/store/authStore'
import { walletsApi } from '@/services/endpoints/wallets'
import { paymentsApi } from '@/services/endpoints/payments'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { formatCurrency } from '@/utils/cn'
import { ArrowDownLeft, CheckCircle2, AlertCircle, ShieldAlert } from 'lucide-react'

export function DepositPage() {
  const user = useAuthStore((s) => s.user)
  const [wallets, setWallets] = useState([])
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')

  const [form, setForm] = useState({
    walletId: '',
    amount: '',
    paymentMethod: 'UPI_SIMULATED',
    description: 'Simulated top-up'
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

  const handleDeposit = async (e) => {
    e.preventDefault()
    setError('')
    setResult(null)
    setSubmitting(true)

    try {
      const payload = {
        walletId: Number(form.walletId),
        amount: parseFloat(form.amount),
        currency: 'INR',
        paymentMethod: form.paymentMethod,
        idempotencyKey: crypto.randomUUID(),
        description: form.description
      }

      const res = await paymentsApi.deposit(payload)
      if (res.success) {
        setResult(res.data)
        const wRes = await walletsApi.getWalletsByUser(user.id)
        if (wRes.success) setWallets(wRes.data)
        setForm((f) => ({ ...f, amount: '' }))
      }
    } catch (err) {
      setError(err.message || 'Deposit failed')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Deposit Funds</h1>
        <p className="text-sm text-slate-500 dark:text-slate-400">
          Simulate an incoming bank or payment gateway credit into your digital wallet.
        </p>
      </div>

      {/* FinTech Simulation Disclaimer */}
      <div className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-xs text-amber-800 dark:border-amber-900 dark:bg-amber-950/40 dark:text-amber-300 flex items-start gap-2">
        <ShieldAlert className="h-4 w-4 shrink-0 mt-0.5" />
        <span>
          <strong>Simulated Banking Environment:</strong> No real funds, cards, or bank credentials are required or stored. All credits are executed against the sandbox double-entry ledger.
        </span>
      </div>

      {result && (
        <div className="rounded-xl border border-emerald-200 bg-emerald-50 p-6 dark:border-emerald-900 dark:bg-emerald-950/30">
          <div className="flex items-center gap-3">
            <CheckCircle2 className="h-8 w-8 text-emerald-600 dark:text-emerald-400 shrink-0" />
            <div>
              <h3 className="font-semibold text-emerald-900 dark:text-emerald-200">Deposit Cleared (Simulated)!</h3>
              <p className="text-sm text-emerald-700 dark:text-emerald-300">
                Credited {formatCurrency(result.amount, result.currency)} to wallet. Payment Reference: <span className="font-mono font-bold">{result.paymentRef}</span>
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
            <ArrowDownLeft className="h-5 w-5 text-emerald-600" />
            Simulated Deposit Adapter
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleDeposit} className="space-y-4">
            <div>
              <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Target Wallet</label>
              <select
                value={form.walletId}
                onChange={(e) => setForm({ ...form, walletId: e.target.value })}
                className="mt-1 block w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100"
              >
                {wallets.map((w) => (
                  <option key={w.id} value={w.id}>
                    {w.walletNumber} — Current Balance: {formatCurrency(w.balance, w.currency)}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Simulated Payment Channel</label>
              <select
                value={form.paymentMethod}
                onChange={(e) => setForm({ ...form, paymentMethod: e.target.value })}
                className="mt-1 block w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100"
              >
                <option value="UPI_SIMULATED">Instant UPI Payment (Virtual PSP)</option>
                <option value="NETBANKING_SIMULATED">NetBanking Gateway (Simulated HDFC/ICICI/SBI)</option>
                <option value="CARD_SIMULATED">Debit Card Authorization (Sandbox)</option>
              </select>
            </div>

            <div>
              <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Deposit Amount (INR)</label>
              <Input
                required
                type="number"
                step="0.01"
                min="1"
                placeholder="1000.00"
                value={form.amount}
                onChange={(e) => setForm({ ...form, amount: e.target.value })}
                className="mt-1"
              />
            </div>

            <div>
              <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Transaction Reference Note</label>
              <Input
                value={form.description}
                onChange={(e) => setForm({ ...form, description: e.target.value })}
                className="mt-1"
              />
            </div>

            <div className="pt-2">
              <Button type="submit" disabled={submitting || !form.walletId || !form.amount} className="w-full">
                {submitting ? 'Simulating Clearance...' : 'Simulate Deposit Credit'}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
