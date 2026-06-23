import { useQuery } from '@tanstack/react-query'
import { getEstadoPartida } from '@/shared/api/partidaApi'

export function usePartida(codigoSala: string) {
  return useQuery({
    queryKey:        ['partida', codigoSala],
    queryFn:         () => getEstadoPartida(codigoSala),
    refetchInterval: 5_000,
  })
}
