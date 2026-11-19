import clsx from 'clsx'
import type { TicketStatus } from '../tipos'

export type FiltroEstadoTicket = 'TODOS' | Extract<TicketStatus, 'PAID' | 'USED' | 'CANCELLED'>

const FILTROS: { valor: FiltroEstadoTicket; etiqueta: string }[] = [
  { valor: 'TODOS', etiqueta: 'Todos' },
  { valor: 'PAID', etiqueta: 'Pagados' },
  { valor: 'USED', etiqueta: 'Usados' },
  { valor: 'CANCELLED', etiqueta: 'Cancelados' },
]

export function FiltroChips({
  valor,
  onCambiar,
}: {
  valor: FiltroEstadoTicket
  onCambiar: (filtro: FiltroEstadoTicket) => void
}) {
  return (
    <div role="group" aria-label="Filtrar tickets por estado" className="flex flex-wrap gap-2">
      {FILTROS.map((filtro) => (
        <button
          key={filtro.valor}
          type="button"
          aria-pressed={valor === filtro.valor}
          onClick={() => onCambiar(filtro.valor)}
          className={clsx(
            'min-h-10 rounded-full border px-4 text-sm font-medium transition',
            valor === filtro.valor
              ? 'border-marca-600 bg-marca-600 text-white'
              : 'border-slate-300 bg-white text-slate-600 hover:bg-slate-50 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300 dark:hover:bg-slate-800',
          )}
        >
          {filtro.etiqueta}
        </button>
      ))}
    </div>
  )
}
