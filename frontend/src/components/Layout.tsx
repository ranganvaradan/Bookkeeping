import { Link, Navigate, Outlet } from 'react-router-dom'
import { isAuthenticated, logout } from '../hooks/useAuth'

export default function Layout() {
  if (!isAuthenticated()) {
    return <Navigate to="/login" replace />
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b border-gray-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <div className="flex items-center gap-8">
            <span className="text-lg font-semibold text-gray-900">BillionTech Bookkeeping</span>
            <nav className="flex gap-4 text-sm">
              <Link to="/dashboard" className="text-gray-600 hover:text-gray-900">
                Dashboard
              </Link>
              <Link to="/clients" className="text-gray-600 hover:text-gray-900">
                Clients
              </Link>
            </nav>
          </div>
          <button
            type="button"
            onClick={() => {
              logout()
              window.location.href = '/login'
            }}
            className="text-sm text-gray-600 hover:text-gray-900"
          >
            Sign out
          </button>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-6 py-8">
        <Outlet />
      </main>
    </div>
  )
}
