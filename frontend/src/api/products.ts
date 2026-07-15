import api from './axios'
import type { Product, ProductRequest, PageResponse } from '@/types'

interface ProductFilters {
    active?: boolean
    category?: string
    search?: string
    page?: number
    size?: number
    sortBy?: string
    sortDir?: 'asc' | 'desc'
}

export const productsApi = {
    getAll: (filters: ProductFilters = {}) =>
        api
            .get<PageResponse<Product>>('/products', { params: filters })
            .then((r) => r.data),

    getById: (id: number) =>
        api.get<Product>(`/products/${id}`).then((r) => r.data),

    create: (data: ProductRequest) =>
        api.post<Product>('/products', data).then((r) => r.data),

    update: (id: number, data: ProductRequest) =>
        api.put<Product>(`/products/${id}`, data).then((r) => r.data),

    delete: (id: number) => api.delete(`/products/${id}`),

    getCategories: () =>
        api.get<string[]>('/products/categories').then((r) => r.data),

    getLowStock: (threshold = 10) =>
        api
            .get<Product[]>('/products/low-stock', { params: { threshold } })
            .then((r) => r.data),

    getStatsByCategory: () =>
        api
            .get<Record<string, number>>('/products/stats/by-category')
            .then((r) => r.data),
}