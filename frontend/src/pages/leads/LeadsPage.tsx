import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { Plus, Search, Trash2, Pencil, ChevronLeft, ChevronRight } from 'lucide-react'
import toast from 'react-hot-toast'
import { leadsApi } from '@/api/leads'
import StatusBadge from '@/components/ui/StatusBadge'
import { LoadingSpinner, EmptyState } from '@/components/ui/States'
import { formatDate } from '@/utils/formatters'
import LeadFormModal from './LeadFormModal'
import type { Lead, LeadRequest, LeadStatus } from '@/types'

export default function LeadsPage() {
    const queryClient = useQueryClient()
    const [search, setSearch] = useState('')
    const [statusFilter, setStatusFilter] = useState<LeadStatus | ''>('')
    const [page, setPage] = useState(0)
    const [modalOpen, setModalOpen] = useState(false)
    const [editingLead, setEditingLead] = useState<Lead | null>(null)

    const { data, isLoading } = useQuery({
        queryKey: ['leads', search, statusFilter, page],
        queryFn: () => leadsApi.getAll({
            search: search || undefined,
            status: statusFilter || undefined,
            page, size: 10,
        }),
    })

    const createMutation = useMutation({
        mutationFn: leadsApi.create,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['leads'] })
            toast.success('Lead created')
            setModalOpen(false)
        },
    })

    const updateMutation = useMutation({
        mutationFn: ({ id, data }: { id: number; data: LeadRequest }) => leadsApi.update(id, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['leads'] })
            toast.success('Lead updated')
            setModalOpen(false)
            setEditingLead(null)
        },
    })

    const deleteMutation = useMutation({
        mutationFn: leadsApi.delete,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['leads'] })
            toast.success('Lead deleted')
        },
    })

    const handleSubmit = (formData: LeadRequest) => {
        if (editingLead) updateMutation.mutate({ id: editingLead.id, data: formData })
        else createMutation.mutate(formData)
    }

    const handleDelete = (id: number) => {
        if (confirm('Delete this lead?')) deleteMutation.mutate(id)
    }

    return (
        <div className="space-y-5 animate-fade-in">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="page-title">Leads</h1>
                    <p className="mt-1 text-sm text-gray-500">Manage and track your sales leads</p>
                </div>
                <button onClick={() => { setEditingLead(null); setModalOpen(true) }} className="btn-primary">
                    <Plus className="h-4 w-4" /> New Lead
                </button>
            </div>

            {/* Filters */}
            <div className="card flex flex-col gap-3 p-4 sm:flex-row">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
                    <input
                        placeholder="Search by name, email, or company..."
                        className="input pl-10"
                        value={search}
                        onChange={(e) => { setSearch(e.target.value); setPage(0) }}
                    />
                </div>
                <select
                    className="select sm:w-48"
                    value={statusFilter}
                    onChange={(e) => { setStatusFilter(e.target.value as LeadStatus | ''); setPage(0) }}
                >
                    <option value="">All Statuses</option>
                    <option value="NEW">New</option>
                    <option value="CONTACTED">Contacted</option>
                    <option value="QUALIFIED">Qualified</option>
                    <option value="UNQUALIFIED">Unqualified</option>
                    <option value="CONVERTED">Converted</option>
                    <option value="LOST">Lost</option>
                </select>
            </div>

            {/* Table */}
            <div className="card overflow-hidden">
                {isLoading ? (
                    <LoadingSpinner />
                ) : !data || data.content.length === 0 ? (
                    <EmptyState title="No leads found" description="Create your first lead to get started." />
                ) : (
                    <>
                        <div className="overflow-x-auto">
                            <table className="w-full">
                                <thead>
                                <tr>
                                    <th className="table-th">Name</th>
                                    <th className="table-th">Company</th>
                                    <th className="table-th">Source</th>
                                    <th className="table-th">Status</th>
                                    <th className="table-th">Assigned To</th>
                                    <th className="table-th">Created</th>
                                    <th className="table-th text-right">Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                {data.content.map((lead) => (
                                    <tr key={lead.id} className="table-row">
                                        <td className="table-td">
                                            <Link to={`/leads/${lead.id}`} className="font-medium text-gray-900 hover:text-primary-600">
                                                {lead.name}
                                            </Link>
                                            <p className="text-xs text-gray-500">{lead.email}</p>
                                        </td>
                                        <td className="table-td">{lead.company || '—'}</td>
                                        <td className="table-td">{lead.source.replace(/_/g, ' ')}</td>
                                        <td className="table-td"><StatusBadge status={lead.status} /></td>
                                        <td className="table-td">{lead.assignedToName || '—'}</td>
                                        <td className="table-td">{formatDate(lead.createdAt)}</td>
                                        <td className="table-td text-right">
                                            <div className="flex justify-end gap-2">
                                                <button onClick={() => { setEditingLead(lead); setModalOpen(true) }}
                                                        className="rounded-lg p-1.5 text-gray-500 hover:bg-gray-100 hover:text-primary-600">
                                                    <Pencil className="h-4 w-4" />
                                                </button>
                                                <button onClick={() => handleDelete(lead.id)}
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

                        {/* Pagination */}
                        <div className="flex items-center justify-between border-t border-gray-100 px-4 py-3">
                            <p className="text-sm text-gray-500">
                                Page {data.page + 1} of {data.totalPages} · {data.totalElements} total
                            </p>
                            <div className="flex gap-2">
                                <button disabled={data.first} onClick={() => setPage((p) => p - 1)}
                                        className="btn-secondary px-3 py-1.5 disabled:opacity-40">
                                    <ChevronLeft className="h-4 w-4" />
                                </button>
                                <button disabled={data.last} onClick={() => setPage((p) => p + 1)}
                                        className="btn-secondary px-3 py-1.5 disabled:opacity-40">
                                    <ChevronRight className="h-4 w-4" />
                                </button>
                            </div>
                        </div>
                    </>
                )}
            </div>

            <LeadFormModal
                isOpen={modalOpen}
                onClose={() => { setModalOpen(false); setEditingLead(null) }}
                onSubmit={handleSubmit}
                initialData={editingLead}
                isSubmitting={createMutation.isPending || updateMutation.isPending}
            />
        </div>
    )
}