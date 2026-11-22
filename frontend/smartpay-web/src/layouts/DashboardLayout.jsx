import React from 'react'
import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom'
import { Logo } from '@/components/common/Logo'
import { ThemeToggle } from '@/components/common/ThemeToggle'
import { NAV_ITEMS } from '@/constants'
import { useAuthStore } from '@/store/authStore'
import {
  LayoutDashboard,
  Wallet,
  ArrowLeftRight,
  CreditCard,
  Users,
  Bell,
  Settings,
  LogOut,
  ShieldAlert
} from 'lucide-react'

const icons = {
  LayoutDashboard,
  Wallet,
  ArrowLeftRight,
  CreditCard,
  Users,
  Bell,
  Settings
}

export function DashboardLayout() {
  const location = useLocation()
  const navigate = useNavigate()
  const { user, logout } = useAuthStore()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="flex h-screen bg-slate-50 dark:bg-slate-950">
      {/* Sidebar */}
      <aside className="hidden md:flex w-64 flex-col border-r border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900">
        <div className="p-6">
          <Logo />
        </div>
        <nav className="flex-1 space-y-1 px-4">
          {NAV_ITEMS.map((item) => {
            const Icon = icons[item.icon]
            const active = location.pathname === item.path
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors ${
                  active
                    ? 'bg-blue-50 text-blue-600 dark:bg-blue-950/40 dark:text-blue-400'
                    : 'text-slate-600 hover:bg-slate-50 dark:text-slate-400 dark:hover:bg-slate-800'
                }`}
              >
                {Icon && <Icon className="h-5 w-5" />}
                {item.name}
              </Link>
            )
          })}
        </nav>
        <div className="border-t border-slate-200 p-4 dark:border-slate-800">
          <div className="flex items-center justify-between mb-4">
            <div>
              <p className="text-sm font-medium text-slate-900 dark:text-slate-100">{user?.email || 'User'}</p>
              <p className="text-xs text-slate-500 dark:text-slate-400">{user?.role || 'ROLE_USER'}</p>
            </div>
            <ThemeToggle />
          </div>
          <button
            onClick={handleLogout}
            className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/30"
          >
            <LogOut className="h-4 w-4" />
            Sign Out
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <div className="flex flex-1 flex-col overflow-hidden">
        <main className="flex-1 overflow-y-auto p-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
