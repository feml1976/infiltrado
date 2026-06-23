import { useNavigate } from 'react-router-dom'
import type { EstadoPartida } from '@/shared/api/types'

interface Props {
  partida: EstadoPartida
}

export function FinalizadaView({ partida }: Props) {
  const navigate = useNavigate()

  const jugadoresOrdenados = partida.jugadores
    .slice()
    .sort((a, b) => b.puntosAcumulados - a.puntosAcumulados)

  const maxPuntos = jugadoresOrdenados[0]?.puntosAcumulados ?? 0

  return (
    <div className="fase-section">
      <div className="turno-info">
        <div style={{ color: '#818cf8', fontSize: '1.5rem', fontWeight: 800, marginBottom: '0.25rem' }}>
          Partida finalizada
        </div>
        <div style={{ color: '#64748b', fontSize: '0.875rem' }}>
          {partida.numRondas} ronda{partida.numRondas !== 1 ? 's' : ''} jugada{partida.numRondas !== 1 ? 's' : ''}
        </div>
      </div>

      <h3 style={{ marginTop: '1.5rem' }}>Clasificación final</h3>
      <div className="player-list">
        {jugadoresOrdenados.map((j, idx) => (
          <div key={j.id} className="revelacion-jugador">
            <div
              className="player-badge"
              style={{
                background: idx === 0 ? '#f59e0b' : idx === 1 ? '#94a3b8' : idx === 2 ? '#b45309' : '#334155',
              }}
            >
              {idx + 1}
            </div>
            <span className="player-name" style={{ flex: 1 }}>{j.nombre}</span>
            <span
              style={{
                fontWeight: 700,
                color: j.puntosAcumulados === maxPuntos ? '#f59e0b' : '#818cf8',
              }}
            >
              {j.puntosAcumulados} pts
            </span>
          </div>
        ))}
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
