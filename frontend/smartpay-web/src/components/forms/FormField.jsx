import React from 'react'
import { Input } from '@/components/ui/input'

export function FormField({ label, error, ...props }) {
  return (
    <div className="space-y-1.5">
      {label && <label className="text-sm font-medium text-slate-700 dark:text-slate-300">{label}</label>}
      <Input error={!!error} {...props} />
      {error && <p className="text-xs text-rose-500">{error}</p>}
    </div>
  )
}
