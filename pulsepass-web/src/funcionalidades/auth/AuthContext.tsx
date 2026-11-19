import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { queryClient } from '../../lib/queryClient'
import { RUTAS } from '../../lib/rutas'
import { navegarA } from '../../lib/navegacion'
import { loginApi, sesionApi } from './api'
import {
  guardarSesion,
  limpiarSesion,
  leerSesionGuardada,
  registrarAlNoAutorizado,
} from './sesion'
import type { Rol, UsuarioAutenticado } from './tipos'

export type Sesion =
  | { estado: 'cargando' }
  | { estado: 'anonimo' }
  | {
      estado: 'autenticado'
      token: string
      expiraEn: number
      usuario: UsuarioAutenticado
    }

type AuthContextValor = {
  sesion: Sesion
  iniciarSesion: (identifier: string, password: string) => Promise<void>
  cerrarSesion: () => void
  tieneRol: (...roles: Rol[]) => boolean
}

const AuthContext = createContext<AuthContextValor | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [sesion, setSesion] = useState<Sesion>({ estado: 'cargando' })

  useEffect(() => {
    const guardada = leerSesionGuardada()
    setSesion(
      guardada
        ? { estado: 'autenticado', ...guardada }
        : { estado: 'anonimo' },
    )
  }, [])

  useEffect(() => {
    return registrarAlNoAutorizado(() => {
      queryClient.clear()
      setSesion({ estado: 'anonimo' })
      navegarA(RUTAS.login, {
        replace: true,
        state: { motivo: 'expirada' },
      })
    })
  }, [])

  useEffect(() => {
    if (sesion.estado !== 'autenticado') return
    const restante = Math.max(0, sesion.expiraEn - Date.now())
    const temporizador = window.setTimeout(() => {
      limpiarSesion()
      queryClient.clear()
      setSesion({ estado: 'anonimo' })
      navegarA(RUTAS.login, {
        replace: true,
        state: { motivo: 'expirada' },
      })
    }, restante)
    return () => window.clearTimeout(temporizador)
  }, [sesion])

  const iniciarSesion = useCallback(async (identifier: string, password: string) => {
    const respuesta = await loginApi({ identifier, password })
    const usuario = await sesionApi(respuesta.accessToken)
    const datos = {
      token: respuesta.accessToken,
      expiraEn: Date.now() + respuesta.expiresIn * 1000,
      usuario,
    }
    guardarSesion(datos)
    setSesion({ estado: 'autenticado', ...datos })
  }, [])

  const cerrarSesion = useCallback(() => {
    limpiarSesion()
    queryClient.clear()
    setSesion({ estado: 'anonimo' })
  }, [])

  const tieneRol = useCallback(
    (...roles: Rol[]) =>
      sesion.estado === 'autenticado' &&
      roles.some((rol) => sesion.usuario.roles.includes(rol)),
    [sesion],
  )

  const valor = useMemo(
    () => ({ sesion, iniciarSesion, cerrarSesion, tieneRol }),
    [sesion, iniciarSesion, cerrarSesion, tieneRol],
  )

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValor {
  const contexto = useContext(AuthContext)
  if (!contexto) throw new Error('useAuth debe usarse dentro de AuthProvider')
  return contexto
}

export function useSesion(): Sesion {
  return useAuth().sesion
}
