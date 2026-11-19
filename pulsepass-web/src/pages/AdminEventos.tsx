import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { Badge } from '../componentes/Badge'
import { Boton } from '../componentes/Boton'
import { Campo } from '../componentes/Campo'
import { CodigoTexto } from '../componentes/CodigoTexto'
import { Contenedor } from '../componentes/Contenedor'
import { Icono } from '../componentes/Icono'
import { Tarjeta } from '../componentes/Tarjeta'
import { ApiError } from '../lib/http'
import { RUTAS } from '../lib/rutas'
import { eventoApi } from '../funcionalidades/eventos/api'

type EventoCreado = { eventCode: string; name: string; status: string }
const CLAVE = 'pulsepass.admin.creados'

function leerCreados(): EventoCreado[] {
  try {
    const texto = sessionStorage.getItem(CLAVE)
    return texto ? (JSON.parse(texto) as EventoCreado[]) : []
  } catch {
    return []
  }
}

export function AdminEventos() {
  const navegar = useNavigate()
  const [codigo, setCodigo] = useState('')
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [creados] = useState<EventoCreado[]>(leerCreados)

  async function abrir(evento: React.FormEvent) {
    evento.preventDefault()
    const limpio = codigo.trim()
    if (!limpio) return
    setError(null)
    setCargando(true)
    try {
      await eventoApi(limpio)
      navegar(RUTAS.adminGestionEvento(limpio))
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No pudimos abrir el evento.')
    } finally {
      setCargando(false)
    }
  }

  return (
    <Contenedor estrecho className="space-y-8">
      <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold text-slate-900 dark:text-white">Administración de eventos</h1>
          <p className="mt-1 text-slate-600 dark:text-slate-400">Crea, publica y gestiona eventos.</p>
        </div>
        <Link
          to={RUTAS.adminCrearEvento}
          className="inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-marca-600 px-4 py-2 font-semibold text-white hover:bg-marca-700"
        >
          <Icono nombre="boleto" /> Nuevo evento
        </Link>
      </header>

      <Tarjeta className="p-6">
        <h2 className="text-lg font-bold text-slate-900 dark:text-white">Buscar evento por código</h2>
        <form onSubmit={abrir} className="mt-4 flex flex-col gap-3 sm:flex-row sm:items-end">
          <div className="flex-1">
            <Campo
              etiqueta="Código"
              placeholder="CMF-2026"
              value={codigo}
              onChange={(evento) => setCodigo(evento.target.value)}
              error={error ?? undefined}
            />
          </div>
          <Boton type="submit" cargando={cargando} disabled={codigo.trim().length === 0}>
            Abrir
          </Boton>
        </form>
      </Tarjeta>

      <section aria-labelledby="creados-sesion" className="space-y-3">
        <h2 id="creados-sesion" className="text-lg font-bold text-slate-900 dark:text-white">
          Creados en esta sesión
        </h2>
        {creados.length === 0 ? (
          <p className="text-sm text-slate-500 dark:text-slate-400">Aún no has creado eventos en esta sesión.</p>
        ) : (
          <ul className="space-y-3">
            {creados.map((evento) => (
              <li key={evento.eventCode}>
                <Tarjeta className="flex flex-col gap-3 p-4 sm:flex-row sm:items-center sm:justify-between">
                  <div>
                    <p className="font-semibold text-slate-900 dark:text-white">{evento.name}</p>
                    <div className="mt-1 flex items-center gap-2">
                      <CodigoTexto className="text-slate-500">{evento.eventCode}</CodigoTexto>
                      <Badge tono={evento.status as never}>{evento.status}</Badge>
                    </div>
                  </div>
                  <Link
                    to={RUTAS.adminGestionEvento(evento.eventCode)}
                    className="inline-flex min-h-11 items-center justify-center rounded-lg border border-slate-300 px-4 py-2 text-sm font-semibold hover:bg-slate-50 dark:border-slate-700 dark:hover:bg-slate-800"
                  >
                    Gestionar
                  </Link>
                </Tarjeta>
              </li>
            ))}
          </ul>
        )}
        <p className="flex items-start gap-2 text-sm text-slate-500 dark:text-slate-400">
          <Icono nombre="alerta" className="mt-0.5 size-4 shrink-0" />
          Nota: la API no ofrece un listado completo de eventos (GAP-01).
        </p>
      </section>
    </Contenedor>
  )
}
