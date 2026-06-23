import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { registro } from '@/shared/api/authApi'
import { useAuthStore } from '@/store/authStore'
import type { RegistroRequest } from '@/shared/api/types'

export function useRegistro() {
  const setAuth  = useAuthStore((s) => s.setAuth)
  const navigate = useNavigate()

  return useMutation({
    mutationFn: (data: RegistroRequest) => registro(data),
    onSuccess: ({ token, nombre, esAdmin }) => {
      setAuth(token, nombre, esAdmin)
      navigate('/lobby')
    },
  })
}
