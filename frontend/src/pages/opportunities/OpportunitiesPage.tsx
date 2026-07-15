import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, DollarSign, Calendar, MoreVertical } from 'lucide-react'
import toast from 'react-hot-toast'
import { Menu } from '@headlessui/react'
import { opportunitiesApi } from '@/api/opportunities'
import { LoadingSpinner } from '@/components/ui/States'
import { formatCurrency, formatDate } from '@/utils/formatters'
import OpportunityFormModal from './OpportunityFormModal'
import type { Opportunity, OpportunityRequest, OpportunityStage } from '@/types'

const STAGES: { key: OpportunityStage; label: string; color: string }[] = [
    { key: 'NEW',         label: 'New',         color: 'border-blue-400 bg-blue-50' },
    { key: 'QUALIFIED',   label: 'Qualified',   color: 'border-green-400 bg-green-50' },
    { key: 'PROPOSAL',    label: 'Proposal',    color: 'border-purple-400 bg-purple-50' },
    { key: 'NEGOTIATION', label: 'Negotiation', color: 'border-orange-400 bg-orange-50' },
    { key: 'WON',         label: 'Won',         color: 'border-emerald-400 bg-emerald-50' },
    { key: 'LOST',        label: 'Lost',        color: 'border-red-400 bg-red-50' },
]

export default function OpportunitiesPage() {
    const queryClient = useQueryClient()
    const [modalOpen, setModalOpen] = useState(false)
    const [editing, setEditing] = useState<Opportunity | null>(null)

    const { data: pipeline } = useQuery({
        queryKey: ['opportunities', 'pipeline'],
        queryFn: opportunitiesApi.getPipeline,
    })

    const { data, isLoading } = useQuery({
        queryKey: ['opportunities', 'all'],
        queryFn: () => opportunitiesApi.getAll({ size: 100 }),
    })

    const createMutation = useMutation({
        mutationFn: opportunitiesApi.create,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['opportunities'] })
            toast.success('Opportunity created')
            setModalOpen(false)
        },
    })

    const updateMutation = useMutation({
        mutationFn: ({ id, data }: { id: number; data: OpportunityRequest }) => opportunitiesApi.update(id, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['opportunities'] })
            toast.success('Opportunity updated')
            setModalOpen(false)
            setEditing(null)
        },
    })

    const stageMutation = useMutation({
        mutationFn: ({ id, stage }: { id: number; stage: OpportunityStage }) =>
            opportunitiesApi.updateStage(id, stage),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['opportunities'] })
            toast.success('Stage updated')
        },
    })

    const handleSubmit = (formData: OpportunityRequest) => {
        if (editing) updateMutation.mutate({ id: editing.id, data: formData })
        else createMutation.mutate(formData)
    }

    if (isLoading) return <LoadingSpinner />

    const opportunitiesByStage = (stage: OpportunityStage) =>
        data?.content.filter((o) => o.stage === stage) || []

    return (
        <div className="space-y-5 animate-fade-in">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="page-title">Opportunities</h1>
                    <p className="mt-1 text-sm text-gray-500">
                        {pipeline && `${formatCurrency(pipeline.totalPipelineValue)} total pipeline · ${formatCurrency(pipeline.weightedPipelineValue)} weighted`}
                    </p>
                </div>
                <button onClick={() => { setEditing(null); setModalOpen(true) }} className="btn-primary">
                    <Plus className="h-4 w-4" /> New Deal
                </button>
            </div>

            {/* Kanban board */}
            <div className="flex gap-4 overflow-x-auto pb-4">
                {STAGES.map((stageInfo) => {
                    const deals = opportunitiesByStage(stageInfo.key)
                    const stageTotal = deals.reduce((sum, d) => sum + d.value, 0)

                    return (
                        <div key={stageInfo.key} className="w-72 flex-shrink-0">
                            <div className={`mb-3 rounded-lg border-l-4 ${stageInfo.color} px-3 py-2`}>
                                <div className="flex items-center justify-between">
                                    <p className="text-sm font-semibold text-gray-700">{stageInfo.label}</p>
                                    <span className="text-xs text-gray-500">{deals.length}</span>
                                </div>
                                <p className="text-xs text-gray-500">{formatCurrency(stageTotal)}</p>
                            </div>

                            <div className="space-y-2">
                                {deals.map((deal) => (
                                    <div key={deal.id} className="card p-3 hover:shadow-md transition-shadow cursor-pointer">
                                        <div className="flex items-start justify-between">
                                            <p className="text-sm font-medium text-gray-900 flex-1" onClick={() => { setEditing(deal); setModalOpen(true) }}>
                                                {deal.dealName}
                                            </p>
                                            <Menu as="div" className="relative">
                                                <Menu.Button className="text-gray-400 hover:text-gray-600">
                                                    <MoreVertical className="h-4 w-4" />
                                                </Menu.Button>
                                                <Menu.Items className="absolute right-0 z-10 mt-1 w-40 rounded-lg bg-white shadow-lg ring-1 ring-gray-200 p-1">
                                                    {STAGES.filter((s) => s.key !== deal.stage).map((s) => (
                                                        <Menu.Item key={s.key}>
                                                            {({ active }) => (
                                                                <button
                                                                    onClick={() => stageMutation.mutate({ id: deal.id, stage: s.key })}
                                                                    className={`block w-full rounded-md px-2 py-1.5 text-left text-xs ${active ? 'bg-gray-50' : ''}`}
                                                                >
                                                                    Move to {s.label}
                                                                </button>
                                                            )}
                                                        </Menu.Item>
                                                    ))}
                                                </Menu.Items>
                                            </Menu>
                                        </div>
                                        <p className="text-xs text-gray-500 mt-0.5">{deal.customerName}</p>
                                        <div className="mt-2 flex items-center justify-between text-xs">
                      <span className="flex items-center gap-1 font-semibold text-emerald-600">
                        <DollarSign className="h-3 w-3" />{formatCurrency(deal.value)}
                      </span>
                                            <span className="text-gray-400">{deal.probability}%</span>
                                        </div>
                                        {deal.expectedCloseDate && (
                                            <div className="mt-1.5 flex items-center gap-1 text-xs text-gray-400">
                                                <Calendar className="h-3 w-3" />{formatDate(deal.expectedCloseDate)}
                                            </div>
                                        )}
                                    </div>
                                ))}
                                {deals.length === 0 && (
                                    <p className="py-6 text-center text-xs text-gray-400">No deals</p>
                                )}
                            </div>
                        </div>
                    )
                })}
            </div>

            <OpportunityFormModal
                isOpen={modalOpen}
                onClose={() => { setModalOpen(false); setEditing(null) }}
                onSubmit={handleSubmit}
                initialData={editing}
                isSubmitting={createMutation.isPending || updateMutation.isPending}
            />
        </div>
    )
}