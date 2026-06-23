import { useEffect, useRef } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '@/store/authStore'
import { usePartidaStore } from '@/store/partidaStore'
import { connectPartidaSocket, disconnectPartidaSocket } from './stompClient'
import type {
  WsEnvelope,
  WsTurnoDeDatos,
  WsPistaRegistradaDatos,
  WsRevelacionDatos,
} from './wsTypes'
import type { EstadoPartida } from '@/shared/api/types'

export function usePartidaSocket(partida: EstadoPartida | undefined, codigoSala: string) {
  const queryClient = useQueryClient()
  const token       = useAuthStore((s) => s.token)

  const codigoRef = useRef(codigoSala)
  codigoRef.current = codigoSala

  useEffect(() => {
    if (!partida?.id || !token) return

    const onMessage = (body: string) => {
      let envelope: WsEnvelope
      try {
        envelope = JSON.parse(body) as WsEnvelope
      } catch {
        return
      }

      const store = usePartidaStore.getState()

      switch (envelope.tipo) {
        case 'turno_de':
          store.setTurnoActual((envelope.datos as WsTurnoDeDatos).idJugador)
          break
        case 'pista_registrada':
          store.addJugadorConPista((envelope.datos as WsPistaRegistradaDatos).idJugador)
          break
        case 'cambio_fase':
          store.resetRoundState()
          break
        case 'revelacion':
          store.setRevelacion(envelope.datos as WsRevelacionDatos)
          queryClient.invalidateQueries({ queryKey: ['revelacion', codigoRef.current] })
          break
        default:
          break
      }

      queryClient.invalidateQueries({ queryKey: ['partida', codigoRef.current] })
    }

    connectPartidaSocket(
      token,
      partida.id,
      onMessage,
      () => queryClient.invalidateQueries({ queryKey: ['partida', codigoRef.current] }),
    )

    return () => {
      disconnectPartidaSocket()
    }
  // Reconnect only when partida ID or token changes
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [partida?.id, token])
}
