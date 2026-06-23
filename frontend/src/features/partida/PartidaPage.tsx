import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { usePartida } from './hooks/usePartida'
import { usePartidaSocket } from '@/shared/ws/usePartidaSocket'
import { useAuthStore } from '@/store/authStore'
import { usePartidaStore } from '@/store/partidaStore'
import { iniciarPartida } from '@/shared/api/partidaApi'
import { extractApiError } from '@/shared/api/client'
import { getPlayerColor } from '@/shared/utils/playerColors'
import { CartaView } from './CartaView'
import { EnCursoView } from './fases/EnCursoView'
import { SenalamientoView } from './fases/SenalamientoView'
import { AdivinanzaView } from './fases/AdivinanzaView'
import { RevelacionView } from './fases/RevelacionView'
import { FinalizadaView } from './fases/FinalizadaView'

export function PartidaPage() {
  const { codigoSala } = useParams<{ codigoSala: string }>()
  const navigate       = useNavigate()
  const userId              = useAuthStore((s) => s.userId)
  const idJugadorTurnoActual = usePartidaStore((s) => s.idJugadorTurnoActual)
  const setContextoPartida  = usePartidaStore((s) => s.setContextoPartida)
  const queryClient         = useQueryClient()

  const [mostrarCarta, setMostrarCarta] = useState(false)

  const { data: partida, isLoading, isError } = usePartida(codigoSala ?? '')
  usePartidaSocket(partida, codigoSala ?? '')

  const esModerador = partida?.idModerador === userId

  useEffect(() => {
    if (partida && codigoSala) {
      setContextoPartida(codigoSala, partida.estado, esModerador)
    }
  }, [partida, codigoSala, esModerador, setContextoPartida])

  const iniciarMutation = useMutation({
    mutationFn: () => iniciarPartida(codigoSala!),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['partida', codigoSala] }),
  })

  if (!codigoSala) {
    navigate('/lobby')
    return null
  }

  const renderFase = () => {
    if (!partida || !userId) return null
    if (mostrarCarta) return null

    switch (partida.estado) {
      case 'LOBBY':
        return (
          <div className="mt-2">
            <h3>Jugadores inscritos ({partida.jugadores.length}/{partida.numJugadores})</h3>
            <div className="player-list">
              {partida.jugadores
                .slice()
                .sort((a, b) => a.ordenTurno - b.ordenTurno)
                .map((j) => (
                  <div key={j.id} className="player-item">
                    <div
                      className="player-badge"
                      style={{ background: getPlayerColor(j.ordenTurno) }}
                    >
                      {j.ordenTurno}
                    </div>
                    <span className="player-name">{j.nombre}</span>
                  </div>
                ))}
            </div>
            {esModerador && (
              <div className="mt-2">
                {iniciarMutation.error && (
                  <div className="alert-error mb-1">
                    {extractApiError(iniciarMutation.error, 'No se pudo iniciar la partida')}
                  </div>
                )}
                <button
                  className="btn btn-primary btn-block"
                  disabled={
                    iniciarMutation.isPending ||
                    partida.jugadores.length < 3
                  }
                  onClick={() => iniciarMutation.mutate()}
                >
                  {iniciarMutation.isPending ? 'Iniciando…' : 'Iniciar partida'}
                </button>
                {partida.jugadores.length < 3 && (
                  <p style={{ fontSize: '0.8125rem', color: 'var(--c-text-dim)', marginTop: '0.5rem', textAlign: 'center' }}>
                    Se necesitan al menos 3 jugadores
                  </p>
                )}
              </div>
            )}
            {!esModerador && (
              <p className="text-center mt-2" style={{ color: 'var(--c-text-dim)', fontSize: '0.875rem' }}>
                Esperando que el moderador inicie la partida
              </p>
            )}
          </div>
        )

      case 'EN_CURSO':
        return <EnCursoView partida={partida} codigoSala={codigoSala} userId={userId} />

      case 'SENALAMIENTO':
        return <SenalamientoView partida={partida} codigoSala={codigoSala} userId={userId} />

      case 'ADIVINANZA':
        return <AdivinanzaView partida={partida} codigoSala={codigoSala} userId={userId} />

      case 'REVELACION':
        return <RevelacionView partida={partida} codigoSala={codigoSala} userId={userId} />

      case 'FINALIZADA':
        return <FinalizadaView partida={partida} />

      default:
        return null
    }
  }

  return (
    <div className="page-top">
      <div className="card card-wide">
        {/* Header */}
        <div className="flex-between mb-2">
          <h1>Partida</h1>
          <button className="btn btn-secondary" onClick={() => navigate('/lobby')}>
            ← Lobby
          </button>
        </div>

        {isLoading && <p className="text-center mt-2">Cargando…</p>}
        {isError   && <div className="alert-error mt-2">Error al cargar la partida</div>}

        {partida && (
          <>
            {/* Estado + ronda */}
            <div className="text-center mt-1 mb-1">
              <span className="estado-badge">{partida.estado}</span>
              {partida.estado !== 'LOBBY' && partida.estado !== 'FINALIZADA' && (
                <span style={{ color: 'var(--c-text-dim)', fontSize: '0.875rem', marginLeft: '0.75rem' }}>
                  Ronda {Math.min(partida.rondaActual, partida.numRondas)}/{partida.numRondas}
                </span>
              )}
            </div>

            {/* Turno activo (solo en EN_CURSO) */}
            {partida.estado === 'EN_CURSO' && idJugadorTurnoActual && (
              <div className="text-center mb-2" style={{ fontSize: '0.8125rem', color: 'var(--c-text-muted)' }}>
                {idJugadorTurnoActual === userId
                  ? <strong style={{ color: 'var(--c-primary-light)' }}>Tu turno</strong>
                  : <>Turno de: <strong>{partida.jugadores.find((j) => j.id === idJugadorTurnoActual)?.nombre ?? '…'}</strong></>
                }
              </div>
            )}
            {(partida.estado !== 'EN_CURSO' || !idJugadorTurnoActual) && <div className="mb-2" />}

            {/* Carta toggle */}
            {!mostrarCarta ? (
              <button
                className="btn btn-secondary btn-block mb-2"
                style={{ fontSize: '0.875rem' }}
                onClick={() => setMostrarCarta(true)}
              >
                Ver mi carta
              </button>
            ) : (
              <CartaView codigoSala={codigoSala} onClose={() => setMostrarCarta(false)} />
            )}

            {/* Fase actual */}
            {renderFase()}
          </>
        )}
      </div>
    </div>
  )
}
