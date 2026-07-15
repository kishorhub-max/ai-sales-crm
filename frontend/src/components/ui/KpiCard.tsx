import { LucideIcon } from 'lucide-react'
import clsx from 'clsx'

interface KpiCardProps {
    label: string
    value: string
    icon: LucideIcon
    trend?: { value: string; positive: boolean }
    iconColor?: string
    iconBg?: string
}

export default function KpiCard({
                                    label,
                                    value,
                                    icon: Icon,
                                    trend,
                                    iconColor = 'text-primary-600',
                                    iconBg = 'bg-primary-50',
                                }: KpiCardProps) {
    return (
        <div className="kpi-card">
            <div>
                <p className="text-sm font-medium text-gray-500">{label}</p>
                <p className="mt-1.5 text-2xl font-bold text-gray-900">{value}</p>
                {trend && (
                    <p
                        className={clsx(
                            'mt-1.5 text-xs font-medium',
                            trend.positive ? 'text-green-600' : 'text-red-600'
                        )}
                    >
                        {trend.positive ? '↑' : '↓'} {trend.value}
                    </p>
                )}
            </div>
            <div className={clsx('flex h-11 w-11 items-center justify-center rounded-xl', iconBg)}>
                <Icon className={clsx('h-5 w-5', iconColor)} />
            </div>
        </div>
    )
}
