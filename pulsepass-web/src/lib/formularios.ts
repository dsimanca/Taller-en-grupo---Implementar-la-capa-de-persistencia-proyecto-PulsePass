import type { FieldPath, FieldValues, UseFormSetError } from 'react-hook-form'
import { ApiError } from './http'

export function mensajeError(error: unknown): string {
  if (error instanceof ApiError) return error.message
  if (error instanceof Error) return 'Ocurrió un error inesperado. Inténtalo más tarde.'
  return 'Ocurrió un error inesperado. Inténtalo más tarde.'
}

export function aplicarDetails<T extends FieldValues>(
  error: unknown,
  setError: UseFormSetError<T>,
  campos: readonly FieldPath<T>[],
): string | null {
  if (!(error instanceof ApiError)) return mensajeError(error)
  const entradas = Object.entries(error.details)
  if (entradas.length === 0) return error.message

  const generales: string[] = []
  for (const [clave, mensaje] of entradas) {
    if (campos.includes(clave as FieldPath<T>)) {
      setError(clave as FieldPath<T>, { message: mensaje })
    } else {
      generales.push(mensaje)
    }
  }
  return generales.length > 0 ? generales.join(' ') : null
}
