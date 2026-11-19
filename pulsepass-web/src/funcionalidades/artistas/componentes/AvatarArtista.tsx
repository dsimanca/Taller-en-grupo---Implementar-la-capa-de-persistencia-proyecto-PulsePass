import clsx from 'clsx'
import { iniciales } from '../../../lib/formato'

export function AvatarArtista({
  nombre,
  className,
}: {
  nombre: string
  className?: string
}) {
  return (
    <span
      aria-hidden="true"
      className={clsx(
        'inline-flex size-12 shrink-0 items-center justify-center rounded-full bg-linear-to-br from-marca-500 to-acento-500 font-titulo text-sm font-bold text-white',
        className,
      )}
    >
      {iniciales(nombre)}
    </span>
  )
}
