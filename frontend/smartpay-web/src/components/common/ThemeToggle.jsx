import React from 'react'
import { Moon, Sun } from 'lucide-react'
import { useUiStore } from '@/store/uiStore'

export function ThemeToggle() {
  const { theme, toggleTheme } = useUiStore()

  return (
    <button
      onClick={toggleTheme}
      className="rounded-lg p-2 text-slate-500 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-slate-800"
      aria-label="Toggle theme"
    >
      {theme === 'dark' ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5 text-slate-700" />}
    </button>
  )
}
