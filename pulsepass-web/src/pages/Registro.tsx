import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router'
import { Boton } from '../componentes/Boton'
import { Campo } from '../componentes/Campo'
import { Contenedor } from '../componentes/Contenedor'
import { Icono } from '../componentes/Icono'
import { aplicarDetails, mensajeError } from '../lib/formularios'
import { RUTAS } from '../lib/rutas'
import { useRegistrarUsuario } from '../funcionalidades/usuarios/hooks'
import {
  RegisterUserRequestSchema,
  type RegistroForm,
} from '../funcionalidades/usuarios/tipos'
import { ApiError } from '../lib/http'

const CAMPOS: (keyof RegistroForm)[] = [
  'username',
  'email',
  'password',
  'firstName',
  'lastName',
  'phone',
  'city',
  'birthDate',
]

export function Registro() {
  const navegar = useNavigate()
  const registro = useRegistrarUsuario()
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<RegistroForm>({
    resolver: zodResolver(RegisterUserRequestSchema),
    defaultValues: {
      username: '',
      email: '',
      password: '',
      firstName: '',
      lastName: '',
      phone: '',
      city: '',
      birthDate: '',
    },
  })

  async function onSubmit(datos: RegistroForm) {
    setErrorGeneral(null)
    try {
      await registro.mutateAsync(datos)
      navegar(RUTAS.login, { replace: true, state: { registrado: true } })
    } catch (error) {
      if (error instanceof ApiError && error.status === 409) {
        setErrorGeneral('El usuario o correo ya está registrado.')
        return
      }
      const mensaje = aplicarDetails(error, setError, CAMPOS)
      setErrorGeneral(mensaje ?? mensajeError(error))
    }
  }

  return (
    <Contenedor estrecho>
      <section className="mx-auto max-w-xl rounded-2xl border border-slate-200 bg-white p-8 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div className="mb-6 text-center">
          <span className="mx-auto inline-flex size-12 items-center justify-center rounded-xl bg-marca-100 text-marca-600 dark:bg-slate-800">
            <Icono nombre="usuario" className="size-7" />
          </span>
          <h1 className="mt-4 text-2xl font-bold text-slate-900 dark:text-white">Crea tu cuenta</h1>
          <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">
            Regístrate para comprar y gestionar tus entradas.
          </p>
        </div>

        {errorGeneral && (
          <p role="alert" className="mb-4 rounded-lg bg-red-100 px-3 py-2 text-sm text-red-800">
            {errorGeneral}
          </p>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Campo etiqueta="Nombre de usuario" autoComplete="username" required error={errors.username?.message} {...register('username')} />
            <Campo etiqueta="Correo electrónico" type="email" autoComplete="email" required error={errors.email?.message} {...register('email')} />
          </div>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Campo etiqueta="Nombres" autoComplete="given-name" required error={errors.firstName?.message} {...register('firstName')} />
            <Campo etiqueta="Apellidos" autoComplete="family-name" required error={errors.lastName?.message} {...register('lastName')} />
          </div>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Campo etiqueta="Teléfono" type="tel" autoComplete="tel" required error={errors.phone?.message} {...register('phone')} />
            <Campo etiqueta="Ciudad" autoComplete="address-level2" required error={errors.city?.message} {...register('city')} />
          </div>
          <Campo etiqueta="Fecha de nacimiento" type="date" required error={errors.birthDate?.message} {...register('birthDate')} />
          <Campo
            etiqueta="Contraseña (mín. 12 caracteres)"
            type="password"
            autoComplete="new-password"
            required
            ayuda="Usa una frase larga y segura."
            error={errors.password?.message}
            {...register('password')}
          />
          <Boton type="submit" className="w-full" cargando={registro.isPending}>
            Crear cuenta
          </Boton>
        </form>

        <p className="mt-6 text-center text-sm text-slate-600 dark:text-slate-400">
          ¿Ya tienes cuenta?{' '}
          <Link to={RUTAS.login} className="font-semibold text-marca-600 hover:underline">
            Inicia sesión
          </Link>
        </p>
      </section>
    </Contenedor>
  )
}
