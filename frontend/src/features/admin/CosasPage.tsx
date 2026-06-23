import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useCosas, useCosaDetalle, useEliminarCosa } from './hooks/useCosas'
import { CosaFormModal } from './CosaFormModal'
import type { CosaResumen } from '@/shared/api/types'

const PAGE_SIZE = 10

export function CosasPage() {
  const [showInactive, setShowInactive] = useState(false)
  const [page,         setPage]         = useState(1)
  const [showCreate,   setShowCreate]   = useState(false)
  const [editId,       setEditId]       = useState<string | null>(null)

  const { data: cosas = [], isLoading, isError } = useCosas()
  const { data: editDetail, isLoading: loadingDetail } = useCosaDetalle(editId)
  const eliminarMutation = useEliminarCosa()

  const filtered   = showInactive ? cosas : cosas.filter((c) => c.activo)
  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE))
  const pageItems  = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE)

  function handleEliminar(c: CosaResumen) {
    if (!window.confirm(`¿Eliminar "${c.nombre}"?`)) return
    eliminarMutation.mutate(c.id)
  }

  function handleCloseModal() {
    setShowCreate(false)
    setEditId(null)
  }

  const showEditModal = editId !== null && !loadingDetail && editDetail !== undefined

  return (
    <div className="page-top">
      <div className="card card-wide" style={{ maxWidth: '780px' }}>
        <div className="flex-between mb-2">
          <h1>Banco de cosas</h1>
          <Link to="/lobby" className="btn btn-secondary" style={{ fontSize: '0.875rem' }}>
            ← Lobby
          </Link>
        </div>

        <div className="admin-toolbar">
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => setShowCreate(true)}
          >
            + Nueva cosa
          </button>
          <label className="admin-toggle">
            <input
              type="checkbox"
              checked={showInactive}
              onChange={(e) => { setShowInactive(e.target.checked); setPage(1) }}
            />
            Mostrar inactivas
          </label>
        </div>

        {isLoading && <p className="text-center mt-2">Cargando…</p>}
        {isError   && <div className="alert-error mt-2">Error al cargar el banco de cosas</div>}

        {!isLoading && !isError && (
          <>
            <div className="admin-table-wrapper">
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>Nombre</th>
                    <th>Tipo</th>
                    <th>Estado</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {pageItems.length === 0 && (
                    <tr>
                      <td colSpan={4} style={{ textAlign: 'center', color: '#64748b', padding: '1.5rem' }}>
                        Sin cosas{showInactive ? '' : ' activas'}
                      </td>
                    </tr>
                  )}
                  {pageItems.map((c) => (
                    <tr key={c.id} className={c.activo ? '' : 'admin-row-inactive'}>
                      <td>{c.nombre}</td>
                      <td>{c.tipo === 'PALABRA' ? 'Palabra' : 'Imagen'}</td>
                      <td>
                        <span className={c.activo ? 'admin-badge-active' : 'admin-badge-inactive'}>
                          {c.activo ? 'Activa' : 'Inactiva'}
                        </span>
                      </td>
                      <td className="admin-actions">
                        <button
                          type="button"
                          className="btn btn-secondary"
                          style={{ fontSize: '0.8125rem', padding: '0.3rem 0.7rem' }}
                          onClick={() => setEditId(c.id)}
                          disabled={loadingDetail && editId === c.id}
                        >
                          {loadingDetail && editId === c.id ? '…' : 'Editar'}
                        </button>
                        {c.activo && (
                          <button
                            type="button"
                            className="btn admin-btn-danger"
                            onClick={() => handleEliminar(c)}
                            disabled={eliminarMutation.isPending}
                          >
                            Eliminar
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {totalPages > 1 && (
              <div className="admin-pagination">
                <button
                  type="button"
                  className="btn btn-secondary"
                  style={{ padding: '0.4rem 0.8rem', fontSize: '0.875rem' }}
                  disabled={page === 1}
                  onClick={() => setPage((p) => p - 1)}
                >
                  ← Anterior
                </button>
                <span style={{ color: '#94a3b8', fontSize: '0.875rem' }}>
                  Página {page} de {totalPages}
                </span>
                <button
                  type="button"
                  className="btn btn-secondary"
                  style={{ padding: '0.4rem 0.8rem', fontSize: '0.875rem' }}
                  disabled={page === totalPages}
                  onClick={() => setPage((p) => p + 1)}
                >
                  Siguiente →
                </button>
              </div>
            )}
          </>
        )}
      </div>

      {showCreate    && <CosaFormModal cosa={null}       onClose={handleCloseModal} />}
      {showEditModal && <CosaFormModal cosa={editDetail} onClose={handleCloseModal} />}
    </div>
  )
}
