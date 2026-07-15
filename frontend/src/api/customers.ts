import api from './axios'
import type { Customer, CustomerRequest, PageResponse } from '@/types'

interface CustomerFilters {
    active?: boolean
    assignedToId?: number
    search?: string
    page?: number
    size?: number
    sortBy?: string
    sortDir?: 'asc' | 'desc'
}

interface ConvertLeadRequest {
    leadId: number
    company?: string
    jobTitle?: string
    industry?: string
    website?: string
    notes?: string
    assignedToId?: number
}

export const customersApi = {
    getAll: (filters: CustomerFilters = {}) =>
        api
            .get<PageResponse<Customer>>('/customers', { params: filters })
            .then((r) => r.data),

    getById: (id: number) =>
        api.get<Customer>(`/customers/${id}`).then((r) => r.data),

    create: (data: CustomerRequest) =>
        api.post<Customer>('/customers', data).then((r) => r.data),

    update: (id: number, data: CustomerRequest) =>
        api.put<Customer>(`/customers/${id}`, data).then((r) => r.data),

    convertLead: (data: ConvertLeadRequest) =>
        api.post<Customer>('/customers/convert-lead', data).then((r) => r.data),

    delete: (id: number) => api.delete(`/customers/${id}`),

    getTop: (limit = 10) =>
        api
            .get<Customer[]>('/customers/top', { params: { limit } })
            .then((r) => r.data),

    getRecent: (limit = 10) =>
        api
            .get<Customer[]>('/customers/recent', { params: { limit } })
            .then((r) => r.data),

    getStats: () => api.get('/customers/stats').then((r) => r.data),
}