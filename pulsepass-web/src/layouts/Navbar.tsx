import { useEffect, useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router'
import clsx from 'clsx'
import { Icono } from '../componentes/Icono'
import { iniciales } from '../lib/formato'
import { RUTAS } from '../lib/rutas'
import { useAuth } from '../funcionalidades/auth/AuthContext'
import type { Rol } from '../funcionalidades/auth/tipos'

const CLAVE_TEMA = 'pulsepass.tema'

function temaInicial(): boolean {
  try {
    const guardado = localStorage.getItem(CLAVE_TEMA)
    if (guardado === 'dark') return true
    if (guardado === 'light') return false
  } catch {
    /* sin preferencia guardada */
  }
  return window.matchMedia('(prefers-color-scheme: dark)').matches
}

type Item = { ruta: string; etiqueta: string; roles?: Rol[] }

const ITEMS_PUBLICOS: Item[] = [
  { ruta: RUTAS.eventos, etiqueta: 'Eventos' },
  { ruta: RUTAS.artistas, etiqueta: 'Artistas' },
  { ruta: RUTAS.sedes, etiqueta: 'Sedes' },
]

function enlacesPorRol(sesion: ReturnType<typeof useAuth>['sesion']): Item[] {
  if (sesion.estado !== 'autenticado') return []
  const roles = sesion.usuario.roles
  const items: Item[] = []
  if (roles.includes('USER')) items.push({ ruta: RUTAS.misTickets, etiqueta: 'Mis tickets' })
  if (roles.includes('STAFF') || roles.includes('ADMIN')) {
    items.push({ ruta: RUTAS.validacionIngreso, etiqueta: 'Validar ingreso' })
  }
  if (roles.includes('ADMIN')) {
    items.push({ ruta: RUTAS.adminEventos, etiqueta: 'Administrar eventos' })
  }
  return items
}

function Enlace({
  ruta,
  etiqueta,
  onClick,
}: {
  ruta: string
  etiqueta: string
  onClick?: () => void
}) {
  return (
    <NavLink
      to={ruta}
      end={ruta === '/'}
      onClick={onClick}
      className={({ isActive }) =>
        clsx(
          'rounded-lg px-3 py-2 text-sm font-medium transition-colors',
          isActive
            ? 'bg-marca-50 text-marca-700 dark:bg-slate-800 dark:text-marca-500'
            : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900 dark:text-slate-300 dark:hover:bg-slate-800 dark:hover:text-white',
        )
      }
    >
      {etiqueta}
    </NavLink>
  )
}

export function Navbar() {
  const { sesion, cerrarSesion } = useAuth()
  const navegar = useNavigate()
  const [menuMovil, setMenuMovil] = useState(false)
  const [menuUsuario, setMenuUsuario] = useState(false)
  const [oscuro, setOscuro] = useState(temaInicial)
  const autenticado = sesion.estado === 'autenticado'
  const nombre = autenticado ? sesion.usuario.username : ''

  useEffect(() => {
    setMenuMovil(false)
    setMenuUsuario(false)
  }, [])

  useEffect(() => {
    document.documentElement.classList.toggle('dark', oscuro)
    try {
      localStorage.setItem(CLAVE_TEMA, oscuro ? 'dark' : 'light')
    } catch {
      /* preferencia solo en memoria */
    }
  }, [oscuro])

  function salir() {
    cerrarSesion()
    setMenuUsuario(false)
    setMenuMovil(false)
    navegar(RUTAS.inicio)
  }

  const rol = autenticado ? sesion.usuario.roles : []
  const enlacesRol = enlacesPorRol(sesion)

  return (
    <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/95 backdrop-blur dark:border-slate-800 dark:bg-slate-950/95">
      <a
        href="#contenido"
        className="sr-only focus:not-sr-only focus:absolute focus:top-2 focus:left-2 focus:z-50 focus:rounded-lg focus:bg-white focus:px-4 focus:py-2 focus:text-marca-700"
      >
        Saltar al contenido
      </a>
      <nav
        aria-label="Principal"
        className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4 sm:px-6 lg:px-8"
      >
        <Link
          to={RUTAS.inicio}
          className="flex items-center gap-2 font-titulo text-xl font-bold text-marca-600 dark:text-marca-500"
        >
          <Icono nombre="marca" className="size-7" />
          <span>PulsePass</span>
        </Link>

        <ul className="hidden items-center gap-1 md:flex">
          {ITEMS_PUBLICOS.map((item) => (
            <li key={item.ruta}>
              <Enlace ruta={item.ruta} etiqueta={item.etiqueta} />
            </li>
          ))}
          {enlacesRol.map((item) => (
            <li key={item.ruta}>
              <Enlace ruta={item.ruta} etiqueta={item.etiqueta} />
            </li>
          ))}
        </ul>

        <div className="hidden items-center gap-2 md:flex">
          <button
            type="button"
            onClick={() => setOscuro((valor) => !valor)}
            aria-label={oscuro ? 'Activar modo claro' : 'Activar modo oscuro'}
            className="inline-flex min-h-11 min-w-11 items-center justify-center rounded-lg text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800"
          >
            <Icono nombre={oscuro ? 'sol' : 'luna'} />
          </button>
          {!autenticado ? (
            <>
              <Link
                to={RUTAS.login}
                className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 dark:border-slate-700 dark:text-slate-200 dark:hover:bg-slate-800"
              >
                Ingresar
              </Link>
              <Link
                to={RUTAS.registro}
                className="rounded-lg bg-marca-600 px-4 py-2 text-sm font-semibold text-white hover:bg-marca-700"
              >
                Registrarse
              </Link>
            </>
          ) : (
            <div className="relative">
              <button
                type="button"
                onClick={() => setMenuUsuario((valor) => !valor)}
                aria-expanded={menuUsuario}
                aria-haspopup="menu"
                className="flex min-h-11 items-center gap-2 rounded-lg px-2 py-1 hover:bg-slate-100 dark:hover:bg-slate-800"
              >
                <span className="inline-flex size-8 items-center justify-center rounded-full bg-marca-600 text-xs font-bold text-white">
                  {iniciales(nombre)}
                </span>
                <span className="text-sm font-medium text-slate-700 dark:text-slate-200">
                  {nombre}
                </span>
              </button>
              {menuUsuario && (
                <div
                  role="menu"
                  className="absolute right-0 mt-2 w-48 rounded-xl border border-slate-200 bg-white p-2 shadow-lg dark:border-slate-800 dark:bg-slate-900"
                >
                  <Link
                    to={RUTAS.cuenta}
                    role="menuitem"
                    onClick={() => setMenuUsuario(false)}
                    className="block rounded-lg px-3 py-2 text-sm hover:bg-slate-100 dark:hover:bg-slate-800"
                  >
                    Mi cuenta
                  </Link>
                  {rol.includes('USER') && (
                    <Link
                      to={RUTAS.misTickets}
                      role="menuitem"
                      onClick={() => setMenuUsuario(false)}
                      className="block rounded-lg px-3 py-2 text-sm hover:bg-slate-100 dark:hover:bg-slate-800"
                    >
                      Mis tickets
                    </Link>
                  )}
                  <button
                    type="button"
                    role="menuitem"
                    onClick={salir}
                    className="block w-full rounded-lg px-3 py-2 text-left text-sm text-red-600 hover:bg-red-50 dark:hover:bg-red-950"
                  >
                    Salir
                  </button>
                </div>
              )}
            </div>
          )}
        </div>

        <div className="flex items-center gap-1 md:hidden">
          <button
            type="button"
            onClick={() => setOscuro((valor) => !valor)}
            aria-label={oscuro ? 'Activar modo claro' : 'Activar modo oscuro'}
            className="inline-flex min-h-11 min-w-11 items-center justify-center rounded-lg text-slate-700 hover:bg-slate-100 dark:text-slate-200 dark:hover:bg-slate-800"
          >
            <Icono nombre={oscuro ? 'sol' : 'luna'} />
          </button>
          <button
            type="button"
            className="inline-flex items-center justify-center rounded-lg p-2 text-slate-700 hover:bg-slate-100 dark:text-slate-200 dark:hover:bg-slate-800"
            aria-expanded={menuMovil}
            aria-controls="menu-movil"
            aria-label={menuMovil ? 'Cerrar menú' : 'Abrir menú'}
            onClick={() => setMenuMovil((valor) => !valor)}
          >
            <Icono nombre={menuMovil ? 'cerrar' : 'menu'} className="size-6" />
          </button>
        </div>
      </nav>

      {menuMovil && (
        <div
          id="menu-movil"
          className="border-t border-slate-200 bg-white px-4 py-4 md:hidden dark:border-slate-800 dark:bg-slate-950"
        >
          <ul className="flex flex-col gap-1">
            {ITEMS_PUBLICOS.map((item) => (
              <li key={item.ruta}>
                <Enlace
                  ruta={item.ruta}
                  etiqueta={item.etiqueta}
                  onClick={() => setMenuMovil(false)}
                />
              </li>
            ))}
            {enlacesRol.map((item) => (
              <li key={item.ruta}>
                <Enlace
                  ruta={item.ruta}
                  etiqueta={item.etiqueta}
                  onClick={() => setMenuMovil(false)}
                />
              </li>
            ))}
          </ul>
          <div className="mt-4 flex flex-col gap-2 border-t border-slate-200 pt-4 dark:border-slate-800">
            {!autenticado ? (
              <>
                <Link
                  to={RUTAS.login}
                  onClick={() => setMenuMovil(false)}
                  className="flex min-h-11 items-center justify-center rounded-lg border border-slate-300 px-4 py-2 font-semibold dark:border-slate-700"
                >
                  Ingresar
                </Link>
                <Link
                  to={RUTAS.registro}
                  onClick={() => setMenuMovil(false)}
                  className="flex min-h-11 items-center justify-center rounded-lg bg-marca-600 px-4 py-2 font-semibold text-white"
                >
                  Registrarse
                </Link>
              </>
            ) : (
              <>
                <Link
                  to={RUTAS.cuenta}
                  onClick={() => setMenuMovil(false)}
                  className="flex min-h-11 items-center rounded-lg px-3 py-2 font-medium hover:bg-slate-100 dark:hover:bg-slate-800"
                >
                  Mi cuenta
                </Link>
                <button
                  type="button"
                  onClick={salir}
                  className="flex min-h-11 items-center rounded-lg px-3 py-2 font-medium text-red-600 hover:bg-red-50 dark:hover:bg-red-950"
                >
                  Salir
                </button>
              </>
            )}
          </div>
        </div>
      )}
    </header>
  )
}
