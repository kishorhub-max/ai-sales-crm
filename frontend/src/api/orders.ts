import api from './axios'
import type { Order, OrderRequest, PageResponse, OrderStatus } from '@/types'

interface OrderFilters {
    status?: OrderStatus
    customerId?: number
    search?: string
    page?: number
    size?: number
    sortBy?: string
    sortDir?: 'asc' | 'desc'
}

export const ordersApi = {
    getAll: (filters: OrderFilters = {}) =>
        api
            .get<PageResponse<Order>>('/orders', { params: filters })
            .then((r) => r.data),

    getById: (id: number) =>
        api.get<Order>(`/orders/${id}`).then((r) => r.data),

    getByNumber: (orderNumber: string) =>
        api.get<Order>(`/orders/number/${orderNumber}`).then((r) => r.data),

    create: (data: OrderRequest) =>
        api.post<Order>('/orders', data).then((r) => r.data),

    updateStatus: (id: number, status: OrderStatus) =>
        api.patch<Order>(`/orders/${id}/status`, { status }).then((r) => r.data),

    cancel: (id: number) => api.patch(`/orders/${id}/cancel`),

    getByCustomer: (customerId: number) =>
        api.get<Order[]>(`/orders/customer/${customerId}`).then((r) => r.data),

    getStats: () => api.get('/orders/stats').then((r) => r.data),
}