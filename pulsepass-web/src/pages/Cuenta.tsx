import { Link, useNavigate } from 'react-router'
import { Badge } from '../componentes/Badge'
import { Boton } from '../componentes/Boton'
import { Contenedor } from '../componentes/Contenedor'
import { Cargando } from '../componentes/Cargando'
import { EstadoError } from '../componentes/EstadoError'
import { Icono } from '../componentes/Icono'
import { formatearFecha, iniciales } from '../lib/formato'
import { RUTAS } from '../lib/rutas'
import { useAuth } from '../funcionalidades/auth/AuthContext'
import { useUsuarioPorEmail, useUsuarioPorUsername } from '../funcionalidades/usuarios/hooks'

export function Cuenta() {
  const { sesion, cerrarSesion } = useAuth()
  const navegar = useNavigate()
  const email = sesion.estado === 'autenticado' ? sesion.usuario.email : null
  const username = sesion.estado === 'autenticado' ? sesion.usuario.username : null
  const usuario = useUsuarioPorEmail(email)
  useUsuarioPorUsername(username)

  if (sesion.estado !== 'autenticado') return null

  function salir() {
    cerrarSesion()
    navegar(RUTAS.inicio)
  }

  return (
    <Contenedor estrecho className="space-y-6">
      <h1 className="text-3xl font-bold text-slate-900 dark:text-white">Mi cuenta</h1>

      {usuario.isPending && <Cargando mensaje="Cargando cuenta…" />}
      {usuario.isError && (
        <EstadoError mensaje="No pudimos cargar tu perfil." onReintentar={() => void usuario.refetch()} />
      )}
      {usuario.isSuccess && (
        <section className="rounded-2xl border border-slate-200 bg-white p-8 shadow-sm dark:border-slate-800 dark:bg-slate-900">
          <div className="flex flex-col items-start gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex items-center gap-4">
              <span className="inline-flex size-16 items-center justify-center rounded-full bg-marca-600 font-titulo text-xl font-bold text-white">
                {iniciales(`${usuario.data.firstName} ${usuario.data.lastName}`)}
              </span>
              <div>
                <h2 className="text-xl font-bold text-slate-900 dark:text-white">
                  {usuario.data.firstName} {usuario.data.lastName}
                </h2>
                <p className="text-slate-600 dark:text-slate-400">@{usuario.data.username}</p>
              </div>
            </div>
            <div className="flex flex-wrap gap-2">
              {sesion.usuario.roles.map((rol) => (
                <Badge key={rol} tono="info">{rol}</Badge>
              ))}
              <Badge tono={usuario.data.active ? 'exito' : 'error'}>
                {usuario.data.active ? 'Activa' : 'Inactiva'}
              </Badge>
            </div>
          </div>

          <dl className="mt-8 grid grid-cols-1 gap-4 text-sm sm:grid-cols-2">
            <div><dt className="text-slate-500 dark:text-slate-400">Correo</dt><dd className="mt-1 flex items-center gap-2 font-medium break-all"><Icono nombre="correo" className="size-4 text-marca-600" /> {usuario.data.email}</dd></div>
            <div><dt className="text-slate-500 dark:text-slate-400">Nacimiento</dt><dd className="mt-1 flex items-center gap-2 font-medium"><Icono nombre="calendario" className="size-4 text-marca-600" /> {formatearFecha(usuario.data.birthDate)}</dd></div>
            <div><dt className="text-slate-500 dark:text-slate-400">Teléfono</dt><dd className="mt-1 font-medium">{usuario.data.phone}</dd></div>
            <div><dt className="text-slate-500 dark:text-slate-400">Ciudad</dt><dd className="mt-1 font-medium">{usuario.data.city}</dd></div>
          </dl>

          <div className="mt-8 flex flex-wrap gap-3">
            {sesion.usuario.roles.includes('USER') && (
              <Link to={RUTAS.misTickets} className="inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-marca-600 px-4 py-2 font-semibold text-white hover:bg-marca-700">
                <Icono nombre="boleto" /> Ver mis tickets
              </Link>
            )}
            <Boton variante="secundario" onClick={salir}>Cerrar sesión</Boton>
          </div>
        </section>
      )}
    </Contenedor>
  )
}
