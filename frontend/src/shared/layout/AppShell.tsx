import { Outlet, Link, useNavigate } from 'react-router-dom'
import { useAuthStore }    from '@/store/authStore'
import { usePartidaStore } from '@/store/partidaStore'
import { APP_VERSION, AUTHOR_NAME, COPYRIGHT_YEAR } from '@/config'

export function AppShell() {
  const navigate = useNavigate()

  const token          = useAuthStore((s) => s.token)
  const nombre         = useAuthStore((s) => s.nombre)
  const esAdmin        = useAuthStore((s) => s.esAdmin)
  const limpiarSesion  = useAuthStore((s) => s.limpiarSesion)

  const codigoSala     = usePartidaStore((s) => s.codigoSala)
  const estadoActual   = usePartidaStore((s) => s.estadoActual)
  const esModerador    = usePartidaStore((s) => s.esModerador)
  const limpiarPartida = usePartidaStore((s) => s.limpiarPartida)

  function handleLogout() {
    limpiarPartida()
    limpiarSesion()
    navigate('/login', { replace: true })
  }

  return (
    <div className="shell-root">
      <header className="shell-header" role="banner">
        <div className="shell-header-inner">
          <Link
            to={token ? '/lobby' : '/login'}
            className="shell-brand"
            aria-label="El Infiltrado — inicio"
          >
            El Infiltrado
          </Link>

          {codigoSala && (
            <div className="shell-context" aria-label="Sala activa">
              <span className="shell-chip shell-chip-sala">{codigoSala}</span>
              {estadoActual && (
                <span
                  className={`shell-chip shell-chip-fase shell-chip-fase--${estadoActual.toLowerCase()}`}
                >
                  {estadoActual}
                </span>
              )}
            </div>
          )}

          {token && nombre && (
            <nav className="shell-user" aria-label="Usuario">
              <span className="shell-nombre">{nombre}</span>
              {esModerador && (
                <span className="shell-badge shell-badge-mod" aria-label="Moderador">
                  Mod
                </span>
              )}
              {esAdmin && (
                <Link
                  to="/admin/cosas"
                  className="shell-badge shell-badge-admin"
                  aria-label="Panel de administración"
                >
                  Admin
                </Link>
              )}
              <button
                className="shell-logout"
                onClick={handleLogout}
                aria-label="Cerrar sesión"
              >
                Salir
              </button>
            </nav>
          )}
        </div>
      </header>

      <main className="shell-main">
        <Outlet />
      </main>

      <footer className="shell-footer" role="contentinfo">
        <span>{AUTHOR_NAME}</span>
        <span className="shell-footer-sep" aria-hidden="true">·</span>
        <span>© {COPYRIGHT_YEAR}</span>
        <span className="shell-footer-sep" aria-hidden="true">·</span>
        <span>v{APP_VERSION}</span>
      </footer>
    </div>
  )
}
