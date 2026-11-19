import { z } from 'zod'
import { httpGet, httpPatch, httpPost } from '../../lib/http'
import {
  TicketResponseSchema,
  type PurchaseTicketRequest,
  type Ticket,
} from './tipos'

const ListaTicketsSchema = z.array(TicketResponseSchema)

export function comprarTicketApi(datos: PurchaseTicketRequest) {
  return httpPost('/tickets', TicketResponseSchema, datos)
}

export function ticketApi(ticketCode: string) {
  return httpGet(`/tickets/${encodeURIComponent(ticketCode)}`, TicketResponseSchema)
}

export function ticketsPorUsuarioApi(email: string) {
  return httpGet(`/tickets/by-user?email=${encodeURIComponent(email)}`, ListaTicketsSchema)
}

export function ticketsPagadosApi(eventCode: string) {
  return httpGet(
    `/events/${encodeURIComponent(eventCode)}/tickets/paid`,
    ListaTicketsSchema,
  )
}

export function cancelarTicketApi(ticketCode: string) {
  return httpPatch(
    `/tickets/${encodeURIComponent(ticketCode)}/cancel`,
    TicketResponseSchema,
  )
}

export function usarTicketApi(ticketCode: string) {
  return httpPatch(`/tickets/${encodeURIComponent(ticketCode)}/use`, TicketResponseSchema)
}

export type { Ticket }
