import { Boton } from '../../../componentes/Boton'
import { Campo } from '../../../componentes/Campo'
import { Selector } from '../../../componentes/Selector'
import { EventCategorySchema } from '../tipos'

export type FiltrosEvento = { q: string; categoria: string; artista: string }

export function BarraFiltros({
  filtros,
  onCambiar,
  onLimpiar,
  total,
}: {
  filtros: FiltrosEvento
  onCambiar: (campo: keyof FiltrosEvento, valor: string) => void
  onLimpiar: () => void
  total: number
}) {
  return (
    <section aria-label="Filtros de cartelera" className="space-y-3">
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <Campo
          etiqueta="Buscar por nombre"
          placeholder="Buscar por nombre…"
          value={filtros.q}
          onChange={(evento) => onCambiar('q', evento.target.value)}
        />
        <Selector
          etiqueta="Categoría"
          value={filtros.categoria}
          onChange={(evento) => onCambiar('categoria', evento.target.value)}
        >
          <option value="">Todas</option>
          {EventCategorySchema.options.map((categoria) => (
            <option key={categoria} value={categoria}>
              {categoria}
            </option>
          ))}
        </Selector>
        <Campo
          etiqueta="Artista"
          placeholder="Todos"
          value={filtros.artista}
          onChange={(evento) => onCambiar('artista', evento.target.value)}
        />
        <div className="flex items-end">
          <Boton variante="secundario" onClick={onLimpiar} className="w-full">
            Limpiar
          </Boton>
        </div>
      </div>
      <p className="text-sm text-slate-500 dark:text-slate-400" aria-live="polite">
        {total} {total === 1 ? 'evento' : 'eventos'}
      </p>
    </section>
  )
}
