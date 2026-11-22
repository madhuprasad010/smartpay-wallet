import React, { useState } from 'react'
import { Eye, EyeOff } from 'lucide-react'
import { Input } from '@/components/ui/input'

export const PasswordInput = React.forwardRef(({ error, label, ...props }, ref) => {
  const [show, setShow] = useState(false)

  return (
    <div className="space-y-1.5">
      {label && <label className="text-sm font-medium text-slate-700 dark:text-slate-300">{label}</label>}
      <div className="relative">
        <Input
          type={show ? 'text' : 'password'}
          error={!!error}
          ref={ref}
          className="pr-10"
          {...props}
        />
        <button
          type="button"
          onClick={() => setShow(!show)}
          className="absolute right-3 top-2.5 text-slate-400 hover:text-slate-600"
        >
          {show ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
        </button>
      </div>
      {error && <p className="text-xs text-rose-500">{error}</p>}
    </div>
  )
})
PasswordInput.displayName = 'PasswordInput'
