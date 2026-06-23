import { useQuery } from '@tanstack/react-query'
import { getMiCarta } from '@/shared/api/partidaApi'

export function useMiCarta(codigoSala: string, enabled: boolean) {
  return useQuery({
    queryKey:            ['carta', codigoSala],
    queryFn:             () => getMiCarta(codigoSala),
    enabled,
    gcTime:              0,
    staleTime:           0,
    refetchOnWindowFocus: false,
    retry:               false,
  })
}
