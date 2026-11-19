import { useState } from 'react'
import { Link, useParams } from 'react-router'
import { Badge } from '../componentes/Badge'
import { Boton } from '../componentes/Boton'
import { Contenedor } from '../componentes/Contenedor'
import { Cargando } from '../componentes/Cargando'
import { EstadoError } from '../componentes/EstadoError'
import { Icono } from '../componentes/Icono'
import { Modal } from '../componentes/Modal'
import { Selector } from '../componentes/Selector'
import { Tarjeta } from '../componentes/Tarjeta'
import { CodigoTexto } from '../componentes/CodigoTexto'
import { useAviso } from '../componentes/Aviso'
import { ApiError } from '../lib/http'
import { formatearFechaHora } from '../lib/formato'
import { RUTAS } from '../lib/rutas'
import { AvatarArtista } from '../funcionalidades/artistas/componentes/AvatarArtista'
import { useArtistasActivos } from '../funcionalidades/artistas/hooks'
import {
  useAsociarArtista,
  useEvento,
  usePublicarEvento,
} from '../funcionalidades/eventos/hooks'

export function AdminGestionEvento() {
  const { eventCode = '' } = useParams()
  const aviso = useAviso()
  const evento = useEvento(eventCode)
  const artistas = useArtistasActivos()
  const publicar = usePublicarEvento()
  const asociar = useAsociarArtista()
  const [confirmarPublicar, setConfirmarPublicar] = useState(false)
  const [artistaSeleccionado, setArtistaSeleccionado] = useState('')
  const [errorAccion, setErrorAccion] = useState<string | null>(null)

  if (evento.isPending) return <Contenedor><Cargando mensaje="Cargando evento…" /></Contenedor>
  if (evento.isError) {
    if (evento.error instanceof ApiError && evento.error.status === 404) {
      return (
        <Contenedor className="space-y-4 text-center">
          <h1 className="text-2xl font-bold">Evento no disponible</h1>
          <Link to={RUTAS.adminEventos} className="text-marca-600 hover:underline">Volver a administración</Link>
        </Contenedor>
      )
    }
    return <Contenedor><EstadoError mensaje="No pudimos cargar el evento." onReintentar={() => void evento.refetch()} /></Contenedor>
  }

  const datos = evento.data
  const idsAsociados = new Set(datos.artists.map((artista) => artista.id))
  const disponibles = (artistas.data ?? []).filter((artista) => artista.active && !idsAsociados.has(artista.id))

  async function confirmarPublicacion() {
    setErrorAccion(null)
    try {
      await publicar.mutateAsync(datos.eventCode)
      setConfirmarPublicar(false)
      aviso.mostrar('exito', 'Evento publicado correctamente.')
      try {
        const clave = 'pulsepass.admin.creados'
        const existentes = JSON.parse(sessionStorage.getItem(clave) ?? '[]') as Array<{ eventCode: string; name: string; status: string }>
        sessionStorage.setItem(clave, JSON.stringify(existentes.map((item) => item.eventCode === datos.eventCode ? { ...item, status: 'PUBLISHED' } : item)))
      } catch { /* auxiliar */ }
    } catch (error) {
      setErrorAccion(error instanceof ApiError ? error.message : 'No pudimos publicar el evento.')
    }
  }

  async function asociarArtista() {
    const artistId = Number(artistaSeleccionado)
    if (!Number.isFinite(artistId)) return
    setErrorAccion(null)
    try {
      await asociar.mutateAsync({ eventCode: datos.eventCode, artistId })
      setArtistaSeleccionado('')
      aviso.mostrar('exito', 'Artista asociado correctamente.')
    } catch (error) {
      setErrorAccion(error instanceof ApiError ? error.message : 'No pudimos asociar el artista.')
    }
  }

  return (
    <Contenedor estrecho className="space-y-6">
      <Link to={RUTAS.adminEventos} className="text-sm font-semibold text-marca-600 hover:underline">← Eventos</Link>

      <header className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <h1 className="text-2xl font-bold text-slate-900 dark:text-white">{datos.name}</h1>
            <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">
              <CodigoTexto>{datos.eventCode}</CodigoTexto> · {datos.category} · {formatearFechaHora(datos.eventDate)} · Mín. {datos.minimumAge} · {datos.venueName}
            </p>
          </div>
          <Badge tono={datos.status}>{datos.status}</Badge>
        </div>
      </header>

      {errorAccion && (
        <p role="alert" className="rounded-lg bg-red-100 px-3 py-2 text-sm text-red-800">{errorAccion}</p>
      )}

      <Tarjeta className="space-y-4 p-6">
        <h2 className="text-lg font-bold text-slate-900 dark:text-white">Publicación</h2>
        <p className="text-sm text-slate-600 dark:text-slate-400">
          El evento es visible al público una vez publicado.
        </p>
        <Boton
          onClick={() => { setErrorAccion(null); setConfirmarPublicar(true) }}
          disabled={datos.status !== 'DRAFT'}
        >
          Publicar evento
        </Boton>
      </Tarjeta>

      <Tarjeta className="space-y-4 p-6">
        <h2 className="text-lg font-bold text-slate-900 dark:text-white">Artistas asociados</h2>
        {datos.artists.length === 0 ? (
          <p className="text-sm text-slate-500">Aún no hay artistas asociados.</p>
        ) : (
          <ul className="space-y-2">
            {datos.artists.map((artista) => (
              <li key={artista.id} className="flex items-center gap-3">
                <AvatarArtista nombre={artista.stageName} className="size-10" />
                <span className="font-medium text-slate-900 dark:text-white">{artista.stageName}</span>
              </li>
            ))}
          </ul>
        )}

        <div className="border-t border-slate-200 pt-4 dark:border-slate-800">
          <h3 className="mb-3 text-sm font-semibold text-slate-700 dark:text-slate-300">Agregar artista</h3>
          <div className="flex flex-col gap-3 sm:flex-row sm:items-end">
            <div className="flex-1">
              <Selector
                etiqueta="Artista activo"
                value={artistaSeleccionado}
                onChange={(evento) => setArtistaSeleccionado(evento.target.value)}
              >
                <option value="">Selecciona un artista</option>
                {disponibles.map((artista) => (
                  <option key={artista.id} value={String(artista.id)}>{artista.stageName}</option>
                ))}
              </Selector>
            </div>
            <Boton
              variante="secundario"
              onClick={() => void asociarArtista()}
              cargando={asociar.isPending}
              disabled={artistaSeleccionado.length === 0}
            >
              Asociar
            </Boton>
          </div>
        </div>
      </Tarjeta>

      <Tarjeta className="p-6">
        <Link
          to={RUTAS.ticketsPagados(datos.eventCode)}
          className="inline-flex items-center gap-2 font-semibold text-marca-600 hover:underline"
        >
          Tickets pagados <Icono nombre="flechaDerecha" className="size-4" />
        </Link>
      </Tarjeta>

      <Modal abierto={confirmarPublicar} titulo="Publicar evento" onCerrar={() => !publicar.isPending && setConfirmarPublicar(false)}>
        <p className="text-sm text-slate-600 dark:text-slate-300">
          ¿Deseas publicar <span className="font-semibold">{datos.name}</span>? El evento será visible en la cartelera pública.
        </p>
        <div className="mt-6 flex justify-end gap-3">
          <Boton variante="secundario" onClick={() => setConfirmarPublicar(false)}>Cancelar</Boton>
          <Boton onClick={() => void confirmarPublicacion()} cargando={publicar.isPending}>Publicar evento</Boton>
        </div>
      </Modal>
    </Contenedor>
  )
}
