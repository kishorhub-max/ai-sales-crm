import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { ArrowLeft, Mail, Phone, MapPin, Sparkles, Loader2 } from 'lucide-react'
import { useState } from 'react'
import { customersApi } from '@/api/customers'
import { opportunitiesApi } from '@/api/opportunities'
import { ordersApi } from '@/api/orders'
import { aiApi } from '@/api/ai'
import StatusBadge from '@/components/ui/StatusBadge'
import { LoadingSpinner } from '@/components/ui/States'
import { formatCurrency, formatDate } from '@/utils/formatters'

export default function CustomerDetailPage() {
    const { id } = useParams()
    const navigate = useNavigate()
    const customerId = Number(id)
    const [showInsights, setShowInsights] = useState(false)

    const { data: customer, isLoading } = useQuery({
        queryKey: ['customer', customerId],
        queryFn: () => customersApi.getById(customerId),
    })

    const { data: opportunities } = useQuery({
        queryKey: ['opportunities', 'customer', customerId],
        queryFn: () => opportunitiesApi.getByCustomer(customerId),
    })

    const { data: orders } = useQuery({
        queryKey: ['orders', 'customer', customerId],
        queryFn: () => ordersApi.getByCustomer(customerId),
    })

    const insightsMutation = useMutation({
        mutationFn: () => aiApi.getCustomerInsights(customerId),
        onSuccess: () => setShowInsights(true),
    })

    if (isLoading) return <LoadingSpinner />
    if (!customer) return <p>Customer not found.</p>

    return (
        <div className="space-y-5 animate-fade-in max-w-4xl">
            <button onClick={() => navigate('/customers')} className="flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-700">
                <ArrowLeft className="h-4 w-4" /> Back to Customers
            </button>

            {/* Profile card */}
            <div className="card p-6">
                <div className="flex items-start justify-between">
                    <div>
                        <h1 className="text-xl font-bold text-gray-900">{customer.fullName}</h1>
                        <p className="text-sm text-gray-500">{customer.jobTitle} {customer.company && `at ${customer.company}`}</p>
                    </div>
                    <button
                        onClick={() => insightsMutation.mutate()}
                        disabled={insightsMutation.isPending}
                        className="btn-primary"
                    >
                        {insightsMutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Sparkles className="h-4 w-4" />}
                        AI Insights
                    </button>
                </div>

                <div className="mt-5 grid grid-cols-2 gap-4 text-sm">
                    <div className="flex items-center gap-2 text-gray-600"><Mail className="h-4 w-4 text-gray-400" />{customer.email}</div>
                    <div className="flex items-center gap-2 text-gray-600"><Phone className="h-4 w-4 text-gray-400" />{customer.phone || '—'}</div>
                    <div className="flex items-center gap-2 text-gray-600 col-span-2">
                        <MapPin className="h-4 w-4 text-gray-400" />
                        {[customer.city, customer.country].filter(Boolean).join(', ') || '—'}
                    </div>
                </div>

                <div className="mt-5 grid grid-cols-2 gap-4">
                    <div className="rounded-lg bg-emerald-50 px-4 py-3">
                        <p className="text-xs text-emerald-600">Total Purchases</p>
                        <p className="text-lg font-bold text-emerald-700">{formatCurrency(customer.totalPurchaseValue)}</p>
                    </div>
                    <div className="rounded-lg bg-blue-50 px-4 py-3">
                        <p className="text-xs text-blue-600">Orders Placed</p>
                        <p className="text-lg font-bold text-blue-700">{customer.purchaseCount}</p>
                    </div>
                </div>
            </div>

            {/* AI Insights panel */}
            {showInsights && insightsMutation.data && (
                <div className="card p-6 border-primary-200 bg-primary-50/30 animate-slide-in">
                    <div className="mb-4 flex items-center gap-2">
                        <Sparkles className="h-5 w-5 text-primary-600" />
                        <h3 className="section-title">AI Customer Insights</h3>
                    </div>

                    <div className="grid grid-cols-3 gap-3 mb-4">
                        <div className="rounded-lg bg-white p-3 text-center">
                            <p className="text-xs text-gray-500">Health Score</p>
                            <p className="text-2xl font-bold text-gray-900">{insightsMutation.data.healthScore}</p>
                            <StatusBadge status={insightsMutation.data.healthScoreLabel} />
                        </div>
                        <div className="rounded-lg bg-white p-3 text-center">
                            <p className="text-xs text-gray-500">Churn Risk</p>
                            <p className="text-2xl font-bold text-gray-900">{insightsMutation.data.churnRisk}</p>
                            <StatusBadge status={insightsMutation.data.churnRiskLabel} />
                        </div>
                        <div className="rounded-lg bg-white p-3 text-center">
                            <p className="text-xs text-gray-500">Purchase Prob.</p>
                            <p className="text-2xl font-bold text-gray-900">{insightsMutation.data.purchaseProbability}</p>
                            <StatusBadge status={insightsMutation.data.purchaseProbabilityLabel} />
                        </div>
                    </div>

                    <p className="text-sm text-gray-700 mb-3">{insightsMutation.data.insights}</p>

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <p className="text-xs font-semibold text-gray-500 mb-1.5">UPSELL SUGGESTIONS</p>
                            <ul className="space-y-1">
                                {insightsMutation.data.upsellSuggestions.map((s, i) => (
                                    <li key={i} className="text-sm text-gray-600">• {s}</li>
                                ))}
                            </ul>
                        </div>
                        <div>
                            <p className="text-xs font-semibold text-gray-500 mb-1.5">RECOMMENDED ACTIONS</p>
                            <ul className="space-y-1">
                                {insightsMutation.data.recommendedActions.map((a, i) => (
                                    <li key={i} className="text-sm text-gray-600">• {a}</li>
                                ))}
                            </ul>
                        </div>
                    </div>
                </div>
            )}

            {/* Opportunities */}
            <div className="card p-5">
                <h3 className="section-title mb-3">Opportunities</h3>
                {!opportunities || opportunities.length === 0 ? (
                    <p className="text-sm text-gray-500">No opportunities yet.</p>
                ) : (
                    <div className="space-y-2">
                        {opportunities.map((o) => (
                            <div key={o.id} className="flex items-center justify-between rounded-lg border border-gray-100 px-3 py-2.5">
                                <div>
                                    <p className="text-sm font-medium text-gray-900">{o.dealName}</p>
                                    <p className="text-xs text-gray-500">{formatCurrency(o.value)} · {o.probability}% probability</p>
                                </div>
                                <StatusBadge status={o.stage} />
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Orders */}
            <div className="card p-5">
                <h3 className="section-title mb-3">Order History</h3>
                {!orders || orders.length === 0 ? (
                    <p className="text-sm text-gray-500">No orders yet.</p>
                ) : (
                    <div className="space-y-2">
                        {orders.map((o) => (
                            <div key={o.id} className="flex items-center justify-between rounded-lg border border-gray-100 px-3 py-2.5">
                                <div>
                                    <p className="text-sm font-medium text-gray-900">{o.orderNumber}</p>
                                    <p className="text-xs text-gray-500">{formatDate(o.createdAt)}</p>
                                </div>
                                <div className="flex items-center gap-3">
                                    <p className="text-sm font-semibold text-gray-900">{formatCurrency(o.total)}</p>
                                    <StatusBadge status={o.status} />
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    )
}