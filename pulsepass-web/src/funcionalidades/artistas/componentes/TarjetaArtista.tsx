import { Link } from 'react-router'
import { Badge } from '../../../componentes/Badge'
import { RUTAS } from '../../../lib/rutas'
import type { Artista } from '../tipos'
import { AvatarArtista } from './AvatarArtista'

export function TarjetaArtista({ artista }: { artista: Artista }) {
  return (
    <Link
      to={RUTAS.artista(artista.id)}
      className="flex flex-col items-center gap-3 rounded-xl border border-slate-200 bg-white p-5 text-center shadow-sm transition hover:-translate-y-0.5 hover:shadow-md focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-600 dark:border-slate-800 dark:bg-slate-900"
    >
      <AvatarArtista nombre={artista.stageName} className="size-16" />
      <h3 className="font-semibold text-slate-900 dark:text-white">{artista.stageName}</h3>
      <p className="text-sm text-slate-500 dark:text-slate-400">{artista.genre ?? 'Artista'}</p>
      <Badge tono={artista.active ? 'exito' : 'neutro'}>
        {artista.active ? 'Activo' : 'Inactivo'}
      </Badge>
    </Link>
  )
}
