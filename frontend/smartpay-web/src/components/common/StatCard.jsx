import React from 'react'
import { Card, CardContent } from '@/components/ui/card'
import { TrendingUp, TrendingDown } from 'lucide-react'

export function StatCard({ title, value, change, trend = 'up', icon: Icon }) {
  return (
    <Card>
      <CardContent className="p-6">
        <div className="flex items-center justify-between">
          <p className="text-sm font-medium text-slate-500 dark:text-slate-400">{title}</p>
          {Icon && (
            <div className="rounded-lg bg-blue-50 p-2.5 text-blue-600 dark:bg-blue-950/40 dark:text-blue-400">
              <Icon className="h-5 w-5" />
            </div>
          )}
        </div>
        <div className="mt-3">
          <h3 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-50">{value}</h3>
          {change && (
            <div className="mt-2 flex items-center gap-1 text-xs">
              {trend === 'up' ? (
                <TrendingUp className="h-3.5 w-3.5 text-emerald-600" />
              ) : (
                <TrendingDown className="h-3.5 w-3.5 text-rose-600" />
              )}
              <span className={trend === 'up' ? 'font-medium text-emerald-600' : 'font-medium text-rose-600'}>
                {change}
              </span>
              <span className="text-slate-500 dark:text-slate-400">vs last month</span>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
