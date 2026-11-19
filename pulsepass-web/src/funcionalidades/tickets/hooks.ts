import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  cancelarTicketApi,
  comprarTicketApi,
  ticketApi,
  ticketsPagadosApi,
  ticketsPorUsuarioApi,
  usarTicketApi,
} from './api'
import type { PurchaseTicketRequest, Ticket } from './tipos'

export const CLAVES_TICKETS = {
  ticket: (ticketCode: string) => ['ticket', ticketCode] as const,
  usuario: (email: string) => ['tickets', 'usuario', email] as const,
  pagados: (eventCode: string) => ['tickets', 'pagados', eventCode] as const,
}

export function useTicket(ticketCode: string | null) {
  return useQuery({
    queryKey: CLAVES_TICKETS.ticket(ticketCode ?? ''),
    queryFn: () => ticketApi(ticketCode as string),
    enabled: Boolean(ticketCode),
    staleTime: 15_000,
    retry: false,
  })
}

export function useBuscarTicket(ticketCode: string | null) {
  return useTicket(ticketCode)
}

export function useMisTickets(email: string | null) {
  return useQuery({
    queryKey: CLAVES_TICKETS.usuario(email ?? ''),
    queryFn: () => ticketsPorUsuarioApi(email as string),
    enabled: Boolean(email),
    staleTime: 15_000,
    retry: false,
  })
}

export function useTicketsPagadosPorEvento(eventCode: string) {
  return useQuery({
    queryKey: CLAVES_TICKETS.pagados(eventCode),
    queryFn: () => ticketsPagadosApi(eventCode),
    enabled: eventCode.length > 0,
    staleTime: 15_000,
    retry: false,
  })
}

export function useComprarTicket() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (datos: PurchaseTicketRequest) => comprarTicketApi(datos),
    onSuccess: (ticket: Ticket) => {
      void queryClient.invalidateQueries({
        queryKey: CLAVES_TICKETS.usuario(ticket.userEmail),
      })
      void queryClient.invalidateQueries({
        queryKey: ['evento', ticket.eventCode],
      })
    },
  })
}

export function useCancelarTicket() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: cancelarTicketApi,
    onSuccess: (ticket) => {
      void queryClient.invalidateQueries({
        queryKey: CLAVES_TICKETS.usuario(ticket.userEmail),
      })
      queryClient.setQueryData(CLAVES_TICKETS.ticket(ticket.ticketCode), ticket)
      void queryClient.invalidateQueries({
        queryKey: CLAVES_TICKETS.pagados(ticket.eventCode),
      })
    },
  })
}

export function useUsarTicket() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: usarTicketApi,
    onSuccess: (ticket) => {
      queryClient.setQueryData(CLAVES_TICKETS.ticket(ticket.ticketCode), ticket)
      void queryClient.invalidateQueries({
        queryKey: CLAVES_TICKETS.pagados(ticket.eventCode),
      })
    },
  })
}
