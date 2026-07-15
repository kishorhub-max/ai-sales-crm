import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import type { Role } from '@/types'

interface ProtectedRouteProps {
    allowedRoles?: Role[]
}

export default function ProtectedRoute({ allowedRoles }: ProtectedRouteProps) {
    const { isAuthenticated, isLoading, hasRole } = useAuth()

    if (isLoading) {
        return (
            <div className="flex h-screen items-center justify-center bg-gray-50">
                <div className="flex flex-col items-center gap-3">
                    <div className="h-10 w-10 animate-spin rounded-full border-3 border-primary-200 border-t-primary-600" />
                    <p className="text-sm text-gray-500">Loading...</p>
                </div>
            </div>
        )
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />
    }

    if (allowedRoles && !hasRole(...allowedRoles)) {
        return <Navigate to="/dashboard" replace />
    }

    return <Outlet />
}