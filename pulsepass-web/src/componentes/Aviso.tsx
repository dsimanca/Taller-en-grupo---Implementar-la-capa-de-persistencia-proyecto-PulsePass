import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from 'react'
import clsx from 'clsx'
import { Icono } from './Icono'

export type TipoAviso = 'exito' | 'error' | 'info'
type Aviso = { id: number; tipo: TipoAviso; mensaje: string }
type AvisoContexto = {
  mostrar: (tipo: TipoAviso, mensaje: string) => void
  cerrar: () => void
}

const ContextoAviso = createContext<AvisoContexto | null>(null)

const ESTILOS: Record<TipoAviso, string> = {
  exito: 'border-emerald-300 bg-emerald-50 text-emerald-900 dark:border-emerald-800 dark:bg-emerald-950 dark:text-emerald-100',
  error: 'border-red-300 bg-red-50 text-red-900 dark:border-red-900 dark:bg-red-950 dark:text-red-100',
  info: 'border-sky-300 bg-sky-50 text-sky-900 dark:border-sky-800 dark:bg-sky-950 dark:text-sky-100',
}

const ICONOS: Record<TipoAviso, 'check' | 'alerta' | 'correo'> = {
  exito: 'check',
  error: 'alerta',
  info: 'correo',
}

export function AvisoProvider({ children }: { children: ReactNode }) {
  const [aviso, setAviso] = useState<Aviso | null>(null)
  const contador = useRef(0)

  const cerrar = useCallback(() => setAviso(null), [])

  const mostrar = useCallback((tipo: TipoAviso, mensaje: string) => {
    contador.current += 1
    setAviso({ id: contador.current, tipo, mensaje })
  }, [])

  useEffect(() => {
    if (!aviso || aviso.tipo === 'error') return
    const temporizador = window.setTimeout(cerrar, 4000)
    return () => window.clearTimeout(temporizador)
  }, [aviso, cerrar])

  const valor = useMemo(() => ({ mostrar, cerrar }), [mostrar, cerrar])

  return (
    <ContextoAviso.Provider value={valor}>
      {children}
      {aviso && (
        <div
          key={aviso.id}
          role={aviso.tipo === 'error' ? 'alert' : 'status'}
          aria-live={aviso.tipo === 'error' ? 'assertive' : 'polite'}
          className={clsx(
            'fixed right-4 bottom-4 z-60 flex max-w-sm items-start gap-3 rounded-xl border px-4 py-3 shadow-lg',
            ESTILOS[aviso.tipo],
          )}
        >
          <Icono nombre={ICONOS[aviso.tipo]} className="mt-0.5 size-5 shrink-0" />
          <p className="flex-1 text-sm font-medium">{aviso.mensaje}</p>
          <button
            type="button"
            onClick={cerrar}
            aria-label="Cerrar aviso"
            className="rounded p-0.5 hover:opacity-70"
          >
            <Icono nombre="cerrar" className="size-4" />
          </button>
        </div>
      )}
    </ContextoAviso.Provider>
  )
}

export function useAviso(): AvisoContexto {
  const contexto = useContext(ContextoAviso)
  if (!contexto) throw new Error('useAviso debe usarse dentro de AvisoProvider')
  return contexto
}
