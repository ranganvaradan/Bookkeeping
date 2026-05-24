import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { createClient, type EntityType } from '../api/clients'
import { ENTITY_TYPE_OPTIONS } from '../components/utils'

export default function NewClientPage() {
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const [entityType, setEntityType] = useState<EntityType>('LLC')

  const mutation = useMutation({
    mutationFn: createClient,
    onSuccess: (client) => {
      navigate(`/clients/${client.id}`)
    },
  })

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    mutation.mutate({ name, entityType })
  }

  return (
    <div className="max-w-lg">
      <Link to="/clients" className="mb-4 inline-block text-sm text-blue-600 hover:text-blue-700">
        ← Back to clients
      </Link>

      <h1 className="mb-6 text-2xl font-bold text-gray-900">Add client</h1>

      <form onSubmit={handleSubmit} className="space-y-4 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <div>
          <label htmlFor="name" className="mb-1 block text-sm font-medium text-gray-700">
            Name
          </label>
          <input
            id="name"
            type="text"
            required
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
          />
        </div>

        <div>
          <label htmlFor="entityType" className="mb-1 block text-sm font-medium text-gray-700">
            Entity type
          </label>
          <select
            id="entityType"
            value={entityType}
            onChange={(e) => setEntityType(e.target.value as EntityType)}
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
          >
            {ENTITY_TYPE_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        {mutation.isError && (
          <p className="text-sm text-red-600">Failed to create client. Please try again.</p>
        )}

        <button
          type="submit"
          disabled={mutation.isPending}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
        >
          {mutation.isPending ? 'Creating…' : 'Create client'}
        </button>
      </form>
    </div>
  )
}
