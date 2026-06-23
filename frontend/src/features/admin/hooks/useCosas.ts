import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { listarCosas, getCosa, crearCosa, actualizarCosa, eliminarCosa } from '@/shared/api/catalogoApi'
import type { CrearCosaPayload, ActualizarCosaPayload } from '@/shared/api/types'

export function useCosas() {
  return useQuery({
    queryKey: ['cosas'],
    queryFn:  listarCosas,
  })
}

export function useCosaDetalle(id: string | null) {
  return useQuery({
    queryKey: ['cosa', id],
    queryFn:  () => getCosa(id!),
    enabled:  !!id,
  })
}

export function useCrearCosa() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: CrearCosaPayload) => crearCosa(data),
    onSuccess:  () => { qc.invalidateQueries({ queryKey: ['cosas'] }) },
  })
}

export function useActualizarCosa() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: ActualizarCosaPayload }) =>
      actualizarCosa(id, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['cosas'] }) },
  })
}

export function useEliminarCosa() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => eliminarCosa(id),
    onSuccess:  () => { qc.invalidateQueries({ queryKey: ['cosas'] }) },
  })
}
