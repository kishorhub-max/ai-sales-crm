import { useParams, useNavigate, Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, Mail, Phone, Building2, Globe, UserCheck } from 'lucide-react'
import toast from 'react-hot-toast'
import { leadsApi } from '@/api/leads'
import { customersApi } from '@/api/customers'
import StatusBadge from '@/components/ui/StatusBadge'
import { LoadingSpinner } from '@/components/ui/States'
import { formatCurrency, formatDateTime } from '@/utils/formatters'

export default function LeadDetailPage() {
    const { id } = useParams()
    const navigate = useNavigate()
    const queryClient = useQueryClient()
    const leadId = Number(id)

    const { data: lead, isLoading } = useQuery({
        queryKey: ['lead', leadId],
        queryFn: () => leadsApi.getById(leadId),
    })

    const convertMutation = useMutation({
        mutationFn: () => customersApi.convertLead({ leadId }),
        onSuccess: (customer) => {
            toast.success('Lead converted to customer!')
            queryClient.invalidateQueries({ queryKey: ['leads'] })
            navigate(`/customers/${customer.id}`)
        },
    })

    if (isLoading) return <LoadingSpinner />
    if (!lead) return <p>Lead not found.</p>

    return (
        <div className="space-y-5 animate-fade-in max-w-3xl">
            <button onClick={() => navigate('/leads')} className="flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-700">
                <ArrowLeft className="h-4 w-4" /> Back to Leads
            </button>

            <div className="card p-6">
                <div className="flex items-start justify-between">
                    <div>
                        <h1 className="text-xl font-bold text-gray-900">{lead.name}</h1>
                        <p className="text-sm text-gray-500">{lead.jobTitle} {lead.company && `at ${lead.company}`}</p>
                    </div>
                    <StatusBadge status={lead.status} />
                </div>

                <div className="mt-5 grid grid-cols-2 gap-4 text-sm">
                    <div className="flex items-center gap-2 text-gray-600"><Mail className="h-4 w-4 text-gray-400" />{lead.email}</div>
                    <div className="flex items-center gap-2 text-gray-600"><Phone className="h-4 w-4 text-gray-400" />{lead.phone || '—'}</div>
                    <div className="flex items-center gap-2 text-gray-600"><Building2 className="h-4 w-4 text-gray-400" />{lead.industry || '—'}</div>
                    <div className="flex items-center gap-2 text-gray-600"><Globe className="h-4 w-4 text-gray-400" />{lead.website || '—'}</div>
                </div>

                {lead.estimatedValue && (
                    <div className="mt-4 rounded-lg bg-primary-50 px-4 py-3">
                        <p className="text-xs text-primary-600">Estimated Value</p>
                        <p className="text-lg font-bold text-primary-700">{formatCurrency(lead.estimatedValue)}</p>
                    </div>
                )}

                {lead.notes && (
                    <div className="mt-4">
                        <p className="label">Notes</p>
                        <p className="text-sm text-gray-600">{lead.notes}</p>
                    </div>
                )}

                <div className="mt-5 flex items-center justify-between border-t border-gray-100 pt-4 text-xs text-gray-400">
                    <span>Source: {lead.source.replace(/_/g, ' ')}</span>
                    <span>Created {formatDateTime(lead.createdAt)}</span>
                </div>

                {lead.status !== 'CONVERTED' && (
                    <button
                        onClick={() => convertMutation.mutate()}
                        disabled={convertMutation.isPending}
                        className="btn-primary mt-5 w-full justify-center"
                    >
                        <UserCheck className="h-4 w-4" />
                        {convertMutation.isPending ? 'Converting...' : 'Convert to Customer'}
                    </button>
                )}
            </div>
        </div>
    )
}