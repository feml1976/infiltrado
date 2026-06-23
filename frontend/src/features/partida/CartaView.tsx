import { useEffect } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { useMiCarta } from './hooks/useMiCarta'

interface CartaViewProps {
  codigoSala: string
  onClose: () => void
}

export function CartaView({ codigoSala, onClose }: CartaViewProps) {
  const queryClient = useQueryClient()
  const { data: carta, isLoading, isError } = useMiCarta(codigoSala, true)

  useEffect(() => {
    return () => {
      // Limpiar carta de la caché al desmontar — protección en dispositivos compartidos
      queryClient.removeQueries({ queryKey: ['carta', codigoSala] })
    }
  }, [codigoSala, queryClient])

  if (isLoading) {
    return (
      <div className="carta-wrapper">
        <p>Consultando tu carta…</p>
        <button className="btn btn-secondary mt-3" onClick={onClose}>
          Cancelar
        </button>
      </div>
    )
  }

  if (isError || !carta) {
    return (
      <div className="carta-wrapper">
        <div className="alert-error">
          No se pudo consultar tu carta. Inténtalo de nuevo.
        </div>
        <button className="btn btn-secondary mt-2" onClick={onClose}>
          Cerrar
        </button>
      </div>
    )
  }

  const esInfiltrado = carta.rol === 'INFILTRADO'
  const rolLabel     = esInfiltrado ? 'INFILTRADO' : 'JUGADOR'
  const rolClass     = esInfiltrado ? 'carta-rol-infiltrado' : 'carta-rol-normal'

  return (
    <div className="carta-wrapper">
      <div className={`carta-rol ${rolClass}`}>{rolLabel}</div>

      {!esInfiltrado && carta.nombreCosa && (
        carta.tipo === 'IMAGEN' && carta.imagenBase64 ? (
          <>
            <img
              className="carta-imagen"
              src={`data:image/jpeg;base64,${carta.imagenBase64}`}
              alt={carta.nombreCosa}
            />
            {/* Nombre visible bajo la imagen, para evitar confusiones entre jugadores */}
            <div className="carta-cosa">{carta.nombreCosa}</div>
          </>
        ) : (
          <div className="carta-cosa">{carta.nombreCosa}</div>
        )
      )}

      {esInfiltrado && (
        <p className="carta-hint">
          Debes deducir la cosa objetivo a partir de las pistas de los demás jugadores.
        </p>
      )}

      {carta.rol === null && (
        <p className="carta-hint">La partida aún no ha comenzado.</p>
      )}

      <button className="btn btn-secondary mt-3" onClick={onClose}>
        Cerrar carta
      </button>
    </div>
  )
}
