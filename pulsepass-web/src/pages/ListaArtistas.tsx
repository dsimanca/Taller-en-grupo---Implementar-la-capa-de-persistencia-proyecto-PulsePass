import { Contenedor } from '../componentes/Contenedor'
import { Cargando } from '../componentes/Cargando'
import { EstadoVacio } from '../componentes/EstadoVacio'
import { EstadoError } from '../componentes/EstadoError'
import { TarjetaArtista } from '../funcionalidades/artistas/componentes/TarjetaArtista'
import { useArtistasActivos } from '../funcionalidades/artistas/hooks'

export function ListaArtistas() {
  const artistas = useArtistasActivos()
  return (
    <Contenedor className="space-y-6">
      <header>
        <h1 className="text-3xl font-bold text-slate-900 dark:text-white">Artistas</h1>
        <p className="mt-1 text-slate-600 dark:text-slate-400">Conoce a los artistas activos de PulsePass.</p>
      </header>
      {artistas.isPending && <Cargando mensaje="Cargando artistas…" />}
      {artistas.isError && <EstadoError mensaje="No pudimos cargar los artistas." onReintentar={() => void artistas.refetch()} />}
      {artistas.isSuccess && artistas.data.length === 0 && <EstadoVacio mensaje="Aún no hay artistas activos." icono="usuario" />}
      {artistas.isSuccess && artistas.data.length > 0 && (
        <ul className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
          {artistas.data.map((artista) => (
            <li key={artista.id}><TarjetaArtista artista={artista} /></li>
          ))}
        </ul>
      )}
    </Contenedor>
  )
}
