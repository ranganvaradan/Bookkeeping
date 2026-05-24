import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { getClient } from '../api/clients'
import EntityTypeBadge from '../components/EntityTypeBadge'
import StatusDot from '../components/StatusDot'
import { defaultSyncDateRange, formatDateTime } from '../components/utils'
import { useQboConnect } from '../hooks/useQboConnect'
import { useSyncJob } from '../hooks/useSyncJob'

export default function ClientDetailPage() {
  const { id } = useParams<{ id: string }>()
  const queryClient = useQueryClient()
  const defaults = defaultSyncDateRange()
  const [fromDate, setFromDate] = useState(defaults.fromDate)
  const [toDate, setToDate] = useState(defaults.toDate)

  const { data: client, isLoading, error } = useQuery({
    queryKey: ['client', id],
    queryFn: () => getClient(id!),
    enabled: !!id,
  })

  const { state: qboState, error: qboError, connect } = useQboConnect(id!, () => {
    queryClient.invalidateQueries({ queryKey: ['client', id] })
    queryClient.invalidateQueries({ queryKey: ['clients'] })
  })

  const { state: syncState, job, error: syncError, runSync } = useSyncJob(id!)

  useEffect(() => {
    if (syncState === 'done') {
      queryClient.invalidateQueries({ queryKey: ['client', id] })
      queryClient.invalidateQueries({ queryKey: ['clients'] })
    }
  }, [syncState, queryClient, id])

  if (isLoading) {
    return <p className="text-gray-600">Loading client…</p>
  }

  if (error || !client) {
    return <p className="text-red-600">Client not found.</p>
  }

  const connected = !!client.qboRealmId

  return (
    <div className="space-y-8">
      <div>
        <Link to="/clients" className="mb-4 inline-block text-sm text-blue-600 hover:text-blue-700">
          ← Back to clients
        </Link>

        <div className="flex flex-wrap items-center gap-3">
          <h1 className="text-2xl font-bold text-gray-900">{client.name}</h1>
          <EntityTypeBadge entityType={client.entityType} />
        </div>
        <div className="mt-3">
          <StatusDot connected={connected} />
        </div>
        <p className="mt-2 text-sm text-gray-500">Last sync: {formatDateTime(client.lastSyncAt)}</p>
      </div>

      <section className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <h2 className="mb-4 text-lg font-semibold text-gray-900">QuickBooks Online</h2>

        {!connected && qboState !== 'connected' && (
          <button
            type="button"
            onClick={connect}
            disabled={qboState === 'waiting'}
            className="rounded-md bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700 disabled:opacity-50"
          >
            {qboState === 'waiting' ? 'Waiting for connection…' : 'Connect QuickBooks'}
          </button>
        )}

        {qboState === 'waiting' && (
          <p className="mt-3 text-sm text-gray-600">
            Complete the QuickBooks authorization in the other tab. Checking connection…
          </p>
        )}

        {(connected || qboState === 'connected') && (
          <p className="text-sm font-medium text-green-700">Connected to QuickBooks successfully.</p>
        )}

        {qboState === 'timeout' && (
          <p className="text-sm text-amber-600">
            Connection not detected yet. Try refreshing the page if you completed authorization.
          </p>
        )}

        {qboError && <p className="mt-2 text-sm text-red-600">{qboError}</p>}
      </section>

      <section className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <h2 className="mb-4 text-lg font-semibold text-gray-900">Sync transactions</h2>

        <div className="mb-4 flex flex-wrap gap-4">
          <div>
            <label htmlFor="fromDate" className="mb-1 block text-sm font-medium text-gray-700">
              From
            </label>
            <input
              id="fromDate"
              type="date"
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
              className="rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
          </div>
          <div>
            <label htmlFor="toDate" className="mb-1 block text-sm font-medium text-gray-700">
              To
            </label>
            <input
              id="toDate"
              type="date"
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
              className="rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
          </div>
        </div>

        <button
          type="button"
          onClick={() => runSync({ fromDate, toDate })}
          disabled={!connected || syncState === 'running'}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
        >
          {syncState === 'running' ? 'Syncing…' : 'Run sync'}
        </button>

        {!connected && (
          <p className="mt-3 text-sm text-gray-500">Connect QuickBooks before running a sync.</p>
        )}

        {syncState === 'running' && (
          <div className="mt-4">
            <div className="mb-2 flex items-center gap-2 text-sm text-gray-600">
              <span className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-blue-600 border-t-transparent" />
              Sync in progress… {job?.status && `(${job.status})`}
            </div>
            <div className="h-2 w-full overflow-hidden rounded-full bg-gray-200">
              <div className="h-full w-1/2 animate-pulse rounded-full bg-blue-600" />
            </div>
          </div>
        )}

        {syncState === 'done' && (
          <p className="mt-4 text-sm font-medium text-green-700">
            Sync completed successfully.
            {job?.resultSummary && ` ${job.resultSummary}`}
          </p>
        )}

        {syncState === 'failed' && (
          <p className="mt-4 text-sm font-medium text-red-600">
            Sync failed.
            {syncError && ` ${syncError}`}
            {job?.resultSummary && ` ${job.resultSummary}`}
          </p>
        )}
      </section>
    </div>
  )
}
