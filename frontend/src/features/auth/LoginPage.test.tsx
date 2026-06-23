import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { LoginPage } from './LoginPage'
import * as authApi from '@/shared/api/authApi'

vi.mock('@/shared/api/authApi')

function renderLogin() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
  render(
    <QueryClientProvider client={qc}>
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('LoginPage', () => {
  beforeEach(() => {
    vi.mocked(authApi.login).mockResolvedValue({
      token: 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIwMDAwMDAwMC0wMDAwLTAwMDAtMDAwMC0wMDAwMDAwMDAwMDEiLCJub21icmUiOiJUZXN0In0.sig',
      nombre: 'Test',
      esAdmin: false,
    })
  })

  it('renderiza el formulario de login', () => {
    renderLogin()
    expect(screen.getByLabelText('Email')).toBeInTheDocument()
    expect(screen.getByLabelText('Contraseña')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Ingresar' })).toBeInTheDocument()
  })

  it('llama a la API con email y password al enviar el formulario', async () => {
    renderLogin()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Email'), 'ana@test.com')
    await user.type(screen.getByLabelText('Contraseña'), 'Test1234!')
    await user.click(screen.getByRole('button', { name: 'Ingresar' }))

    await waitFor(() =>
      expect(authApi.login).toHaveBeenCalledWith({
        email:    'ana@test.com',
        password: 'Test1234!',
      }),
    )
  })

  it('muestra mensaje de error cuando el login falla', async () => {
    vi.mocked(authApi.login).mockRejectedValue({
      isAxiosError: true,
      response: { data: { mensaje: 'Credenciales inválidas' } },
    })
    renderLogin()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Email'), 'wrong@test.com')
    await user.type(screen.getByLabelText('Contraseña'), 'wrongpass')
    await user.click(screen.getByRole('button', { name: 'Ingresar' }))

    await waitFor(() =>
      expect(screen.getByText('Credenciales inválidas')).toBeInTheDocument(),
    )
  })
})
