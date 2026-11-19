import { useQuery } from '@tanstack/react-query'
import { sedeApi, sedesActivasApi } from './api'

export const CLAVES_SEDES = {
  activas: ['sedes', 'activas'] as const,
  sede: (code: string) => ['sede', code] as const,
}

export function useSedesActivas() {
  return useQuery({
    queryKey: CLAVES_SEDES.activas,
    queryFn: sedesActivasApi,
    staleTime: 5 * 60_000,
  })
}

export function useSede(code: string) {
  return useQuery({
    queryKey: CLAVES_SEDES.sede(code),
    queryFn: () => sedeApi(code),
    enabled: code.length > 0,
    staleTime: 5 * 60_000,
  })
}
