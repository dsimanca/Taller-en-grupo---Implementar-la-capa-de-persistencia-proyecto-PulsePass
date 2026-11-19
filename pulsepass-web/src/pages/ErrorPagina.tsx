import { isRouteErrorResponse, Link, useRouteError } from 'react-router'
import { Boton } from '../componentes/Boton'
import { Contenedor } from '../componentes/Contenedor'
import { Icono } from '../componentes/Icono'
import { RUTAS } from '../lib/rutas'

export function ErrorPagina() {
  const error = useRouteError()

  let titulo = '¡Ups! Algo salió mal.'
  let mensaje = 'Ocurrió un error inesperado. Inténtalo más tarde.'
  if (isRouteErrorResponse(error)) {
    titulo = `Error ${error.status}`
    mensaje = 'No pudimos mostrar esta página.'
  }

  return (
    <Contenedor className="flex min-h-[60vh] flex-col items-center justify-center text-center">
      <Icono nombre="alerta" className="size-14 text-red-500" />
      <h1 className="mt-4 text-3xl font-bold text-slate-900 dark:text-white">{titulo}</h1>
      <p className="mt-2 text-slate-600 dark:text-slate-400">{mensaje}</p>
      <div className="mt-8 flex flex-wrap justify-center gap-3">
        <Boton onClick={() => window.location.reload()}>Reintentar</Boton>
        <Link
          to={RUTAS.inicio}
          className="inline-flex min-h-11 items-center justify-center rounded-lg border border-slate-300 px-4 py-2 font-semibold text-slate-700 hover:bg-slate-50 dark:border-slate-700 dark:text-slate-200 dark:hover:bg-slate-800"
        >
          Volver al inicio
        </Link>
      </div>
    </Contenedor>
  )
}

