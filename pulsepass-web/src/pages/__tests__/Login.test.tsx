import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { describe, expect, it } from 'vitest'
import { server } from '../../test/mocks/server'
import { renderConRouter } from '../../test/utils'

describe('Login', () => {
  it('login sin campos -> errores y 0 peticiones', async () => {
    let peticiones = 0
    const listener = ({ request }: { request: Request }) => {
      if (new URL(request.url).pathname.includes('/auth/login')) peticiones += 1
    }
    server.events.on('request:start', listener)
    try {
      const user = userEvent.setup()
      renderConRouter('/login')
      await screen.findByRole('heading', { name: /inicia sesión/i })
      await user.click(screen.getByRole('button', { name: /ingresar/i }))
      expect(await screen.findAllByRole('alert')).not.toHaveLength(0)
      expect(peticiones).toBe(0)
    } finally {
      server.events.removeListener('request:start', listener)
    }
  })

  it('login válido -> redirección a "desde"', async () => {
    const user = userEvent.setup()
    const { router } = renderConRouter('/mis-tickets')
    await screen.findByRole('heading', { name: /inicia sesión/i })
    await user.type(screen.getByLabelText(/usuario o correo/i), 'andrea')
    await user.type(screen.getByLabelText(/contraseña/i), 'passwordSegura123')
    await user.click(screen.getByRole('button', { name: /ingresar/i }))
    await waitFor(() => expect(router.state.location.pathname).toBe('/mis-tickets'))
  })

  it('login 401 -> "Credenciales inválidas"', async () => {
    server.use(
      http.post('*/api/auth/login', () =>
        HttpResponse.json(
          { status: 401, error: 'Unauthorized', message: 'Bad credentials', details: {} },
          { status: 401 },
        ),
      ),
    )
    const user = userEvent.setup()
    renderConRouter('/login')
    await screen.findByRole('heading', { name: /inicia sesión/i })
    await user.type(screen.getByLabelText(/usuario o correo/i), 'andrea')
    await user.type(screen.getByLabelText(/contraseña/i), 'passwordSegura123')
    await user.click(screen.getByRole('button', { name: /ingresar/i }))
    expect(await screen.findByRole('alert')).toHaveTextContent(/credenciales inválidas/i)
  })
})