import { Link } from 'react-router-dom'
import type { Client } from '../api/clients'
import EntityTypeBadge from './EntityTypeBadge'
import StatusDot from './StatusDot'
import { formatDateTime } from './utils'

interface ClientCardProps {
  client: Client
}

export default function ClientCard({ client }: ClientCardProps) {
  const connected = !!client.qboRealmId

  return (
    <Link
      to={`/clients/${client.id}`}
      className="block rounded-lg border border-gray-200 bg-white p-5 shadow-sm transition hover:border-blue-300 hover:shadow-md"
    >
      <div className="mb-3 flex items-start justify-between gap-3">
        <h2 className="text-lg font-semibold text-gray-900">{client.name}</h2>
        <EntityTypeBadge entityType={client.entityType} />
      </div>
      <div className="space-y-2">
        <StatusDot connected={connected} />
        <p className="text-sm text-gray-500">Last sync: {formatDateTime(client.lastSyncAt)}</p>
      </div>
    </Link>
  )
}
