// ── Auth ─────────────────────────────────────────────────────────────────────

export interface RegistroRequest {
  email: string
  password: string
  nombre: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  nombre: string
  esAdmin: boolean
}

// ── Partida ───────────────────────────────────────────────────────────────────

export type EstadoPartidaEnum =
  | 'LOBBY'
  | 'EN_CURSO'
  | 'SENALAMIENTO'
  | 'ADIVINANZA'
  | 'REVELACION'
  | 'FINALIZADA'

export type RolJugador = 'NORMAL' | 'INFILTRADO'

export interface JugadorResumen {
  id: string
  nombre: string
  ordenTurno: number
  puntosAcumulados: number
  haSenalado: boolean
}

export interface EstadoPartida {
  id: string
  codigoSala: string
  idModerador: string
  estado: EstadoPartidaEnum
  numRondas: number
  numInfiltrados: number
  numJugadores: number
  rondaActual: number
  jugadores: JugadorResumen[]
}

export interface CrearPartidaRequest {
  numRondas: number
  numInfiltrados: number
  numJugadores: number
}

export interface CrearPartidaResponse {
  id: string
  codigoSala: string
  estado: EstadoPartidaEnum
}

// ── Carta ─────────────────────────────────────────────────────────────────────

export interface CartaResponse {
  rol: RolJugador | null
  idCosa: string | null
  nombreCosa: string | null
  tipo: 'PALABRA' | 'IMAGEN' | null
  imagenBase64: string | null
}

// ── Action Requests ───────────────────────────────────────────────────────────

export interface RegistrarPistaRequest {
  contenido: string
}

export interface RegistrarSenalamienatoRequest {
  idsSenalados: string[]
}

export interface RegistrarAdivinanzaRequest {
  textoAdivinanza: string
}

// ── Catálogo ──────────────────────────────────────────────────────────────────

export type TipoCosa = 'PALABRA' | 'IMAGEN'

export interface CosaResumen {
  id: string
  nombre: string
  tipo: TipoCosa
  activo: boolean
}

export interface CosaDetalle {
  id: string
  nombre: string
  tipo: TipoCosa
  imagenBase64: string | null
  activo: boolean
}

export interface CrearCosaPayload {
  nombre: string
  tipo: TipoCosa
  imagenBase64?: string | null
}

export interface ActualizarCosaPayload {
  nombre: string
  tipo: TipoCosa
  imagenBase64?: string | null
}

// ── Revelacion ────────────────────────────────────────────────────────────────

export interface RevelacionResponse {
  idPartida: string
  codigoSala: string
  nombreCosa: string
  tipoCosa: string
  jugadores: RevelacionJugadorItem[]
  senalamientos: RevelacionSenalamienatoItem[]
  adivinanzas: RevelacionAdivinanzaItem[]
}

export interface RevelacionJugadorItem {
  id: string
  nombre: string
  ordenTurno: number
  rol: RolJugador
  deltaRonda: number
  puntosAcumulados: number
}

export interface RevelacionSenalamienatoItem {
  idJugadorOrigen: string
  idJugadorSenalado: string
}

export interface RevelacionAdivinanzaItem {
  idJugadorInfiltrado: string
  textoAdivinanza: string
  acierto: boolean
}
