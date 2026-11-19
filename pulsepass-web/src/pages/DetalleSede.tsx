import { Link, useParams } from 'react-router'
import { Badge } from '../componentes/Badge'
import { Contenedor } from '../componentes/Contenedor'
import { Cargando } from '../componentes/Cargando'
import { EstadoError } from '../componentes/EstadoError'
import { Icono } from '../componentes/Icono'
import { CodigoTexto } from '../componentes/CodigoTexto'
import { ApiError } from '../lib/http'
import { formatearNumero } from '../lib/formato'
import { RUTAS } from '../lib/rutas'
import { useSede } from '../funcionalidades/sedes/hooks'
import { NoEncontrada } from './NoEncontrada'

export function DetalleSede() {
  const { code = '' } = useParams()
  const sede = useSede(code)

  if (sede.isPending) return <Contenedor><Cargando mensaje="Cargando sede…" /></Contenedor>
  if (sede.isError) {
    if (sede.error instanceof ApiError && sede.error.status === 404) return <NoEncontrada />
    return <Contenedor><EstadoError mensaje="No pudimos cargar la sede." onReintentar={() => void sede.refetch()} /></Contenedor>
  }

  const datos = sede.data
  return (
    <Contenedor className="max-w-3xl space-y-6">
      <Link to={RUTAS.sedes} className="inline-flex items-center gap-2 text-sm font-semibold text-marca-600 hover:underline">
        <Icono nombre="flechaIzquierda" className="size-4" /> Sedes
      </Link>
      <article className="rounded-2xl border border-slate-200 bg-white p-8 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <h1 className="text-3xl font-bold text-slate-900 dark:text-white">{datos.name}</h1>
            <CodigoTexto className="text-slate-500 dark:text-slate-400">{datos.code}</CodigoTexto>
          </div>
          <Badge tono={datos.active ? 'exito' : 'neutro'}>{datos.active ? 'Activa' : 'Inactiva'}</Badge>
        </div>
        <dl className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-2">
          <div><dt className="text-sm text-slate-500 dark:text-slate-400">Ciudad</dt><dd className="mt-1 flex items-center gap-2 font-medium"><Icono nombre="ubicacion" className="size-4 text-marca-600" /> {datos.city}</dd></div>
          <div><dt className="text-sm text-slate-500 dark:text-slate-400">Capacidad</dt><dd className="mt-1 flex items-center gap-2 font-medium"><Icono nombre="usuario" className="size-4 text-marca-600" /> {formatearNumero(datos.capacity)} personas</dd></div>
          {datos.address && <div className="sm:col-span-2"><dt className="text-sm text-slate-500 dark:text-slate-400">Dirección</dt><dd className="mt-1 font-medium">{datos.address}</dd></div>}
        </dl>
      </article>
    </Contenedor>
  )
}
