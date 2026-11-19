import { Link, useParams } from 'react-router'
import { Badge } from '../componentes/Badge'
import { Contenedor } from '../componentes/Contenedor'
import { Cargando } from '../componentes/Cargando'
import { EstadoVacio } from '../componentes/EstadoVacio'
import { EstadoError } from '../componentes/EstadoError'
import { Icono } from '../componentes/Icono'
import { ApiError } from '../lib/http'
import { RUTAS } from '../lib/rutas'
import { AvatarArtista } from '../funcionalidades/artistas/componentes/AvatarArtista'
import { useArtista } from '../funcionalidades/artistas/hooks'
import { useEventosPorArtista } from '../funcionalidades/eventos/hooks'
import { TarjetaEvento } from '../funcionalidades/eventos/componentes/TarjetaEvento'
import { NoEncontrada } from './NoEncontrada'

export function DetalleArtista() {
  const { id } = useParams()
  const idNumero = Number(id)
  const invalido = !Number.isFinite(idNumero) || idNumero <= 0
  const artista = useArtista(invalido ? null : idNumero)
  const eventos = useEventosPorArtista(artista.data?.stageName ?? '')

  if (invalido) return <NoEncontrada />
  if (artista.isPending) return <Contenedor><Cargando mensaje="Cargando artista…" /></Contenedor>
  if (artista.isError) {
    if (artista.error instanceof ApiError && artista.error.status === 404) return <NoEncontrada />
    return <Contenedor><EstadoError mensaje="No pudimos cargar el artista." onReintentar={() => void artista.refetch()} /></Contenedor>
  }

  const datos = artista.data

  return (
    <Contenedor className="space-y-8">
      <Link to={RUTAS.artistas} className="inline-flex items-center gap-2 text-sm font-semibold text-marca-600 hover:underline">
        <Icono nombre="flechaIzquierda" className="size-4" /> Artistas
      </Link>

      <header className="flex flex-col items-center gap-4 rounded-2xl border border-slate-200 bg-white p-8 text-center sm:flex-row sm:text-left dark:border-slate-800 dark:bg-slate-900">
        <AvatarArtista nombre={datos.stageName} className="size-24 text-2xl" />
        <div>
          <h1 className="text-3xl font-bold text-slate-900 dark:text-white">{datos.stageName}</h1>
          <p className="mt-1 text-slate-600 dark:text-slate-400">{datos.genre ?? 'Género no especificado'} · {datos.country ?? 'Origen no especificado'}</p>
          <div className="mt-2"><Badge tono={datos.active ? 'exito' : 'neutro'}>{datos.active ? 'Activo' : 'Inactivo'}</Badge></div>
        </div>
      </header>

      <section aria-labelledby="eventos-artista">
        <h2 id="eventos-artista" className="text-2xl font-bold text-slate-900 dark:text-white">Próximos eventos de {datos.stageName}</h2>
        <div className="mt-4">
          {eventos.isPending && <Cargando mensaje="Cargando eventos…" />}
          {eventos.isError && <EstadoError mensaje="No pudimos cargar los eventos." onReintentar={() => void eventos.refetch()} />}
          {eventos.isSuccess && eventos.data.length === 0 && <EstadoVacio mensaje="Sin eventos próximos." icono="calendario" />}
          {eventos.isSuccess && eventos.data.length > 0 && (
            <ul className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {eventos.data.map((evento) => <li key={evento.eventCode}><TarjetaEvento evento={evento} /></li>)}
            </ul>
          )}
        </div>
      </section>
    </Contenedor>
  )
}
