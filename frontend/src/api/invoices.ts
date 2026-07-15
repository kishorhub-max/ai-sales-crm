import api from './axios'
import type { Invoice, PageResponse, InvoiceStatus } from '@/types'

interface InvoiceFilters {
    status?: InvoiceStatus
    customerId?: number
    search?: string
    page?: number
    size?: number
    sortBy?: string
    sortDir?: 'asc' | 'desc'
}

interface InvoiceRequest {
    customerId: number
    orderId?: number
    issueDate: string
    dueDate: string
    subtotal?: number
    taxPercent?: number
    discountPercent?: number
    notes?: string
}

export const invoicesApi = {
    getAll: (filters: InvoiceFilters = {}) =>
        api
            .get<PageResponse<Invoice>>('/invoices', { params: filters })
            .then((r) => r.data),

    getById: (id: number) =>
        api.get<Invoice>(`/invoices/${id}`).then((r) => r.data),

    create: (data: InvoiceRequest) =>
        api.post<Invoice>('/invoices', data).then((r) => r.data),

    generateFromOrder: (orderId: number) =>
        api
            .post<Invoice>(`/invoices/generate-from-order/${orderId}`)
            .then((r) => r.data),

    markAsSent: (id: number) =>
        api.patch<Invoice>(`/invoices/${id}/send`).then((r) => r.data),

    markAsPaid: (id: number, paidDate: string, paymentMethod: string) =>
        api
            .patch<Invoice>(`/invoices/${id}/pay`, { paidDate, paymentMethod })
            .then((r) => r.data),

    cancel: (id: number) =>
        api.patch<Invoice>(`/invoices/${id}/cancel`).then((r) => r.data),

    getOverdue: () =>
        api.get<Invoice[]>('/invoices/overdue').then((r) => r.data),

    getByCustomer: (customerId: number) =>
        api.get<Invoice[]>(`/invoices/customer/${customerId}`).then((r) => r.data),

    getStats: () => api.get('/invoices/stats').then((r) => r.data),

    // PDF download — returns blob, triggers browser download
    downloadPdf: async (id: number, invoiceNumber: string) => {
        const response = await api.get(`/invoices/${id}/pdf`, {
            responseType: 'blob',
        })
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', `${invoiceNumber}.pdf`)
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)
    },
}