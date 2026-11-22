import React from 'react'
import { ShieldCheck } from 'lucide-react'
import { Link } from 'react-router-dom'

export function Logo({ size = 'default' }) {
  const isLarge = size === 'large'
  return (
    <Link to="/" className="flex items-center gap-2">
      <div className={`flex items-center justify-center rounded-xl bg-blue-600 text-white ${isLarge ? 'h-10 w-10' : 'h-8 w-8'}`}>
        <ShieldCheck className={isLarge ? 'h-6 w-6' : 'h-5 w-5'} />
      </div>
      <span className={`font-bold tracking-tight text-slate-900 dark:text-white ${isLarge ? 'text-2xl' : 'text-xl'}`}>
        Smart<span className="text-blue-600">Pay</span>
      </span>
    </Link>
  )
}
