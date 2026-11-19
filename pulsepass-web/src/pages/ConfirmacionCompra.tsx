import { Link, useParams } from 'react-router'
import { Badge } from '../componentes/Badge'
import { Contenedor } from '../componentes/Contenedor'
import { Cargando } from '../componentes/Cargando'
import { EstadoError } from '../componentes/EstadoError'
import { Icono } from '../componentes/Icono'
import { CodigoTexto } from '../componentes/CodigoTexto'
import { PrecioCOP } from '../componentes/PrecioCOP'
import { ApiError } from '../lib/http'
import { RUTAS } from '../lib/rutas'
import { useTicket } from '../funcionalidades/tickets/hooks'

export function ConfirmacionCompra() {
  const { ticketCode = '' } = useParams()
  const ticket = useTicket(ticketCode)

  if (ticket.isPending) return <Contenedor><Cargando mensaje="Cargando confirmación…" /></Contenedor>

  if (ticket.isError) {
    if (ticket.error instanceof ApiError && [403, 404].includes(ticket.error.status)) {
      return (
        <Contenedor className="space-y-4 text-center">
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white">Ticket no disponible</h1>
          <p className="text-slate-600 dark:text-slate-400">No pudimos mostrar la confirmación.</p>
          <Link to={RUTAS.misTickets} className="text-marca-600 hover:underline">Ir a mis tickets</Link>
        </Contenedor>
      )
    }
    return <Contenedor><EstadoError mensaje="No pudimos cargar la confirmación." onReintentar={() => void ticket.refetch()} /></Contenedor>
  }

  const datos = ticket.data
  return (
    <Contenedor estrecho className="flex min-h-[60vh] items-center justify-center">
      <section className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-8 text-center shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <span className="mx-auto inline-flex size-16 items-center justify-center rounded-full bg-emerald-100 text-emerald-600">
          <Icono nombre="check" className="size-8" />
        </span>
        <h1 className="mt-4 text-2xl font-bold text-slate-900 dark:text-white">¡Compra realizada con éxito!</h1>
        <div className="mt-6 space-y-2 text-left rounded-xl bg-slate-50 p-4 dark:bg-slate-800">
          <p className="font-semibold text-slate-900 dark:text-white">{datos.eventName}</p>
          <p className="text-sm text-slate-600 dark:text-slate-300">
            Ticket <CodigoTexto>{datos.ticketCode}</CodigoTexto> · {datos.type} · <PrecioCOP valor={datos.price} />
          </p>
          <div><Badge tono={datos.status}>{datos.status}</Badge></div>
        </div>
        <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:justify-center">
          <Link to={RUTAS.misTickets} className="inline-flex min-h-11 items-center justify-center rounded-lg bg-marca-600 px-5 py-2 font-semibold text-white hover:bg-marca-700">Ver mis tickets</Link>
          <Link to={RUTAS.eventos} className="inline-flex min-h-11 items-center justify-center rounded-lg border border-slate-300 px-5 py-2 font-semibold text-slate-700 hover:bg-slate-50 dark:border-slate-700 dark:text-slate-200 dark:hover:bg-slate-800">Seguir explorando</Link>
        </div>
      </section>
    </Contenedor>
  )
}
