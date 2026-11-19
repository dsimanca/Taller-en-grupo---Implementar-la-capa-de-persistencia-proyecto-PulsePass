import { Link } from 'react-router'
import { RUTAS } from '../lib/rutas'

export function Footer() {
  return (
    <footer className="border-t border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-950">
      <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-4 px-4 py-8 text-sm text-slate-500 sm:flex-row sm:px-6 lg:px-8 dark:text-slate-400">
        <p>© 2026 PulsePass · Caso de estudio académico</p>
        <nav aria-label="Secundaria" className="flex gap-4">
          <Link to={RUTAS.sedes} className="hover:text-marca-600">
            Sedes
          </Link>
          <Link to={RUTAS.artistas} className="hover:text-marca-600">
            Artistas
          </Link>
        </nav>
      </div>
    </footer>
  )
}
