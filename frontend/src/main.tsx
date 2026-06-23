import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import App from './App.tsx'
import { setupInterceptors } from '@/shared/api/client'
import { useAuthStore }     from '@/store/authStore'
import { usePartidaStore }  from '@/store/partidaStore'
import '@/shared/ui/styles.css'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 30_000,
    },
  },
})

setupInterceptors(
  () => useAuthStore.getState().token,
  () => {
    useAuthStore.getState().limpiarSesion()
    usePartidaStore.getState().limpiarPartida()
    queryClient.clear()
    window.location.replace('/login')
  },
)

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <QueryClientProvider client={queryClient}>
        <App />
      </QueryClientProvider>
    </BrowserRouter>
  </StrictMode>,
)
