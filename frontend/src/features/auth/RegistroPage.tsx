import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import axios from 'axios'
import { useRegistro } from './hooks/useRegistro'

export function RegistroPage() {
  const [nombre, setNombre]     = useState('')
  const [email, setEmail]       = useState('')
  const [password, setPassword] = useState('')
  const mutation = useRegistro()

  const errorMsg = mutation.error
    ? (axios.isAxiosError(mutation.error)
        ? (mutation.error.response?.data as { mensaje?: string })?.mensaje
        : undefined) ?? 'Error al registrarse'
    : null

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    mutation.mutate({ nombre, email, password })
  }

  return (
    <div className="page">
      <div className="card">
        <h2>Crear cuenta</h2>
        {errorMsg && <div className="alert-error">{errorMsg}</div>}
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="reg-nombre">Nombre</label>
            <input
              id="reg-nombre"
              type="text"
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              required
              autoComplete="name"
            />
          </div>
          <div className="field">
            <label htmlFor="reg-email">Email</label>
            <input
              id="reg-email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoComplete="email"
            />
          </div>
          <div className="field">
            <label htmlFor="reg-password">Contraseña</label>
            <input
              id="reg-password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete="new-password"
              minLength={8}
            />
          </div>
          <button
            type="submit"
            className="btn btn-primary btn-block"
            disabled={mutation.isPending}
          >
            {mutation.isPending ? 'Creando cuenta…' : 'Registrarse'}
          </button>
        </form>
        <p className="text-center mt-2">
          ¿Ya tienes cuenta?{' '}
          <Link to="/login" className="link">Iniciar sesión</Link>
        </p>
      </div>
    </div>
  )
}
