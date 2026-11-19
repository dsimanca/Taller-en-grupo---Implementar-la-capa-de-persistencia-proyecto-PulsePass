import { Link } from 'react-router'
import { Contenedor } from '../componentes/Contenedor'
import { Icono } from '../componentes/Icono'
import { RUTAS } from '../lib/rutas'

export function NoEncontrada() {
  return (
    <Contenedor className="flex min-h-[60vh] flex-col items-center justify-center text-center">
      <p className="text-6xl font-extrabold text-marca-600">404</p>
      <h1 className="mt-2 text-2xl font-bold text-slate-900 dark:text-white">
        No encontramos esta página
      </h1>
      <p className="mt-2 text-slate-600 dark:text-slate-400">
        La dirección que abriste no existe o fue movida.
      </p>
      <Link
        to={RUTAS.inicio}
        className="mt-8 inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-marca-600 px-5 py-2 font-semibold text-white hover:bg-marca-700"
      >
        <Icono nombre="flechaIzquierda" /> Volver al inicio
      </Link>
    </Contenedor>
  )
}
