import React from 'react'
import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/button'

export function NotFoundPage() {
  return (
    <div className="flex h-screen flex-col items-center justify-center p-4 text-center">
      <h1 className="text-6xl font-bold text-blue-600">404</h1>
      <h2 className="mt-4 text-2xl font-semibold">Page Not Found</h2>
      <p className="mt-2 text-slate-500">The requested resource could not be located on the platform.</p>
      <Link to="/" className="mt-6">
        <Button>Return Home</Button>
      </Link>
    </div>
  )
}
