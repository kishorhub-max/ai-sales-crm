import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { Plus, Search, Trash2, Pencil, ChevronLeft, ChevronRight, Building2 } from 'lucide-react'
import toast from 'react-hot-toast'
import { customersApi } from '@/api/customers'
import { LoadingSpinner, EmptyState } from '@/components/ui/States'
import { formatCurrency, formatDate } from '@/utils/formatters'
import CustomerFormModal from './CustomerFormModal'
import type { Customer, CustomerRequest } from '@/types'

export default function CustomersPage() {
    const queryClient = useQueryClient()
    const [search, setSearch] = useState('')
    const [page, setPage] = useState(0)
    const [modalOpen, setModalOpen] = useState(false)
    const [editing, setEditing] = useState<Customer | null>(null)

    const { data, isLoading } = useQuery({
        queryKey: ['customers', search, page],
        queryFn: () => customersApi.getAll({ search: search || undefined, page, size: 10, active: true }),
    })

    const createMutation = useMutation({
        mutationFn: customersApi.create,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['customers'] })
            toast.success('Customer created')
            setModalOpen(false)
        },
    })

    const updateMutation = useMutation({
        mutationFn: ({ id, data }: { id: number; data: CustomerRequest }) => customersApi.update(id, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['customers'] })
            toast.success('Customer updated')
            setModalOpen(false)
            setEditing(null)
        },
    })

    const deleteMutation = useMutation({
        mutationFn: customersApi.delete,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['customers'] })
            toast.success('Customer deactivated')
        },
    })

    const handleSubmit = (formData: CustomerRequest) => {
        if (editing) updateMutation.mutate({ id: editing.id, data: formData })
        else createMutation.mutate(formData)
    }

    return (
        <div className="space-y-5 animate-fade-in">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="page-title">Customers</h1>
                    <p className="mt-1 text-sm text-gray-500">Manage your customer accounts</p>
                </div>
                <button onClick={() => { setEditing(null); setModalOpen(true) }} className="btn-primary">
                    <Plus className="h-4 w-4" /> New Customer
                </button>
            </div>

            <div className="card p-4">
                <div className="relative">
                    <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
                    <input
                        placeholder="Search by name, email, or company..."
                        className="input pl-10"
                        value={search}
                        onChange={(e) => { setSearch(e.target.value); setPage(0) }}
                    />
                </div>
            </div>

            <div className="card overflow-hidden">
                {isLoading ? (
                    <LoadingSpinner />
                ) : !data || data.content.length === 0 ? (
                    <EmptyState title="No customers found" description="Create a customer or convert a lead." />
                ) : (
                    <>
                        <div className="overflow-x-auto">
                            <table className="w-full">
                                <thead>
                                <tr>
                                    <th className="table-th">Customer</th>
                                    <th className="table-th">Company</th>
                                    <th className="table-th">Total Spend</th>
                                    <th className="table-th">Orders</th>
                                    <th className="table-th">Assigned To</th>
                                    <th className="table-th">Since</th>
                                    <th className="table-th text-right">Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                {data.content.map((c) => (
                                    <tr key={c.id} className="table-row">
                                        <td className="table-td">
                                            <Link to={`/customers/${c.id}`} className="font-medium text-gray-900 hover:text-primary-600">
                                                {c.fullName}
                                            </Link>
                                            <p className="text-xs text-gray-500">{c.email}</p>
                                        </td>
                                        <td className="table-td">
                                            <div className="flex items-center gap-1.5">
                                                <Building2 className="h-3.5 w-3.5 text-gray-400" />
                                                {c.company || '—'}
                                            </div>
                                        </td>
                                        <td className="table-td font-medium text-emerald-600">{formatCurrency(c.totalPurchaseValue)}</td>
                                        <td className="table-td">{c.purchaseCount}</td>
                                        <td className="table-td">{c.assignedToName || '—'}</td>
                                        <td className="table-td">{formatDate(c.createdAt)}</td>
                                        <td className="table-td text-right">
                                            <div className="flex justify-end gap-2">
                                                <button onClick={() => { setEditing(c); setModalOpen(true) }}
                                                        className="rounded-lg p-1.5 text-gray-500 hover:bg-gray-100 hover:text-primary-600">
                                                    <Pencil className="h-4 w-4" />
                                                </button>
                                                <button onClick={() => confirm('Deactivate this customer?') && deleteMutation.mutate(c.id)}
                                                        className="rounded-lg p-1.5 text-gray-500 hover:bg-red-50 hover:text-red-600">
                                                    <Trash2 className="h-4 w-4" />
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>

                        <div className="flex items-center justify-between border-t border-gray-100 px-4 py-3">
                            <p className="text-sm text-gray-500">
                                Page {data.page + 1} of {data.totalPages} · {data.totalElements} total
                            </p>
                            <div className="flex gap-2">
                                <button disabled={data.first} onClick={() => setPage((p) => p - 1)} className="btn-secondary px-3 py-1.5 disabled:opacity-40">
                                    <ChevronLeft className="h-4 w-4" />
                                </button>
                                <button disabled={data.last} onClick={() => setPage((p) => p + 1)} className="btn-secondary px-3 py-1.5 disabled:opacity-40">
                                    <ChevronRight className="h-4 w-4" />
                                </button>
                            </div>
                        </div>
                    </>
                )}
            </div>

            <CustomerFormModal
                isOpen={modalOpen}
                onClose={() => { setModalOpen(false); setEditing(null) }}
                onSubmit={handleSubmit}
                initialData={editing}
                isSubmitting={createMutation.isPending || updateMutation.isPending}
            />
        </div>
    )
}