import { apiClient } from './client'
import type { CosaResumen, CosaDetalle, CrearCosaPayload, ActualizarCosaPayload } from './types'

export async function listarCosas(): Promise<CosaResumen[]> {
  const res = await apiClient.get<CosaResumen[]>('/catalogo/cosas')
  return res.data
}

export async function getCosa(id: string): Promise<CosaDetalle> {
  const res = await apiClient.get<CosaDetalle>(`/catalogo/cosas/${id}`)
  return res.data
}

export async function crearCosa(data: CrearCosaPayload): Promise<CosaDetalle> {
  const res = await apiClient.post<CosaDetalle>('/catalogo/cosas', data)
  return res.data
}

export async function actualizarCosa(id: string, data: ActualizarCosaPayload): Promise<CosaDetalle> {
  const res = await apiClient.put<CosaDetalle>(`/catalogo/cosas/${id}`, data)
  return res.data
}

export async function eliminarCosa(id: string): Promise<void> {
  await apiClient.delete(`/catalogo/cosas/${id}`)
}
