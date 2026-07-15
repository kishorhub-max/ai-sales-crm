import clsx from 'clsx'

interface StatusBadgeProps {
    status: string
    variant?: 'default'
}

// Maps any status string (lead, opportunity, order, invoice) to a color
const statusColorMap: Record<string, string> = {
    // Lead statuses
    NEW: 'bg-blue-100 text-blue-700',
    CONTACTED: 'bg-amber-100 text-amber-700',
    QUALIFIED: 'bg-green-100 text-green-700',
    UNQUALIFIED: 'bg-gray-100 text-gray-700',
    CONVERTED: 'bg-emerald-100 text-emerald-700',
    LOST: 'bg-red-100 text-red-700',

    // Opportunity stages
    PROPOSAL: 'bg-purple-100 text-purple-700',
    NEGOTIATION: 'bg-orange-100 text-orange-700',
    WON: 'bg-emerald-100 text-emerald-700',

    // Order statuses
    PENDING: 'bg-amber-100 text-amber-700',
    CONFIRMED: 'bg-blue-100 text-blue-700',
    PROCESSING: 'bg-indigo-100 text-indigo-700',
    SHIPPED: 'bg-cyan-100 text-cyan-700',
    DELIVERED: 'bg-emerald-100 text-emerald-700',
    CANCELLED: 'bg-red-100 text-red-700',
    REFUNDED: 'bg-gray-100 text-gray-700',

    // Invoice statuses
    DRAFT: 'bg-gray-100 text-gray-700',
    SENT: 'bg-blue-100 text-blue-700',
    PAID: 'bg-emerald-100 text-emerald-700',
    OVERDUE: 'bg-red-100 text-red-700',
}

export default function StatusBadge({ status }: StatusBadgeProps) {
    const colorClass = statusColorMap[status] || 'bg-gray-100 text-gray-700'
    return (
        <span className={clsx('badge', colorClass)}>
      {status.replace(/_/g, ' ')}
    </span>
    )
}