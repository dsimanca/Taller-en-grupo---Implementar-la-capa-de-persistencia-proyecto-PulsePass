import clsx from 'clsx'
import type { ReactNode } from 'react'

export function CodigoTexto({ children, className }: { children: ReactNode; className?: string }) {
  return (
    <span className={clsx('font-mono text-sm tracking-tight', className)}>{children}</span>
  )
}
