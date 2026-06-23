import { useState, type FormEvent } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { usePartidaStore } from '@/store/partidaStore'
import { registrarAdivinanza } from '@/shared/api/partidaApi'
import { extractApiError } from '@/shared/api/client'
import type { EstadoPartida } from '@/shared/api/types'

interface Props {
  partida: EstadoPartida
  codigoSala: string
  userId: string
}

export function AdivinanzaView({ partida, codigoSala, userId: _userId }: Props) {
  const queryClient    = useQueryClient()
  const revelacionWs   = usePartidaStore((s) => s.revelacionWs)

  const [texto, setTexto]           = useState('')
  const [declarada, setDeclarada]   = useState(false)

  // Total infiltrados: from partida numInfiltrados
  const totalInfiltrados = partida.numInfiltrados

  // If we received a revelacion WS event we already know the adivinanza count
  const adivinanzasWs = revelacionWs?.adivinanzas.length ?? 0
  const pendientesWs  = Math.max(0, totalInfiltrados - adivinanzasWs)

  const mutation = useMutation({
    mutationFn: () => registrarAdivinanza(codigoSala, texto.trim()),
    onSuccess: () => {
      setDeclarada(true)
      setTexto('')
      queryClient.invalidateQueries({ queryKey: ['partida', codigoSala] })
    },
  })

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (texto.trim()) mutation.mutate()
  }

  return (
    <div className="fase-section">
      <div className="turno-info">
        <div style={{ color: '#64748b', fontSize: '0.8125rem', marginBottom: '0.25rem' }}>
          Fase de adivinanza
        </div>
        <div className="turno-nombre">
          {pendientesWs > 0
            ? `${totalInfiltrados - pendientesWs}/${totalInfiltrados} infiltrados han declarado`
            : 'Todos los infiltrados han declarado'}
        </div>
      </div>

      {declarada ? (
        <div style={{ textAlign: 'center', color: '#34d399', padding: '1rem 0' }}>
          Adivinanza registrada — esperando la revelación
        </div>
      ) : (
        <form onSubmit={handleSubmit} style={{ marginTop: '1rem' }}>
          <p style={{ color: '#94a3b8', marginBottom: '1rem', fontSize: '0.9375rem' }}>
            Si eres infiltrado, declara tu adivinanza sobre la cosa objetivo.
            Los jugadores normales pueden esperar aquí.
          </p>
          {mutation.error && (
            <div className="alert-error">
              {extractApiError(mutation.error, 'No se pudo registrar la adivinanza')}
            </div>
          )}
          <div className="field">
            <label htmlFor="adiv-texto">Tu adivinanza</label>
            <input
              id="adiv-texto"
              type="text"
              value={texto}
              onChange={(e) => setTexto(e.target.value)}
              maxLength={500}
              placeholder="¿Cuál crees que es la cosa objetivo?"
            />
          </div>
          <button
            type="submit"
            className="btn btn-primary btn-block"
            disabled={mutation.isPending || !texto.trim()}
          >
            {mutation.isPending ? 'Declarando…' : 'Declarar adivinanza'}
          </button>
        </form>
      )}
    </div>
  )
}
