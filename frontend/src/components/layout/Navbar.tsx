import { Fragment } from 'react'
import { Menu, Transition } from '@headlessui/react'
import { Menu as MenuIcon, ChevronDown, LogOut, User as UserIcon } from 'lucide-react'
import { useAuth } from '@/contexts/AuthContext'
import clsx from 'clsx'

interface NavbarProps {
    onMenuClick: () => void
}

const roleLabels: Record<string, string> = {
    ADMIN: 'Administrator',
    SALES_MANAGER: 'Sales Manager',
    SALES_REPRESENTATIVE: 'Sales Representative',
}

const roleColors: Record<string, string> = {
    ADMIN: 'bg-purple-100 text-purple-700',
    SALES_MANAGER: 'bg-blue-100 text-blue-700',
    SALES_REPRESENTATIVE: 'bg-green-100 text-green-700',
}

export default function Navbar({ onMenuClick }: NavbarProps) {
    const { user, logout } = useAuth()

    if (!user) return null

    const initials = `${user.firstName[0]}${user.lastName[0]}`.toUpperCase()

    return (
        <header className="flex h-16 items-center justify-between border-b border-gray-200 bg-white px-4 lg:px-6">

            {/* Mobile menu button */}
            <button
                onClick={onMenuClick}
                className="text-gray-500 hover:text-gray-700 lg:hidden"
            >
                <MenuIcon className="h-6 w-6" />
            </button>

            <div className="hidden lg:block" />

            {/* User menu */}
            <Menu as="div" className="relative">
                <Menu.Button className="flex items-center gap-3 rounded-lg px-2 py-1.5 hover:bg-gray-50 transition-colors">
                    <div className="flex h-9 w-9 items-center justify-center rounded-full bg-primary-100 text-sm font-semibold text-primary-700">
                        {initials}
                    </div>
                    <div className="hidden text-left sm:block">
                        <p className="text-sm font-medium text-gray-900 leading-tight">
                            {user.firstName} {user.lastName}
                        </p>
                        <span className={clsx('badge mt-0.5', roleColors[user.role])}>
              {roleLabels[user.role]}
            </span>
                    </div>
                    <ChevronDown className="hidden h-4 w-4 text-gray-400 sm:block" />
                </Menu.Button>

                <Transition
                    as={Fragment}
                    enter="transition ease-out duration-100"
                    enterFrom="opacity-0 scale-95"
                    enterTo="opacity-100 scale-100"
                    leave="transition ease-in duration-75"
                    leaveFrom="opacity-100 scale-100"
                    leaveTo="opacity-0 scale-95"
                >
                    <Menu.Items className="absolute right-0 z-50 mt-2 w-56 origin-top-right rounded-xl bg-white shadow-lg ring-1 ring-gray-200 focus:outline-none">
                        <div className="px-4 py-3 border-b border-gray-100">
                            <p className="text-sm font-medium text-gray-900">{user.firstName} {user.lastName}</p>
                            <p className="text-xs text-gray-500 truncate">{user.email}</p>
                        </div>
                        <div className="p-1.5">
                            <Menu.Item>
                                {({ active }) => (
                                    <button
                                        className={clsx(
                                            'flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm',
                                            active ? 'bg-gray-50' : '',
                                            'text-gray-700'
                                        )}
                                    >
                                        <UserIcon className="h-4 w-4" />
                                        My Profile
                                    </button>
                                )}
                            </Menu.Item>
                            <Menu.Item>
                                {({ active }) => (
                                    <button
                                        onClick={logout}
                                        className={clsx(
                                            'flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm text-red-600',
                                            active ? 'bg-red-50' : ''
                                        )}
                                    >
                                        <LogOut className="h-4 w-4" />
                                        Sign Out
                                    </button>
                                )}
                            </Menu.Item>
                        </div>
                    </Menu.Items>
                </Transition>
            </Menu>
        </header>
    )
}