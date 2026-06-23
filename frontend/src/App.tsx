import { Routes, Route, Navigate } from 'react-router-dom'
import { AppShell }      from '@/shared/layout/AppShell'
import { LoginPage }     from '@/features/auth/LoginPage'
import { RegistroPage }  from '@/features/auth/RegistroPage'
import { LobbyPage }     from '@/features/lobby/LobbyPage'
import { PartidaPage }   from '@/features/partida/PartidaPage'
import { CosasPage }     from '@/features/admin/CosasPage'
import { ProtectedRoute } from '@/shared/ui/ProtectedRoute'
import { AdminRoute }    from '@/shared/ui/AdminRoute'

function App() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route path="/login"    element={<LoginPage />} />
        <Route path="/registro" element={<RegistroPage />} />
        <Route element={<ProtectedRoute />}>
          <Route path="/lobby"               element={<LobbyPage />} />
          <Route path="/partida/:codigoSala" element={<PartidaPage />} />
        </Route>
        <Route element={<AdminRoute />}>
          <Route path="/admin/cosas" element={<CosasPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/lobby" replace />} />
      </Route>
    </Routes>
  )
}

export default App
