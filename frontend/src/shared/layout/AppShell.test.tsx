import { render, screen } from '@testing-library/react'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import { describe, it, expect } from 'vitest'
import { AppShell } from './AppShell'
import { AUTHOR_NAME, APP_VERSION } from '@/config'

function renderShell(path = '/login') {
  render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route element={<AppShell />}>
          <Route path="/login"  element={<div>login</div>} />
          <Route path="/lobby"  element={<div>lobby</div>} />
        </Route>
      </Routes>
    </MemoryRouter>,
  )
}

describe('AppShell', () => {
  it('renderiza el footer con autor y versión', () => {
    renderShell()
    const footer = screen.getByRole('contentinfo')
    expect(footer).toHaveTextContent(AUTHOR_NAME)
    expect(footer).toHaveTextContent(APP_VERSION)
  })

  it('renderiza la marca del encabezado', () => {
    renderShell()
    expect(screen.getByRole('banner')).toBeInTheDocument()
    expect(screen.getByLabelText('El Infiltrado — inicio')).toBeInTheDocument()
  })

  it('renderiza el contenido hijo via Outlet', () => {
    renderShell('/lobby')
    expect(screen.getByText('lobby')).toBeInTheDocument()
  })
})
