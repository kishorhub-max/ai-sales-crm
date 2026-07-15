import { useQuery } from '@tanstack/react-query'
import {
    LineChart, Line, BarChart, Bar, PieChart, Pie, Cell,
    XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend,
} from 'recharts'
import {
    DollarSign, Users, TrendingUp, Target, AlertCircle, ArrowRight,
} from 'lucide-react'
import { Link } from 'react-router-dom'
import { dashboardApi } from '@/api/dashboard'
import KpiCard from '@/components/ui/KpiCard'
import StatusBadge from '@/components/ui/StatusBadge'
import { LoadingSpinner } from '@/components/ui/States'
import { formatCurrency, formatDate, formatMonthLabel, formatPercent } from '@/utils/formatters'
import { useAuth } from '@/contexts/AuthContext'

const STAGE_COLORS: Record<string, string> = {
    NEW: '#3b82f6',
    QUALIFIED: '#22c55e',
    PROPOSAL: '#a855f7',
    NEGOTIATION: '#f97316',
    WON: '#10b981',
    LOST: '#ef4444',
}

export default function DashboardPage() {
    const { user } = useAuth()
    const { data, isLoading } = useQuery({
        queryKey: ['dashboard'],
        queryFn: dashboardApi.getStats,
    })

    if (isLoading) return <LoadingSpinner label="Loading dashboard..." />
    if (!data) return null

    const pipelineChartData = data.opportunityPipeline.map((p) => ({
        stage: p.stage,
        value: p.value,
        count: p.count,
    }))

    const revenueChartData = data.monthlyRevenue.map((m) => ({
        month: formatMonthLabel(m.month),
        revenue: m.revenue,
    }))

    return (
        <div className="space-y-6 animate-fade-in">

            {/* Header */}
            <div>
                <h1 className="page-title">Welcome back, {user?.firstName} 👋</h1>
                <p className="mt-1 text-sm text-gray-500">
                    Here's what's happening with your sales pipeline today.
                </p>
            </div>

            {/* KPI Cards */}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
                <KpiCard
                    label="Total Revenue"
                    value={formatCurrency(data.totalRevenue)}
                    icon={DollarSign}
                    iconColor="text-emerald-600"
                    iconBg="bg-emerald-50"
                />
                <KpiCard
                    label="Active Customers"
                    value={data.activeCustomers.toString()}
                    icon={Users}
                    iconColor="text-blue-600"
                    iconBg="bg-blue-50"
                />
                <KpiCard
                    label="Open Opportunities"
                    value={data.openOpportunities.toString()}
                    icon={TrendingUp}
                    iconColor="text-purple-600"
                    iconBg="bg-purple-50"
                />
                <KpiCard
                    label="Conversion Rate"
                    value={formatPercent(data.conversionRate)}
                    icon={Target}
                    iconColor="text-orange-600"
                    iconBg="bg-orange-50"
                />
            </div>

            {/* Secondary KPIs */}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                <div className="card p-4 flex items-center justify-between">
                    <div>
                        <p className="text-xs font-medium text-gray-500">Pipeline Value (weighted)</p>
                        <p className="mt-1 text-lg font-bold text-gray-900">{formatCurrency(data.pipelineValue)}</p>
                    </div>
                </div>
                <div className="card p-4 flex items-center justify-between">
                    <div>
                        <p className="text-xs font-medium text-gray-500">New Leads This Month</p>
                        <p className="mt-1 text-lg font-bold text-gray-900">{data.newLeadsThisMonth}</p>
                    </div>
                </div>
                <div className="card p-4 flex items-center justify-between border-red-100">
                    <div>
                        <p className="text-xs font-medium text-gray-500">Overdue Invoices</p>
                        <p className="mt-1 text-lg font-bold text-red-600">{data.overdueInvoices}</p>
                    </div>
                    {data.overdueInvoices > 0 && <AlertCircle className="h-5 w-5 text-red-500" />}
                </div>
            </div>

            {/* Charts row */}
            <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">

                {/* Revenue Trend */}
                <div className="card p-5">
                    <h3 className="section-title mb-4">Revenue Trend</h3>
                    <ResponsiveContainer width="100%" height={260}>
                        <LineChart data={revenueChartData}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                            <XAxis dataKey="month" tick={{ fontSize: 12 }} stroke="#94a3b8" />
                            <YAxis tick={{ fontSize: 12 }} stroke="#94a3b8"
                                   tickFormatter={(v) => `$${(v / 1000).toFixed(0)}k`} />
                            <Tooltip formatter={(v: number) => formatCurrency(v)} />
                            <Line type="monotone" dataKey="revenue" stroke="#2563eb" strokeWidth={2.5}
                                  dot={{ r: 3 }} activeDot={{ r: 5 }} />
                        </LineChart>
                    </ResponsiveContainer>
                </div>

                {/* Opportunity Pipeline */}
                <div className="card p-5">
                    <h3 className="section-title mb-4">Opportunity Pipeline</h3>
                    <ResponsiveContainer width="100%" height={260}>
                        <BarChart data={pipelineChartData}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                            <XAxis dataKey="stage" tick={{ fontSize: 11 }} stroke="#94a3b8" />
                            <YAxis tick={{ fontSize: 12 }} stroke="#94a3b8" />
                            <Tooltip formatter={(v: number) => formatCurrency(v)} />
                            <Bar dataKey="value" radius={[6, 6, 0, 0]}>
                                {pipelineChartData.map((entry, i) => (
                                    <Cell key={i} fill={STAGE_COLORS[entry.stage] || '#94a3b8'} />
                                ))}
                            </Bar>
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            </div>

            {/* Bottom row: Top Customers + Recent Leads */}
            <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">

                {/* Top Customers */}
                <div className="card p-5">
                    <div className="mb-4 flex items-center justify-between">
                        <h3 className="section-title">Top Customers</h3>
                        <Link to="/customers" className="flex items-center gap-1 text-xs font-medium text-primary-600 hover:text-primary-700">
                            View all <ArrowRight className="h-3 w-3" />
                        </Link>
                    </div>
                    <div className="space-y-3">
                        {data.topCustomers.length === 0 && (
                            <p className="text-sm text-gray-500">No customer data yet.</p>
                        )}
                        {data.topCustomers.map((c) => (
                            <Link
                                key={c.id}
                                to={`/customers/${c.id}`}
                                className="flex items-center justify-between rounded-lg px-2 py-2 hover:bg-gray-50 transition-colors"
                            >
                                <div>
                                    <p className="text-sm font-medium text-gray-900">{c.fullName}</p>
                                    <p className="text-xs text-gray-500">{c.company || 'No company'}</p>
                                </div>
                                <p className="text-sm font-semibold text-emerald-600">
                                    {formatCurrency(c.totalPurchaseValue)}
                                </p>
                            </Link>
                        ))}
                    </div>
                </div>

                {/* Recent Leads */}
                <div className="card p-5">
                    <div className="mb-4 flex items-center justify-between">
                        <h3 className="section-title">Recent Leads</h3>
                        <Link to="/leads" className="flex items-center gap-1 text-xs font-medium text-primary-600 hover:text-primary-700">
                            View all <ArrowRight className="h-3 w-3" />
                        </Link>
                    </div>
                    <div className="space-y-3">
                        {data.recentLeads.length === 0 && (
                            <p className="text-sm text-gray-500">No leads yet.</p>
                        )}
                        {data.recentLeads.map((lead) => (
                            <Link
                                key={lead.id}
                                to={`/leads/${lead.id}`}
                                className="flex items-center justify-between rounded-lg px-2 py-2 hover:bg-gray-50 transition-colors"
                            >
                                <div>
                                    <p className="text-sm font-medium text-gray-900">{lead.name}</p>
                                    <p className="text-xs text-gray-500">{lead.company || lead.email}</p>
                                </div>
                                <StatusBadge status={lead.status} />
                            </Link>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    )
}