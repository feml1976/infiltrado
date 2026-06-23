import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getPlayerColor } from '@/shared/utils/playerColors'
import { useCountUp } from '@/shared/hooks/useCountUp'
import { useConfetti } from '@/shared/hooks/useConfetti'
import { useSoundToggle } from '@/shared/hooks/useSoundToggle'
import type { EstadoPartida, JugadorResumen } from '@/shared/api/types'

interface Props {
  partida: EstadoPartida
}

interface RankedPlayer extends JugadorResumen {
  position: number
}

function rankPlayers(jugadores: JugadorResumen[]): RankedPlayer[] {
  const sorted = [...jugadores].sort((a, b) => b.puntosAcumulados - a.puntosAcumulados)
  let pos = 1
  return sorted.map((j, i) => {
    if (i > 0 && j.puntosAcumulados < sorted[i - 1].puntosAcumulados) {
      pos = i + 1
    }
    return { ...j, position: pos }
  })
}

type Medal = { emoji: string; cls: 'oro' | 'plata' | 'bronce' }

function medalFor(pos: number): Medal | null {
  if (pos === 1) return { emoji: '🥇', cls: 'oro' }
  if (pos === 2) return { emoji: '🥈', cls: 'plata' }
  if (pos === 3) return { emoji: '🥉', cls: 'bronce' }
  return null
}

function PodioScore({ pts }: { pts: number }) {
  const val = useCountUp(pts)
  return <>{val}</>
}

export function FinalizadaView({ partida }: Props) {
  const navigate = useNavigate()
  const { soundOn, toggleSound, playFinale } = useSoundToggle()

  useConfetti(true)

  useEffect(() => {
    playFinale()
  }, [playFinale])

  const ranked = rankPlayers(partida.jugadores)

  return (
    <div className="fase-section">
      {/* Toggle de sonido */}
      <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 'var(--sp-2)' }}>
        <button
          className="sound-toggle"
          onClick={toggleSound}
          aria-label={soundOn ? 'Silenciar efectos de sonido' : 'Activar efectos de sonido'}
        >
          <span>{soundOn ? '🔊' : '🔇'}</span>
          <span>{soundOn ? 'Activo' : 'Sonido'}</span>
        </button>
      </div>

      <div className="turno-info">
        <div
          style={{
            color: '#818cf8',
            fontSize: '1.5rem',
            fontWeight: 800,
            marginBottom: '0.25rem',
            fontFamily: 'var(--font-heading)',
          }}
        >
          Partida finalizada
        </div>
        <div style={{ color: '#64748b', fontSize: '0.875rem' }}>
          {partida.numRondas} ronda{partida.numRondas !== 1 ? 's' : ''} jugada
          {partida.numRondas !== 1 ? 's' : ''}
        </div>
      </div>

      <h3 style={{ marginTop: '1.5rem' }}>Clasificación final</h3>
      <div className="podio">
        {ranked.map((j, i) => {
          const medal = medalFor(j.position)
          return (
            <div
              key={j.id}
              className={`podio-card${medal ? ` podio-card--${medal.cls}` : ''}`}
              style={{ animationDelay: `${i * 0.1}s` }}
            >
              {medal ? (
                <span className="podio-medalla">{medal.emoji}</span>
              ) : (
                <span className="podio-posicion">{j.position}.</span>
              )}
              <div
                className="player-badge"
                style={{ background: getPlayerColor(j.ordenTurno) }}
              >
                {j.ordenTurno}
              </div>
              <span className="player-name" style={{ flex: 1 }}>{j.nombre}</span>
              <span className={`podio-puntos${j.position === 1 ? ' podio-puntos--lider' : ''}`}>
                <PodioScore pts={j.puntosAcumulados} /> pts
              </span>
            </div>
          )
        })}
      </div>

      <button
        className="btn btn-primary btn-block mt-3"
        onClick={() => navigate('/lobby')}
      >
        Volver al lobby
      </button>
    </div>
  )
}
