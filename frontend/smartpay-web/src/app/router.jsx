import React from 'react'
import { createBrowserRouter, Navigate } from 'react-router-dom'
import { DashboardLayout } from '@/layouts/DashboardLayout'
import { AuthLayout } from '@/layouts/AuthLayout'
import { LandingPage } from '@/features/public/pages/LandingPage'
import { NotFoundPage } from '@/features/public/pages/NotFoundPage'
import { LoginPage } from '@/features/auth/pages/LoginPage'
import { RegisterPage } from '@/features/auth/pages/RegisterPage'
import { DashboardPage } from '@/features/dashboard/pages/DashboardPage'
import { WalletsPage } from '@/features/wallets/pages/WalletsPage'
import { TransactionsPage } from '@/features/transactions/pages/TransactionsPage'
import { useAuthStore } from '@/store/authStore'

function ProtectedRoute({ children }) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
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
      { path: '/wallets', element: <WalletsPage /> },
      { path: '/transactions', element: <TransactionsPage /> },
      { path: '/payments', element: <TransactionsPage /> },
      { path: '/beneficiaries', element: <WalletsPage /> },
      { path: '/notifications', element: <DashboardPage /> },
      { path: '/settings', element: <DashboardPage /> }
    ]
  },
  {
    path: '*',
    element: <NotFoundPage />
  }
])
