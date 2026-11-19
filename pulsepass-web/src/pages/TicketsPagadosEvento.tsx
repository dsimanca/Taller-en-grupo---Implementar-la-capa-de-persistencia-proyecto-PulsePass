import { useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { Badge } from '../componentes/Badge'
import { Boton } from '../componentes/Boton'
import { Campo } from '../componentes/Campo'
import { Contenedor } from '../componentes/Contenedor'
import { Cargando } from '../componentes/Cargando'
import { EstadoError } from '../componentes/EstadoError'
import { Tabla } from '../componentes/Tabla'
import { CodigoTexto } from '../componentes/CodigoTexto'
import { PrecioCOP } from '../componentes/PrecioCOP'
import { useAviso } from '../componentes/Aviso'
import { ApiError } from '../lib/http'
import { RUTAS } from '../lib/rutas'
import { useTicketsPagadosPorEvento, useUsarTicket } from '../funcionalidades/tickets/hooks'
import type { Ticket } from '../funcionalidades/tickets/tipos'

export function TicketsPagadosEvento() {
  const { eventCode = '' } = useParams()
  const navegar = useNavigate()
  const aviso = useAviso()
  const tickets = useTicketsPagadosPorEvento(eventCode)
  const usar = useUsarTicket()
  const [codigoEvento, setCodigoEvento] = useState(eventCode)
  const [usando, setUsando] = useState<string | null>(null)

  async function marcar(ticket: Ticket) {
    setUsando(ticket.ticketCode)
    try {
      await usar.mutateAsync(ticket.ticketCode)
      aviso.mostrar('exito', `Ingreso registrado para ${ticket.ticketCode}.`)
    } catch (error) {
      aviso.mostrar(
        'error',
        error instanceof ApiError ? error.message : 'No pudimos validar el ticket.',
      )
    } finally {
      setUsando(null)
    }
  }

  return (
    <Contenedor className="space-y-6">
      <header className="space-y-4">
        <div>
          <h1 className="text-3xl font-bold text-slate-900 dark:text-white">
            Tickets pagados · <CodigoTexto>{eventCode}</CodigoTexto>
          </h1>
          <p className="mt-1 text-slate-600 dark:text-slate-400">
            Total: {tickets.data?.length ?? 0}
          </p>
        </div>
        <form
          onSubmit={(evento) => {
            evento.preventDefault()
            if (codigoEvento.trim()) navegar(RUTAS.ticketsPagados(codigoEvento.trim()))
          }}
          className="flex flex-col gap-3 sm:flex-row sm:items-end sm:max-w-md"
        >
          <div className="flex-1">
            <Campo
              etiqueta="Evento"
              value={codigoEvento}
              onChange={(evento) => setCodigoEvento(evento.target.value)}
            />
          </div>
          <Boton type="submit" variante="secundario">Ir</Boton>
        </form>
      </header>

      {tickets.isPending && <Cargando mensaje="Cargando tickets pagados…" />}
      {tickets.isError && (
        <EstadoError
          mensaje={
            tickets.error instanceof ApiError ? tickets.error.message : 'No pudimos cargar los tickets.'
          }
          onReintentar={() => void tickets.refetch()}
        />
      )}
      {tickets.isSuccess && (
        <Tabla<Ticket>
          columnas={[
            { encabezado: 'Código', celda: (fila) => <CodigoTexto>{fila.ticketCode}</CodigoTexto> },
            { encabezado: 'Tipo', celda: (fila) => fila.type },
            { encabezado: 'Titular', celda: (fila) => <span className="break-all">{fila.userEmail}</span> },
            { encabezado: 'Precio', celda: (fila) => <PrecioCOP valor={fila.price} /> },
            { encabezado: 'Estado', celda: (fila) => <Badge tono={fila.status}>{fila.status}</Badge> },
            {
              encabezado: 'Acción',
              celda: (fila) => (
                <Boton
                  variante="secundario"
                  onClick={() => void marcar(fila)}
                  cargando={usando === fila.ticketCode}
                  disabled={fila.status !== 'PAID'}
                >
                  Usar
                </Boton>
              ),
            },
          ]}
          filas={tickets.data}
          obtenerClave={(fila) => fila.ticketCode}
          mensajeVacio="No hay tickets pagados para este evento."
        />
      )}
    </Contenedor>
  )
}
