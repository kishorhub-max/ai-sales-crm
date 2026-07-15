import { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { authApi } from '@/api/auth'
import type { LoginRequest, RegisterRequest, Role } from '@/types'

interface AuthUser {
    userId: number
    firstName: string
    lastName: string
    email: string
    role: Role
}

interface AuthContextType {
    user: AuthUser | null
    isAuthenticated: boolean
    isLoading: boolean
    login: (data: LoginRequest) => Promise<void>
    register: (data: RegisterRequest) => Promise<void>
    logout: () => void
    hasRole: (...roles: Role[]) => boolean
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

const USER_KEY = 'auth_user'

export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<AuthUser | null>(null)
    const [isLoading, setIsLoading] = useState(true)
    const navigate = useNavigate()

    // ── Restore session on mount ────────────────────────────────────────────────
    useEffect(() => {
        const token = localStorage.getItem('accessToken')
        const storedUser = localStorage.getItem(USER_KEY)

        if (token && storedUser) {
            try {
                setUser(JSON.parse(storedUser))
            } catch {
                localStorage.clear()
            }
        }
        setIsLoading(false)
    }, [])

    // ── Persist helper ───────────────────────────────────────────────────────────
    const persistSession = (authData: {
        accessToken: string
        refreshToken: string
        userId: number
        firstName: string
        lastName: string
        email: string
        role: Role
    }) => {
        const authUser: AuthUser = {
            userId: authData.userId,
            firstName: authData.firstName,
            lastName: authData.lastName,
            email: authData.email,
            role: authData.role,
        }
        localStorage.setItem('accessToken', authData.accessToken)
        localStorage.setItem('refreshToken', authData.refreshToken)
        localStorage.setItem(USER_KEY, JSON.stringify(authUser))
        setUser(authUser)
    }

    // ── Login ────────────────────────────────────────────────────────────────────
    const login = async (data: LoginRequest) => {
        const response = await authApi.login(data)
        persistSession(response)
        toast.success(`Welcome back, ${response.firstName}!`)
        navigate('/dashboard')
    }

    // ── Register ─────────────────────────────────────────────────────────────────
    const register = async (data: RegisterRequest) => {
        const response = await authApi.register(data)
        persistSession(response)
        toast.success(`Account created! Welcome, ${response.firstName}!`)
        navigate('/dashboard')
    }

    // ── Logout ───────────────────────────────────────────────────────────────────
    const logout = () => {
        localStorage.clear()
        setUser(null)
        navigate('/login')
        toast.success('Logged out successfully')
    }

    // ── Role check helper ────────────────────────────────────────────────────────
    const hasRole = (...roles: Role[]) => {
        return user ? roles.includes(user.role) : false
    }

    return (
        <AuthContext.Provider
            value={{
                user,
                isAuthenticated: !!user,
                isLoading,
                login,
                register,
                logout,
                hasRole,
            }}
        >
            {children}
        </AuthContext.Provider>
    )
}

export function useAuth() {
    const context = useContext(AuthContext)
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider')
    }
    return context
}