import { formatearCOP } from '../lib/formato'

export function PrecioCOP({ valor, className }: { valor: number; className?: string }) {
  return <span className={className}>{formatearCOP(valor)}</span>
}
