import React from 'react'
import { Outlet } from 'react-router-dom'
import { Logo } from '@/components/common/Logo'

export function AuthLayout() {
  return (
    <div className="flex min-h-screen items-center justify-center p-4 bg-slate-50 dark:bg-slate-950">
      <div className="w-full max-w-md space-y-6">
        <div className="text-center flex flex-col items-center">
          <Logo size="large" />
        </div>
        <Outlet />
      </div>
    </div>
  )
}
