import { z } from 'zod'
import { httpGet, httpPatch, httpPost } from '../../lib/http'
import {
  EventResponseSchema,
  EventSummaryResponseSchema,
  type CreateEventRequest,
  type EventResponse,
  type EventSummaryResponse,
} from './tipos'

const ListaEventosSchema = z.array(EventSummaryResponseSchema)

export function eventosPublicadosApi() {
  return httpGet('/events/published', ListaEventosSchema)
}

function eventosPorArtistaApi(stageName: string) {
  return httpGet(
    `/events/by-artist?stageName=${encodeURIComponent(stageName)}`,
    ListaEventosSchema,
  )
}

export function eventoApi(eventCode: string) {
  return httpGet(`/events/${encodeURIComponent(eventCode)}`, EventResponseSchema)
}

export function crearEventoApi(datos: CreateEventRequest) {
  return httpPost('/events', EventResponseSchema, datos)
}

export function publicarEventoApi(eventCode: string) {
  return httpPatch(
    `/events/${encodeURIComponent(eventCode)}/publish`,
    EventResponseSchema,
    undefined,
  )
}

export function asociarArtistaApi(eventCode: string, artistId: number) {
  return httpPost(
    `/events/${encodeURIComponent(eventCode)}/artists/${artistId}`,
    EventResponseSchema,
    undefined,
  )
}

export const apiEventos = {
  eventosPublicadosApi,
  eventosPorArtistaApi,
  eventoApi,
}

export type { CreateEventRequest, EventResponse, EventSummaryResponse }
