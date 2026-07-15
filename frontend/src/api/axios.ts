import axios from 'axios'
import toast from 'react-hot-toast'

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

const api = axios.create({
    baseURL,
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 90000,
})

// Request interceptor — attach Bearer token
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

// Response interceptor — handle 401 / errors
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config

        if (
            error.response?.status === 401 &&
            originalRequest &&
            !originalRequest._retry
        ) {
            originalRequest._retry = true

            const refreshToken = localStorage.getItem('refreshToken')

            if (refreshToken) {
                try {
                    const { data } = await axios.post(
                        `${baseURL}/auth/refresh`,
                        null,
                        {
                            headers: {
                                'X-Refresh-Token': refreshToken,
                            },
                            timeout: 90000,
                        }
                    )

                    localStorage.setItem('accessToken', data.accessToken)
                    localStorage.setItem('refreshToken', data.refreshToken)

                    originalRequest.headers =
                        originalRequest.headers || {}

                    originalRequest.headers.Authorization =
                        `Bearer ${data.accessToken}`

                    return api(originalRequest)
                } catch {
                    localStorage.removeItem('accessToken')
                    localStorage.removeItem('refreshToken')
                    window.location.href = '/login'

                    return Promise.reject(error)
                }
            }

            localStorage.removeItem('accessToken')
            localStorage.removeItem('refreshToken')
            window.location.href = '/login'
        }

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