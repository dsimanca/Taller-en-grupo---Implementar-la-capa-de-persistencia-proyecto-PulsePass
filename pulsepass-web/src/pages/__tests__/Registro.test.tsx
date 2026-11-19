import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { describe, expect, it } from 'vitest'
import { server } from '../../test/mocks/server'
import { renderConRouter } from '../../test/utils'

async function completarFormulario() {
  const user = userEvent.setup()
  await screen.findByRole('heading', { name: /crea tu cuenta/i })
  await user.type(screen.getByLabelText(/nombre de usuario/i), 'andrea')
  await user.type(screen.getByLabelText(/correo electrónico/i), 'andrea@pulsepass.test')
  await user.type(screen.getByLabelText(/nombres/i), 'Andrea')
  await user.type(screen.getByLabelText(/apellidos/i), 'Ruiz')
  await user.type(screen.getByLabelText(/teléfono/i), '3001234567')
  await user.type(screen.getByLabelText(/ciudad/i), 'Bogotá')
  await user.type(screen.getByLabelText(/fecha de nacimiento/i), '1998-04-12')
  return user
}

describe('Registro', () => {
  it('contraseña corta -> error y 0 peticiones', async () => {
    let peticiones = 0
    const listener = ({ request }: { request: Request }) => {
      if (new URL(request.url).pathname.includes('/api/users')) peticiones += 1
    }
    server.events.on('request:start', listener)
    try {
      const user = userEvent.setup()
      renderConRouter('/registro')
      await screen.findByRole('heading', { name: /crea tu cuenta/i })
      await user.type(screen.getByLabelText(/contraseña/i), 'corta')
      await user.click(screen.getByRole('button', { name: /crear cuenta/i }))
      expect(await screen.findAllByRole('alert')).not.toHaveLength(0)
      expect(peticiones).toBe(0)
    } finally {
      server.events.removeListener('request:start', listener)
    }
  })

  it('registro 201 -> redirección a /login', async () => {
    renderConRouter('/registro')
    const user = await completarFormulario()
    await user.type(screen.getByLabelText(/contraseña/i), 'passwordSegura123')
    await user.click(screen.getByRole('button', { name: /crear cuenta/i }))
    expect(await screen.findByRole('heading', { name: /inicia sesión/i })).toBeInTheDocument()
  })

  it('registro 409 -> mensaje genérico', async () => {
    server.use(
      http.post('*/api/users', () =>
        HttpResponse.json(
          { status: 409, error: 'Conflict', message: 'Duplicado', details: {} },
          { status: 409 },
        ),
      ),
    )
    renderConRouter('/registro')
    const user = await completarFormulario()
    await user.type(screen.getByLabelText(/contraseña/i), 'passwordSegura123')
    await user.click(screen.getByRole('button', { name: /crear cuenta/i }))
    expect(await screen.findByRole('alert')).toHaveTextContent(/usuario o correo ya está registrado/i)
  })
})