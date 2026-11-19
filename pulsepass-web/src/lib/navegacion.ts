export type OpcionesNavegacion = {
  replace?: boolean
  state?: Record<string, unknown>
}

type Navegador = (ruta: string, opciones?: OpcionesNavegacion) => void

let navegador: Navegador | null = null

export function registrarNavegador(fn: Navegador): () => void {
  navegador = fn
  return () => {
    if (navegador === fn) navegador = null
  }
}

export function navegarA(ruta: string, opciones?: OpcionesNavegacion): void {
  navegador?.(ruta, opciones)
}
