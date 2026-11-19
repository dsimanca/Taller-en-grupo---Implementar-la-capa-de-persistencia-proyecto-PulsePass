import { screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { renderConRouter, sembrarSesion } from '../../test/utils'

describe('Guardas', () => {
  it('RutaProtegida: anónimo -> /login', async () => {
    renderConRouter('/cuenta')
    expect(await screen.findByRole('heading', { name: /inicia sesión/i })).toBeInTheDocument()
  })

  it('RutaPorRol: USER en /admin -> /403', async () => {
    sembrarSesion(['USER'])
    renderConRouter('/admin/eventos')
    expect(await screen.findByRole('heading', { name: /no tienes permisos/i })).toBeInTheDocument()
  })

  it('RutaPorRol: ADMIN en /admin -> renderiza', async () => {
    sembrarSesion(['ADMIN'])
    renderConRouter('/admin/eventos')
    expect(await screen.findByRole('heading', { name: /administración de eventos/i })).toBeInTheDocument()
  })

  it('SoloAnonimo: autenticado en /login -> inicio', async () => {
    sembrarSesion(['USER'])
    renderConRouter('/login')
    expect(await screen.findByRole('heading', { name: /descubre los mejores eventos/i })).toBeInTheDocument()
  })
})