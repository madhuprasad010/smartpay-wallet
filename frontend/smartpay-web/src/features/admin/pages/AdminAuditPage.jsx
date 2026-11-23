import React, { useEffect, useState } from 'react'
import { auditApi } from '@/services/endpoints/audit'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { formatDate } from '@/utils/cn'
import { FileText, Search, ShieldCheck } from 'lucide-react'

export function AdminAuditPage() {
  const [logs, setLogs] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(true)
  const [actorFilter, setActorFilter] = useState('')

  useEffect(() => {
    loadLogs()
  }, [page])

  async function loadLogs() {
    try {
      setLoading(true)
      let res
      if (actorFilter.trim()) {
        res = await auditApi.getByActor(actorFilter.trim(), { page, size: 20 })
      } else {
        res = await auditApi.getAll({ page, size: 20 })
      }
      if (res.success && res.data) {
        setLogs(res.data.content || [])
        setTotalPages(res.data.totalPages || 1)
      }
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = (e) => {
    e.preventDefault()
    setPage(0)
    loadLogs()
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">System Audit Logs</h1>
        <p className="text-sm text-slate-500 dark:text-slate-400">
          Immutable forensic log of security events, administrative operations, and user authentication.
        </p>
      </div>

      {/* Filter */}
      <form onSubmit={handleSearch} className="flex gap-3 max-w-md">
        <Input
          placeholder="Filter by Actor / User ID..."
          value={actorFilter}
          onChange={(e) => setActorFilter(e.target.value)}
        />
        <Button type="submit" className="flex items-center gap-1.5 shrink-0">
          <Search className="h-4 w-4" />
          Filter
        </Button>
      </form>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <FileText className="h-5 w-5 text-purple-600" />
            Security & Compliance Audit Trail
          </CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="p-8 text-center text-slate-500">Loading audit records...</div>
          ) : logs.length === 0 ? (
            <div className="p-8 text-center text-slate-500">No audit events recorded.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="border-b border-slate-200 text-xs font-semibold text-slate-500 dark:border-slate-800">
                  <tr>
                    <th className="py-3 px-4">Event Type</th>
                    <th className="py-3 px-4">Actor</th>
                    <th className="py-3 px-4">Action</th>
                    <th className="py-3 px-4">Resource</th>
                    <th className="py-3 px-4">Details</th>
                    <th className="py-3 px-4">IP Address</th>
                    <th className="py-3 px-4">Timestamp</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 dark:divide-slate-800 text-xs">
                  {logs.map((log) => (
                    <tr key={log.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/50">
                      <td className="py-3 px-4 font-mono font-semibold text-slate-900 dark:text-slate-100">
                        {log.eventType}
                      </td>
                      <td className="py-3 px-4 font-mono">
                        {log.actorId ? `User #${log.actorId}` : 'SYSTEM'}
                      </td>
                      <td className="py-3 px-4">
                        <span className="rounded bg-slate-100 px-2 py-0.5 font-medium dark:bg-slate-800">
                          {log.action}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-slate-500">
                        {log.resourceType || '—'}
                      </td>
                      <td className="py-3 px-4 max-w-xs truncate text-slate-700 dark:text-slate-300">
                        {log.details || '—'}
                      </td>
                      <td className="py-3 px-4 font-mono text-slate-400">
                        {log.ipAddress || '127.0.0.1'}
                      </td>
                      <td className="py-3 px-4 text-slate-400 whitespace-nowrap">
                        {formatDate(log.createdAt)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {/* Pagination */}
              <div className="mt-4 flex items-center justify-between border-t border-slate-100 pt-4 dark:border-slate-800">
                <Button size="sm" variant="outline" disabled={page === 0} onClick={() => setPage(page - 1)}>
                  Previous
                </Button>
                <span className="text-xs text-slate-500">
                  Page {page + 1} of {totalPages}
                </span>
                <Button size="sm" variant="outline" disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>
                  Next
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
