import { Link } from 'react-router'
import { Contenedor } from '../componentes/Contenedor'
import { Cargando } from '../componentes/Cargando'
import { EstadoVacio } from '../componentes/EstadoVacio'
import { EstadoError } from '../componentes/EstadoError'
import { Icono } from '../componentes/Icono'
import { RUTAS } from '../lib/rutas'
import { useAuth } from '../funcionalidades/auth/AuthContext'
import { useEventosPublicados } from '../funcionalidades/eventos/hooks'
import { useArtistasActivos } from '../funcionalidades/artistas/hooks'
import { TarjetaEvento } from '../funcionalidades/eventos/componentes/TarjetaEvento'
import { TarjetaArtista } from '../funcionalidades/artistas/componentes/TarjetaArtista'

export function Inicio() {
  const { sesion } = useAuth()
  const eventos = useEventosPublicados()
  const artistas = useArtistasActivos()

  return (
    <>
      <section className="bg-linear-to-r from-marca-600 to-acento-500 px-6 py-16 text-center text-white md:py-24">
        <div className="mx-auto max-w-4xl">
          <h1 className="text-3xl font-extrabold tracking-tight md:text-5xl">
            Descubre los mejores eventos
          </h1>
          <p className="mx-auto mt-4 max-w-xl text-lg text-indigo-100">
            Música, cultura, deporte y tecnología en un solo lugar.
          </p>
          <div className="mt-8 flex flex-col items-center justify-center gap-3 sm:flex-row">
            <Link
              to={RUTAS.eventos}
              className="inline-flex min-h-11 w-full items-center justify-center gap-2 rounded-lg bg-white px-6 py-3 font-semibold text-marca-700 hover:bg-indigo-50 sm:w-auto"
            >
              <Icono nombre="boleto" /> Ver cartelera
            </Link>
            {sesion.estado !== 'autenticado' && (
              <Link
                to={RUTAS.registro}
                className="inline-flex min-h-11 w-full items-center justify-center gap-2 rounded-lg border border-white/70 px-6 py-3 font-semibold text-white hover:bg-white/10 sm:w-auto"
              >
                Crear cuenta
              </Link>
            )}
          </div>
        </div>
      </section>

      <Contenedor className="space-y-14">
        <section aria-labelledby="titulo-eventos">
          <div className="mb-6 flex items-end justify-between gap-4">
            <h2 id="titulo-eventos" className="text-2xl font-bold text-slate-900 dark:text-white">
              Próximos eventos
            </h2>
            <Link
              to={RUTAS.eventos}
              className="inline-flex items-center gap-1 text-sm font-semibold text-marca-600 hover:text-marca-700"
            >
              Ver todos <Icono nombre="flechaDerecha" className="size-4" />
            </Link>
          </div>
          {eventos.isPending && <Cargando mensaje="Cargando eventos…" />}
          {eventos.isError && (
            <EstadoError
              mensaje="No pudimos cargar los eventos."
              onReintentar={() => void eventos.refetch()}
            />
          )}
          {eventos.isSuccess && eventos.data.length === 0 && (
            <EstadoVacio mensaje="Aún no hay eventos publicados." icono="calendario" />
          )}
          {eventos.isSuccess && eventos.data.length > 0 && (
            <ul className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {eventos.data.slice(0, 3).map((evento) => (
                <li key={evento.eventCode}>
                  <TarjetaEvento evento={evento} />
                </li>
              ))}
            </ul>
          )}
        </section>

        <section aria-labelledby="titulo-artistas">
          <div className="mb-6 flex items-end justify-between gap-4">
            <h2 id="titulo-artistas" className="text-2xl font-bold text-slate-900 dark:text-white">
              Artistas destacados
            </h2>
            <Link
              to={RUTAS.artistas}
              className="inline-flex items-center gap-1 text-sm font-semibold text-marca-600 hover:text-marca-700"
            >
              Ver todos <Icono nombre="flechaDerecha" className="size-4" />
            </Link>
          </div>
          {artistas.isPending && <Cargando mensaje="Cargando artistas…" />}
          {artistas.isError && (
            <EstadoError
              mensaje="No pudimos cargar los artistas."
              onReintentar={() => void artistas.refetch()}
            />
          )}
          {artistas.isSuccess && artistas.data.length === 0 && (
            <EstadoVacio mensaje="Aún no hay artistas activos." icono="usuario" />
          )}
          {artistas.isSuccess && artistas.data.length > 0 && (
            <ul className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-6">
              {artistas.data.slice(0, 6).map((artista) => (
                <li key={artista.id}>
                  <TarjetaArtista artista={artista} />
                </li>
              ))}
            </ul>
          )}
        </section>
      </Contenedor>
    </>
  )
}
