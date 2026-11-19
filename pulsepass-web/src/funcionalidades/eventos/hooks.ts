import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  apiEventos,
  asociarArtistaApi,
  crearEventoApi,
  publicarEventoApi,
} from './api'

export const CLAVES_EVENTOS = {
  publicados: ['eventos', 'publicados'] as const,
  evento: (eventCode: string) => ['evento', eventCode] as const,
  porArtista: (stageName: string) => ['eventos', 'artista', stageName] as const,
}

export function useEventosPublicados() {
  return useQuery({
    queryKey: CLAVES_EVENTOS.publicados,
    queryFn: apiEventos.eventosPublicadosApi,
    staleTime: 60_000,
  })
}

export function useEventosPorArtista(stageName: string) {
  return useQuery({
    queryKey: CLAVES_EVENTOS.porArtista(stageName),
    queryFn: () => apiEventos.eventosPorArtistaApi(stageName),
    enabled: stageName.trim().length > 0,
    staleTime: 60_000,
  })
}

export function useEvento(eventCode: string) {
  return useQuery({
    queryKey: CLAVES_EVENTOS.evento(eventCode),
    queryFn: () => apiEventos.eventoApi(eventCode),
    enabled: eventCode.length > 0,
    staleTime: 30_000,
  })
}

export function useCrearEvento() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: crearEventoApi,
    onSuccess: (evento) => {
      queryClient.setQueryData(CLAVES_EVENTOS.evento(evento.eventCode), evento)
      void queryClient.invalidateQueries({ queryKey: CLAVES_EVENTOS.publicados })
    },
  })
}

export function usePublicarEvento() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: publicarEventoApi,
    onSuccess: (evento) => {
      queryClient.setQueryData(CLAVES_EVENTOS.evento(evento.eventCode), evento)
      void queryClient.invalidateQueries({ queryKey: CLAVES_EVENTOS.publicados })
    },
  })
}

export function useAsociarArtista() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ eventCode, artistId }: { eventCode: string; artistId: number }) =>
      asociarArtistaApi(eventCode, artistId),
    onSuccess: (evento) => {
      queryClient.setQueryData(CLAVES_EVENTOS.evento(evento.eventCode), evento)
    },
  })
}
