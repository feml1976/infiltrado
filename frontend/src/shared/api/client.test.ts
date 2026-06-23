import { AxiosError } from 'axios'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setupInterceptors, apiClient } from './client'

describe('setupInterceptors — 401 handling', () => {
  let originalAdapter: typeof apiClient.defaults.adapter

  beforeEach(() => {
    originalAdapter = apiClient.defaults.adapter
    // Limpia interceptores previos para aislar el test
    apiClient.interceptors.request.clear()
    apiClient.interceptors.response.clear()
  })

  afterEach(() => {
    apiClient.defaults.adapter = originalAdapter
  })

  it('llama a onUnauthorized cuando la API responde con 401', async () => {
    const onUnauthorized = vi.fn()
    setupInterceptors(() => 'fake-token', onUnauthorized)

    // Sobreescribir el adapter para simular respuesta 401
    apiClient.defaults.adapter = async () => {
      throw new AxiosError(
        'Unauthorized',
        'ERR_BAD_RESPONSE',
        { headers: {}, method: 'get', url: '/test' } as any,
        undefined,
        {
          status:     401,
          data:       { mensaje: 'Token expirado' },
          statusText: 'Unauthorized',
          headers:    {},
          config:     { headers: {} } as any,
        },
      )
    }

    await expect(apiClient.get('/test')).rejects.toThrow()
    expect(onUnauthorized).toHaveBeenCalledOnce()
  })

  it('NO llama a onUnauthorized para errores que no son 401', async () => {
    const onUnauthorized = vi.fn()
    setupInterceptors(() => null, onUnauthorized)

    apiClient.defaults.adapter = async () => {
      throw new AxiosError(
        'Not Found',
        'ERR_BAD_RESPONSE',
        { headers: {}, method: 'get', url: '/test' } as any,
        undefined,
        {
          status:     404,
          data:       { mensaje: 'No encontrado' },
          statusText: 'Not Found',
          headers:    {},
          config:     { headers: {} } as any,
        },
      )
    }

    await expect(apiClient.get('/test')).rejects.toThrow()
    expect(onUnauthorized).not.toHaveBeenCalled()
  })

  it('adjunta el token Bearer en las peticiones salientes', async () => {
    let capturedAuth: string | undefined
    setupInterceptors(() => 'my-jwt', vi.fn())

    apiClient.defaults.adapter = async (config) => {
      capturedAuth = config.headers?.['Authorization'] as string
      return { status: 200, data: {}, headers: {}, config, statusText: 'OK' }
    }

    await apiClient.get('/test')
    expect(capturedAuth).toBe('Bearer my-jwt')
  })
})
