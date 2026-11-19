import { SesionGuardadaSchema, type SesionGuardada } from './tipos'

const CLAVE = 'pulsepass.sesion'
let sesionEnMemoria: SesionGuardada | null = null
let alNoAutorizado: (() => void) | null = null

function expirada(sesion: SesionGuardada): boolean {
  return sesion.expiraEn <= Date.now()
}

export function leerSesionGuardada(): SesionGuardada | null {
  if (sesionEnMemoria) {
    if (expirada(sesionEnMemoria)) {
      limpiarSesion()
      return null
    }
    return sesionEnMemoria
  }

  try {
    const texto = sessionStorage.getItem(CLAVE)
    if (!texto) return null
    const resultado = SesionGuardadaSchema.safeParse(JSON.parse(texto) as unknown)
    if (!resultado.success || expirada(resultado.data)) {
      limpiarSesion()
      return null
    }
    sesionEnMemoria = resultado.data
    return sesionEnMemoria
  } catch {
    limpiarSesion()
    return null
  }
}

export function guardarSesion(sesion: SesionGuardada): void {
  sesionEnMemoria = sesion
  try {
    sessionStorage.setItem(CLAVE, JSON.stringify(sesion))
  } catch {
    sesionEnMemoria = sesion
  }
}

export function limpiarSesion(): void {
  sesionEnMemoria = null
  try {
    sessionStorage.removeItem(CLAVE)
  } catch {
    sesionEnMemoria = null
  }
}

export function obtenerToken(): string | null {
  return leerSesionGuardada()?.token ?? null
}

export function registrarAlNoAutorizado(callback: () => void): () => void {
  alNoAutorizado = callback
  return () => {
    if (alNoAutorizado === callback) alNoAutorizado = null
  }
}

export function notificarNoAutorizado(): void {
  limpiarSesion()
  alNoAutorizado?.()
}
