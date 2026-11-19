import type { ReactNode } from 'react'
import { Icono, type NombreIcono } from './Icono'

export function EstadoVacio({
  mensaje,
  accion,
  icono = 'boleto',
}: {
  mensaje: string
  accion?: ReactNode
  icono?: NombreIcono
}) {
  return (
    <div className="flex flex-col items-center gap-3 rounded-xl border border-dashed border-slate-300 bg-white px-6 py-12 text-center dark:border-slate-700 dark:bg-slate-900">
      <span className="rounded-full bg-marca-50 p-3 text-marca-600 dark:bg-slate-800 dark:text-marca-500">
        <Icono nombre={icono} className="size-7" />
      </span>
      <p className="text-slate-600 dark:text-slate-300">{mensaje}</p>
      {accion}
    </div>
  )
}
