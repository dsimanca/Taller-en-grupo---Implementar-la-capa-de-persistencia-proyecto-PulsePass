import { Navigate, Outlet, useLocation } from 'react-router'
import { Cargando } from '../componentes/Cargando'
import { RUTAS } from '../lib/rutas'
import { useAuth } from '../funcionalidades/auth/AuthContext'
import type { Rol } from '../funcionalidades/auth/tipos'

export function RutaPorRol({ roles }: { roles: Rol[] }) {
  const { sesion } = useAuth()
  const location = useLocation()

  if (sesion.estado === 'cargando') return <Cargando />
  if (sesion.estado === 'anonimo') {
    return <Navigate to={RUTAS.login} replace state={{ desde: location.pathname }} />
  }
  if (!roles.some((rol) => sesion.usuario.roles.includes(rol))) {
    return <Navigate to={RUTAS.sinPermisos} replace />
  }
  return <Outlet />
}
