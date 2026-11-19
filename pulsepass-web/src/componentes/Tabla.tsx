import type { ReactNode } from 'react'
import { EstadoVacio } from './EstadoVacio'

export type Columna<T> = {
  encabezado: string
  celda: (fila: T) => ReactNode
}

export function Tabla<T>({
  columnas,
  filas,
  obtenerClave,
  mensajeVacio = 'No hay datos para mostrar.',
}: {
  columnas: Columna<T>[]
  filas: T[]
  obtenerClave: (fila: T) => string
  mensajeVacio?: string
}) {
  if (filas.length === 0) return <EstadoVacio mensaje={mensajeVacio} />

  return (
    <>
      <div className="hidden overflow-x-auto rounded-xl border border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900 md:block">
        <table className="w-full text-left text-sm">
          <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500 dark:bg-slate-800 dark:text-slate-400">
            <tr>
              {columnas.map((columna) => (
                <th key={columna.encabezado} scope="col" className="px-4 py-3 font-semibold">
                  {columna.encabezado}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200 dark:divide-slate-800">
            {filas.map((fila) => (
              <tr
                key={obtenerClave(fila)}
                className="odd:bg-white even:bg-slate-50 hover:bg-marca-50 dark:odd:bg-slate-900 dark:even:bg-slate-950 dark:hover:bg-slate-800"
              >
                {columnas.map((columna) => (
                  <td key={columna.encabezado} className="px-4 py-3 align-middle">
                    {columna.celda(fila)}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <ul className="space-y-3 md:hidden">
        {filas.map((fila) => (
          <li
            key={obtenerClave(fila)}
            className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900"
          >
            {columnas.map((columna) => (
              <div key={columna.encabezado} className="flex items-center justify-between gap-4 py-1">
                <span className="text-xs font-semibold uppercase text-slate-500 dark:text-slate-400">
                  {columna.encabezado}
                </span>
                <span className="text-right text-sm text-slate-900 dark:text-slate-100">
                  {columna.celda(fila)}
                </span>
              </div>
            ))}
          </li>
        ))}
      </ul>
    </>
  )
}
