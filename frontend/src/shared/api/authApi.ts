import { apiClient } from './client'
import type { AuthResponse, LoginRequest, RegistroRequest } from './types'

export async function registro(data: RegistroRequest): Promise<AuthResponse> {
  const res = await apiClient.post<AuthResponse>('/auth/registro', data)
  return res.data
}

export async function login(data: LoginRequest): Promise<AuthResponse> {
  const res = await apiClient.post<AuthResponse>('/auth/login', data)
  return res.data
}
