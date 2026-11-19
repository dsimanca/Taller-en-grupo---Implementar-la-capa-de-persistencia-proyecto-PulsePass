import { useQuery } from '@tanstack/react-query'
import { artistaApi, artistasActivosApi, artistaPorNombreApi } from './api'

export const CLAVES_ARTISTAS = {
  activos: ['artistas', 'activos'] as const,
  artista: (id: number) => ['artista', id] as const,
  porNombre: (stageName: string) => ['artista', 'nombre', stageName] as const,
}

export function useArtistasActivos() {
  return useQuery({
    queryKey: CLAVES_ARTISTAS.activos,
    queryFn: artistasActivosApi,
    staleTime: 5 * 60_000,
  })
}

export function useArtista(id: number | null) {
  return useQuery({
    queryKey: CLAVES_ARTISTAS.artista(id ?? -1),
    queryFn: () => artistaApi(id as number),
    enabled: id !== null && Number.isFinite(id),
    staleTime: 5 * 60_000,
  })
}

export function useArtistaPorNombre(stageName: string) {
  return useQuery({
    queryKey: CLAVES_ARTISTAS.porNombre(stageName),
    queryFn: () => artistaPorNombreApi(stageName),
    enabled: stageName.trim().length > 0,
    staleTime: 5 * 60_000,
    retry: false,
  })
}
