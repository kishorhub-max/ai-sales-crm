import { NavLink } from 'react-router-dom'
import {
    LayoutDashboard, Users, Building2, TrendingUp,
    Package, ShoppingCart, FileText, Bot, X,
} from 'lucide-react'
import clsx from 'clsx'

interface SidebarProps {
    isOpen: boolean
    onClose: () => void
}

const navItems = [
    { to: '/dashboard',     icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/leads',         icon: Users,           label: 'Leads' },
    { to: '/customers',     icon: Building2,       label: 'Customers' },
    { to: '/opportunities', icon: TrendingUp,      label: 'Opportunities' },
    { to: '/products',      icon: Package,         label: 'Products' },
    { to: '/orders',        icon: ShoppingCart,    label: 'Orders' },
    { to: '/invoices',      icon: FileText,        label: 'Invoices' },
    { to: '/ai-copilot',    icon: Bot,             label: 'AI Copilot' },
]

export default function Sidebar({ isOpen, onClose }: SidebarProps) {
    return (
        <>
            {/* Mobile overlay */}
            {isOpen && (
                <div
                    className="fixed inset-0 z-30 bg-black/40 lg:hidden"
                    onClick={onClose}
                />
            )}

            <aside
                className={clsx(
                    'fixed inset-y-0 left-0 z-40 w-64 transform bg-gray-900 transition-transform duration-200 lg:static lg:translate-x-0',
                    isOpen ? 'translate-x-0' : '-translate-x-full'
                )}
            >
                <div className="flex h-full flex-col">

                    {/* Logo */}
                    <div className="flex items-center justify-between px-5 py-5">
                        <div className="flex items-center gap-2.5">
                            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-primary-600">
                                <Bot className="h-5 w-5 text-white" />
                            </div>
                            <div>
                                <p className="text-sm font-bold text-white leading-tight">AI Sales CRM</p>
                                <p className="text-[11px] text-gray-400">Enterprise</p>
                            </div>
                        </div>
                        <button onClick={onClose} className="text-gray-400 hover:text-white lg:hidden">
                            <X className="h-5 w-5" />
                        </button>
                    </div>

                    {/* Nav links */}
                    <nav className="flex-1 space-y-1 overflow-y-auto px-3 py-2">
                        {navItems.map(({ to, icon: Icon, label }) => (
                            <NavLink
                                key={to}
                                to={to}
                                onClick={onClose}
                                className={({ isActive }) =>
                                    clsx(
                                        'flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors',
                                        isActive
                                            ? 'bg-primary-600 text-white shadow-sm'
                                            : 'text-gray-300 hover:bg-gray-800 hover:text-white'
                                    )
                                }
                            >
                                <Icon className="h-[18px] w-[18px] flex-shrink-0" />
                                {label}
                            </NavLink>
                        ))}
                    </nav>

                    {/* Footer */}
                    <div className="px-5 py-4 border-t border-gray-800">
                        <p className="text-[11px] text-gray-500">v1.0.0 · Enterprise Edition</p>
                    </div>
                </div>
            </aside>
        </>
    )
}
