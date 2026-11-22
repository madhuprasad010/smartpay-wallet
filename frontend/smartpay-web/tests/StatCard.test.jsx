import React from 'react'
import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { StatCard } from '@/components/common/StatCard'

describe('StatCard Component', () => {
  it('renders title and formatted value', () => {
    render(<StatCard title="Total Balance" value="$12,500.00" change="+5.2%" />)
    expect(screen.getByText('Total Balance')).toBeInTheDocument()
    expect(screen.getByText('$12,500.00')).toBeInTheDocument()
  })
})
