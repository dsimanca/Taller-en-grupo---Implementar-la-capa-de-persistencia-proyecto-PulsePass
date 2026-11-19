import { z, type ZodType } from 'zod'
import { notificarNoAutorizado, obtenerToken } from '../funcionalidades/auth/sesion'

const BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? '/api').replace(/\/$/, '')

const ErrorResponseSchema = z.object({
  timestamp: z.string().optional(),
  status: z.number().int(),
  error: z.string().optional(),
  message: z.string(),
  details: z.record(z.string(), z.string()).optional(),
})

const MENSAJES_GENERICOS: Record<number, string> = {
  400: 'Revisa los datos e inténtalo de nuevo.',
  401: 'Necesitas iniciar sesión para continuar.',
  403: 'No tienes permisos para realizar esta operación.',
  404: 'No encontramos el recurso solicitado.',
  409: 'La operación no se pudo completar.',
  500: 'Ocurrió un error inesperado. Inténtalo más tarde.',
}

export class ApiError extends Error {
  readonly status: number
  readonly details: Record<string, string>

  constructor(status: number, message: string, details: Record<string, string> = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.details = details
  }
}

type OpcionesPeticion = {
  body?: unknown
  skipAuth?: boolean
  skipUnauthorized?: boolean
  token?: string
  signal?: AbortSignal
}

async function construirError(respuesta: Response): Promise<ApiError> {
  const generico = MENSAJES_GENERICOS[respuesta.status] ?? 'Ocurrió un error inesperado.'
  try {
    const datos: unknown = JSON.parse(await respuesta.text())
    const resultado = ErrorResponseSchema.safeParse(datos)
    if (!resultado.success) return new ApiError(respuesta.status, generico)
    return new ApiError(
      resultado.data.status,
      resultado.data.message || generico,
      resultado.data.details ?? {},
    )
  } catch {
    return new ApiError(respuesta.status, generico)
  }
}

async function peticion<T>(
  metodo: 'GET' | 'POST' | 'PATCH',
  ruta: string,
  esquema: ZodType<T>,
  opciones: OpcionesPeticion = {},
): Promise<T> {
  const cabeceras = new Headers({ Accept: 'application/json' })
  if (opciones.body !== undefined) cabeceras.set('Content-Type', 'application/json')

  const token = opciones.skipAuth ? null : (opciones.token ?? obtenerToken())
  if (token) cabeceras.set('Authorization', `Bearer ${token}`)

  let respuesta: Response
  try {
    respuesta = await fetch(`${BASE_URL}${ruta}`, {
      method: metodo,
      headers: cabeceras,
      body: opciones.body === undefined ? undefined : JSON.stringify(opciones.body),
      signal: opciones.signal,
    })
  } catch {
    throw new ApiError(0, 'No pudimos conectar con el servidor.')
  }

  if (!respuesta.ok) {
    const error = await construirError(respuesta)
    if (respuesta.status === 401 && !opciones.skipUnauthorized) notificarNoAutorizado()
    throw error
  }

  let datos: unknown
  try {
    datos = await respuesta.json()
  } catch {
    throw new ApiError(respuesta.status, 'La respuesta del servidor no es válida.')
  }

  const validados = esquema.safeParse(datos)
  if (!validados.success) {
    throw new ApiError(respuesta.status, 'La respuesta del servidor no es válida.')
  }
  return validados.data
}

export function httpGet<T>(ruta: string, esquema: ZodType<T>, opciones?: OpcionesPeticion) {
  return peticion('GET', ruta, esquema, opciones)
}

export function httpPost<T>(
  ruta: string,
  esquema: ZodType<T>,
  body: unknown,
  opciones?: OpcionesPeticion,
) {
  return peticion('POST', ruta, esquema, { ...opciones, body })
}

export function httpPatch<T>(
  ruta: string,
  esquema: ZodType<T>,
  body?: unknown,
  opciones?: OpcionesPeticion,
) {
  return peticion('PATCH', ruta, esquema, { ...opciones, body })
}
