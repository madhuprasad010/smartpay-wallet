import React, { useEffect, useState } from 'react'
import { StatCard } from '@/components/common/StatCard'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { StatusBadge } from '@/components/common/StatusBadge'
import { formatCurrency, formatDate } from '@/utils/cn'
import { useAuthStore } from '@/store/authStore'
import { walletsApi } from '@/services/endpoints/wallets'
import { transactionsApi } from '@/services/endpoints/transactions'
import { Wallet, ArrowLeftRight, CreditCard, ShieldCheck, Plus } from 'lucide-react'
import { Link } from 'react-router-dom'
import { ResponsiveContainer, AreaChart, Area, XAxis, YAxis, Tooltip } from 'recharts'

const sampleChartData = [
  { day: 'Mon', balance: 12400 },
  { day: 'Tue', balance: 13200 },
  { day: 'Wed', balance: 12900 },
  { day: 'Thu', balance: 14500 },
  { day: 'Fri', balance: 15800 },
  { day: 'Sat', balance: 15400 },
  { day: 'Sun', balance: 16250 }
]

export function DashboardPage() {
  const user = useAuthStore((s) => s.user)
  const [wallets, setWallets] = useState([])
  const [transactions, setTransactions] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function loadData() {
      try {
        if (user?.id) {
          const wRes = await walletsApi.getWalletsByUser(user.id)
          if (wRes.success) setWallets(wRes.data || [])
        }
        const tRes = await transactionsApi.getAll({ size: 5 })
        if (tRes.success) setTransactions(tRes.data?.content || [])
      } catch (e) {
        console.error(e)
      } finally {
        setLoading(false)
      }
    }
    loadData()
  }, [user])

  const totalBalance = wallets.reduce((acc, w) => acc + Number(w.balance || 0), 0)

  return (
    <div className="space-y-8">
      {/* Welcome Bar */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Overview</h1>
          <p className="text-sm text-slate-500">Welcome back to your financial command center.</p>
        </div>
        <div className="flex gap-3">
          <Link to="/wallets">
            <Button variant="outline" className="gap-2">
              <Plus className="h-4 w-4" /> New Wallet
            </Button>
          </Link>
          <Link to="/transactions">
            <Button className="gap-2">
              <ArrowLeftRight className="h-4 w-4" /> Transfer
            </Button>
          </Link>
        </div>
      </div>

      {/* Metrics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <StatCard
          title="Total Portfolio"
          value={formatCurrency(totalBalance, 'USD')}
          change="+12.5%"
          trend="up"
          icon={Wallet}
        />
        <StatCard
          title="Active Wallets"
          value={wallets.length}
          change="+1 new"
          trend="up"
          icon={CreditCard}
        />
        <StatCard
          title="Recent Transfers"
          value={transactions.length}
          change="+4 this week"
          trend="up"
          icon={ArrowLeftRight}
        />
        <StatCard
          title="Security Status"
          value="Protected"
          icon={ShieldCheck}
        />
      </div>

      {/* Financial Analytics Chart */}
      <Card>
        <CardHeader>
          <CardTitle>Portfolio Trajectory</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-72 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={sampleChartData}>
                <defs>
                  <linearGradient id="colorBal" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#2563eb" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#2563eb" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <XAxis dataKey="day" stroke="#94a3b8" />
                <YAxis stroke="#94a3b8" />
                <Tooltip />
                <Area type="monotone" dataKey="balance" stroke="#2563eb" fillOpacity={1} fill="url(#colorBal)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </CardContent>
      </Card>

      {/* Recent Transactions Table */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle>Recent Transactions</CardTitle>
          <Link to="/transactions" className="text-sm font-medium text-blue-600 hover:underline">
            View all
          </Link>
        </CardHeader>
        <CardContent>
          {transactions.length === 0 ? (
            <p className="py-8 text-center text-sm text-slate-500">No recent transactions recorded.</p>
          ) : (
            <div className="divide-y divide-slate-100 dark:divide-slate-800">
              {transactions.map((t) => (
                <div key={t.id} className="flex items-center justify-between py-3">
                  <div>
                    <p className="font-medium text-sm">{t.transactionRef}</p>
                    <p className="text-xs text-slate-500">{formatDate(t.createdAt)}</p>
                  </div>
                  <div className="flex items-center gap-4">
                    <StatusBadge status={t.status} />
                    <span className="font-mono font-medium text-sm">
                      {formatCurrency(t.amount, t.currency)}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
