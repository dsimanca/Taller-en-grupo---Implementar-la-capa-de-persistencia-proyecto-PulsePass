import { Link } from 'react-router'
import { Contenedor } from '../componentes/Contenedor'
import { Cargando } from '../componentes/Cargando'
import { EstadoVacio } from '../componentes/EstadoVacio'
import { EstadoError } from '../componentes/EstadoError'
import { RUTAS } from '../lib/rutas'
import { TarjetaSede } from '../funcionalidades/sedes/componentes/TarjetaSede'
import { useSedesActivas } from '../funcionalidades/sedes/hooks'

export function ListaSedes() {
  const sedes = useSedesActivas()
  return (
    <Contenedor className="space-y-6">
      <header>
        <h1 className="text-3xl font-bold text-slate-900 dark:text-white">Sedes</h1>
        <p className="mt-1 text-slate-600 dark:text-slate-400">Lugares donde vive la experiencia PulsePass.</p>
      </header>
      {sedes.isPending && <Cargando mensaje="Cargando sedes…" />}
      {sedes.isError && <EstadoError mensaje="No pudimos cargar las sedes." onReintentar={() => void sedes.refetch()} />}
      {sedes.isSuccess && sedes.data.length === 0 && <EstadoVacio mensaje="Aún no hay sedes activas." icono="ubicacion" />}
      {sedes.isSuccess && sedes.data.length > 0 && (
        <ul className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {sedes.data.map((sede) => (
            <li key={sede.code}>
              <Link to={RUTAS.sede(sede.code)} className="block focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-600 rounded-xl">
                <TarjetaSede sede={sede} />
              </Link>
            </li>
          ))}
        </ul>
      )}
    </Contenedor>
  )
}
