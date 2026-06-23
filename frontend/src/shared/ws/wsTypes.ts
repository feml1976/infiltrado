import type { EstadoPartidaEnum } from '@/shared/api/types'

export interface WsEnvelope {
  tipo: WsEventTipo
  datos: unknown
}

export type WsEventTipo =
  | 'turno_de'
  | 'pista_registrada'
  | 'cambio_fase'
  | 'progreso_senalamiento'
  | 'progreso_adivinanza'
  | 'revelacion'

export interface WsTurnoDeDatos {
  idJugador: string
  nombreJugador: string
  ordenTurno: number
  rondaActual: number
}

export interface WsCambioFaseDatos {
  estado: EstadoPartidaEnum
  rondaActual: number
}

export interface WsPistaRegistradaDatos {
  idJugador: string
  nombreJugador: string
}

export interface WsProgresoSenalamientoDatos {
  idJugador: string
  nombre: string
  pendientes: number
}

export interface WsProgresoAdivinanzaDatos {
  pendientes: number
}

export interface WsRevelacionDatos {
  jugadores: WsRevelacionJugador[]
  idCosa: string
  nombreCosa: string
  senalamientos: WsSenalamiento[]
  adivinanzas: WsAdivinanza[]
}

export interface WsRevelacionJugador {
  idJugador: string
  nombreJugador: string
  rol: 'NORMAL' | 'INFILTRADO'
  deltaRonda: number
  puntosAcumulados: number
}

export interface WsSenalamiento {
  idJugadorOrigen: string
  idJugadorSenalado: string
}

export interface WsAdivinanza {
  idJugadorInfiltrado: string
  textoAdivinanza: string
  acierto: boolean
}
