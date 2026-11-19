import { Link } from 'react-router'
import { Badge } from '../../../componentes/Badge'
import { Icono } from '../../../componentes/Icono'
import { formatearFecha } from '../../../lib/formato'
import { RUTAS } from '../../../lib/rutas'
import type { EventSummaryResponse } from '../tipos'
import { ESTILO_CATEGORIA } from './estilos'

export function TarjetaEvento({ evento }: { evento: EventSummaryResponse }) {
  const estilo = ESTILO_CATEGORIA[evento.category]

  return (
    <Link
      to={RUTAS.evento(evento.eventCode)}
      className="group flex h-full flex-col overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-lg focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-600 dark:border-slate-800 dark:bg-slate-900"
    >
      <div className={`flex h-32 items-center justify-center ${estilo.fondo} ${estilo.texto}`}>
        <Icono nombre={estilo.icono} className="size-14" />
      </div>
      <div className="flex flex-1 flex-col gap-3 p-4">
        <div className="flex items-center justify-between gap-2">
          <span className="text-xs font-bold tracking-wide uppercase">{estilo.etiqueta}</span>
          <Badge tono={evento.status}>{evento.status}</Badge>
        </div>
        <h3 className="text-lg leading-snug font-semibold text-slate-900 group-hover:text-marca-600 dark:text-white">
          {evento.name}
        </h3>
        <div className="mt-auto space-y-1 text-sm text-slate-600 dark:text-slate-400">
          <p className="flex items-center gap-2">
            <Icono nombre="calendario" className="size-4" />
            {formatearFecha(evento.eventDate)}
          </p>
          <p className="flex items-center gap-2">
            <Icono nombre="ubicacion" className="size-4" />
            {evento.venueName ?? 'Sede por confirmar'}
          </p>
        </div>
      </div>
    </Link>
  )
}
