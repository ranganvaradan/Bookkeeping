import { useEffect, useState } from 'react'

function App() {
  const [healthStatus, setHealthStatus] = useState<string | null>(null)

  useEffect(() => {
    fetch('/api/health')
      .then((res) => res.json())
      .then((data) => setHealthStatus(data.status))
      .catch(() => setHealthStatus('unavailable'))
  }, [])

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center">
      <div className="text-center">
        <h1 className="text-4xl font-bold text-gray-900 mb-4">
          BillionTech Bookkeeping Platform
        </h1>
        <p className="text-lg text-gray-600 mb-8">
          Automated bookkeeping for CPA firms
        </p>
        <div className="inline-flex items-center gap-2 rounded-full bg-white px-4 py-2 shadow-sm border border-gray-200">
          <span
            className={`h-3 w-3 rounded-full ${
              healthStatus === 'ok'
                ? 'bg-green-500'
                : healthStatus === 'unavailable'
                  ? 'bg-red-500'
                  : 'bg-yellow-500 animate-pulse'
            }`}
          />
          <span className="text-sm text-gray-700">
            API: {healthStatus ?? 'checking...'}
          </span>
        </div>
      </div>
    </div>
  )
}

export default App
