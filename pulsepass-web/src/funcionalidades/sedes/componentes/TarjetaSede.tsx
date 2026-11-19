import { Badge } from '../../../componentes/Badge'
import { Icono } from '../../../componentes/Icono'
import { formatearNumero } from '../../../lib/formato'
import type { Sede } from '../tipos'

export function TarjetaSede({ sede }: { sede: Sede }) {
  return (
    <article className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition hover:shadow-md dark:border-slate-800 dark:bg-slate-900">
      <div className="flex items-start justify-between gap-3">
        <h3 className="font-semibold text-slate-900 dark:text-white">{sede.name}</h3>
        <Badge tono={sede.active ? 'exito' : 'neutro'}>
          {sede.active ? 'Activa' : 'Inactiva'}
        </Badge>
      </div>
      <p className="mt-1 font-mono text-xs text-slate-500 dark:text-slate-400">{sede.code}</p>
      <div className="mt-4 space-y-1 text-sm text-slate-600 dark:text-slate-400">
        <p className="flex items-center gap-2">
          <Icono nombre="ubicacion" className="size-4" />
          {sede.city}
        </p>
        <p className="flex items-center gap-2">
          <Icono nombre="usuario" className="size-4" />
          Capacidad {formatearNumero(sede.capacity)} personas
        </p>
      </div>
    </article>
  )
}
