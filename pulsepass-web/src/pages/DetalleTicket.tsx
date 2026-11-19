import { useState } from 'react'
import { Link, useParams } from 'react-router'
import { Badge } from '../componentes/Badge'
import { Boton } from '../componentes/Boton'
import { Contenedor } from '../componentes/Contenedor'
import { Cargando } from '../componentes/Cargando'
import { EstadoError } from '../componentes/EstadoError'
import { Icono } from '../componentes/Icono'
import { CodigoTexto } from '../componentes/CodigoTexto'
import { PrecioCOP } from '../componentes/PrecioCOP'
import { useAviso } from '../componentes/Aviso'
import { ApiError } from '../lib/http'
import { formatearFechaHora } from '../lib/formato'
import { RUTAS } from '../lib/rutas'
import { useCancelarTicket, useTicket } from '../funcionalidades/tickets/hooks'
import { ModalConfirmarCancelacion } from '../funcionalidades/tickets/componentes/ModalConfirmarCancelacion'

export function DetalleTicket() {
  const { ticketCode = '' } = useParams()
  const aviso = useAviso()
  const ticket = useTicket(ticketCode)
  const cancelar = useCancelarTicket()
  const [confirmando, setConfirmando] = useState(false)
  const [errorCancelacion, setErrorCancelacion] = useState<string | null>(null)

  if (ticket.isPending) return <Contenedor><Cargando mensaje="Cargando ticket…" /></Contenedor>

  if (ticket.isError) {
    if (ticket.error instanceof ApiError && [403, 404].includes(ticket.error.status)) {
      return (
        <Contenedor className="space-y-4 text-center">
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white">Ticket no disponible</h1>
          <p className="text-slate-600 dark:text-slate-400">
            No encontramos este ticket o no tienes acceso a él.
          </p>
          <Link to={RUTAS.misTickets} className="text-marca-600 hover:underline">
            Volver a mis tickets
          </Link>
        </Contenedor>
      )
    }
    return (
      <Contenedor>
        <EstadoError mensaje="No pudimos cargar el ticket." onReintentar={() => void ticket.refetch()} />
      </Contenedor>
    )
  }

  const datos = ticket.data

  async function confirmarCancelacion() {
    setErrorCancelacion(null)
    try {
      await cancelar.mutateAsync(datos.ticketCode)
      setConfirmando(false)
      aviso.mostrar('exito', 'Ticket cancelado correctamente.')
    } catch (error) {
      setErrorCancelacion(
        error instanceof ApiError ? error.message : 'No pudimos cancelar el ticket.',
      )
    }
  }

  async function copiarCodigo() {
    try {
      await navigator.clipboard.writeText(datos.ticketCode)
      aviso.mostrar('info', 'Código copiado.')
    } catch {
      aviso.mostrar('error', 'No pudimos copiar el código.')
    }
  }

  return (
    <Contenedor estrecho className="space-y-6">
      <Link to={RUTAS.misTickets} className="inline-flex items-center gap-2 text-sm font-semibold text-marca-600 hover:underline">
        <Icono nombre="flechaIzquierda" className="size-4" /> Mis tickets
      </Link>

      <article className="rounded-2xl border border-slate-200 bg-white p-8 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <h1 className="text-2xl font-bold text-slate-900 dark:text-white">{datos.eventName}</h1>
            <CodigoTexto className="text-slate-500 dark:text-slate-400">{datos.eventCode}</CodigoTexto>
          </div>
          <Badge tono={datos.status}>{datos.status}</Badge>
        </div>

        <dl className="mt-8 space-y-4 text-sm">
          <div className="flex items-center justify-between gap-4 border-b border-slate-100 pb-3 dark:border-slate-800">
            <dt className="text-slate-500 dark:text-slate-400">Código</dt>
            <dd className="flex items-center gap-2 font-medium">
              <CodigoTexto>{datos.ticketCode}</CodigoTexto>
              <button type="button" onClick={() => void copiarCodigo()} className="rounded p-1 text-marca-600 hover:bg-marca-50 dark:hover:bg-slate-800" aria-label="Copiar código">
                <Icono nombre="copiar" className="size-4" />
              </button>
            </dd>
          </div>
          <div className="flex justify-between gap-4 border-b border-slate-100 pb-3 dark:border-slate-800">
            <dt className="text-slate-500 dark:text-slate-400">Tipo</dt>
            <dd className="font-medium text-slate-900 dark:text-white">{datos.type}</dd>
          </div>
          <div className="flex justify-between gap-4 border-b border-slate-100 pb-3 dark:border-slate-800">
            <dt className="text-slate-500 dark:text-slate-400">Precio</dt>
            <dd className="font-semibold text-slate-900 dark:text-white"><PrecioCOP valor={datos.price} /></dd>
          </div>
          <div className="flex justify-between gap-4 border-b border-slate-100 pb-3 dark:border-slate-800">
            <dt className="text-slate-500 dark:text-slate-400">Comprado el</dt>
            <dd className="font-medium text-slate-900 dark:text-white">{formatearFechaHora(datos.purchaseDate)}</dd>
          </div>
          <div className="flex justify-between gap-4">
            <dt className="text-slate-500 dark:text-slate-400">Titular</dt>
            <dd className="font-medium break-all text-slate-900 dark:text-white">{datos.userEmail}</dd>
          </div>
        </dl>

        <p className="mt-8 rounded-lg bg-slate-50 px-4 py-3 text-sm text-slate-600 dark:bg-slate-800 dark:text-slate-300">
          Presenta este código en el ingreso.
        </p>

        {datos.status === 'PAID' && (
          <div className="mt-6 flex justify-end">
            <Boton variante="peligro" onClick={() => { setErrorCancelacion(null); setConfirmando(true) }}>
              Cancelar ticket
            </Boton>
          </div>
        )}
      </article>

      <ModalConfirmarCancelacion
        ticket={confirmando ? datos : null}
        cargando={cancelar.isPending}
        error={errorCancelacion}
        onCerrar={() => { if (!cancelar.isPending) { setConfirmando(false); setErrorCancelacion(null) } }}
        onConfirmar={() => void confirmarCancelacion()}
      />
    </Contenedor>
  )
}
