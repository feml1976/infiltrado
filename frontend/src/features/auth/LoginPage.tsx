import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import axios from 'axios'
import { useLogin } from './hooks/useLogin'

export function LoginPage() {
  const [email, setEmail]       = useState('')
  const [password, setPassword] = useState('')
  const mutation = useLogin()

  const errorMsg = mutation.error
    ? (axios.isAxiosError(mutation.error)
        ? (mutation.error.response?.data as { mensaje?: string })?.mensaje
        : undefined) ?? 'Error al iniciar sesión'
    : null

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    mutation.mutate({ email, password })
  }

  return (
    <div className="page">
      <div className="card">
        <h2>Iniciar sesión</h2>
        {errorMsg && <div className="alert-error">{errorMsg}</div>}
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="login-email">Email</label>
            <input
              id="login-email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoComplete="email"
            />
          </div>
          <div className="field">
            <label htmlFor="login-password">Contraseña</label>
            <input
              id="login-password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete="current-password"
            />
          </div>
          <button
            type="submit"
            className="btn btn-primary btn-block"
            disabled={mutation.isPending}
          >
            {mutation.isPending ? 'Ingresando…' : 'Ingresar'}
          </button>
        </form>
        <p className="text-center mt-2">
          ¿Sin cuenta?{' '}
          <Link to="/registro" className="link">Registrarse</Link>
        </p>
      </div>
    </div>
  )
}
