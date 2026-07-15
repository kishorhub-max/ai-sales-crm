import api from './axios'
import type { AuthResponse, LoginRequest, RegisterRequest } from '@/types'

export const authApi = {
    login: (data: LoginRequest) =>
        api.post<AuthResponse>('/auth/login', data).then((r) => r.data),

    register: (data: RegisterRequest) =>
        api.post<AuthResponse>('/auth/register', data).then((r) => r.data),

    refresh: (refreshToken: string) =>
        api
            .post<AuthResponse>('/auth/refresh', null, {
                headers: { 'X-Refresh-Token': refreshToken },
            })
            .then((r) => r.data),
}