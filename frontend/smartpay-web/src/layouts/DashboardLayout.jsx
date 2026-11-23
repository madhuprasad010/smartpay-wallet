import React, { useState } from 'react'
import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom'
import { Logo } from '@/components/common/Logo'
import { ThemeToggle } from '@/components/common/ThemeToggle'
import { NAV_ITEMS, ADMIN_NAV_ITEMS } from '@/constants'
import { useAuthStore } from '@/store/authStore'
import {
  LayoutDashboard,
  User,
  Wallet,
  ArrowLeftRight,
  ArrowDownLeft,
  ArrowUpRight,
  Receipt,
  Users,
  Bell,
  Settings,
  Shield,
  UserCheck,
  ReceiptText,
  FileText,
  LogOut,
  Menu,
  X
} from 'lucide-react'

const icons = {
  LayoutDashboard,
  User,
  Wallet,
  ArrowLeftRight,
  ArrowDownLeft,
  ArrowUpRight,
  Receipt,
  Users,
  Bell,
  Settings,
  Shield,
  UserCheck,
  ReceiptText,
  FileText
}

export function DashboardLayout() {
  const location = useLocation()
  const navigate = useNavigate()
  const { user, logout } = useAuthStore()
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const isAdmin = user?.role === 'ADMIN' || user?.role === 'ROLE_ADMIN'

  return (
    <div className="flex h-screen bg-slate-50 dark:bg-slate-950">
      {/* Mobile Menu Backdrop */}
      {mobileMenuOpen && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/50 backdrop-blur-sm md:hidden"
          onClick={() => setMobileMenuOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`fixed inset-y-0 left-0 z-50 flex w-72 flex-col border-r border-slate-200 bg-white transition-transform duration-200 dark:border-slate-800 dark:bg-slate-900 md:static md:translate-x-0 ${
          mobileMenuOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="flex items-center justify-between p-6">
          <Logo />
          <button
            onClick={() => setMobileMenuOpen(false)}
            className="rounded-lg p-1.5 text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 md:hidden"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto px-4 py-2 space-y-6">
          <div>
            <p className="px-3 text-xs font-semibold uppercase tracking-wider text-slate-400">Personal Banking</p>
            <nav className="mt-2 space-y-1">
              {NAV_ITEMS.map((item) => {
                const Icon = icons[item.icon]
                const active = location.pathname === item.path
                return (
                  <Link
                    key={item.path}
                    to={item.path}
                    onClick={() => setMobileMenuOpen(false)}
                    className={`flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors ${
                      active
                        ? 'bg-blue-50 text-blue-600 dark:bg-blue-950/40 dark:text-blue-400 font-semibold'
                        : 'text-slate-600 hover:bg-slate-50 dark:text-slate-400 dark:hover:bg-slate-800/60'
                    }`}
                  >
                    {Icon && <Icon className="h-4 w-4 shrink-0" />}
                    {item.name}
                  </Link>
                )
              })}
            </nav>
          </div>

          {isAdmin && (
            <div>
              <p className="px-3 text-xs font-semibold uppercase tracking-wider text-amber-500">Administration</p>
              <nav className="mt-2 space-y-1">
                {ADMIN_NAV_ITEMS.map((item) => {
                  const Icon = icons[item.icon]
                  const active = location.pathname === item.path
                  return (
                    <Link
                      key={item.path}
                      to={item.path}
                      onClick={() => setMobileMenuOpen(false)}
                      className={`flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors ${
                        active
                          ? 'bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-400 font-semibold'
                          : 'text-slate-600 hover:bg-slate-50 dark:text-slate-400 dark:hover:bg-slate-800/60'
                      }`}
                    >
                      {Icon && <Icon className="h-4 w-4 shrink-0" />}
                      {item.name}
                    </Link>
                  )
                })}
              </nav>
            </div>
          )}
        </div>

        {/* User Card & Logout */}
        <div className="border-t border-slate-200 p-4 dark:border-slate-800">
          <div className="flex items-center justify-between mb-3">
            <div className="truncate pr-2">
              <p className="text-sm font-semibold text-slate-900 dark:text-slate-100 truncate">{user?.email || 'User'}</p>
              <span className={`inline-block mt-0.5 rounded px-1.5 py-0.5 text-xs font-semibold ${
                isAdmin ? 'bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-300' : 'bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-300'
              }`}>
                {user?.role || 'CUSTOMER'}
              </span>
            </div>
            <ThemeToggle />
          </div>
          <button
            onClick={handleLogout}
            className="flex w-full items-center justify-center gap-2 rounded-lg border border-slate-200 px-3 py-2 text-sm font-medium text-rose-600 hover:bg-rose-50 dark:border-slate-800 dark:text-rose-400 dark:hover:bg-rose-950/30 transition-colors"
          >
            <LogOut className="h-4 w-4" />
            Sign Out
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex flex-1 flex-col overflow-hidden">
        {/* Top Navbar */}
        <header className="flex h-16 items-center justify-between border-b border-slate-200 bg-white px-4 dark:border-slate-800 dark:bg-slate-900 md:hidden">
          <button
            onClick={() => setMobileMenuOpen(true)}
            className="rounded-lg p-2 text-slate-600 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-slate-800"
          >
            <Menu className="h-6 w-6" />
          </button>
          <Logo />
          <div className="w-8" />
        </header>

        {/* Main View */}
        <main className="flex-1 overflow-y-auto p-4 md:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
