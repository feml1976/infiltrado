import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getRevelacion, continuarPartida, terminarPartida } from '@/shared/api/partidaApi'
import { extractApiError } from '@/shared/api/client'
import type { EstadoPartida } from '@/shared/api/types'

interface Props {
  partida: EstadoPartida
  codigoSala: string
  userId: string
}

export function RevelacionView({ partida, codigoSala, userId }: Props) {
  const queryClient = useQueryClient()
  const esModerador = partida.idModerador === userId

  const { data: rev, isLoading, isError } = useQuery({
    queryKey: ['revelacion', codigoSala],
    queryFn:  () => getRevelacion(codigoSala),
    staleTime: Infinity,
  })

  const continuarMutation = useMutation({
    mutationFn: () => continuarPartida(codigoSala),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['partida', codigoSala] }),
  })

  const terminarMutation = useMutation({
    mutationFn: () => terminarPartida(codigoSala),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['partida', codigoSala] }),
  })

  if (isLoading) return <p className="text-center mt-2">Cargando revelación…</p>
  if (isError || !rev)
    return <div className="alert-error mt-2">No se pudo cargar la revelación</div>

  const jugadoresPorId = Object.fromEntries(rev.jugadores.map((j) => [j.id, j]))

  return (
    <div className="fase-section">
      {/* Cosa objetivo */}
      <div className="turno-info">
        <div style={{ color: '#64748b', fontSize: '0.8125rem', marginBottom: '0.25rem' }}>
          La cosa objetivo era
        </div>
        <div className="cosa-titulo">{rev.nombreCosa}</div>
        {rev.tipoCosa && (
          <div style={{ color: '#64748b', fontSize: '0.875rem' }}>{rev.tipoCosa}</div>
        )}
      </div>

      {/* Jugadores con roles */}
      <h3 style={{ marginTop: '1.5rem' }}>Roles revelados</h3>
      <div className="player-list">
        {rev.jugadores
          .slice()
          .sort((a, b) => a.ordenTurno - b.ordenTurno)
          .map((j) => (
            <div key={j.id} className="revelacion-jugador">
              <div className="player-badge">{j.ordenTurno}</div>
              <span className="player-name" style={{ flex: 1 }}>{j.nombre}</span>
              <span className={j.rol === 'INFILTRADO' ? 'rol-infiltrado' : 'rol-normal'}>
                {j.rol}
              </span>
              <span
                className={
                  j.deltaRonda > 0
                    ? 'delta-positivo'
                    : j.deltaRonda < 0
                    ? 'delta-negativo'
                    : 'delta-neutral'
                }
                style={{ minWidth: '60px', textAlign: 'right' }}
              >
                {j.deltaRonda > 0 ? `+${j.deltaRonda}` : j.deltaRonda}
                <span style={{ color: '#64748b', fontWeight: 400 }}> = {j.puntosAcumulados}</span>
              </span>
            </div>
          ))}
      </div>

      {/* Adivinanzas */}
      {rev.adivinanzas.length > 0 && (
        <>
          <h3 style={{ marginTop: '1.5rem' }}>Adivinanzas</h3>
          <div className="player-list">
            {rev.adivinanzas.map((a, i) => {
              const jug = jugadoresPorId[a.idJugadorInfiltrado]
              return (
                <div key={i} className="player-item" style={{ flexDirection: 'column', alignItems: 'flex-start', gap: '0.25rem' }}>
                  <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', width: '100%' }}>
                    <span className="rol-infiltrado" style={{ fontSize: '0.8125rem' }}>
                      {jug?.nombre ?? a.idJugadorInfiltrado}
                    </span>
                    <span style={{ marginLeft: 'auto' }}>
                      {a.acierto ? (
                        <span style={{ color: '#34d399' }}>Acertó ✓</span>
                      ) : (
                        <span style={{ color: '#f87171' }}>Falló ✗</span>
                      )}
                    </span>
                  </div>
                  <div style={{ color: '#94a3b8', fontSize: '0.875rem' }}>
                    "{a.textoAdivinanza}"
                  </div>
                </div>
              )
            })}
          </div>
        </>
      )}

      {/* Señalamientos */}
      {rev.senalamientos.length > 0 && (
        <>
          <h3 style={{ marginTop: '1.5rem' }}>Señalamientos</h3>
          <div className="player-list">
            {rev.senalamientos.map((s, i) => {
              const origen   = jugadoresPorId[s.idJugadorOrigen]
              const señalado = jugadoresPorId[s.idJugadorSenalado]
              return (
                <div key={i} className="player-item" style={{ gap: '0.5rem' }}>
                  <span style={{ color: '#94a3b8' }}>{origen?.nombre ?? '?'}</span>
                  <span style={{ color: '#64748b' }}>→</span>
                  <span style={{ color: '#e2e8f0' }}>{señalado?.nombre ?? '?'}</span>
                </div>
              )
            })}
          </div>
        </>
      )}

      {/* Acciones del moderador */}
      {esModerador && (
        <div className="flex-gap-2 mt-3">
          <button
            className="btn btn-primary"
            style={{ flex: 1 }}
            disabled={continuarMutation.isPending || terminarMutation.isPending}
            onClick={() => continuarMutation.mutate()}
          >
            {continuarMutation.isPending ? 'Continuando…' : 'Continuar (otra ronda)'}
          </button>
          <button
            className="btn btn-secondary"
            disabled={continuarMutation.isPending || terminarMutation.isPending}
            onClick={() => terminarMutation.mutate()}
          >
            {terminarMutation.isPending ? 'Terminando…' : 'Terminar partida'}
          </button>
        </div>
      )}
      {!esModerador && (
        <p className="text-center mt-2" style={{ fontSize: '0.875rem', color: '#64748b' }}>
          Esperando que el moderador decida continuar o terminar
        </p>
      )}

      {(continuarMutation.error || terminarMutation.error) && (
        <div className="alert-error mt-2">
          {extractApiError(
            continuarMutation.error ?? terminarMutation.error,
            'No se pudo realizar la acción',
          )}
        </div>
      )}
    </div>
  )
}
