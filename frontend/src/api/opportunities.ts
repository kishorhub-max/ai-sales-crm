import api from './axios'
import type {
    Opportunity, OpportunityRequest, PageResponse, OpportunityStage,
} from '@/types'

interface OppFilters {
    stage?: OpportunityStage
    customerId?: number
    assignedToId?: number
    search?: string
    page?: number
    size?: number
    sortBy?: string
    sortDir?: 'asc' | 'desc'
}

interface PipelineStats {
    stages: { stage: string; count: number; value: number }[]
    totalPipelineValue: number
    weightedPipelineValue: number
    wonRevenue: number
    totalOpportunities: number
    openOpportunities: number
    wonCount: number
    lostCount: number
    monthlyRevenue: { month: string; revenue: number }[]
}

export const opportunitiesApi = {
    getAll: (filters: OppFilters = {}) =>
        api
            .get<PageResponse<Opportunity>>('/opportunities', { params: filters })
            .then((r) => r.data),

    getById: (id: number) =>
        api.get<Opportunity>(`/opportunities/${id}`).then((r) => r.data),

    create: (data: OpportunityRequest) =>
        api.post<Opportunity>('/opportunities', data).then((r) => r.data),

    update: (id: number, data: OpportunityRequest) =>
        api.put<Opportunity>(`/opportunities/${id}`, data).then((r) => r.data),

    updateStage: (id: number, stage: OpportunityStage, probability?: number, lostReason?: string) =>
        api
            .patch<Opportunity>(`/opportunities/${id}/stage`, {
                stage, probability, lostReason,
            })
            .then((r) => r.data),

    delete: (id: number) => api.delete(`/opportunities/${id}`),

    getByCustomer: (customerId: number) =>
        api.get<Opportunity[]>(`/opportunities/customer/${customerId}`).then((r) => r.data),

    getAtRisk: () =>
        api.get<Opportunity[]>('/opportunities/at-risk').then((r) => r.data),

    getClosingSoon: (days = 30) =>
        api
            .get<Opportunity[]>('/opportunities/closing-soon', { params: { days } })
            .then((r) => r.data),

    getPipeline: () =>
        api.get<PipelineStats>('/opportunities/pipeline').then((r) => r.data),
}