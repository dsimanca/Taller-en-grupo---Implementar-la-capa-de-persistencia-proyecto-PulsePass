const locale = 'es-CO'

function aFecha(valor: string): Date {
  if (!valor.includes('T')) return new Date(`${valor}T00:00:00`)
  return new Date(valor)
}

export function formatearFecha(valor: string): string {
  const opciones: Intl.DateTimeFormatOptions = {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }
  if (!valor.includes('T')) opciones.timeZone = 'UTC'
  return new Intl.DateTimeFormat(locale, opciones).format(aFecha(valor))
}

export function formatearFechaHora(valor: string): string {
  return new Intl.DateTimeFormat(locale, {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: true,
  }).format(aFecha(valor))
}

export function formatearCOP(valor: number): string {
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0,
  }).format(valor)
}

export function formatearNumero(valor: number): string {
  return new Intl.NumberFormat(locale).format(valor)
}

export function iniciales(nombre: string): string {
  return nombre
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((parte) => parte.charAt(0).toUpperCase())
    .join('')
}
