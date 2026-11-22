import React from 'react'
import { cva } from 'class-variance-authority'
import { cn } from '@/utils/cn'

const badgeVariants = cva(
  'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2',
  {
    variants: {
      variant: {
        default: 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300',
        secondary: 'bg-slate-100 text-slate-800 dark:bg-slate-800 dark:text-slate-300',
        success: 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-300',
        destructive: 'bg-rose-100 text-rose-800 dark:bg-rose-900/30 dark:text-rose-300',
        warning: 'bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-300',
        outline: 'text-slate-950 border border-slate-200 dark:text-slate-50 dark:border-slate-800'
      }
    },
    defaultVariants: {
      variant: 'default'
    }
  }
)

export function Badge({ className, variant, ...props }) {
  return <div className={cn(badgeVariants({ variant }), className)} {...props} />
}
