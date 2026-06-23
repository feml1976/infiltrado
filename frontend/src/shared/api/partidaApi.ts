import { apiClient } from './client'
import type {
  CartaResponse,
  CrearPartidaRequest,
  CrearPartidaResponse,
  EstadoPartida,
  RevelacionResponse,
} from './types'

export async function crearPartida(data: CrearPartidaRequest): Promise<CrearPartidaResponse> {
  const res = await apiClient.post<CrearPartidaResponse>('/partidas', data)
  return res.data
}

export async function unirseAPartida(codigoSala: string): Promise<EstadoPartida> {
  const res = await apiClient.post<EstadoPartida>(`/partidas/${codigoSala}/unirse`)
  return res.data
}

export async function getEstadoPartida(codigoSala: string): Promise<EstadoPartida> {
  const res = await apiClient.get<EstadoPartida>(`/partidas/${codigoSala}`)
  return res.data
}

export async function getMiCarta(codigoSala: string): Promise<CartaResponse> {
  const res = await apiClient.get<CartaResponse>(`/partidas/${codigoSala}/mi-carta`)
  return res.data
}

export async function iniciarPartida(codigoSala: string): Promise<EstadoPartida> {
  const res = await apiClient.post<EstadoPartida>(`/partidas/${codigoSala}/iniciar`)
  return res.data
}

export async function avanzarTurno(codigoSala: string): Promise<EstadoPartida> {
  const res = await apiClient.post<EstadoPartida>(`/partidas/${codigoSala}/turno/avanzar`)
  return res.data
}

export async function registrarPista(codigoSala: string, contenido: string): Promise<EstadoPartida> {
  const res = await apiClient.post<EstadoPartida>(`/partidas/${codigoSala}/turno/pista`, { contenido })
  return res.data
}

export async function registrarSenalamiento(
  codigoSala: string,
  idsSenalados: string[],
): Promise<EstadoPartida> {
  const res = await apiClient.post<EstadoPartida>(`/partidas/${codigoSala}/senalamiento`, {
    idsSenalados,
  })
  return res.data
}

export async function registrarAdivinanza(
  codigoSala: string,
  textoAdivinanza: string,
): Promise<EstadoPartida> {
  const res = await apiClient.post<EstadoPartida>(`/partidas/${codigoSala}/adivinanza`, {
    textoAdivinanza,
  })
  return res.data
}

export async function getRevelacion(codigoSala: string): Promise<RevelacionResponse> {
  const res = await apiClient.get<RevelacionResponse>(`/partidas/${codigoSala}/revelacion`)
  return res.data
}

export async function continuarPartida(codigoSala: string): Promise<EstadoPartida> {
  const res = await apiClient.post<EstadoPartida>(`/partidas/${codigoSala}/continuar`)
  return res.data
}

export async function terminarPartida(codigoSala: string): Promise<EstadoPartida> {
  const res = await apiClient.post<EstadoPartida>(`/partidas/${codigoSala}/terminar`)
  return res.data
}
