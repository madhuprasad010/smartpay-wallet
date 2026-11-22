import React, { useEffect, useState } from 'react'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Dialog } from '@/components/ui/dialog'
import { FormField } from '@/components/forms/FormField'
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from '@/components/ui/table'
import { StatusBadge } from '@/components/common/StatusBadge'
import { formatCurrency, formatDate } from '@/utils/cn'
import { transactionsApi } from '@/services/endpoints/transactions'
import { ArrowLeftRight } from 'lucide-react'

export function TransactionsPage() {
  const [transactions, setTransactions] = useState([])
  const [open, setOpen] = useState(false)
  const [form, setForm] = useState({
    sourceWalletId: '1',
    destWalletId: '2',
    amount: '100.00',
    currency: 'USD',
    description: 'Direct Transfer'
  })
  const [loading, setLoading] = useState(false)

  const loadData = async () => {
    try {
      const res = await transactionsApi.getAll()
      if (res.success) setTransactions(res.data?.content || [])
    } catch (e) {
      console.error(e)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const handleTransfer = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      await transactionsApi.transfer({
        idempotencyKey: crypto.randomUUID(),
        sourceWalletId: Number(form.sourceWalletId),
        destWalletId: Number(form.destWalletId),
        amount: Number(form.amount),
        currency: form.currency,
        description: form.description
      })
      setOpen(false)
      loadData()
    } catch (e) {
      alert(e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Transactions</h1>
          <p className="text-sm text-slate-500">Real-time financial transfer ledger with double-entry idempotency.</p>
        </div>
        <Button onClick={() => setOpen(true)} className="gap-2">
          <ArrowLeftRight className="h-4 w-4" /> Initiate Transfer
        </Button>
      </div>

      <Card>
        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Reference</TableHead>
                <TableHead>Type</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Source / Dest</TableHead>
                <TableHead>Date</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {transactions.map((t) => (
                <TableRow key={t.id}>
                  <TableCell className="font-mono text-xs">{t.transactionRef}</TableCell>
                  <TableCell>{t.type}</TableCell>
                  <TableCell className="font-mono font-medium">
                    {formatCurrency(t.amount, t.currency)}
                  </TableCell>
                  <TableCell>
                    <StatusBadge status={t.status} />
                  </TableCell>
                  <TableCell className="text-xs text-slate-500">
                    W#{t.sourceWalletId} → W#{t.destWalletId}
                  </TableCell>
                  <TableCell className="text-xs text-slate-500">{formatDate(t.createdAt)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <Dialog open={open} onClose={() => setOpen(false)} title="Initiate Instant Money Transfer">
        <form onSubmit={handleTransfer} className="space-y-4 mt-4">
          <div className="grid grid-cols-2 gap-3">
            <FormField
              label="Source Wallet ID"
              value={form.sourceWalletId}
              onChange={(e) => setForm({ ...form, sourceWalletId: e.target.value })}
              required
            />
            <FormField
              label="Destination Wallet ID"
              value={form.destWalletId}
              onChange={(e) => setForm({ ...form, destWalletId: e.target.value })}
              required
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <FormField
              label="Amount"
              type="number"
              step="0.01"
              value={form.amount}
              onChange={(e) => setForm({ ...form, amount: e.target.value })}
              required
            />
            <FormField
              label="Currency"
              value={form.currency}
              onChange={(e) => setForm({ ...form, currency: e.target.value })}
              required
            />
          </div>
          <FormField
            label="Memo / Description"
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
          />
          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? 'Processing Ledger...' : 'Execute Transfer'}
          </Button>
        </form>
      </Dialog>
    </div>
  )
}
