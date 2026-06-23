import { useState, useRef, type FormEvent, type ChangeEvent } from 'react'
import axios from 'axios'
import { extractApiError } from '@/shared/api/client'
import { validateAndEncodeImage } from '@/shared/utils/imageUtils'
import { useCrearCosa, useActualizarCosa } from './hooks/useCosas'
import type { CosaDetalle, TipoCosa } from '@/shared/api/types'

interface Props {
  cosa:    CosaDetalle | null
  onClose: () => void
}

export function CosaFormModal({ cosa, onClose }: Props) {
  const [nombre,     setNombre]     = useState(cosa?.nombre ?? '')
  const [tipo,       setTipo]       = useState<TipoCosa>(cosa?.tipo ?? 'PALABRA')
  const [imageB64,   setImageB64]   = useState<string | null>(cosa?.imagenBase64 ?? null)
  const [imageError, setImageError] = useState<string | null>(null)
  const fileRef = useRef<HTMLInputElement>(null)

  const crearMutation     = useCrearCosa()
  const actualizarMutation = useActualizarCosa()

  const isPending = crearMutation.isPending || actualizarMutation.isPending
  const mutError  = crearMutation.error ?? actualizarMutation.error

  const isDuplicate = mutError && axios.isAxiosError(mutError) && mutError.response?.status === 409
  const apiError    = mutError
    ? (isDuplicate ? 'Ya existe una cosa con ese nombre.' : extractApiError(mutError, 'Error al guardar'))
    : null
  const errorMsg = imageError ?? apiError

  async function handleFile(e: ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    setImageError(null)
    const result = await validateAndEncodeImage(file)
    if (result.error) {
      setImageError(result.error)
      if (fileRef.current) fileRef.current.value = ''
      return
    }
    setImageB64(result.base64)
  }

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (imageError) return

    const payload = {
      nombre:       nombre.trim().toLowerCase(),
      tipo,
      imagenBase64: tipo === 'IMAGEN' ? imageB64 : null,
    }

    if (cosa !== null) {
      actualizarMutation.mutate({ id: cosa.id, data: payload }, { onSuccess: onClose })
    } else {
      crearMutation.mutate(payload, { onSuccess: onClose })
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <div className="flex-between mb-2">
          <h2 style={{ marginBottom: 0 }}>{cosa !== null ? 'Editar cosa' : 'Nueva cosa'}</h2>
          <button
            type="button"
            className="btn btn-secondary"
            style={{ padding: '0.375rem 0.75rem' }}
            onClick={onClose}
          >
            ✕
          </button>
        </div>

        {errorMsg && <div className="alert-error">{errorMsg}</div>}

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="cosa-nombre">Nombre</label>
            <input
              id="cosa-nombre"
              type="text"
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              required
              placeholder="ej. manzana"
              style={{ textTransform: 'lowercase' }}
            />
          </div>

          <div className="field">
            <label htmlFor="cosa-tipo">Tipo</label>
            <select
              id="cosa-tipo"
              value={tipo}
              onChange={(e) => {
                setTipo(e.target.value as TipoCosa)
                setImageB64(null)
                setImageError(null)
              }}
              className="admin-select"
            >
              <option value="PALABRA">Palabra</option>
              <option value="IMAGEN">Imagen</option>
            </select>
          </div>

          {tipo === 'IMAGEN' && (
            <div className="field">
              <label htmlFor="cosa-imagen">Imagen (PNG/JPEG/WEBP, máx. 200 KB)</label>
              <input
                id="cosa-imagen"
                type="file"
                accept="image/png,image/jpeg,image/webp"
                ref={fileRef}
                onChange={handleFile}
                className="admin-file-input"
              />
              {imageB64 && (
                <img src={imageB64} alt="preview" className="admin-img-preview" />
              )}
            </div>
          )}

          <div className="flex-gap-2 mt-2">
            <button
              type="submit"
              className="btn btn-primary"
              style={{ flex: 1 }}
              disabled={isPending || (tipo === 'IMAGEN' && !imageB64)}
            >
              {isPending ? 'Guardando…' : 'Guardar'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              Cancelar
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
