import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, FileText } from 'lucide-react'
import toast from 'react-hot-toast'
import { ordersApi } from '@/api/orders'
import { invoicesApi } from '@/api/invoices'
import StatusBadge from '@/components/ui/StatusBadge'
import { LoadingSpinner } from '@/components/ui/States'
import { formatCurrency, formatDate } from '@/utils/formatters'
import type { OrderStatus } from '@/types'

export default function OrderDetailPage() {
    const { id } = useParams()
    const navigate = useNavigate()
    const queryClient = useQueryClient()
    const orderId = Number(id)

    const { data: order, isLoading } = useQuery({
        queryKey: ['order', orderId],
        queryFn: () => ordersApi.getById(orderId),
    })

    const statusMutation = useMutation({
        mutationFn: (status: OrderStatus) => ordersApi.updateStatus(orderId, status),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['order', orderId] })
            toast.success('Status updated')
        },
    })

    const invoiceMutation = useMutation({
        mutationFn: () => invoicesApi.generateFromOrder(orderId),
        onSuccess: (invoice) => {
            toast.success(`Invoice ${invoice.invoiceNumber} generated`)
            navigate('/invoices')
        },
    })

    if (isLoading) return <LoadingSpinner />
    if (!order) return <p>Order not found.</p>

    return (
        <div className="space-y-5 animate-fade-in max-w-3xl">
            <button onClick={() => navigate('/orders')} className="flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-700">
                <ArrowLeft className="h-4 w-4" /> Back to Orders
            </button>

            <div className="card p-6">
                <div className="flex items-start justify-between">
                    <div>
                        <h1 className="text-xl font-bold text-gray-900">{order.orderNumber}</h1>
                        <p className="text-sm text-gray-500">{order.customerName} · {formatDate(order.createdAt)}</p>
                    </div>
                    <select
                        className="select w-40"
                        value={order.status}
                        onChange={(e) => statusMutation.mutate(e.target.value as OrderStatus)}
                    >
                        <option value="PENDING">Pending</option>
                        <option value="CONFIRMED">Confirmed</option>
                        <option value="PROCESSING">Processing</option>
                        <option value="SHIPPED">Shipped</option>
                        <option value="DELIVERED">Delivered</option>
                        <option value="CANCELLED">Cancelled</option>
                    </select>
                </div>

                {/* Line items */}
                <div className="mt-5 border-t border-gray-100 pt-4">
                    <p className="label">Items</p>
                    <div className="space-y-2">
                        {order.items.map((item) => (
                            <div key={item.id} className="flex items-center justify-between text-sm">
                                <div>
                                    <p className="font-medium text-gray-900">{item.productName}</p>
                                    <p className="text-xs text-gray-500">{item.quantity} × {formatCurrency(item.unitPrice)}</p>
                                </div>
                                <p className="font-medium text-gray-900">{formatCurrency(item.totalPrice)}</p>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Totals */}
                <div className="mt-4 space-y-1 border-t border-gray-100 pt-4 text-sm">
                    <div className="flex justify-between text-gray-600"><span>Subtotal</span><span>{formatCurrency(order.subtotal)}</span></div>
                    {order.discountAmount > 0 && (
                        <div className="flex justify-between text-gray-600"><span>Discount</span><span>-{formatCurrency(order.discountAmount)}</span></div>
                    )}
                    {order.taxAmount > 0 && (
                        <div className="flex justify-between text-gray-600"><span>Tax</span><span>{formatCurrency(order.taxAmount)}</span></div>
                    )}
                    <div className="flex justify-between text-base font-bold text-gray-900 pt-1">
                        <span>Total</span><span>{formatCurrency(order.total)}</span>
                    </div>
                </div>

                {!order.invoiceId && (
                    <button
                        onClick={() => invoiceMutation.mutate()}
                        disabled={invoiceMutation.isPending}
                        className="btn-primary mt-5 w-full justify-center"
                    >
                        <FileText className="h-4 w-4" />
                        {invoiceMutation.isPending ? 'Generating...' : 'Generate Invoice'}
                    </button>
                )}
            </div>
        </div>
    )
}