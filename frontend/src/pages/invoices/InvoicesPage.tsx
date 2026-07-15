import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Download, Send, CheckCircle2, ChevronLeft, ChevronRight, AlertCircle } from 'lucide-react'
import toast from 'react-hot-toast'
import { invoicesApi } from '@/api/invoices'
import StatusBadge from '@/components/ui/StatusBadge'
import { LoadingSpinner, EmptyState } from '@/components/ui/States'
import { formatCurrency, formatDate } from '@/utils/formatters'
import type { InvoiceStatus } from '@/types'

export default function InvoicesPage() {
    const queryClient = useQueryClient()
    const [statusFilter, setStatusFilter] = useState<InvoiceStatus | ''>('')
    const [page, setPage] = useState(0)

    const { data, isLoading } = useQuery({
        queryKey: ['invoices', statusFilter, page],
        queryFn: () => invoicesApi.getAll({ status: statusFilter || undefined, page, size: 10 }),
    })

    const sendMutation = useMutation({
        mutationFn: invoicesApi.markAsSent,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['invoices'] })
            toast.success('Invoice marked as sent')
        },
    })

    const payMutation = useMutation({
        mutationFn: (id: number) => invoicesApi.markAsPaid(id, new Date().toISOString().split('T')[0], 'Bank Transfer'),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['invoices'] })
            toast.success('Invoice marked as paid')
        },
    })

    const handleDownload = async (id: number, invoiceNumber: string) => {
        try {
            await invoicesApi.downloadPdf(id, invoiceNumber)
            toast.success('Invoice downloaded')
        } catch {
            toast.error('Failed to download invoice')
        }
    }

    return (
        <div className="space-y-5 animate-fade-in">
            <div>
                <h1 className="page-title">Invoices</h1>
                <p className="mt-1 text-sm text-gray-500">Manage billing and payment status</p>
            </div>

            <div className="card p-4">
                <select className="select sm:w-56" value={statusFilter} onChange={(e) => { setStatusFilter(e.target.value as InvoiceStatus | ''); setPage(0) }}>
                    <option value="">All Statuses</option>
                    <option value="DRAFT">Draft</option>
                    <option value="SENT">Sent</option>
                    <option value="PAID">Paid</option>
                    <option value="OVERDUE">Overdue</option>
                    <option value="CANCELLED">Cancelled</option>
                </select>
            </div>

            <div className="card overflow-hidden">
                {isLoading ? (
                    <LoadingSpinner />
                ) : !data || data.content.length === 0 ? (
                    <EmptyState title="No invoices found" description="Generate an invoice from an order." />
                ) : (
                    <>
                        <div className="overflow-x-auto">
                            <table className="w-full">
                                <thead>
                                <tr>
                                    <th className="table-th">Invoice #</th>
                                    <th className="table-th">Customer</th>
                                    <th className="table-th">Amount</th>
                                    <th className="table-th">Due Date</th>
                                    <th className="table-th">Status</th>
                                    <th className="table-th text-right">Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                {data.content.map((inv) => (
                                    <tr key={inv.id} className="table-row">
                                        <td className="table-td font-medium text-gray-900">{inv.invoiceNumber}</td>
                                        <td className="table-td">{inv.customerName}</td>
                                        <td className="table-td font-semibold">{formatCurrency(inv.total)}</td>
                                        <td className="table-td">
                                            <div className="flex items-center gap-1.5">
                                                {formatDate(inv.dueDate)}
                                                {inv.overdue && <AlertCircle className="h-3.5 w-3.5 text-red-500" />}
                                            </div>
                                        </td>
                                        <td className="table-td"><StatusBadge status={inv.overdue ? 'OVERDUE' : inv.status} /></td>
                                        <td className="table-td text-right">
                                            <div className="flex justify-end gap-1.5">
                                                {inv.status === 'DRAFT' && (
                                                    <button onClick={() => sendMutation.mutate(inv.id)}
                                                            className="rounded-lg p-1.5 text-gray-500 hover:bg-blue-50 hover:text-blue-600" title="Mark as sent">
                                                        <Send className="h-4 w-4" />
                                                    </button>
                                                )}
                                                {inv.status !== 'PAID' && inv.status !== 'CANCELLED' && (
                                                    <button onClick={() => payMutation.mutate(inv.id)}
                                                            className="rounded-lg p-1.5 text-gray-500 hover:bg-emerald-50 hover:text-emerald-600" title="Mark as paid">
                                                        <CheckCircle2 className="h-4 w-4" />
                                                    </button>
                                                )}
                                                <button onClick={() => handleDownload(inv.id, inv.invoiceNumber)}
                                                        className="rounded-lg p-1.5 text-gray-500 hover:bg-gray-100 hover:text-gray-700" title="Download PDF">
                                                    <Download className="h-4 w-4" />
                                                </button>
                                            </div>
                                        </td>
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
        </div>
    )
}