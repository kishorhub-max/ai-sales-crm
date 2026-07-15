import { Loader2, Inbox } from 'lucide-react'

export function LoadingSpinner({ label = 'Loading...' }: { label?: string }) {
    return (
        <div className="flex flex-col items-center justify-center gap-3 py-16">
            <Loader2 className="h-7 w-7 animate-spin text-primary-600" />
            <p className="text-sm text-gray-500">{label}</p>
        </div>
    )
}

export function EmptyState({
                               title = 'No data found',
                               description = 'Try adjusting your filters or create a new item.',
                               action,
                           }: {
    title?: string
    description?: string
    action?: React.ReactNode
}) {
    return (
        <div className="flex flex-col items-center justify-center gap-3 py-16 text-center">
            <div className="flex h-14 w-14 items-center justify-center rounded-full bg-gray-100">
                <Inbox className="h-6 w-6 text-gray-400" />
            </div>
            <div>
                <p className="text-sm font-medium text-gray-900">{title}</p>
                <p className="mt-1 text-sm text-gray-500">{description}</p>
            </div>
            {action}
        </div>
    )
}