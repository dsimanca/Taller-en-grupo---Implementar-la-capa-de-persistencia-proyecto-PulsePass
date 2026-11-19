import clsx from 'clsx'
import type { ReactNode } from 'react'

export function Contenedor({
  children,
  estrecho = false,
  className,
}: {
  children: ReactNode
  estrecho?: boolean
  className?: string
}) {
  return (
    <div
      className={clsx(
        'mx-auto w-full px-4 py-8 sm:px-6 lg:px-8',
        estrecho ? 'max-w-3xl' : 'max-w-6xl',
        className,
      )}
    >
      {children}
    </div>
  )
}
