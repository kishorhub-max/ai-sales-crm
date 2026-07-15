export function formatCurrency(value: number | null | undefined): string {
    if (value === null || value === undefined) return '$0'
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
        maximumFractionDigits: value % 1 === 0 ? 0 : 2,
    }).format(value)
}

export function formatCompactNumber(value: number): string {
    return new Intl.NumberFormat('en-US', {
        notation: 'compact',
        maximumFractionDigits: 1,
    }).format(value)
}

export function formatDate(dateString: string | null | undefined): string {
    if (!dateString) return '—'
    return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
    })
}

export function formatDateTime(dateString: string | null | undefined): string {
    if (!dateString) return '—'
    return new Date(dateString).toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    })
}

export function formatPercent(value: number | null | undefined): string {
    if (value === null || value === undefined) return '0%'
    return `${value.toFixed(0)}%`
}

export function getInitials(firstName: string, lastName: string): string {
    return `${firstName?.[0] || ''}${lastName?.[0] || ''}`.toUpperCase()
}

export function formatMonthLabel(monthStr: string): string {
    // "2024-01" → "Jan 2024"
    const [year, month] = monthStr.split('-')
    const date = new Date(parseInt(year), parseInt(month) - 1)
    return date.toLocaleDateString('en-US', { month: 'short', year: 'numeric' })
}