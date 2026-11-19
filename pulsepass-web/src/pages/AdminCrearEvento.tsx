import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router'
import { AreaTexto } from '../componentes/AreaTexto'
import { Boton } from '../componentes/Boton'
import { Campo } from '../componentes/Campo'
import { Contenedor } from '../componentes/Contenedor'
import { Selector } from '../componentes/Selector'
import { aplicarDetails, mensajeError } from '../lib/formularios'
import { RUTAS } from '../lib/rutas'
import { ApiError } from '../lib/http'
import { useAviso } from '../componentes/Aviso'
import { useCrearEvento } from '../funcionalidades/eventos/hooks'
import {
  CreateEventRequestSchema,
  EventCategorySchema,
  type CreateEventForm,
} from '../funcionalidades/eventos/tipos'
import { useSedesActivas } from '../funcionalidades/sedes/hooks'

const CAMPOS: (keyof CreateEventForm)[] = [
  'eventCode',
  'name',
  'description',
  'category',
  'eventDate',
  'minimumAge',
  'venueCode',
]
const CLAVE_CREADOS = 'pulsepass.admin.creados'

export function AdminCrearEvento() {
  const navegar = useNavigate()
  const aviso = useAviso()
  const sedes = useSedesActivas()
  const crear = useCrearEvento()
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    control,
    setError,
    formState: { errors },
  } = useForm<CreateEventForm>({
    resolver: zodResolver(CreateEventRequestSchema),
    defaultValues: {
      eventCode: '',
      name: '',
      description: '',
      category: 'MUSIC',
      eventDate: '',
      minimumAge: 0,
      venueCode: '',
    },
  })

  async function onSubmit(datos: CreateEventForm) {
    setErrorGeneral(null)
    try {
      const eventDate =
        datos.eventDate.length === 16 ? `${datos.eventDate}:00` : datos.eventDate
      const evento = await crear.mutateAsync({ ...datos, eventDate })
      try {
        const existentes = JSON.parse(sessionStorage.getItem(CLAVE_CREADOS) ?? '[]') as unknown[]
        const actualizados = [
          { eventCode: evento.eventCode, name: evento.name, status: evento.status },
          ...existentes.filter((item) => (item as { eventCode?: string }).eventCode !== evento.eventCode),
        ]
        sessionStorage.setItem(CLAVE_CREADOS, JSON.stringify(actualizados))
      } catch {
        // la lista de sesión es auxiliar
      }
      aviso.mostrar('exito', 'Evento creado correctamente.')
      navegar(RUTAS.adminGestionEvento(evento.eventCode))
    } catch (error) {
      if (error instanceof ApiError && error.status === 409) {
        setErrorGeneral(error.message)
        return
      }
      const mensaje = aplicarDetails(error, setError, CAMPOS)
      setErrorGeneral(mensaje ?? mensajeError(error))
    }
  }

  return (
    <Contenedor estrecho>
      <Link to={RUTAS.adminEventos} className="text-sm font-semibold text-marca-600 hover:underline">
        ← Eventos
      </Link>
      <h1 className="mt-2 text-3xl font-bold text-slate-900 dark:text-white">Nuevo evento</h1>

      <form
        onSubmit={handleSubmit(onSubmit)}
        className="mt-6 space-y-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900"
        noValidate
      >
        {errorGeneral && (
          <p role="alert" className="rounded-lg bg-red-100 px-3 py-2 text-sm text-red-800">
            {errorGeneral}
          </p>
        )}

        <Campo etiqueta="Código del evento" required error={errors.eventCode?.message} {...register('eventCode')} />
        <Campo etiqueta="Nombre" required error={errors.name?.message} {...register('name')} />
        <AreaTexto etiqueta="Descripción" rows={4} error={errors.description?.message} {...register('description')} />

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Controller
            control={control}
            name="category"
            render={({ field }) => (
              <Selector etiqueta="Categoría" required error={errors.category?.message} {...field}>
                {EventCategorySchema.options.map((categoria) => (
                  <option key={categoria} value={categoria}>{categoria}</option>
                ))}
              </Selector>
            )}
          />
          <Campo
            etiqueta="Edad mínima"
            type="number"
            min={0}
            required
            error={errors.minimumAge?.message}
            {...register('minimumAge', { valueAsNumber: true })}
          />
        </div>

        <Campo
          etiqueta="Fecha y hora"
          type="datetime-local"
          required
          error={errors.eventDate?.message}
          {...register('eventDate')}
        />

        <Controller
          control={control}
          name="venueCode"
          render={({ field }) => (
            <Selector etiqueta="Sede" required error={errors.venueCode?.message} {...field}>
              <option value="">Selecciona una sede</option>
              {sedes.data?.map((sede) => (
                <option key={sede.code} value={sede.code}>
                  {sede.name} ({sede.code})
                </option>
              ))}
            </Selector>
          )}
        />

        <div className="flex justify-end gap-3 pt-2">
          <Link
            to={RUTAS.adminEventos}
            className="inline-flex min-h-11 items-center justify-center rounded-lg border border-slate-300 px-4 py-2 font-semibold text-slate-700 hover:bg-slate-50 dark:border-slate-700 dark:text-slate-200 dark:hover:bg-slate-800"
          >
            Cancelar
          </Link>
          <Boton type="submit" cargando={crear.isPending}>Crear evento</Boton>
        </div>
      </form>
    </Contenedor>
  )
}
