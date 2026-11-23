import React from 'react'
import { createBrowserRouter, Navigate } from 'react-router-dom'
import { DashboardLayout } from '@/layouts/DashboardLayout'
import { AuthLayout } from '@/layouts/AuthLayout'
import { LandingPage } from '@/features/public/pages/LandingPage'
import { NotFoundPage } from '@/features/public/pages/NotFoundPage'
import { LoginPage } from '@/features/auth/pages/LoginPage'
import { RegisterPage } from '@/features/auth/pages/RegisterPage'
import { DashboardPage } from '@/features/dashboard/pages/DashboardPage'
import { ProfilePage } from '@/features/profile/pages/ProfilePage'
import { WalletsPage } from '@/features/wallets/pages/WalletsPage'
import { TransferPage } from '@/features/transactions/pages/TransferPage'
import { DepositPage } from '@/features/payments/pages/DepositPage'
import { WithdrawalPage } from '@/features/payments/pages/WithdrawalPage'
import { TransactionsPage } from '@/features/transactions/pages/TransactionsPage'
import { BeneficiariesPage } from '@/features/beneficiaries/pages/BeneficiariesPage'
import { NotificationsPage } from '@/features/notifications/pages/NotificationsPage'
import { SettingsPage } from '@/features/settings/pages/SettingsPage'
import { AdminDashboardPage } from '@/features/admin/pages/AdminDashboardPage'
import { AdminUsersPage } from '@/features/admin/pages/AdminUsersPage'
import { AdminTransactionsPage } from '@/features/admin/pages/AdminTransactionsPage'
import { AdminAuditPage } from '@/features/admin/pages/AdminAuditPage'
import { useAuthStore } from '@/store/authStore'

function ProtectedRoute({ children }) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }
  return children
}

function AdminRoute({ children }) {
  const { isAuthenticated, user } = useAuthStore()
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }
  const isAdmin = user?.role === 'ADMIN' || user?.role === 'ROLE_ADMIN'
  if (!isAdmin) {
    return <Navigate to="/dashboard" replace />
  }
  return children
}

export const router = createBrowserRouter([
  {
    path: '/',
    element: <LandingPage />
  },
  {
    element: <AuthLayout />,
    children: [
      { path: '/login', element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> }
    ]
  },
  {
    element: (
      <ProtectedRoute>
        <DashboardLayout />
      </ProtectedRoute>
    ),
    children: [
      { path: '/dashboard', element: <DashboardPage /> },
      { path: '/profile', element: <ProfilePage /> },
      { path: '/wallets', element: <WalletsPage /> },
      { path: '/transfer', element: <TransferPage /> },
      { path: '/deposit', element: <DepositPage /> },
      { path: '/withdraw', element: <WithdrawalPage /> },
      { path: '/transactions', element: <TransactionsPage /> },
      { path: '/beneficiaries', element: <BeneficiariesPage /> },
      { path: '/notifications', element: <NotificationsPage /> },
      { path: '/settings', element: <SettingsPage /> },
      // Admin Routes
      {
        path: '/admin',
        element: (
          <AdminRoute>
            <AdminDashboardPage />
          </AdminRoute>
        )
      },
      {
        path: '/admin/users',
        element: (
          <AdminRoute>
            <AdminUsersPage />
          </AdminRoute>
        )
      },
      {
        path: '/admin/transactions',
        element: (
          <AdminRoute>
            <AdminTransactionsPage />
          </AdminRoute>
        )
      },
      {
        path: '/admin/audit',
        element: (
          <AdminRoute>
            <AdminAuditPage />
          </AdminRoute>
        )
      }
    ]
  },
  {
    path: '*',
    element: <NotFoundPage />
  }
])
