import { Link } from 'react-router'
import { Contenedor } from '../componentes/Contenedor'
import { Icono } from '../componentes/Icono'
import { RUTAS } from '../lib/rutas'

export function SinPermisos() {
  return (
    <Contenedor className="flex min-h-[60vh] flex-col items-center justify-center text-center">
      <span className="rounded-full bg-red-100 p-4 text-red-600 dark:bg-red-950">
        <Icono nombre="escudo" className="size-10" />
      </span>
      <p className="mt-4 text-6xl font-extrabold text-red-600">403</p>
      <h1 className="mt-2 text-2xl font-bold text-slate-900 dark:text-white">
        No tienes permisos para acceder a esta sección
      </h1>
      <p className="mt-2 text-slate-600 dark:text-slate-400">
        Tu cuenta no cuenta con el rol necesario para ver este contenido.
      </p>
      <Link
        to={RUTAS.inicio}
        className="mt-8 inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-marca-600 px-5 py-2 font-semibold text-white hover:bg-marca-700"
      >
        Volver al inicio
      </Link>
    </Contenedor>
  )
}
