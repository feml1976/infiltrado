import { create } from 'zustand'
import type { WsRevelacionDatos } from '@/shared/ws/wsTypes'

interface PartidaState {
  codigoSala:           string | null
  estadoActual:         string | null
  esModerador:          boolean
  idJugadorTurnoActual: string | null
  jugadoresConPista:    string[]
  revelacionWs:         WsRevelacionDatos | null
  setCodigoSala:        (codigo: string | null) => void
  setContextoPartida:   (codigoSala: string, estadoActual: string, esModerador: boolean) => void
  setTurnoActual:       (idJugador: string) => void
  addJugadorConPista:   (idJugador: string) => void
  setRevelacion:        (datos: WsRevelacionDatos) => void
  resetRoundState:      () => void
  limpiarPartida:       () => void
}

export const usePartidaStore = create<PartidaState>((set) => ({
  codigoSala:           null,
  estadoActual:         null,
  esModerador:          false,
  idJugadorTurnoActual: null,
  jugadoresConPista:    [],
  revelacionWs:         null,

  setCodigoSala: (codigoSala) => set({ codigoSala }),

  setContextoPartida: (codigoSala, estadoActual, esModerador) =>
    set({ codigoSala, estadoActual, esModerador }),

  setTurnoActual: (idJugador) => set({ idJugadorTurnoActual: idJugador }),

  addJugadorConPista: (idJugador) =>
    set((s) => ({
      jugadoresConPista: s.jugadoresConPista.includes(idJugador)
        ? s.jugadoresConPista
        : [...s.jugadoresConPista, idJugador],
    })),

  setRevelacion: (datos) => set({ revelacionWs: datos }),

  resetRoundState: () =>
    set({ idJugadorTurnoActual: null, jugadoresConPista: [] }),

  limpiarPartida: () =>
    set({
      codigoSala:           null,
      estadoActual:         null,
      esModerador:          false,
      idJugadorTurnoActual: null,
      jugadoresConPista:    [],
      revelacionWs:         null,
    }),
}))
