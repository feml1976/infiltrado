import { useState, useRef, useEffect, type FormEvent } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { usePartidaStore } from '@/store/partidaStore'
import { registrarPista, avanzarTurno } from '@/shared/api/partidaApi'
import { extractApiError } from '@/shared/api/client'
import { getPlayerColor } from '@/shared/utils/playerColors'
import { useCountUp } from '@/shared/hooks/useCountUp'
import type { EstadoPartida, JugadorResumen } from '@/shared/api/types'

interface Props {
  partida: EstadoPartida
  codigoSala: string
  userId: string
}

interface ScoreRowProps {
  jugador: JugadorResumen
  isActiveTurn: boolean
}

function ScoreRow({ jugador, isActiveTurn }: ScoreRowProps) {
  const pts = useCountUp(jugador.puntosAcumulados)
  const [showFlash, setShowFlash] = useState(false)
  const prevPtsRef = useRef(jugador.puntosAcumulados)

  useEffect(() => {
    if (prevPtsRef.current !== jugador.puntosAcumulados) {
      prevPtsRef.current = jugador.puntosAcumulados
      setShowFlash(true)
      const t = window.setTimeout(() => setShowFlash(false), 750)
      return () => window.clearTimeout(t)
    }
  }, [jugador.puntosAcumulados])

  const color = getPlayerColor(jugador.ordenTurno)

  return (
    <div
      className={`player-item player-item--active-turn ${showFlash ? 'score-row--flash' : ''}`}
      style={isActiveTurn ? {
        background: color + '18',
        boxShadow: `inset 3px 0 0 ${color}`,
      } : undefined}
    >
      <div className="player-badge" style={{ background: color }}>{jugador.ordenTurno}</div>
      <span className="player-name">{jugador.nombre}</span>
      <span style={{ marginLeft: 'auto', color: '#818cf8', fontWeight: 700 }}>
        {pts} pts
      </span>
    </div>
  )
}

export function EnCursoView({ partida, codigoSala, userId }: Props) {
  const queryClient        = useQueryClient()
  const idJugadorTurno     = usePartidaStore((s) => s.idJugadorTurnoActual)
  const jugadoresConPista  = usePartidaStore((s) => s.jugadoresConPista)

  const [pista, setPista]           = useState('')
  const [pistaEnviada, setPistaEnviada] = useState(false)

  const esMiTurno = idJugadorTurno === userId
  const esModerador = partida.idModerador === userId

  const jugadorTurno = partida.jugadores.find((j) => j.id === idJugadorTurno)

  const pistaMutation = useMutation({
    mutationFn: () => registrarPista(codigoSala, pista.trim()),
    onSuccess: () => {
      setPistaEnviada(true)
      setPista('')
      queryClient.invalidateQueries({ queryKey: ['partida', codigoSala] })
    },
  })

  const avanzarMutation = useMutation({
    mutationFn: () => avanzarTurno(codigoSala),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['partida', codigoSala] }),
  })

  function handlePista(e: FormEvent) {
    e.preventDefault()
    if (pista.trim()) pistaMutation.mutate()
  }

  return (
    <div className="fase-section">
      {/* Turno actual */}
      <div className={`turno-info ${esMiTurno ? 'turno-info--mio' : ''}`}>
        {idJugadorTurno ? (
          <>
            <div style={{ color: '#64748b', fontSize: '0.8125rem', marginBottom: '0.25rem' }}>
              Turno
            </div>
            <div className={`turno-nombre ${esMiTurno ? 'turno-mio' : ''}`}>
              {esMiTurno ? '¡Es tu turno!' : (jugadorTurno?.nombre ?? 'Cargando…')}
            </div>
          </>
        ) : (
          <div style={{ color: '#64748b' }}>Esperando inicio de turno…</div>
        )}
      </div>

      {/* Progreso de pistas */}
      <div style={{ marginBottom: '1rem' }}>
        <h3>Pistas registradas ({jugadoresConPista.length}/{partida.numJugadores})</h3>
        <div className="player-list">
          {partida.jugadores
            .slice()
            .sort((a, b) => a.ordenTurno - b.ordenTurno)
            .map((j) => {
              const dioP = jugadoresConPista.includes(j.id)
              const color = getPlayerColor(j.ordenTurno)
              const isActive = j.id === idJugadorTurno
              return (
                <div
                  key={j.id}
                  className="player-item player-item--active-turn"
                  style={isActive ? {
                    background: color + '18',
                    boxShadow: `inset 3px 0 0 ${color}`,
                  } : undefined}
                >
                  <div className="player-badge" style={{ background: color }}>{j.ordenTurno}</div>
                  <span className="player-name">{j.nombre}</span>
                  <span style={{ marginLeft: 'auto', fontSize: '0.875rem' }}>
                    {dioP ? (
                      <span style={{ color: '#34d399' }}>✓</span>
                    ) : (
                      <span style={{ color: '#334155' }}>·</span>
                    )}
                  </span>
                </div>
              )
            })}
        </div>
      </div>

      {/* Acción del jugador en turno */}
      {esMiTurno && !pistaEnviada && (
        <form onSubmit={handlePista}>
          {pistaMutation.error && (
            <div className="alert-error">
              {extractApiError(pistaMutation.error, 'No se pudo registrar la pista')}
            </div>
          )}
          <div className="field">
            <label htmlFor="pista-input">Tu pista</label>
            <input
              id="pista-input"
              type="text"
              value={pista}
              onChange={(e) => setPista(e.target.value)}
              maxLength={500}
              placeholder="Escribe tu pista…"
              required
            />
          </div>
          <div className="flex-gap-2">
            <button
              type="submit"
              className="btn btn-primary"
              style={{ flex: 1 }}
              disabled={pistaMutation.isPending || !pista.trim()}
            >
              {pistaMutation.isPending ? 'Enviando…' : 'Registrar pista'}
            </button>
            <button
              type="button"
              className="btn btn-secondary"
              disabled={avanzarMutation.isPending}
              onClick={() => avanzarMutation.mutate()}
              title="Pasar turno sin pista"
            >
              Pasar
            </button>
          </div>
        </form>
      )}

      {esMiTurno && pistaEnviada && (
        <div className="action-confirmed" style={{ textAlign: 'center', color: '#34d399', marginTop: '1rem' }}>
          Pista registrada — esperando a los demás jugadores
        </div>
      )}

      {!esMiTurno && esModerador && (
        <div className="mt-2">
          <button
            className="btn btn-secondary"
            disabled={avanzarMutation.isPending}
            onClick={() => avanzarMutation.mutate()}
          >
            Avanzar turno (moderador)
          </button>
          {avanzarMutation.error && (
            <div className="alert-error mt-1">
              {extractApiError(avanzarMutation.error, 'No se pudo avanzar el turno')}
            </div>
          )}
        </div>
      )}

      {/* Tabla de puntos con count-up */}
      <div style={{ marginTop: '1.5rem' }}>
        <h3>Puntos actuales</h3>
        <div className="player-list">
          {partida.jugadores
            .slice()
            .sort((a, b) => b.puntosAcumulados - a.puntosAcumulados)
            .map((j) => (
              <ScoreRow
                key={j.id}
                jugador={j}
                isActiveTurn={j.id === idJugadorTurno}
              />
            ))}
        </div>
      </div>
    </div>
  )
}
