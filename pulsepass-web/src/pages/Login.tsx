import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router'
import { Boton } from '../componentes/Boton'
import { Campo } from '../componentes/Campo'
import { Contenedor } from '../componentes/Contenedor'
import { Icono } from '../componentes/Icono'
import { ApiError } from '../lib/http'
import { aplicarDetails } from '../lib/formularios'
import { RUTAS } from '../lib/rutas'
import { useLogin } from '../funcionalidades/auth/hooks'
import { LoginRequestSchema, type LoginForm } from '../funcionalidades/auth/tipos'

type EstadoNavegacion = { desde?: string; motivo?: string; registrado?: boolean } | null

export function Login() {
  const navegar = useNavigate()
  const location = useLocation()
  const estado = (location.state as EstadoNavegacion) ?? null
  const login = useLogin()
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<LoginForm>({
    resolver: zodResolver(LoginRequestSchema),
    defaultValues: { identifier: '', password: '' },
  })

  async function onSubmit(datos: LoginForm) {
    setErrorGeneral(null)
    try {
      await login.mutateAsync(datos)
      navegar(estado?.desde ?? RUTAS.inicio, { replace: true })
    } catch (error) {
      if (error instanceof ApiError && error.status === 401) {
        setErrorGeneral('Credenciales inválidas')
        return
      }
      const mensaje = aplicarDetails(error, setError, ['identifier', 'password'])
      setErrorGeneral(mensaje ?? 'Credenciales inválidas')
    }
  }

  return (
    <Contenedor estrecho className="flex min-h-[70vh] items-center justify-center">
      <section className="w-full max-w-sm rounded-2xl border border-slate-200 bg-white p-8 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div className="mb-6 text-center">
          <span className="mx-auto inline-flex size-12 items-center justify-center rounded-xl bg-marca-100 text-marca-600 dark:bg-slate-800">
            <Icono nombre="marca" className="size-7" />
          </span>
          <h1 className="mt-4 text-2xl font-bold text-slate-900 dark:text-white">Inicia sesión</h1>
          <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">
            Accede para comprar y gestionar tus entradas.
          </p>
        </div>

        {estado?.motivo === 'expirada' && (
          <p role="status" className="mb-4 rounded-lg bg-amber-100 px-3 py-2 text-sm text-amber-800">
            Tu sesión expiró. Inicia sesión de nuevo.
          </p>
        )}
        {estado?.registrado && (
          <p role="status" className="mb-4 rounded-lg bg-emerald-100 px-3 py-2 text-sm text-emerald-800">
            Cuenta creada, inicia sesión.
          </p>
        )}
        {errorGeneral && (
          <p role="alert" className="mb-4 rounded-lg bg-red-100 px-3 py-2 text-sm text-red-800">
            {errorGeneral}
          </p>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
          <Campo
            etiqueta="Usuario o correo"
            autoComplete="username"
            required
            error={errors.identifier?.message}
            {...register('identifier')}
          />
          <Campo
            etiqueta="Contraseña"
            type="password"
            autoComplete="current-password"
            required
            error={errors.password?.message}
            {...register('password')}
          />
          <Boton type="submit" className="w-full" cargando={login.isPending}>
            Ingresar
          </Boton>
        </form>

        <p className="mt-6 text-center text-sm text-slate-600 dark:text-slate-400">
          ¿No tienes cuenta?{' '}
          <Link to={RUTAS.registro} className="font-semibold text-marca-600 hover:underline">
            Regístrate
          </Link>
        </p>
      </section>
    </Contenedor>
  )
}
