import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { login } from '@/shared/api/authApi'
import { useAuthStore } from '@/store/authStore'
import type { LoginRequest } from '@/shared/api/types'

export function useLogin() {
  const setAuth  = useAuthStore((s) => s.setAuth)
  const navigate = useNavigate()

  return useMutation({
    mutationFn: (data: LoginRequest) => login(data),
    onSuccess: ({ token, nombre, esAdmin }) => {
      setAuth(token, nombre, esAdmin)
      navigate('/lobby')
    },
  })
}
