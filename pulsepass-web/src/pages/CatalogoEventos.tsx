import { useMemo } from 'react'
import { useSearchParams } from 'react-router'
import { Contenedor } from '../componentes/Contenedor'
import { Cargando } from '../componentes/Cargando'
import { EstadoVacio } from '../componentes/EstadoVacio'
import { EstadoError } from '../componentes/EstadoError'
import { BarraFiltros } from '../funcionalidades/eventos/componentes/BarraFiltros'
import { TarjetaEvento } from '../funcionalidades/eventos/componentes/TarjetaEvento'
import { useEventosPorArtista, useEventosPublicados } from '../funcionalidades/eventos/hooks'

export function CatalogoEventos() {
  const [params, setParams] = useSearchParams()
  const q = params.get('q') ?? ''
  const categoria = params.get('categoria') ?? ''
  const artista = params.get('artista') ?? ''

  const porArtista = artista.trim().length > 0
  const publicados = useEventosPublicados()
  const porNombreArtista = useEventosPorArtista(artista.trim())
  const consulta = porArtista ? porNombreArtista : publicados

  const filtrados = useMemo(() => {
    if (!consulta.data) return []
    const texto = q.trim().toLowerCase()
    return consulta.data.filter((evento) => {
      const coincideTexto = texto.length === 0 || evento.name.toLowerCase().includes(texto)
      const coincideCategoria = categoria.length === 0 || evento.category === categoria
      return coincideTexto && coincideCategoria
    })
  }, [consulta.data, q, categoria])

  function actualizar(campo: 'q' | 'categoria' | 'artista', valor: string) {
    const siguientes = new URLSearchParams(params)
    if (valor) siguientes.set(campo, valor)
    else siguientes.delete(campo)
    setParams(siguientes, { replace: true })
  }

  return (
    <Contenedor className="space-y-6">
      <header>
        <h1 className="text-3xl font-bold text-slate-900 dark:text-white">Cartelera</h1>
        <p className="mt-1 text-slate-600 dark:text-slate-400">
          Explora los eventos publicados y encuentra tu próxima experiencia.
        </p>
      </header>

      <BarraFiltros
        filtros={{ q, categoria, artista }}
        onCambiar={actualizar}
        onLimpiar={() => setParams({}, { replace: true })}
        total={filtrados.length}
      />

      {consulta.isPending && <Cargando mensaje="Cargando cartelera…" />}
      {consulta.isError && (
        <EstadoError
          mensaje="No pudimos cargar la cartelera."
          onReintentar={() => void consulta.refetch()}
        />
      )}
      {consulta.isSuccess && filtrados.length === 0 && (
        <EstadoVacio mensaje="Sin resultados para tu búsqueda." icono="buscar" />
      )}
      {consulta.isSuccess && filtrados.length > 0 && (
        <ul className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {filtrados.map((evento) => (
            <li key={evento.eventCode}>
              <TarjetaEvento evento={evento} />
            </li>
          ))}
        </ul>
      )}
    </Contenedor>
  )
}
