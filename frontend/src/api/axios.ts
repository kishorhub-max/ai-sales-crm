import axios from 'axios'
import toast from 'react-hot-toast'

const api = axios.create({
    baseURL: '/api',
    headers: { 'Content-Type': 'application/json' },
    timeout: 30000,
})

// ── Request interceptor — attach Bearer token ─────────────────────────────────
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('accessToken')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => Promise.reject(error)
)

// ── Response interceptor — handle 401 / errors ───────────────────────────────
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config

        // Token expired → try refresh once
        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true
            const refreshToken = localStorage.getItem('refreshToken')

            if (refreshToken) {
                try {
                    const { data } = await axios.post('/api/auth/refresh', null, {
                        headers: { 'X-Refresh-Token': refreshToken },
                    })
                    localStorage.setItem('accessToken',  data.accessToken)
                    localStorage.setItem('refreshToken', data.refreshToken)
                    originalRequest.headers.Authorization = `Bearer ${data.accessToken}`
                    return api(originalRequest)
                } catch {
                    // Refresh failed — clear storage and redirect to login
                    localStorage.clear()
                    window.location.href = '/login'
                    return Promise.reject(error)
                }
            } else {
                localStorage.clear()
                window.location.href = '/login'
            }
        }

        // Show error toast for non-401 errors
        if (error.response?.status !== 401) {
            const message =
                error.response?.data?.message ||
                error.response?.data?.error ||
                'Something went wrong'
            toast.error(message)
        }

        return Promise.reject(error)
    }
)

export default api