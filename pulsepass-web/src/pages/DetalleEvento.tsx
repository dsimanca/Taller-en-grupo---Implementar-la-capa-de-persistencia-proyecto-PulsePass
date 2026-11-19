import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { Badge } from '../componentes/Badge'
import { Contenedor } from '../componentes/Contenedor'
import { Cargando } from '../componentes/Cargando'
import { EstadoError } from '../componentes/EstadoError'
import { Icono } from '../componentes/Icono'
import { ApiError } from '../lib/http'
import { formatearFechaHora } from '../lib/formato'
import { RUTAS } from '../lib/rutas'
import { useAuth } from '../funcionalidades/auth/AuthContext'
import { useEvento } from '../funcionalidades/eventos/hooks'
import { ModalConfirmarCompra } from '../funcionalidades/eventos/componentes/ModalConfirmarCompra'
import { PanelCompra } from '../funcionalidades/eventos/componentes/PanelCompra'
import { ESTILO_CATEGORIA } from '../funcionalidades/eventos/componentes/estilos'
import { AvatarArtista } from '../funcionalidades/artistas/componentes/AvatarArtista'
import { useComprarTicket } from '../funcionalidades/tickets/hooks'
import type { TicketType } from '../funcionalidades/tickets/tipos'

export function DetalleEvento() {
  const { eventCode = '' } = useParams()
  const { sesion } = useAuth()
  const navegar = useNavigate()
  const evento = useEvento(eventCode)
  const compra = useComprarTicket()
  const [tipoSeleccionado, setTipoSeleccionado] = useState<TicketType>('GENERAL')
  const [modalAbierto, setModalAbierto] = useState(false)
  const [errorCompra, setErrorCompra] = useState<string | null>(null)

  if (evento.isPending) return <Contenedor><Cargando mensaje="Cargando evento…" /></Contenedor>

  if (evento.isError) {
    const noDisponible = evento.error instanceof ApiError && [403, 404].includes(evento.error.status)
    if (noDisponible) {
      return (
        <Contenedor className="space-y-4 text-center">
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white">Evento no disponible</h1>
          <p className="text-slate-600 dark:text-slate-400">No encontramos este evento o no está publicado.</p>
          <Link to={RUTAS.eventos} className="text-marca-600 hover:underline">Volver a la cartelera</Link>
        </Contenedor>
      )
    }
    return (
      <Contenedor>
        <EstadoError mensaje="No pudimos cargar el evento." onReintentar={() => void evento.refetch()} />
      </Contenedor>
    )
  }

  const datos = evento.data
  const estilo = ESTILO_CATEGORIA[datos.category]
  const correo = sesion.estado === 'autenticado' ? sesion.usuario.email : ''

  function abrirCompra(tipo: TicketType) {
    if (sesion.estado !== 'autenticado') {
      navegar(RUTAS.login, { state: { desde: RUTAS.evento(datos.eventCode) } })
      return
    }
    setTipoSeleccionado(tipo)
    setErrorCompra(null)
    setModalAbierto(true)
  }

  async function confirmarCompra() {
    if (sesion.estado !== 'autenticado') return
    setErrorCompra(null)
    try {
      const ticket = await compra.mutateAsync({
        userEmail: sesion.usuario.email,
        eventCode: datos.eventCode,
        type: tipoSeleccionado,
      })
      setModalAbierto(false)
      navegar(RUTAS.confirmacionCompra(ticket.ticketCode))
    } catch (error) {
      setErrorCompra(error instanceof ApiError ? error.message : 'No pudimos completar la compra.')
    }
  }

  return (
    <Contenedor className="space-y-8 pb-28 md:pb-8">
      <Link to={RUTAS.eventos} className="inline-flex items-center gap-2 text-sm font-semibold text-marca-600 hover:underline">
        <Icono nombre="flechaIzquierda" className="size-4" /> Volver a la cartelera
      </Link>

      <div className="grid grid-cols-1 gap-8 lg:grid-cols-3">
        <article className="space-y-6 lg:col-span-2">
          <div className={`flex h-48 items-center justify-center rounded-2xl ${estilo.fondo} ${estilo.texto}`}>
            <Icono nombre={estilo.icono} className="size-20" />
          </div>
          <div>
            <div className="flex flex-wrap items-center gap-2">
              <Badge tono="neutro">{datos.category}</Badge>
              <Badge tono={datos.status}>{datos.status}</Badge>
            </div>
            <h1 className="mt-3 text-3xl font-extrabold text-slate-900 dark:text-white">{datos.name}</h1>
            <div className="mt-4 space-y-2 text-slate-600 dark:text-slate-300">
              <p className="flex items-center gap-2"><Icono nombre="calendario" className="size-5" /> {formatearFechaHora(datos.eventDate)}</p>
              <p className="flex items-center gap-2"><Icono nombre="escudo" className="size-5" /> Edad mínima: {datos.minimumAge} años</p>
            </div>
          </div>
          {datos.description && <p className="leading-relaxed text-slate-700 dark:text-slate-300">{datos.description}</p>}

          <section aria-labelledby="artistas-evento">
            <h2 id="artistas-evento" className="text-xl font-bold text-slate-900 dark:text-white">Artistas</h2>
            {datos.artists.length === 0 ? (
              <p className="mt-2 text-slate-500">Aún no hay artistas asociados.</p>
            ) : (
              <ul className="mt-3 flex flex-wrap gap-4">
                {datos.artists.map((artista) => (
                  <li key={artista.id}>
                    <Link to={RUTAS.artista(artista.id)} className="flex items-center gap-3 rounded-lg p-2 hover:bg-slate-100 dark:hover:bg-slate-800">
                      <AvatarArtista nombre={artista.stageName} />
                      <span className="font-medium text-slate-900 dark:text-white">{artista.stageName}</span>
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </section>

          <section aria-labelledby="sede-evento" className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900">
            <h2 id="sede-evento" className="text-xl font-bold text-slate-900 dark:text-white">Sede</h2>
            <Link to={RUTAS.sede(datos.venueCode)} className="mt-3 block rounded-lg p-2 hover:bg-slate-50 dark:hover:bg-slate-800">
              <p className="font-semibold text-slate-900 dark:text-white">{datos.venueName}</p>
              <p className="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-400"><Icono nombre="ubicacion" className="size-4" /> {datos.venueCode}</p>
            </Link>
          </section>
        </article>

        <aside className="lg:sticky lg:top-24 lg:self-start">
          {sesion.estado === 'autenticado' && !sesion.usuario.roles.includes('USER') ? (
            <div className="rounded-xl border border-slate-200 bg-white p-5 text-sm text-slate-600 dark:border-slate-800 dark:bg-slate-900 dark:text-slate-300">
              La compra está disponible para cuentas con rol USER.
            </div>
          ) : (
            <PanelCompra evento={datos} sesion={sesion} cargando={compra.isPending} onComprar={abrirCompra} />
          )}
        </aside>
      </div>

      <ModalConfirmarCompra
        abierto={modalAbierto}
        evento={datos}
        tipo={tipoSeleccionado}
        correo={correo}
        cargando={compra.isPending}
        error={errorCompra}
        onCerrar={() => { if (!compra.isPending) setModalAbierto(false) }}
        onConfirmar={() => void confirmarCompra()}
      />
    </Contenedor>
  )
}
