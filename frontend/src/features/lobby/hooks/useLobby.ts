import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import {
  crearPartida,
  unirseAPartida,
  getEstadoPartida,
} from '@/shared/api/partidaApi'
import { usePartidaStore } from '@/store/partidaStore'
import type { CrearPartidaRequest } from '@/shared/api/types'

export function useLobby() {
  const { codigoSala, setCodigoSala } = usePartidaStore()
  const navigate     = useNavigate()
  const queryClient  = useQueryClient()

  const estadoQuery = useQuery({
    queryKey:       ['partida', codigoSala],
    queryFn:        () => getEstadoPartida(codigoSala!),
    enabled:        !!codigoSala,
    refetchInterval: 3_000,
  })

  const crearMutation = useMutation({
    mutationFn: (data: CrearPartidaRequest) => crearPartida(data),
    onSuccess: ({ codigoSala: codigo }) => {
      setCodigoSala(codigo)
      queryClient.invalidateQueries({ queryKey: ['partida', codigo] })
    },
  })

  const unirMutation = useMutation({
    mutationFn: (codigo: string) => unirseAPartida(codigo.toUpperCase()),
    onSuccess: (estado) => {
      setCodigoSala(estado.codigoSala)
    },
  })

  function irAPartida() {
    if (codigoSala) navigate(`/partida/${codigoSala}`)
  }

  function salirDeSala() {
    setCodigoSala(null)
    queryClient.removeQueries({ queryKey: ['partida'] })
  }

  return { codigoSala, estadoQuery, crearMutation, unirMutation, irAPartida, salirDeSala }
}
