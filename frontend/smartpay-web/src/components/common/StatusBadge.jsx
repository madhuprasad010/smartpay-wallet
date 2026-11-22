import React from 'react'
import { Badge } from '@/components/ui/badge'

export function StatusBadge({ status }) {
  switch (status?.toUpperCase()) {
    case 'ACTIVE':
    case 'COMPLETED':
    case 'SUCCESS':
    case 'VERIFIED':
      return <Badge variant="success">{status}</Badge>
    case 'PENDING':
    case 'PROCESSING':
      return <Badge variant="warning">{status}</Badge>
    case 'FAILED':
    case 'CANCELLED':
    case 'REJECTED':
      return <Badge variant="destructive">{status}</Badge>
    default:
      return <Badge variant="secondary">{status || 'UNKNOWN'}</Badge>
  }
}
