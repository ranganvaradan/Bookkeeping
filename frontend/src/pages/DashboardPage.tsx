import { Link } from 'react-router-dom'

export default function DashboardPage() {
  return (
    <div>
      <h1 className="mb-2 text-2xl font-bold text-gray-900">Dashboard</h1>
      <p className="mb-6 text-gray-600">Welcome to the BillionTech Bookkeeping Platform.</p>
      <Link
        to="/clients"
        className="inline-flex rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
      >
        View clients
      </Link>
    </div>
  )
}
