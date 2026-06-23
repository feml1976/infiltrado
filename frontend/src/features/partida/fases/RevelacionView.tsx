import { useState, useEffect, useRef } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getRevelacion, continuarPartida, terminarPartida } from '@/shared/api/partidaApi'
import { extractApiError } from '@/shared/api/client'
import { getPlayerColor } from '@/shared/utils/playerColors'
import { useCountUp } from '@/shared/hooks/useCountUp'
import { useConfetti } from '@/shared/hooks/useConfetti'
import { useSoundToggle } from '@/shared/hooks/useSoundToggle'
import type { EstadoPartida } from '@/shared/api/types'

const prefersReducedMotion =
  typeof window !== 'undefined' &&
  window.matchMedia('(prefers-reduced-motion: reduce)').matches

interface Props {
  partida: EstadoPartida
  codigoSala: string
  userId: string
}

function RevealScore({ pts, delta }: { pts: number; delta: number }) {
  const preRound = pts - Math.max(0, delta)
  const [animTarget, setAnimTarget] = useState(delta > 0 ? preRound : pts)
  const [showFlash, setShowFlash] = useState(false)

  useEffect(() => {
    if (delta <= 0) return
    const t1 = window.setTimeout(() => { setAnimTarget(pts); setShowFlash(true) }, 150)
    const t2 = window.setTimeout(() => setShowFlash(false), 900)
    return () => { window.clearTimeout(t1); window.clearTimeout(t2) }
  }, [pts, delta])

  const display = useCountUp(animTarget)
  return <span className={showFlash ? 'score-row--flash' : ''}>{display}</span>
}

export function RevelacionView({ partida, codigoSala, userId }: Props) {
  const queryClient = useQueryClient()
  const esModerador = partida.idModerador === userId

  /* ── Datos ──────────────────────────────────────────────── */
  const { data: rev, isLoading, isError } = useQuery({
    queryKey: ['revelacion', codigoSala],
    queryFn: () => getRevelacion(codigoSala),
    staleTime: Infinity,
  })

  /* ── Sonido ─────────────────────────────────────────────── */
  const { soundOn, toggleSound, playReveal, playInfiltrado, playFinale } = useSoundToggle()

  /* ── Revelación escalonada ──────────────────────────────── */
  const [revealedCount, setRevealedCount] = useState(0)
  const [confettiActive, setConfettiActive] = useState(false)
  useConfetti(confettiActive)

  const revStartedRef = useRef(false)

  useEffect(() => {
    if (!rev || revStartedRef.current) return
    revStartedRef.current = true

    const sorted = [...rev.jugadores].sort((a, b) => a.ordenTurno - b.ordenTurno)

    if (prefersReducedMotion) {
      setRevealedCount(sorted.length)
      setConfettiActive(true)
      return
    }

    const timers: number[] = []
    sorted.forEach((j, i) => {
      timers.push(window.setTimeout(() => {
        setRevealedCount(i + 1)
        if (j.rol === 'INFILTRADO') playInfiltrado()
        else playReveal()
      }, i * 600 + 400))
    })
    timers.push(window.setTimeout(() => {
      setConfettiActive(true)
      playFinale()
    }, sorted.length * 600 + 800))

    return () => timers.forEach(window.clearTimeout)
  }, [rev, playReveal, playInfiltrado, playFinale])

  /* ── Mutations ──────────────────────────────────────────── */
  const continuarMutation = useMutation({
    mutationFn: () => continuarPartida(codigoSala),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['partida', codigoSala] }),
  })

  const terminarMutation = useMutation({
    mutationFn: () => terminarPartida(codigoSala),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['partida', codigoSala] }),
  })

  /* ── Early returns — después de todos los hooks ─────────── */
  if (isLoading) return <p className="text-center mt-2">Cargando revelación…</p>
  if (isError || !rev)
    return <div className="alert-error mt-2">No se pudo cargar la revelación</div>

  const sorted = [...rev.jugadores].sort((a, b) => a.ordenTurno - b.ordenTurno)
  const jugadoresPorId = Object.fromEntries(rev.jugadores.map((j) => [j.id, j]))
  const allRevealed = revealedCount >= sorted.length

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

      {/* Roles — revelación escalonada */}
      <h3 style={{ marginTop: '1.5rem' }}>Roles revelados</h3>
      <div className="player-list">
        {sorted.map((j, i) => {
          const revealed = i < revealedCount
          return (
            <div
              key={j.id}
              className={[
                'revelacion-jugador',
                revealed ? 'revelacion-jugador--visible' : '',
                revealed && j.rol === 'INFILTRADO' ? 'revelacion-jugador--infiltrado' : '',
              ].join(' ')}
            >
              <div className="player-badge" style={{ background: getPlayerColor(j.ordenTurno) }}>
                {j.ordenTurno}
              </div>
              <span className="player-name" style={{ flex: 1 }}>{j.nombre}</span>

              {revealed ? (
                <>
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
                    <span style={{ color: '#64748b', fontWeight: 400 }}>
                      {' = '}<RevealScore pts={j.puntosAcumulados} delta={j.deltaRonda} />
                    </span>
                  </span>
                </>
              ) : (
                <span className="revelacion-pending">···</span>
              )}
            </div>
          )
        })}
      </div>

      {/* Adivinanzas — visibles solo cuando todo esté revelado */}
      {allRevealed && rev.adivinanzas.length > 0 && (
        <>
          <h3 style={{ marginTop: '1.5rem' }}>Adivinanzas</h3>
          <div className="player-list">
            {rev.adivinanzas.map((a, i) => {
              const jug = jugadoresPorId[a.idJugadorInfiltrado]
              return (
                <div
                  key={i}
                  className="player-item action-confirmed"
                  style={{ flexDirection: 'column', alignItems: 'flex-start', gap: '0.25rem' }}
                >
                  <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', width: '100%' }}>
                    <span className="rol-infiltrado" style={{ fontSize: '0.8125rem' }}>
                      {jug?.nombre ?? a.idJugadorInfiltrado}
                    </span>
                    <span style={{ marginLeft: 'auto' }}>
                      {a.acierto
                        ? <span style={{ color: '#34d399' }}>Acertó ✓</span>
                        : <span style={{ color: '#f87171' }}>Falló ✗</span>
                      }
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

      {/* Señalamientos — visibles solo cuando todo esté revelado */}
      {allRevealed && rev.senalamientos.length > 0 && (
        <>
          <h3 style={{ marginTop: '1.5rem' }}>Señalamientos</h3>
          <div className="player-list">
            {rev.senalamientos.map((s, i) => {
              const origen = jugadoresPorId[s.idJugadorOrigen]
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

      {/* Acciones — visibles solo cuando todo esté revelado */}
      {allRevealed && esModerador && (
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
      {allRevealed && !esModerador && (
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
