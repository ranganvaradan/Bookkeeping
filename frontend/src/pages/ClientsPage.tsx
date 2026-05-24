import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getClients } from '../api/clients'
import ClientCard from '../components/ClientCard'

export default function ClientsPage() {
  const { data: clients, isLoading, error } = useQuery({
    queryKey: ['clients'],
    queryFn: getClients,
  })

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Clients</h1>
          <p className="text-sm text-gray-600">Manage your client accounts and QuickBooks connections.</p>
        </div>
        <Link
          to="/clients/new"
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          Add client
        </Link>
      </div>

      {isLoading && <p className="text-gray-600">Loading clients…</p>}
      {error && <p className="text-red-600">Failed to load clients.</p>}

      {clients && clients.length === 0 && (
        <div className="rounded-lg border border-dashed border-gray-300 bg-white p-10 text-center">
          <p className="mb-4 text-gray-600">No clients yet.</p>
          <Link
            to="/clients/new"
            className="text-sm font-medium text-blue-600 hover:text-blue-700"
          >
            Add your first client
          </Link>
        </div>
      )}

      {clients && clients.length > 0 && (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {clients.map((client) => (
            <ClientCard key={client.id} client={client} />
          ))}
        </div>
      )}
    </div>
  )
}
