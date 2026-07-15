import api from './axios'
import type { DashboardStats } from '@/types'

export const dashboardApi = {
    getStats: () =>
        api.get<DashboardStats>('/dashboard').then((r) => r.data),
}