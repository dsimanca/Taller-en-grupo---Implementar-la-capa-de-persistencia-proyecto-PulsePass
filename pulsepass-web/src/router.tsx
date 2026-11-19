import { createBrowserRouter } from 'react-router'
import { Layout } from './layouts/Layout'
import { RutaProtegida } from './layouts/RutaProtegida'
import { RutaPorRol } from './layouts/RutaPorRol'
import { SoloAnonimo } from './layouts/SoloAnonimo'
import { Inicio } from './pages/Inicio'
import { ErrorPagina } from './pages/ErrorPagina'

export const router = createBrowserRouter([
  {
    path: '/',
    Component: Layout,
    errorElement: <ErrorPagina />,
    children: [
      { index: true, Component: Inicio },
      {
        path: 'eventos',
        lazy: async () => ({ Component: (await import('./pages/CatalogoEventos')).CatalogoEventos }),
      },
      {
        path: 'eventos/:eventCode',
        lazy: async () => ({ Component: (await import('./pages/DetalleEvento')).DetalleEvento }),
      },
      {
        path: 'artistas',
        lazy: async () => ({ Component: (await import('./pages/ListaArtistas')).ListaArtistas }),
      },
      {
        path: 'artistas/:id',
        lazy: async () => ({ Component: (await import('./pages/DetalleArtista')).DetalleArtista }),
      },
      {
        path: 'sedes',
        lazy: async () => ({ Component: (await import('./pages/ListaSedes')).ListaSedes }),
      },
      {
        path: 'sedes/:code',
        lazy: async () => ({ Component: (await import('./pages/DetalleSede')).DetalleSede }),
      },
      {
        Component: SoloAnonimo,
        children: [
          {
            path: 'login',
            lazy: async () => ({ Component: (await import('./pages/Login')).Login }),
          },
          {
            path: 'registro',
            lazy: async () => ({ Component: (await import('./pages/Registro')).Registro }),
          },
        ],
      },
      {
        Component: RutaProtegida,
        children: [
          {
            path: 'cuenta',
            lazy: async () => ({ Component: (await import('./pages/Cuenta')).Cuenta }),
          },
          {
            Component: () => <RutaPorRol roles={['USER']} />,
            children: [
              {
                path: 'mis-tickets',
                lazy: async () => ({ Component: (await import('./pages/MisTickets')).MisTickets }),
              },
              {
                path: 'mis-tickets/:ticketCode',
                lazy: async () => ({
                  Component: (await import('./pages/DetalleTicket')).DetalleTicket,
                }),
              },
              {
                path: 'compra/confirmacion/:ticketCode',
                lazy: async () => ({
                  Component: (await import('./pages/ConfirmacionCompra')).ConfirmacionCompra,
                }),
              },
            ],
          },
          {
            Component: () => <RutaPorRol roles={['STAFF', 'ADMIN']} />,
            children: [
              {
                path: 'staff/validacion',
                lazy: async () => ({
                  Component: (await import('./pages/ValidacionIngreso')).ValidacionIngreso,
                }),
              },
              {
                path: 'staff/eventos/:eventCode/tickets',
                lazy: async () => ({
                  Component: (await import('./pages/TicketsPagadosEvento')).TicketsPagadosEvento,
                }),
              },
            ],
          },
          {
            Component: () => <RutaPorRol roles={['ADMIN']} />,
            children: [
              {
                path: 'admin/eventos',
                lazy: async () => ({
                  Component: (await import('./pages/AdminEventos')).AdminEventos,
                }),
              },
              {
                path: 'admin/eventos/nuevo',
                lazy: async () => ({
                  Component: (await import('./pages/AdminCrearEvento')).AdminCrearEvento,
                }),
              },
              {
                path: 'admin/eventos/:eventCode',
                lazy: async () => ({
                  Component: (await import('./pages/AdminGestionEvento')).AdminGestionEvento,
                }),
              },
            ],
          },
        ],
      },
      {
        path: '403',
        lazy: async () => ({ Component: (await import('./pages/SinPermisos')).SinPermisos }),
      },
      {
        path: '*',
        lazy: async () => ({ Component: (await import('./pages/NoEncontrada')).NoEncontrada }),
      },
    ],
  },
])
