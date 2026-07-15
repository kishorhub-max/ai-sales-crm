import api from './axios'
import type {
    Lead, LeadRequest, PageResponse, LeadStatus, LeadSource,
} from '@/types'

interface LeadFilters {
    status?: LeadStatus
    source?: LeadSource
    assignedToId?: number
    search?: string
    page?: number
    size?: number
    sortBy?: string
    sortDir?: 'asc' | 'desc'
}

export const leadsApi = {
    getAll: (filters: LeadFilters = {}) =>
        api
            .get<PageResponse<Lead>>('/leads', { params: filters })
            .then((r) => r.data),

    getById: (id: number) =>
        api.get<Lead>(`/leads/${id}`).then((r) => r.data),

    create: (data: LeadRequest) =>
        api.post<Lead>('/leads', data).then((r) => r.data),

    update: (id: number, data: LeadRequest) =>
        api.put<Lead>(`/leads/${id}`, data).then((r) => r.data),

    assign: (id: number, assignedToId: number) =>
        api
            .patch<Lead>(`/leads/${id}/assign`, { assignedToId })
            .then((r) => r.data),

    updateStatus: (id: number, status: LeadStatus) =>
        api
            .patch<Lead>(`/leads/${id}/status`, null, { params: { status } })
            .then((r) => r.data),

    delete: (id: number) => api.delete(`/leads/${id}`),

    getByAssignedUser: (userId: number) =>
        api.get<Lead[]>(`/leads/assigned/${userId}`).then((r) => r.data),

    getStatusCounts: () =>
        api.get<Record<string, number>>('/leads/stats/by-status').then((r) => r.data),

    getSourceCounts: () =>
        api.get<Record<string, number>>('/leads/stats/by-source').then((r) => r.data),
}