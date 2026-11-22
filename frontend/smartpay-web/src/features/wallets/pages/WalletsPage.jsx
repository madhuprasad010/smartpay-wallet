import React, { useEffect, useState } from 'react'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Dialog } from '@/components/ui/dialog'
import { StatusBadge } from '@/components/common/StatusBadge'
import { formatCurrency, formatDate } from '@/utils/cn'
import { walletsApi } from '@/services/endpoints/wallets'
import { useAuthStore } from '@/store/authStore'
import { Plus, Wallet as WalletIcon } from 'lucide-react'

export function WalletsPage() {
  const user = useAuthStore((s) => s.user)
  const [wallets, setWallets] = useState([])
  const [open, setOpen] = useState(false)
  const [currency, setCurrency] = useState('USD')
  const [loading, setLoading] = useState(false)

  const loadWallets = async () => {
    if (user?.id) {
      const res = await walletsApi.getWalletsByUser(user.id)
      if (res.success) setWallets(res.data || [])
    }
  }

  useEffect(() => {
    loadWallets()
  }, [user])

  const handleCreate = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      await walletsApi.createWallet({ userId: user.id, currency })
      setOpen(false)
      loadWallets()
    } catch (e) {
      console.error(e)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Digital Wallets</h1>
          <p className="text-sm text-slate-500">Manage your multicurrency accounts and ledger records.</p>
        </div>
        <Button onClick={() => setOpen(true)} className="gap-2">
          <Plus className="h-4 w-4" /> Create Wallet
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {wallets.map((w) => (
          <Card key={w.id} className="relative overflow-hidden">
            <div className="h-2 bg-gradient-to-r from-blue-600 to-indigo-600" />
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <span className="text-xs font-mono font-medium text-slate-500">{w.walletNumber}</span>
              <StatusBadge status={w.status} />
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <p className="text-xs text-slate-500">Current Balance</p>
                <h3 className="text-3xl font-bold font-mono tracking-tight mt-1">
                  {formatCurrency(w.balance, w.currency)}
                </h3>
              </div>
              <div className="flex justify-between items-center text-xs text-slate-500 pt-4 border-t border-slate-100 dark:border-slate-800">
                <span>Created {formatDate(w.createdAt)}</span>
                <span className="font-semibold uppercase">{w.currency}</span>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <Dialog open={open} onClose={() => setOpen(false)} title="Create New Digital Wallet">
        <form onSubmit={handleCreate} className="space-y-4 mt-4">
          <div className="space-y-1.5">
            <label className="text-sm font-medium">Select Currency</label>
            <select
              value={currency}
              onChange={(e) => setCurrency(e.target.value)}
              className="w-full rounded-lg border border-slate-200 p-2.5 text-sm dark:border-slate-800 dark:bg-slate-900"
            >
              <option value="USD">USD — US Dollar</option>
              <option value="EUR">EUR — Euro</option>
              <option value="GBP">GBP — British Pound</option>
              <option value="INR">INR — Indian Rupee</option>
            </select>
          </div>
          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? 'Creating...' : 'Confirm & Open Wallet'}
          </Button>
        </form>
      </Dialog>
    </div>
  )
}
