import { useState, type FormEvent } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { registrarSenalamiento } from '@/shared/api/partidaApi'
import { extractApiError } from '@/shared/api/client'
import type { EstadoPartida } from '@/shared/api/types'

interface Props {
  partida: EstadoPartida
  codigoSala: string
  userId: string
}

export function SenalamientoView({ partida, codigoSala, userId }: Props) {
  const queryClient = useQueryClient()
  const miJugador   = partida.jugadores.find((j) => j.id === userId)
  const yaSeñale    = miJugador?.haSenalado ?? false

  const [seleccion, setSeleccion] = useState<string[]>([])

  const totalSeñalados = partida.jugadores.filter((j) => j.haSenalado).length
  const pendientes     = partida.numJugadores - totalSeñalados

  const mutation = useMutation({
    mutationFn: () => registrarSenalamiento(codigoSala, seleccion),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['partida', codigoSala] }),
  })

  function toggle(id: string) {
    setSeleccion((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id],
    )
  }

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    mutation.mutate()
  }

  return (
    <div className="fase-section">
      <div className="progress-info">
        {pendientes > 0
          ? `${totalSeñalados}/${partida.numJugadores} jugadores han señalado`
          : 'Todos han señalado — procesando resultados…'}
      </div>

      {yaSeñale ? (
        <div style={{ textAlign: 'center', color: '#34d399', padding: '1.5rem 0' }}>
          Ya señalaste — esperando a los demás
        </div>
      ) : (
        <form onSubmit={handleSubmit}>
          <h3>¿Quiénes son los infiltrados?</h3>
          {mutation.error && (
            <div className="alert-error">
              {extractApiError(mutation.error, 'No se pudo registrar el señalamiento')}
            </div>
          )}
          <div className="player-list" style={{ marginBottom: '1rem' }}>
            {partida.jugadores
              .filter((j) => j.id !== userId)
              .slice()
              .sort((a, b) => a.ordenTurno - b.ordenTurno)
              .map((j) => (
                <label
                  key={j.id}
                  className="player-item"
                  style={{ cursor: 'pointer' }}
                >
                  <input
                    type="checkbox"
                    checked={seleccion.includes(j.id)}
                    onChange={() => toggle(j.id)}
                    style={{ accentColor: '#6366f1', width: '18px', height: '18px' }}
                  />
                  <div className="player-badge">{j.ordenTurno}</div>
                  <span className="player-name">{j.nombre}</span>
                </label>
              ))}
          </div>
          <button
            type="submit"
            className="btn btn-primary btn-block"
            disabled={mutation.isPending}
          >
            {mutation.isPending ? 'Registrando…' : 'Confirmar señalamiento'}
          </button>
          <p style={{ fontSize: '0.8125rem', color: '#64748b', textAlign: 'center', marginTop: '0.5rem' }}>
            Puedes dejar la selección vacía para abstenerte
          </p>
        </form>
      )}

      <div style={{ marginTop: '1.5rem' }}>
        <h3>Estado del señalamiento</h3>
        <div className="player-list">
          {partida.jugadores
            .slice()
            .sort((a, b) => a.ordenTurno - b.ordenTurno)
            .map((j) => (
              <div key={j.id} className="player-item">
                <div className="player-badge">{j.ordenTurno}</div>
                <span className="player-name">{j.nombre}</span>
                <span style={{ marginLeft: 'auto', fontSize: '0.875rem' }}>
                  {j.haSenalado ? (
                    <span style={{ color: '#34d399' }}>✓ Señaló</span>
                  ) : (
                    <span style={{ color: '#64748b' }}>Pendiente</span>
                  )}
                </span>
              </div>
            ))}
        </div>
      </div>
    </div>
  )
}
