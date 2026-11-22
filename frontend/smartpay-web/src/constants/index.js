export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'

export const NAV_ITEMS = [
  { name: 'Dashboard', path: '/dashboard', icon: 'LayoutDashboard' },
  { name: 'Wallets', path: '/wallets', icon: 'Wallet' },
  { name: 'Transactions', path: '/transactions', icon: 'ArrowLeftRight' },
  { name: 'Payments', path: '/payments', icon: 'CreditCard' },
  { name: 'Beneficiaries', path: '/beneficiaries', icon: 'Users' },
  { name: 'Notifications', path: '/notifications', icon: 'Bell' },
  { name: 'Settings', path: '/settings', icon: 'Settings' }
]
