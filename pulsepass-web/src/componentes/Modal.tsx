import { useEffect, useId, useRef, type ReactNode } from 'react'
import { Icono } from './Icono'

const SELECTOR_FOCO =
  'button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'

export function Modal({
  abierto,
  titulo,
  onCerrar,
  children,
}: {
  abierto: boolean
  titulo: string
  onCerrar: () => void
  children: ReactNode
}) {
  const tituloId = useId()
  const panel = useRef<HTMLDivElement>(null)
  const botonCerrar = useRef<HTMLButtonElement>(null)

  useEffect(() => {
    if (!abierto) return
    const elementoAnterior = document.activeElement instanceof HTMLElement ? document.activeElement : null
    botonCerrar.current?.focus()

    function alPulsar(evento: KeyboardEvent) {
      if (evento.key === 'Escape') {
        evento.preventDefault()
        onCerrar()
        return
      }
      if (evento.key !== 'Tab' || !panel.current) return
      const enfocables = Array.from(
        panel.current.querySelectorAll<HTMLElement>(SELECTOR_FOCO),
      )
      if (enfocables.length === 0) return
      const primero = enfocables[0] as HTMLElement
      const ultimo = enfocables[enfocables.length - 1] as HTMLElement
      if (evento.shiftKey && document.activeElement === primero) {
        evento.preventDefault()
        ultimo.focus()
      } else if (!evento.shiftKey && document.activeElement === ultimo) {
        evento.preventDefault()
        primero.focus()
      }
    }

    document.addEventListener('keydown', alPulsar)
    return () => {
      document.removeEventListener('keydown', alPulsar)
      elementoAnterior?.focus()
    }
  }, [abierto, onCerrar])

  if (!abierto) return null

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/60 p-4"
      onMouseDown={(evento) => {
        if (evento.target === evento.currentTarget) onCerrar()
      }}
    >
      <div
        ref={panel}
        role="dialog"
        aria-modal="true"
        aria-labelledby={tituloId}
        className="w-full max-w-md rounded-xl bg-white p-6 shadow-xl dark:bg-slate-900"
      >
        <div className="mb-4 flex items-center justify-between gap-4">
          <h2 id={tituloId} className="text-xl font-bold text-slate-900 dark:text-white">
            {titulo}
          </h2>
          <button
            ref={botonCerrar}
            type="button"
            onClick={onCerrar}
            aria-label="Cerrar"
            className="rounded-lg p-2 text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800"
          >
            <Icono nombre="cerrar" />
          </button>
        </div>
        {children}
      </div>
    </div>
  )
}
