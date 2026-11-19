import '@testing-library/jest-dom/vitest'
import { afterAll, afterEach, beforeAll, vi } from 'vitest'
import { cleanup } from '@testing-library/react'
import { server } from './mocks/server'
import { limpiarSesion } from '../funcionalidades/auth/sesion'

Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: (query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: () => {},
    removeListener: () => {},
    addEventListener: () => {},
    removeEventListener: () => {},
    dispatchEvent: () => false,
  }),
})

Object.defineProperty(window, 'scrollTo', { writable: true, value: () => {} })

beforeAll(() => server.listen())
afterEach(() => {
  server.resetHandlers()
  cleanup()
  limpiarSesion()
  sessionStorage.clear()
  vi.restoreAllMocks()
})
afterAll(() => server.close())