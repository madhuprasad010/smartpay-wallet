import { create } from 'zustand'

const getTheme = () => {
  if (typeof window !== 'undefined' && window.localStorage) {
    return window.localStorage.getItem('theme') || 'light'
  }
  return 'light'
}

export const useUiStore = create((set) => ({
  theme: getTheme(),
  sidebarOpen: false,

  toggleTheme: () => set((state) => {
    const nextTheme = state.theme === 'light' ? 'dark' : 'light'
    if (typeof window !== 'undefined') {
      if (window.localStorage) window.localStorage.setItem('theme', nextTheme)
      if (nextTheme === 'dark') {
        document.documentElement.classList.add('dark')
      } else {
        document.documentElement.classList.remove('dark')
      }
    }
    return { theme: nextTheme }
  }),

  setSidebarOpen: (open) => set({ sidebarOpen: open })
}))
