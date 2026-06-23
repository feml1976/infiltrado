import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'

export function AdminRoute() {
  const token   = useAuthStore((s) => s.token)
  const esAdmin = useAuthStore((s) => s.esAdmin)

  if (!token)   return <Navigate to="/login"  replace />
  if (!esAdmin) return <Navigate to="/lobby"  replace />

  return <Outlet />
}
