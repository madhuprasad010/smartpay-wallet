import React, { useEffect, useState } from 'react'
import { transactionsApi } from '@/services/endpoints/transactions'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { StatusBadge } from '@/components/common/StatusBadge'
import { formatCurrency, formatDate } from '@/utils/cn'
import { ReceiptText, RotateCcw, Search, CheckCircle2, AlertCircle, X } from 'lucide-react'

export function AdminTransactionsPage() {
  const [transactions, setTransactions] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(true)
  const [walletFilter, setWalletFilter] = useState('')
  const [reversalModalOpen, setReversalModalOpen] = useState(false)
  const [targetTxn, setTargetTxn] = useState(null)
  const [reason, setReason] = useState('Administrative error correction')
  const [submittingReversal, setSubmittingReversal] = useState(false)
  const [message, setMessage] = useState({ type: '', text: '' })

  useEffect(() => {
    loadTransactions()
  }, [page])

  async function loadTransactions() {
    try {
      setLoading(true)
      const params = { page, size: 15 }
      if (walletFilter) params.walletId = Number(walletFilter)
      const res = await transactionsApi.getAll(params)
      if (res.success && res.data) {
        setTransactions(res.data.content || [])
        setTotalPages(res.data.totalPages || 1)
      }
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = (e) => {
    e.preventDefault()
    setPage(0)
    loadTransactions()
  }

  const handleConfirmReversal = async (e) => {
    e.preventDefault()
    if (!targetTxn) return
    setSubmittingReversal(true)
    setMessage({ type: '', text: '' })

    try {
      const res = await transactionsApi.reverse(targetTxn.transactionRef, { reason })
      if (res.success) {
        setReversalModalOpen(false)
        setMessage({
          type: 'success',
          text: `Transaction ${targetTxn.transactionRef} successfully reversed! Reversal reference: ${res.data.transactionRef}`
        })
        loadTransactions()
      }
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Reversal failed' })
    } finally {
      setSubmittingReversal(false)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Transaction Management</h1>
        <p className="text-sm text-slate-500 dark:text-slate-400">
          Search ledger transactions across all wallets and perform immutable compensating reversals.
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

      {/* Filter Bar */}
      <form onSubmit={handleSearch} className="flex gap-3 max-w-md">
        <Input
          type="number"
          placeholder="Filter by Wallet ID..."
          value={walletFilter}
          onChange={(e) => setWalletFilter(e.target.value)}
        />
        <Button type="submit" className="flex items-center gap-1.5 shrink-0">
          <Search className="h-4 w-4" />
          Filter
        </Button>
      </form>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <ReceiptText className="h-5 w-5 text-amber-500" />
            Global Ledger Transactions
          </CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="p-8 text-center text-slate-500">Loading ledger records...</div>
          ) : transactions.length === 0 ? (
            <div className="p-8 text-center text-slate-500">No transactions found.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="border-b border-slate-200 text-xs font-semibold text-slate-500 dark:border-slate-800">
                  <tr>
                    <th className="py-3 px-4">Reference</th>
                    <th className="py-3 px-4">Type</th>
                    <th className="py-3 px-4">Source Wallet</th>
                    <th className="py-3 px-4">Dest Wallet</th>
                    <th className="py-3 px-4 text-right">Amount</th>
                    <th className="py-3 px-4">Status</th>
                    <th className="py-3 px-4">Timestamp</th>
                    <th className="py-3 px-4 text-right">Operations</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                  {transactions.map((t) => (
                    <tr key={t.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/50">
                      <td className="py-3 px-4 font-mono font-medium text-xs">{t.transactionRef}</td>
                      <td className="py-3 px-4">
                        <span className="rounded bg-slate-100 px-2 py-0.5 text-xs font-semibold dark:bg-slate-800">
                          {t.type}
                        </span>
                      </td>
                      <td className="py-3 px-4 font-mono text-xs">{t.sourceWalletId || 'External'}</td>
                      <td className="py-3 px-4 font-mono text-xs">{t.destWalletId || 'External'}</td>
                      <td className="py-3 px-4 text-right font-medium">{formatCurrency(t.amount, t.currency)}</td>
                      <td className="py-3 px-4">
                        <StatusBadge status={t.status} />
                      </td>
                      <td className="py-3 px-4 text-xs text-slate-400">{formatDate(t.createdAt)}</td>
                      <td className="py-3 px-4 text-right">
                        {t.status === 'COMPLETED' && t.type === 'TRANSFER' && (
                          <Button
                            size="sm"
                            variant="outline"
                            className="text-xs text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/40"
                            onClick={() => {
                              setTargetTxn(t)
                              setReversalModalOpen(true)
                            }}
                          >
                            <RotateCcw className="h-3 w-3 mr-1" />
                            Reverse
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

      {/* Reversal Confirmation Modal */}
      {reversalModalOpen && targetTxn && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4">
          <div className="w-full max-w-md rounded-xl bg-white p-6 shadow-xl dark:bg-slate-900">
            <div className="flex items-center justify-between pb-4 border-b border-slate-200 dark:border-slate-800">
              <h3 className="text-lg font-semibold text-slate-900 dark:text-slate-100">Confirm Reversal</h3>
              <button onClick={() => setReversalModalOpen(false)} className="text-slate-400 hover:text-slate-600">
                <X className="h-5 w-5" />
              </button>
            </div>
            <form onSubmit={handleConfirmReversal} className="mt-4 space-y-4">
              <div className="rounded-lg bg-amber-50 p-3 text-xs text-amber-800 dark:bg-amber-950/40 dark:text-amber-300">
                <strong>Immutable Audit Rule:</strong> Reversing this transfer will not alter historical ledger rows. It will create compensating debit and credit ledger entries and mark the original transaction as REVERSED.
              </div>
              <div>
                <p className="text-xs text-slate-500">Transaction Reference:</p>
                <p className="font-mono font-bold text-sm text-slate-800 dark:text-slate-200">{targetTxn.transactionRef}</p>
                <p className="text-xs text-slate-500 mt-2">Amount to Reverse:</p>
                <p className="font-bold text-base text-slate-900 dark:text-slate-100">{formatCurrency(targetTxn.amount, targetTxn.currency)}</p>
              </div>
              <div>
                <label className="text-xs font-medium text-slate-600 dark:text-slate-400">Reversal Reason (Required for Audit Log)</label>
                <Input
                  required
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  placeholder="e.g. Disputed transfer, customer chargeback"
                  className="mt-1"
                />
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <Button type="button" variant="outline" onClick={() => setReversalModalOpen(false)}>Cancel</Button>
                <Button type="submit" disabled={submittingReversal} className="bg-rose-600 hover:bg-rose-700 text-white">
                  {submittingReversal ? 'Reversing...' : 'Execute Compensating Reversal'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
