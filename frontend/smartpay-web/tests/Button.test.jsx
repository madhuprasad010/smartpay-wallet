import React from 'react'
import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { Button } from '@/components/ui/button'

describe('Button Component', () => {
  it('renders default button correctly', () => {
    render(<Button>Click Me</Button>)
    expect(screen.getByRole('button', { name: /click me/i })).toBeInTheDocument()
  })

  it('renders destructive variant', () => {
    render(<Button variant="destructive">Delete</Button>)
    const btn = screen.getByRole('button', { name: /delete/i })
    expect(btn).toHaveClass('bg-rose-600')
  })
})
