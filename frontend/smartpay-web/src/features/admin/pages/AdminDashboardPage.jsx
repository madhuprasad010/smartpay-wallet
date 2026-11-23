import React, { useEffect, useState } from 'react'
import { usersApi } from '@/services/endpoints/users'
import { transactionsApi } from '@/services/endpoints/transactions'
import { walletsApi } from '@/services/endpoints/wallets'
import { auditApi } from '@/services/endpoints/audit'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { formatCurrency, formatDate } from '@/utils/cn'
import { Shield, Users, ArrowLeftRight, CheckCircle2, AlertTriangle, FileText, RefreshCw, Scale } from 'lucide-react'
import { Link } from 'react-router-dom'

export function AdminDashboardPage() {
  const [stats, setStats] = useState({
    userCount: 0,
    txnCount: 0,
    auditCount: 0
  })
  const [reconciliations, setReconciliations] = useState([])
  const [reconciling, setReconciling] = useState(false)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function load() {
      try {
        const [uRes, tRes, aRes] = await Promise.all([
          usersApi.getAllUsers({ size: 1 }),
          transactionsApi.getAll({ size: 1 }),
          auditApi.getAll({ size: 1 })
        ])

        setStats({
          userCount: uRes.data?.totalElements || 0,
          txnCount: tRes.data?.totalElements || 0,
          auditCount: aRes.data?.totalElements || 0
        })
      } catch (err) {
        console.error(err)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  const handleRunReconciliation = async () => {
    setReconciling(true)
    try {
      const res = await walletsApi.reconcileAll()
      if (res.success) {
        setReconciliations(res.data || [])
      }
    } catch (err) {
      console.error(err)
    } finally {
      setReconciling(false)
    }
  }

  return (
    <div className="space-y-8">
      <div>
        <div className="flex items-center gap-2">
          <Shield className="h-6 w-6 text-amber-500" />
          <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Administrator Console</h1>
        </div>
        <p className="text-sm text-slate-500 dark:text-slate-400">
          Executive controls, core financial ledger audit, user account status, and transaction reversal.
        </p>
      </div>

      {/* Metrics Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card>
          <CardContent className="p-6 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">Registered Users</p>
              <p className="text-3xl font-bold text-slate-900 dark:text-slate-100 mt-2">{stats.userCount}</p>
              <Link to="/admin/users" className="text-xs font-semibold text-blue-600 hover:underline mt-2 inline-block">
                Manage Users & KYC →
              </Link>
            </div>
            <div className="rounded-full bg-blue-100 p-4 text-blue-600 dark:bg-blue-900/40 dark:text-blue-400">
              <Users className="h-6 w-6" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">Total System Transactions</p>
              <p className="text-3xl font-bold text-slate-900 dark:text-slate-100 mt-2">{stats.txnCount}</p>
              <Link to="/admin/transactions" className="text-xs font-semibold text-blue-600 hover:underline mt-2 inline-block">
                View Ledger Transactions →
              </Link>
            </div>
            <div className="rounded-full bg-emerald-100 p-4 text-emerald-600 dark:bg-emerald-900/40 dark:text-emerald-400">
              <ArrowLeftRight className="h-6 w-6" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">Security Audit Events</p>
              <p className="text-3xl font-bold text-slate-900 dark:text-slate-100 mt-2">{stats.auditCount}</p>
              <Link to="/admin/audit" className="text-xs font-semibold text-blue-600 hover:underline mt-2 inline-block">
                Inspect Audit Trail →
              </Link>
            </div>
            <div className="rounded-full bg-purple-100 p-4 text-purple-600 dark:bg-purple-900/40 dark:text-purple-400">
              <FileText className="h-6 w-6" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Authoritative Financial Reconciliation */}
      <Card className="border-t-4 border-t-amber-500 shadow-sm">
        <CardHeader className="flex flex-row items-center justify-between">
          <div>
            <CardTitle className="flex items-center gap-2">
              <Scale className="h-5 w-5 text-amber-500" />
              Authoritative Double-Entry Ledger Reconciliation
            </CardTitle>
            <p className="text-xs text-slate-500 mt-1">
              Compares materialized wallet balance projections against the immutable sum of ledger debits and credits.
            </p>
          </div>
          <Button onClick={handleRunReconciliation} disabled={reconciling} className="flex items-center gap-2">
            <RefreshCw className={`h-4 w-4 ${reconciling ? 'animate-spin' : ''}`} />
            {reconciling ? 'Running Audit...' : 'Reconcile All Wallets'}
          </Button>
        </CardHeader>
        <CardContent>
          {reconciliations.length === 0 ? (
            <p className="text-sm text-slate-500 text-center py-6">
              Click "Reconcile All Wallets" above to audit mathematical consistency across all accounts.
            </p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="border-b border-slate-200 text-xs font-semibold text-slate-500 dark:border-slate-800">
                  <tr>
                    <th className="py-3 px-4">Wallet ID</th>
                    <th className="py-3 px-4">Wallet Number</th>
                    <th className="py-3 px-4 text-right">Projected Balance</th>
                    <th className="py-3 px-4 text-right">Ledger Sum</th>
                    <th className="py-3 px-4 text-center">Entries</th>
                    <th className="py-3 px-4 text-right">Integrity Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                  {reconciliations.map((r) => (
                    <tr key={r.walletId} className="hover:bg-slate-50 dark:hover:bg-slate-800/50">
                      <td className="py-3 px-4 font-mono text-xs">{r.walletId}</td>
                      <td className="py-3 px-4 font-mono font-medium">{r.walletNumber}</td>
                      <td className="py-3 px-4 text-right font-medium">{formatCurrency(r.projectedBalance)}</td>
                      <td className="py-3 px-4 text-right font-medium">{formatCurrency(r.calculatedLedgerBalance)}</td>
                      <td className="py-3 px-4 text-center">{r.totalLedgerEntries}</td>
                      <td className="py-3 px-4 text-right">
                        {r.balanced ? (
                          <span className="inline-flex items-center gap-1 rounded-full bg-emerald-100 px-2.5 py-0.5 text-xs font-semibold text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300">
                            <CheckCircle2 className="h-3 w-3" /> Balanced (0.00 Discrepancy)
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1 rounded-full bg-rose-100 px-2.5 py-0.5 text-xs font-semibold text-rose-800 dark:bg-rose-900/40 dark:text-rose-300">
                            <AlertTriangle className="h-3 w-3" /> Discrepancy: {formatCurrency(r.discrepancy)}
                          </span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
