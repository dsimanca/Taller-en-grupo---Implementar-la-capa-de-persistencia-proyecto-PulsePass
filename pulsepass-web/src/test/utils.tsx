import { render } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { RouterProvider, createMemoryRouter } from 'react-router'
import { AuthProvider } from '../funcionalidades/auth/AuthContext'
import { AvisoProvider } from '../componentes/Aviso'
import type { Rol } from '../funcionalidades/auth/tipos'
import { router } from '../router'

export function crearQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: { retry: false, gcTime: 0 },
      mutations: { retry: false },
    },
  })
}

export function sembrarSesion(roles: Rol[] = ['USER']) {
  const sesion = {
    token: 'token-test',
    expiraEn: Date.now() + 900_000,
    usuario: {
      id: 10,
      username: 'andrea',
      email: 'andrea@pulsepass.test',
      roles,
    },
  }
  sessionStorage.setItem('pulsepass.sesion', JSON.stringify(sesion))
  return sesion
}

export function renderConRouter(
  rutaInicial: string | { pathname: string; state?: unknown } = '/',
  opciones?: { queryClient?: QueryClient },
) {
  const queryClient = opciones?.queryClient ?? crearQueryClient()
  const memoryRouter = createMemoryRouter(router.routes, {
    initialEntries: [rutaInicial],
  })
  return {
    queryClient,
    router: memoryRouter,
    ...render(
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <AvisoProvider>
            <RouterProvider router={memoryRouter} />
          </AvisoProvider>
        </AuthProvider>
      </QueryClientProvider>,
    ),
  }
}