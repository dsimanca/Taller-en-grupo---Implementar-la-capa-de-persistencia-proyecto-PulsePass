import { Link } from 'react-router'
import { Badge } from '../../../componentes/Badge'
import { Boton } from '../../../componentes/Boton'
import { CodigoTexto } from '../../../componentes/CodigoTexto'
import { Icono } from '../../../componentes/Icono'
import { PrecioCOP } from '../../../componentes/PrecioCOP'
import { formatearFecha } from '../../../lib/formato'
import { RUTAS } from '../../../lib/rutas'
import type { Ticket } from '../tipos'

export function TarjetaTicket({
  ticket,
  onCancelar,
}: {
  ticket: Ticket
  onCancelar: (ticket: Ticket) => void
}) {
  return (
    <article className="flex flex-col gap-4 rounded-xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <div className="flex items-start justify-between gap-3">
        <div>
          <h3 className="font-semibold text-slate-900 dark:text-white">{ticket.eventName}</h3>
          <CodigoTexto className="text-slate-500 dark:text-slate-400">{ticket.ticketCode}</CodigoTexto>
        </div>
        <Badge tono={ticket.status}>{ticket.status}</Badge>
      </div>
      <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-sm text-slate-600 dark:text-slate-400">
        <span>{ticket.type}</span>
        <PrecioCOP valor={ticket.price} className="font-semibold text-slate-900 dark:text-white" />
        <span className="flex items-center gap-1">
          <Icono nombre="calendario" className="size-4" />
          {formatearFecha(ticket.purchaseDate)}
        </span>
      </div>
      <div className="mt-auto flex flex-wrap gap-2">
        <Link
          to={RUTAS.detalleTicket(ticket.ticketCode)}
          className="inline-flex min-h-11 items-center justify-center rounded-lg border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 dark:border-slate-700 dark:text-slate-200 dark:hover:bg-slate-800"
        >
          Ver detalle
        </Link>
        {ticket.status === 'PAID' && (
          <Boton variante="secundario" onClick={() => onCancelar(ticket)}>
            Cancelar
          </Boton>
        )}
      </div>
    </article>
  )
}
