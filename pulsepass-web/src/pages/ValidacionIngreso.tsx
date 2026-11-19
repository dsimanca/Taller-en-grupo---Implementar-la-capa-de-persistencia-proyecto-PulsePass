import { useState } from 'react'
import { Badge } from '../componentes/Badge'
import { Boton } from '../componentes/Boton'
import { Campo } from '../componentes/Campo'
import { Contenedor } from '../componentes/Contenedor'
import { Cargando } from '../componentes/Cargando'
import { Icono } from '../componentes/Icono'
import { CodigoTexto } from '../componentes/CodigoTexto'
import { ApiError } from '../lib/http'
import { useBuscarTicket, useUsarTicket } from '../funcionalidades/tickets/hooks'

export function ValidacionIngreso() {
  const [codigo, setCodigo] = useState('')
  const [codigoBuscado, setCodigoBuscado] = useState<string | null>(null)
  const [mensajeResultado, setMensajeResultado] = useState<
    { tipo: 'exito' | 'error'; texto: string } | null
  >(null)
  const ticket = useBuscarTicket(codigoBuscado)
  const usar = useUsarTicket()

  function buscar(evento: React.FormEvent) {
    evento.preventDefault()
    const limpio = codigo.trim()
    if (!limpio) return
    setMensajeResultado(null)
    setCodigoBuscado(limpio)
  }

  async function marcarComoUsado() {
    if (!codigoBuscado) return
    setMensajeResultado(null)
    try {
      await usar.mutateAsync(codigoBuscado)
      setMensajeResultado({ tipo: 'exito', texto: 'Ingreso registrado.' })
      setCodigo('')
      setCodigoBuscado(null)
    } catch (error) {
      const texto = error instanceof ApiError ? error.message : 'No pudimos validar el ticket.'
      setMensajeResultado({ tipo: 'error', texto })
    }
  }

  const datos = ticket.data

  return (
    <Contenedor estrecho className="space-y-6">
      <header>
        <h1 className="text-3xl font-bold text-slate-900 dark:text-white">Validación de ingreso</h1>
        <p className="mt-1 text-slate-600 dark:text-slate-400">
          Busca un ticket por código y regístralo como usado.
        </p>
      </header>

      <form onSubmit={buscar} className="flex flex-col gap-3 sm:flex-row sm:items-end">
        <div className="flex-1">
          <Campo
            etiqueta="Código de ticket"
            placeholder="TKT-8F2A1C"
            value={codigo}
            onChange={(evento) => setCodigo(evento.target.value)}
            autoFocus
          />
        </div>
        <Boton type="submit" disabled={codigo.trim().length === 0}>Buscar</Boton>
      </form>

      {ticket.isFetching && <Cargando mensaje="Buscando ticket…" />}

      {ticket.isError && !ticket.isFetching && (
        <div role="alert" className="rounded-xl border border-red-200 bg-red-50 p-4 text-red-800 dark:border-red-900 dark:bg-red-950/40 dark:text-red-200">
          {ticket.error instanceof ApiError ? ticket.error.message : 'No pudimos buscar el ticket.'}
        </div>
      )}

      {datos && !ticket.isFetching && (
        <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
          <div className="flex flex-wrap items-start justify-between gap-3">
            <div>
              <h2 className="text-xl font-bold text-slate-900 dark:text-white">{datos.eventName}</h2>
              <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">
                <CodigoTexto>{datos.ticketCode}</CodigoTexto> · {datos.type} · Titular: {datos.userEmail}
              </p>
            </div>
            <Badge tono={datos.status}>{datos.status}</Badge>
          </div>

          {mensajeResultado && (
            <div
              role={mensajeResultado.tipo === 'error' ? 'alert' : 'status'}
              className={
                mensajeResultado.tipo === 'exito'
                  ? 'mt-4 flex items-center gap-2 rounded-lg bg-emerald-100 px-3 py-2 text-emerald-800'
                  : 'mt-4 flex items-center gap-2 rounded-lg bg-red-100 px-3 py-2 text-red-800'
              }
            >
              <Icono nombre={mensajeResultado.tipo === 'exito' ? 'check' : 'alerta'} className="size-4" />
              {mensajeResultado.texto}
            </div>
          )}

          <div className="mt-6 flex justify-end">
            <Boton
              onClick={() => void marcarComoUsado()}
              cargando={usar.isPending}
              disabled={datos.status !== 'PAID'}
            >
              Marcar como usado
            </Boton>
          </div>
        </section>
      )}
    </Contenedor>
  )
}
