import { afterEach, describe, expect, it, vi } from 'vitest'
import { z } from 'zod'
import { httpGet, httpPost } from './http'
import { limpiarSesion, registrarAlNoAutorizado } from '../funcionalidades/auth/sesion'

afterEach(() => {
  vi.unstubAllGlobals()
  limpiarSesion()
  sessionStorage.clear()
})

describe('cliente http', () => {
  it('añade Authorization Bearer cuando hay sesión', async () => {
    sessionStorage.setItem(
      'pulsepass.sesion',
      JSON.stringify({
        token: 'abc123',
        expiraEn: Date.now() + 900_000,
        usuario: { id: 1, username: 'u', email: 'u@test.co', roles: ['USER'] },
      }),
    )
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ ok: true }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    )
    vi.stubGlobal('fetch', fetchMock)
    await httpGet('/x', z.object({ ok: z.boolean() }))
    const init = fetchMock.mock.calls[0]?.[1] as RequestInit
    const cabeceras = init.headers as Headers
    expect(cabeceras.get('Authorization')).toBe('Bearer abc123')
  })

  it('ErrorResponse válido -> ApiError con status y details', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            status: 409,
            error: 'Conflict',
            message: 'Conflicto',
            details: { campo: 'x' },
          }),
          { status: 409, headers: { 'Content-Type': 'application/json' } },
        ),
      ),
    )
    await expect(
      httpPost('/x', z.object({}), {}, { skipUnauthorized: true }),
    ).rejects.toMatchObject({ status: 409, details: { campo: 'x' }, message: 'Conflicto' })
  })

  it('cuerpo de error inválido -> ApiError genérico', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(new Response('no-json', { status: 500 })),
    )
    await expect(
      httpPost('/x', z.object({}), {}, { skipUnauthorized: true }),
    ).rejects.toMatchObject({
      status: 500,
      message: 'Ocurrió un error inesperado. Inténtalo más tarde.',
    })
  })

  it('401 dispara cierre de sesión', async () => {
    sessionStorage.setItem(
      'pulsepass.sesion',
      JSON.stringify({
        token: 'abc',
        expiraEn: Date.now() + 900_000,
        usuario: { id: 1, username: 'u', email: 'u@test.co', roles: ['USER'] },
      }),
    )
    const alNoAutorizado = vi.fn()
    const desregistrar = registrarAlNoAutorizado(alNoAutorizado)
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({ status: 401, error: 'Unauthorized', message: 'No autorizado', details: {} }),
          { status: 401, headers: { 'Content-Type': 'application/json' } },
        ),
      ),
    )
    await expect(httpGet('/x', z.object({}))).rejects.toMatchObject({ status: 401 })
    expect(alNoAutorizado).toHaveBeenCalledTimes(1)
    expect(sessionStorage.getItem('pulsepass.sesion')).toBeNull()
    desregistrar()
  })

  it('falla si el contrato no cumple el esquema', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        new Response(JSON.stringify({ otro: 1 }), {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
        }),
      ),
    )
    await expect(httpGet('/x', z.object({ ok: z.boolean() }))).rejects.toMatchObject({
      status: 200,
    })
  })
})