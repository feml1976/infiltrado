import { create } from 'zustand'
import { extractSubFromJwt } from '@/shared/api/client'

const TOKEN_KEY   = 'infiltrado_token'
const NOMBRE_KEY  = 'infiltrado_nombre'
const ESADMIN_KEY = 'infiltrado_esadmin'

function initUserId(): string | null {
  const token = sessionStorage.getItem(TOKEN_KEY)
  return token ? extractSubFromJwt(token) : null
}

interface AuthState {
  token:   string | null
  userId:  string | null
  nombre:  string | null
  esAdmin: boolean
  setAuth: (token: string, nombre: string, esAdmin: boolean) => void
  limpiarSesion: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  token:   sessionStorage.getItem(TOKEN_KEY),
  userId:  initUserId(),
  nombre:  sessionStorage.getItem(NOMBRE_KEY),
  esAdmin: sessionStorage.getItem(ESADMIN_KEY) === 'true',

  setAuth: (token, nombre, esAdmin) => {
    const userId = extractSubFromJwt(token)
    sessionStorage.setItem(TOKEN_KEY,   token)
    sessionStorage.setItem(NOMBRE_KEY,  nombre)
    sessionStorage.setItem(ESADMIN_KEY, String(esAdmin))
    set({ token, userId, nombre, esAdmin })
  },

  limpiarSesion: () => {
    sessionStorage.removeItem(TOKEN_KEY)
    sessionStorage.removeItem(NOMBRE_KEY)
    sessionStorage.removeItem(ESADMIN_KEY)
    set({ token: null, userId: null, nombre: null, esAdmin: false })
  },
}))
