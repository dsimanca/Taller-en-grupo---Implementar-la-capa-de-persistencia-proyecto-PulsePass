import { useState } from 'react'
import { Link } from 'react-router'
import { Contenedor } from '../componentes/Contenedor'
import { Cargando } from '../componentes/Cargando'
import { EstadoVacio } from '../componentes/EstadoVacio'
import { EstadoError } from '../componentes/EstadoError'
import { useAviso } from '../componentes/Aviso'
import { RUTAS } from '../lib/rutas'
import { ApiError } from '../lib/http'
import { useAuth } from '../funcionalidades/auth/AuthContext'
import { useCancelarTicket, useMisTickets } from '../funcionalidades/tickets/hooks'
import { FiltroChips, type FiltroEstadoTicket } from '../funcionalidades/tickets/componentes/FiltroChips'
import { TarjetaTicket } from '../funcionalidades/tickets/componentes/TarjetaTicket'
import { ModalConfirmarCancelacion } from '../funcionalidades/tickets/componentes/ModalConfirmarCancelacion'
import type { Ticket } from '../funcionalidades/tickets/tipos'

export function MisTickets() {
  const { sesion } = useAuth()
  const aviso = useAviso()
  const email = sesion.estado === 'autenticado' ? sesion.usuario.email : null
  const tickets = useMisTickets(email)
  const cancelar = useCancelarTicket()
  const [filtro, setFiltro] = useState<FiltroEstadoTicket>('TODOS')
  const [ticketACancelar, setTicketACancelar] = useState<Ticket | null>(null)
  const [errorCancelacion, setErrorCancelacion] = useState<string | null>(null)

  const filtrados =
    tickets.data?.filter((ticket) => filtro === 'TODOS' || ticket.status === filtro) ?? []

  async function confirmarCancelacion() {
    if (!ticketACancelar) return
    setErrorCancelacion(null)
    try {
      await cancelar.mutateAsync(ticketACancelar.ticketCode)
      setTicketACancelar(null)
      aviso.mostrar('exito', 'Ticket cancelado correctamente.')
    } catch (error) {
      setErrorCancelacion(
        error instanceof ApiError ? error.message : 'No pudimos cancelar el ticket.',
      )
    }
  }

  return (
    <Contenedor className="space-y-6">
      <header>
        <h1 className="text-3xl font-bold text-slate-900 dark:text-white">Mis tickets</h1>
        <p className="mt-1 text-slate-600 dark:text-slate-400">
          Consulta y gestiona las entradas asociadas a tu cuenta.
        </p>
      </header>

      <FiltroChips valor={filtro} onCambiar={setFiltro} />

      {tickets.isPending && <Cargando mensaje="Cargando tus tickets…" />}
      {tickets.isError && (
        <EstadoError
          mensaje="No pudimos cargar tus tickets."
          onReintentar={() => void tickets.refetch()}
        />
      )}
      {tickets.isSuccess && tickets.data.length === 0 && (
        <EstadoVacio
          mensaje="Aún no tienes tickets."
          icono="boleto"
          accion={
            <Link
              to={RUTAS.eventos}
              className="inline-flex min-h-11 items-center justify-center rounded-lg bg-marca-600 px-4 py-2 font-semibold text-white hover:bg-marca-700"
            >
              Ir a la cartelera
            </Link>
          }
        />
      )}
      {tickets.isSuccess && tickets.data.length > 0 && filtrados.length === 0 && (
        <EstadoVacio mensaje="No hay tickets con este estado." icono="boleto" />
      )}
      {tickets.isSuccess && filtrados.length > 0 && (
        <ul className="grid grid-cols-1 gap-4 lg:grid-cols-2">
          {filtrados.map((ticket) => (
            <li key={ticket.ticketCode}>
              <TarjetaTicket
                ticket={ticket}
                onCancelar={(seleccionado) => {
                  setErrorCancelacion(null)
                  setTicketACancelar(seleccionado)
                }}
              />
            </li>
          ))}
        </ul>
      )}

      <ModalConfirmarCancelacion
        ticket={ticketACancelar}
        cargando={cancelar.isPending}
        error={errorCancelacion}
        onCerrar={() => {
          if (!cancelar.isPending) {
            setTicketACancelar(null)
            setErrorCancelacion(null)
          }
        }}
        onConfirmar={() => void confirmarCancelacion()}
      />
    </Contenedor>
  )
}
