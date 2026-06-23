import { render } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { describe, it, expect, vi } from 'vitest'
import { CartaView } from './CartaView'
import * as useMiCartaHook from './hooks/useMiCarta'

vi.mock('./hooks/useMiCarta')

function renderCarta(qc: QueryClient, codigoSala = 'SALA01') {
  return render(
    <QueryClientProvider client={qc}>
      <CartaView codigoSala={codigoSala} onClose={vi.fn()} />
    </QueryClientProvider>,
  )
}

describe('CartaView', () => {
  it('elimina la query de carta al desmontar (protección dispositivo compartido)', () => {
    vi.mocked(useMiCartaHook.useMiCarta).mockReturnValue({
      data:      undefined,
      isLoading: true,
      isError:   false,
    } as any)

    const qc          = new QueryClient()
    const removeQueries = vi.spyOn(qc, 'removeQueries')

    const { unmount } = renderCarta(qc, 'ABCD12')
    unmount()

    expect(removeQueries).toHaveBeenCalledWith({ queryKey: ['carta', 'ABCD12'] })
  })

  it('muestra el rol INFILTRADO cuando la carta lo indica', () => {
    vi.mocked(useMiCartaHook.useMiCarta).mockReturnValue({
      data: {
        rol:          'INFILTRADO',
        idCosa:       null,
        nombreCosa:   null,
        tipo:         null,
        imagenBase64: null,
      },
      isLoading: false,
      isError:   false,
    } as any)

    const qc = new QueryClient()
    const { unmount, getByText } = renderCarta(qc)
    expect(getByText('INFILTRADO')).toBeInTheDocument()
    unmount()
  })

  it('muestra el nombre de la cosa para rol NORMAL', () => {
    vi.mocked(useMiCartaHook.useMiCarta).mockReturnValue({
      data: {
        rol:          'NORMAL',
        idCosa:       '1',
        nombreCosa:   'manzana',
        tipo:         'PALABRA',
        imagenBase64: null,
      },
      isLoading: false,
      isError:   false,
    } as any)

    const qc = new QueryClient()
    const { unmount, getByText } = renderCarta(qc)
    expect(getByText('manzana')).toBeInTheDocument()
    unmount()
  })
})
