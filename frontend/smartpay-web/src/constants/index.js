export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'

export const NAV_ITEMS = [
  { name: 'Dashboard', path: '/dashboard', icon: 'LayoutDashboard' },
  { name: 'My Profile', path: '/profile', icon: 'User' },
  { name: 'Wallets', path: '/wallets', icon: 'Wallet' },
  { name: 'Transfer Money', path: '/transfer', icon: 'ArrowLeftRight' },
  { name: 'Deposit Funds', path: '/deposit', icon: 'ArrowDownLeft' },
  { name: 'Withdraw Funds', path: '/withdraw', icon: 'ArrowUpRight' },
  { name: 'Transactions', path: '/transactions', icon: 'Receipt' },
  { name: 'Beneficiaries', path: '/beneficiaries', icon: 'Users' },
  { name: 'Notifications', path: '/notifications', icon: 'Bell' },
  { name: 'Settings', path: '/settings', icon: 'Settings' }
]

export const ADMIN_NAV_ITEMS = [
  { name: 'Admin Dashboard', path: '/admin', icon: 'Shield' },
  { name: 'User Management', path: '/admin/users', icon: 'UserCheck' },
  { name: 'Transaction Management', path: '/admin/transactions', icon: 'ReceiptText' },
  { name: 'System Audit Logs', path: '/admin/audit', icon: 'FileText' }
]
