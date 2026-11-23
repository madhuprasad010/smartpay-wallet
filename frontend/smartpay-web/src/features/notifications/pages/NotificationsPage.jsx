import React, { useEffect, useState } from 'react'
import { useAuthStore } from '@/store/authStore'
import { notificationsApi } from '@/services/endpoints/notifications'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { formatDate } from '@/utils/cn'
import { Bell, CheckCheck, CheckCircle2, AlertTriangle, ArrowDownLeft, ArrowUpRight, ShieldCheck, Mail } from 'lucide-react'

export function NotificationsPage() {
  const user = useAuthStore((s) => s.user)
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    load()
  }, [user?.id])

  async function load() {
    if (!user?.id) return
    try {
      const res = await notificationsApi.getByUser(user.id)
      if (res.success) setNotifications(res.data || [])
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleMarkRead = async (id) => {
    try {
      await notificationsApi.markRead(id)
      setNotifications(notifications.map((n) => (n.id === id ? { ...n, status: 'READ' } : n)))
    } catch (err) {
      console.error(err)
    }
  }

  const handleMarkAllRead = async () => {
    try {
      await notificationsApi.markAllRead(user.id)
      setNotifications(notifications.map((n) => ({ ...n, status: 'READ' })))
    } catch (err) {
      console.error(err)
    }
  }

  const getIcon = (type) => {
    switch (type) {
      case 'TRANSACTION_SUCCESS':
      case 'PAYMENT_SUCCESS':
        return <CheckCircle2 className="h-5 w-5 text-emerald-500" />
      case 'DEPOSIT':
      case 'DEPOSIT_SUCCESS':
        return <ArrowDownLeft className="h-5 w-5 text-blue-500" />
      case 'WITHDRAWAL':
      case 'WITHDRAWAL_SUCCESS':
        return <ArrowUpRight className="h-5 w-5 text-amber-500" />
      case 'SECURITY_ALERT':
      case 'SECURITY':
        return <AlertTriangle className="h-5 w-5 text-rose-500" />
      default:
        return <Bell className="h-5 w-5 text-slate-500" />
    }
  }

  const unreadCount = notifications.filter((n) => n.status !== 'READ').length

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Notifications</h1>
            {unreadCount > 0 && (
              <span className="rounded-full bg-blue-100 px-2.5 py-0.5 text-xs font-semibold text-blue-800 dark:bg-blue-900/40 dark:text-blue-300">
                {unreadCount} Unread
              </span>
            )}
          </div>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Real-time notifications for transaction events, security updates, and deposits.
          </p>
        </div>

        {unreadCount > 0 && (
          <Button variant="outline" onClick={handleMarkAllRead} className="flex items-center gap-2">
            <CheckCheck className="h-4 w-4" />
            Mark All as Read
          </Button>
        )}
      </div>

      {loading ? (
        <div className="p-8 text-center text-slate-500">Loading notifications...</div>
      ) : notifications.length === 0 ? (
        <Card className="text-center p-12">
          <Bell className="mx-auto h-12 w-12 text-slate-400" />
          <h3 className="mt-4 text-base font-semibold text-slate-900 dark:text-slate-100">All Caught Up!</h3>
          <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">You have no new alerts or notifications at this time.</p>
        </Card>
      ) : (
        <div className="space-y-3">
          {notifications.map((n) => {
            const isRead = n.status === 'READ'
            return (
              <Card
                key={n.id}
                className={`transition-colors ${
                  isRead ? 'opacity-75 bg-white dark:bg-slate-900' : 'border-l-4 border-l-blue-600 bg-blue-50/20 dark:bg-blue-950/20'
                }`}
              >
                <CardContent className="p-4 md:p-6 flex items-start gap-4">
                  <div className="rounded-full bg-slate-100 p-2.5 dark:bg-slate-800 shrink-0">
                    {getIcon(n.type)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between gap-2">
                      <h4 className="text-sm font-semibold text-slate-900 dark:text-slate-100">{n.subject}</h4>
                      <span className="text-xs text-slate-400 shrink-0">{formatDate(n.createdAt)}</span>
                    </div>
                    <p className="text-sm text-slate-600 dark:text-slate-300 mt-1 whitespace-pre-wrap">{n.body}</p>
                    <div className="mt-2 flex items-center justify-between">
                      <span className="text-xs uppercase font-semibold text-slate-400 tracking-wider">
                        {n.channel} • {n.type}
                      </span>
                      {!isRead && (
                        <button
                          onClick={() => handleMarkRead(n.id)}
                          className="text-xs font-semibold text-blue-600 hover:underline dark:text-blue-400"
                        >
                          Mark as Read
                        </button>
                      )}
                    </div>
                  </div>
                </CardContent>
              </Card>
            )
          })}
        </div>
      )}
    </div>
  )
}
