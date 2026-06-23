import axios, { type AxiosInstance } from 'axios'

export function extractSubFromJwt(token: string): string | null {
  try {
    const b64     = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const payload = JSON.parse(atob(b64)) as Record<string, unknown>
    return typeof payload.sub === 'string' ? payload.sub : null
  } catch {
    return null
  }
}

export function extractApiError(error: unknown, fallback: string): string {
  if (axios.isAxiosError(error)) {
    return (error.response?.data as { mensaje?: string })?.mensaje ?? fallback
  }
  return fallback
}

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export const apiClient: AxiosInstance = axios.create({
  baseURL: `${BASE_URL}/api`,
  headers: { 'Content-Type': 'application/json' },
})

export function setupInterceptors(
  getToken: () => string | null,
  onUnauthorized: () => void,
): void {
  apiClient.interceptors.request.use((config) => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  })

  apiClient.interceptors.response.use(
    (response) => response,
    (error: unknown) => {
      if (axios.isAxiosError(error) && error.response?.status === 401) {
        onUnauthorized()
      }
      return Promise.reject(error)
    },
  )
}
