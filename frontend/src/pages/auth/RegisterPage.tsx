import { useState, FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { Bot, Mail, Lock, User, Eye, EyeOff, Loader2 } from 'lucide-react'
import { useAuth } from '@/contexts/AuthContext'
import type { Role } from '@/types'

export default function RegisterPage() {
    const { register } = useAuth()
    const [form, setForm] = useState({
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        confirmPassword: '',
        role: 'SALES_REPRESENTATIVE' as Role,
    })
    const [showPassword, setShowPassword] = useState(false)
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [error, setError] = useState('')

    const update = (field: string, value: string) =>
        setForm((prev) => ({ ...prev, [field]: value }))

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault()
        setError('')

        if (!form.firstName || !form.lastName || !form.email || !form.password) {
            setError('Please fill in all required fields')
            return
        }
        if (form.password.length < 8) {
            setError('Password must be at least 8 characters')
            return
        }
        if (form.password !== form.confirmPassword) {
            setError('Passwords do not match')
            return
        }

        setIsSubmitting(true)
        try {
            await register({
                firstName: form.firstName,
                lastName: form.lastName,
                email: form.email,
                password: form.password,
                role: form.role,
            })
        } catch (err: any) {
            setError(err.response?.data?.message || 'Registration failed')
        } finally {
            setIsSubmitting(false)
        }
    }

    return (
        <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-primary-50 via-white to-primary-50 px-4 py-8">
            <div className="w-full max-w-md animate-fade-in">

                <div className="mb-8 flex flex-col items-center">
                    <div className="mb-3 flex h-14 w-14 items-center justify-center rounded-2xl bg-primary-600 shadow-lg shadow-primary-200">
                        <Bot className="h-8 w-8 text-white" />
                    </div>
                    <h1 className="text-2xl font-bold text-gray-900">Create your account</h1>
                    <p className="mt-1 text-sm text-gray-500">Start managing your sales pipeline</p>
                </div>

                <div className="card p-8">
                    <form onSubmit={handleSubmit} className="space-y-4">

                        {error && (
                            <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-red-700">
                                {error}
                            </div>
                        )}

                        {/* Name row */}
                        <div className="grid grid-cols-2 gap-3">
                            <div>
                                <label className="label">First Name</label>
                                <div className="relative">
                                    <User className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
                                    <input
                                        value={form.firstName}
                                        onChange={(e) => update('firstName', e.target.value)}
                                        placeholder="John"
                                        className="input pl-10"
                                        required
                                    />
                                </div>
                            </div>
                            <div>
                                <label className="label">Last Name</label>
                                <input
                                    value={form.lastName}
                                    onChange={(e) => update('lastName', e.target.value)}
                                    placeholder="Doe"
                                    className="input"
                                    required
                                />
                            </div>
                        </div>

                        {/* Email */}
                        <div>
                            <label className="label">Email Address</label>
                            <div className="relative">
                                <Mail className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
                                <input
                                    type="email"
                                    value={form.email}
                                    onChange={(e) => update('email', e.target.value)}
                                    placeholder="you@company.com"
                                    className="input pl-10"
                                    required
                                />
                            </div>
                        </div>

                        {/* Role */}
                        <div>
                            <label className="label">Role</label>
                            <select
                                value={form.role}
                                onChange={(e) => update('role', e.target.value)}
                                className="select"
                            >
                                <option value="SALES_REPRESENTATIVE">Sales Representative</option>
                                <option value="SALES_MANAGER">Sales Manager</option>
                                <option value="ADMIN">Admin</option>
                            </select>
                        </div>

                        {/* Password */}
                        <div>
                            <label className="label">Password</label>
                            <div className="relative">
                                <Lock className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
                                <input
                                    type={showPassword ? 'text' : 'password'}
                                    value={form.password}
                                    onChange={(e) => update('password', e.target.value)}
                                    placeholder="At least 8 characters"
                                    className="input pl-10 pr-10"
                                    required
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                                >
                                    {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                                </button>
                            </div>
                        </div>

                        {/* Confirm Password */}
                        <div>
                            <label className="label">Confirm Password</label>
                            <input
                                type={showPassword ? 'text' : 'password'}
                                value={form.confirmPassword}
                                onChange={(e) => update('confirmPassword', e.target.value)}
                                placeholder="Re-enter password"
                                className="input"
                                required
                            />
                        </div>

                        <button
                            type="submit"
                            disabled={isSubmitting}
                            className="btn-primary w-full justify-center py-2.5 mt-2"
                        >
                            {isSubmitting ? (
                                <>
                                    <Loader2 className="h-4 w-4 animate-spin" />
                                    Creating account...
                                </>
                            ) : (
                                'Create Account'
                            )}
                        </button>
                    </form>

                    <p className="mt-6 text-center text-sm text-gray-600">
                        Already have an account?{' '}
                        <Link to="/login" className="font-medium text-primary-600 hover:text-primary-700">
                            Sign in
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    )
}