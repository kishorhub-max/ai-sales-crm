import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { Plus, ChevronLeft, ChevronRight } from 'lucide-react'
import toast from 'react-hot-toast'
import { ordersApi } from '@/api/orders'
import StatusBadge from '@/components/ui/StatusBadge'
import { LoadingSpinner, EmptyState } from '@/components/ui/States'
import { formatCurrency, formatDate } from '@/utils/formatters'
import OrderFormModal from './OrderFormModal'
import type { OrderRequest, OrderStatus } from '@/types'

export default function OrdersPage() {
    const queryClient = useQueryClient()
    const [statusFilter, setStatusFilter] = useState<OrderStatus | ''>('')
    const [page, setPage] = useState(0)
    const [modalOpen, setModalOpen] = useState(false)

    const { data, isLoading } = useQuery({
        queryKey: ['orders', statusFilter, page],
        queryFn: () => ordersApi.getAll({ status: statusFilter || undefined, page, size: 10 }),
    })

    const createMutation = useMutation({
        mutationFn: ordersApi.create,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['orders'] })
            toast.success('Order created')
            setModalOpen(false)
        },
    })

    return (
        <div className="space-y-5 animate-fade-in">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="page-title">Orders</h1>
                    <p className="mt-1 text-sm text-gray-500">Track and manage customer orders</p>
                </div>
                <button onClick={() => setModalOpen(true)} className="btn-primary">
                    <Plus className="h-4 w-4" /> New Order
                </button>
            </div>

            <div className="card p-4">
                <select className="select sm:w-56" value={statusFilter} onChange={(e) => { setStatusFilter(e.target.value as OrderStatus | ''); setPage(0) }}>
                    <option value="">All Statuses</option>
                    <option value="PENDING">Pending</option>
                    <option value="CONFIRMED">Confirmed</option>
                    <option value="PROCESSING">Processing</option>
                    <option value="SHIPPED">Shipped</option>
                    <option value="DELIVERED">Delivered</option>
                    <option value="CANCELLED">Cancelled</option>
                    <option value="REFUNDED">Refunded</option>
                </select>
            </div>

            <div className="card overflow-hidden">
                {isLoading ? (
                    <LoadingSpinner />
                ) : !data || data.content.length === 0 ? (
                    <EmptyState title="No orders found" description="Create your first order to get started." />
                ) : (
                    <>
                        <div className="overflow-x-auto">
                            <table className="w-full">
                                <thead>
                                <tr>
                                    <th className="table-th">Order #</th>
                                    <th className="table-th">Customer</th>
                                    <th className="table-th">Items</th>
                                    <th className="table-th">Total</th>
                                    <th className="table-th">Status</th>
                                    <th className="table-th">Date</th>
                                </tr>
                                </thead>
                                <tbody>
                                {data.content.map((order) => (
                                    <tr key={order.id} className="table-row">
                                        <td className="table-td">
                                            <Link to={`/orders/${order.id}`} className="font-medium text-gray-900 hover:text-primary-600">
                                                {order.orderNumber}
                                            </Link>
                                        </td>
                                        <td className="table-td">{order.customerName}</td>
                                        <td className="table-td">{order.items?.length || 0} items</td>
                                        <td className="table-td font-semibold">{formatCurrency(order.total)}</td>
                                        <td className="table-td"><StatusBadge status={order.status} /></td>
                                        <td className="table-td">{formatDate(order.createdAt)}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>

                        <div className="flex items-center justify-between border-t border-gray-100 px-4 py-3">
                            <p className="text-sm text-gray-500">Page {data.page + 1} of {data.totalPages} · {data.totalElements} total</p>
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

            <OrderFormModal
                isOpen={modalOpen}
                onClose={() => setModalOpen(false)}
                onSubmit={(data: OrderRequest) => createMutation.mutate(data)}
                isSubmitting={createMutation.isPending}
            />
        </div>
    )
}