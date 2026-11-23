import React, { useEffect, useState } from 'react'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Dialog } from '@/components/ui/dialog'
import { StatusBadge } from '@/components/common/StatusBadge'
import { formatCurrency, formatDate } from '@/utils/cn'
import { walletsApi } from '@/services/endpoints/wallets'
import { useAuthStore } from '@/store/authStore'
import { Plus, Wallet as WalletIcon, Lock, Unlock, ListOrdered, Scale, Shield } from 'lucide-react'

export function WalletsPage() {
  const user = useAuthStore((s) => s.user)
  const [wallets, setWallets] = useState([])
  const [open, setOpen] = useState(false)
  const [currency, setCurrency] = useState('INR')
  const [loading, setLoading] = useState(false)
  const [selectedWallet, setSelectedWallet] = useState(null)
  const [ledgerEntries, setLedgerEntries] = useState([])
  const [walletLimits, setWalletLimits] = useState(null)
  const [ledgerModalOpen, setLedgerModalOpen] = useState(false)

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

  const handleToggleFreeze = async (w) => {
    try {
      if (w.status === 'FROZEN') {
        await walletsApi.unfreeze(w.id)
      } else {
        await walletsApi.freeze(w.id)
      }
      loadWallets()
    } catch (e) {
      console.error(e)
    }
  }

  const handleViewLedger = async (w) => {
    setSelectedWallet(w)
    try {
      const [lRes, limRes] = await Promise.all([
        walletsApi.getLedger(w.id),
        walletsApi.getLimits(w.id)
      ])
      if (lRes.success) setLedgerEntries(lRes.data || [])
      if (limRes.success) setWalletLimits(limRes.data)
      setLedgerModalOpen(true)
    } catch (e) {
      console.error(e)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100">Digital Wallets & Ledger</h1>
          <p className="text-sm text-slate-500">Manage your multicurrency wallets, security state, and audit double-entry ledger entries.</p>
        </div>
        <Button onClick={() => setOpen(true)} className="gap-2">
          <Plus className="h-4 w-4" /> Create Wallet
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {wallets.map((w) => {
          const isFrozen = w.status === 'FROZEN'
          return (
            <Card key={w.id} className="relative overflow-hidden hover:shadow-md transition-shadow">
              <div className={`h-2 ${isFrozen ? 'bg-rose-500' : 'bg-gradient-to-r from-blue-600 to-indigo-600'}`} />
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <span className="text-xs font-mono font-medium text-slate-500">{w.walletNumber}</span>
                <StatusBadge status={w.status} />
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <p className="text-xs text-slate-500">Current Balance</p>
                  <h3 className="text-3xl font-bold font-mono tracking-tight mt-1 text-slate-900 dark:text-slate-100">
                    {formatCurrency(w.balance, w.currency)}
                  </h3>
                </div>

                <div className="flex justify-between items-center text-xs text-slate-500 pt-3 border-t border-slate-100 dark:border-slate-800">
                  <span>Created {formatDate(w.createdAt)}</span>
                  <span className="font-semibold uppercase">{w.currency}</span>
                </div>

                <div className="flex gap-2 pt-1">
                  <Button
                    size="sm"
                    variant="outline"
                    className="flex-1 flex items-center justify-center gap-1.5 text-xs"
                    onClick={() => handleViewLedger(w)}
                  >
                    <ListOrdered className="h-3.5 w-3.5" />
                    Ledger Audit
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    className={`flex items-center gap-1 text-xs ${
                      isFrozen ? 'text-emerald-600 hover:bg-emerald-50 dark:hover:bg-emerald-950/40' : 'text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/40'
                    }`}
                    onClick={() => handleToggleFreeze(w)}
                  >
                    {isFrozen ? <Unlock className="h-3.5 w-3.5" /> : <Lock className="h-3.5 w-3.5" />}
                    {isFrozen ? 'Unfreeze' : 'Freeze'}
                  </Button>
                </div>
              </CardContent>
            </Card>
          )
        })}
      </div>

      {/* Create Wallet Modal */}
      <Dialog open={open} onClose={() => setOpen(false)} title="Create New Digital Wallet">
        <form onSubmit={handleCreate} className="space-y-4 mt-4">
          <div className="space-y-1.5">
            <label className="text-sm font-medium">Select Currency</label>
            <select
              value={currency}
              onChange={(e) => setCurrency(e.target.value)}
              className="w-full rounded-lg border border-slate-200 p-2.5 text-sm dark:border-slate-800 dark:bg-slate-900"
            >
              <option value="INR">INR — Indian Rupee (Default)</option>
              <option value="USD">USD — US Dollar</option>
              <option value="EUR">EUR — Euro</option>
              <option value="GBP">GBP — British Pound</option>
            </select>
          </div>
          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? 'Creating...' : 'Confirm & Open Wallet'}
          </Button>
        </form>
      </Dialog>

      {/* Ledger Modal */}
      {ledgerModalOpen && selectedWallet && (
        <Dialog open={ledgerModalOpen} onClose={() => setLedgerModalOpen(false)} title={`Wallet Ledger: ${selectedWallet.walletNumber}`}>
          <div className="space-y-4 mt-2">
            {/* Wallet Limits Bar */}
            {walletLimits && (
              <div className="grid grid-cols-2 gap-3 rounded-lg bg-slate-50 p-3 text-xs dark:bg-slate-800/50">
                <div>
                  <span className="text-slate-500">Daily Transfer Limit:</span>
                  <p className="font-semibold text-slate-800 dark:text-slate-200">{formatCurrency(walletLimits.dailyLimit)}</p>
                </div>
                <div>
                  <span className="text-slate-500">Daily Remaining:</span>
                  <p className="font-semibold text-emerald-600">{formatCurrency(walletLimits.remainingDailyLimit)}</p>
                </div>
              </div>
            )}

            <p className="text-xs text-slate-500">Authoritative immutable ledger entries recorded for this wallet:</p>

            {ledgerEntries.length === 0 ? (
              <p className="text-sm text-center py-6 text-slate-500">No ledger entries recorded yet.</p>
            ) : (
              <div className="max-h-72 overflow-y-auto divide-y divide-slate-100 text-xs dark:divide-slate-800">
                {ledgerEntries.map((e) => {
                  const isCredit = e.entryType === 'CREDIT'
                  return (
                    <div key={e.id} className="py-2.5 flex items-center justify-between">
                      <div>
                        <div className="flex items-center gap-1.5">
                          <span className={`font-semibold rounded px-1.5 py-0.5 text-xs ${
                            isCredit ? 'bg-emerald-100 text-emerald-800 dark:bg-emerald-950/40 dark:text-emerald-300' : 'bg-rose-100 text-rose-800 dark:bg-rose-950/40 dark:text-rose-300'
                          }`}>
                            {e.entryType}
                          </span>
                          <span className="font-medium text-slate-800 dark:text-slate-200">{e.description || e.referenceId}</span>
                        </div>
                        <span className="text-[10px] text-slate-400">{formatDate(e.createdAt)}</span>
                      </div>
                      <div className="text-right">
                        <span className={`font-mono font-bold ${isCredit ? 'text-emerald-600' : 'text-rose-600'}`}>
                          {isCredit ? '+' : '-'}{formatCurrency(e.amount, selectedWallet.currency)}
                        </span>
                        <p className="text-[10px] text-slate-400">Bal: {formatCurrency(e.balanceAfter, selectedWallet.currency)}</p>
                      </div>
                    </div>
                  )
                })}
              </div>
            )}
          </div>
        </Dialog>
      )}
    </div>
  )
}
