import { zodResolver } from '@hookform/resolvers/zod'
import clsx from 'clsx'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router'
import { z } from 'zod'
import { Boton } from '../../../componentes/Boton'
import { RUTAS } from '../../../lib/rutas'
import type { Sesion } from '../../auth/AuthContext'
import { TicketTypeSchema, type TicketType } from '../../tickets/tipos'
import type { EventResponse } from '../tipos'

const CompraFormSchema = z.object({ type: TicketTypeSchema })
type CompraForm = z.infer<typeof CompraFormSchema>

const TIPOS: TicketType[] = ['GENERAL', 'VIP', 'BACKSTAGE', 'STUDENT']

export function PanelCompra({
  evento,
  sesion,
  cargando,
  onComprar,
}: {
  evento: EventResponse
  sesion: Sesion
  cargando: boolean
  onComprar: (tipo: TicketType) => void
}) {
  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<CompraForm>({
    resolver: zodResolver(CompraFormSchema),
    defaultValues: { type: 'GENERAL' },
  })
  const tipo = watch('type')

  const anonimo = sesion.estado === 'anonimo'
  const noDisponible = evento.status !== 'PUBLISHED'
  const correo = sesion.estado === 'autenticado' ? sesion.usuario.email : ''

  const onSubmit = (datos: CompraForm) => onComprar(datos.type)

  const enlaceLogin = (
    <Link
      to={RUTAS.login}
      state={{ desde: RUTAS.evento(evento.eventCode) }}
      className="inline-flex min-h-11 w-full items-center justify-center rounded-lg bg-marca-600 px-4 py-2 text-sm font-semibold text-white hover:bg-marca-700"
    >
      Inicia sesión para comprar
    </Link>
  )

  return (
    <>
      <form
        onSubmit={handleSubmit(onSubmit)}
        aria-label="Comprar entrada"
        className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
      >
        <h2 className="text-lg font-bold text-slate-900 dark:text-white">Entradas</h2>
        <fieldset className="mt-4 space-y-2">
          <legend className="mb-2 text-sm font-medium text-slate-700 dark:text-slate-300">
            Tipo de entrada
          </legend>
          {TIPOS.map((tipoEntrada) => (
            <label
              key={tipoEntrada}
              className={clsx(
                'flex min-h-11 cursor-pointer items-center gap-3 rounded-lg border px-3 py-2 text-sm transition',
                tipo === tipoEntrada
                  ? 'border-marca-600 bg-marca-50 text-marca-700 dark:bg-slate-800 dark:text-marca-500'
                  : 'border-slate-200 text-slate-700 hover:bg-slate-50 dark:border-slate-700 dark:text-slate-300 dark:hover:bg-slate-800',
              )}
            >
              <input
                type="radio"
                value={tipoEntrada}
                {...register('type')}
                className="size-4 accent-marca-600"
              />
              {tipoEntrada}
            </label>
          ))}
        </fieldset>
        {errors.type && (
          <p role="alert" className="mt-2 text-sm text-red-600">
            {errors.type.message}
          </p>
        )}

        <div className="mt-5 hidden md:block">
          {anonimo ? (
            enlaceLogin
          ) : (
            <Boton type="submit" className="w-full" cargando={cargando} disabled={noDisponible}>
              {noDisponible ? 'Venta no disponible' : 'Comprar entrada'}
            </Boton>
          )}
        </div>
        <p className="mt-3 text-xs text-slate-500 dark:text-slate-400">
          El precio lo define el sistema al confirmar.
        </p>
      </form>

      <div className="fixed inset-x-0 bottom-0 z-30 flex items-center gap-3 border-t border-slate-200 bg-white p-4 shadow-lg md:hidden dark:border-slate-800 dark:bg-slate-900">
        <div className="min-w-0 flex-1">
          <p className="text-xs text-slate-500 dark:text-slate-400">{tipo}</p>
          <p className="truncate text-sm font-semibold text-slate-900 dark:text-white">
            {evento.name}
          </p>
        </div>
        {anonimo ? (
          enlaceLogin
        ) : (
          <Boton onClick={() => void handleSubmit(onSubmit)()} cargando={cargando} disabled={noDisponible}>
            Comprar
          </Boton>
        )}
      </div>
      {!anonimo && !noDisponible && (
        <p className="sr-only">Cuenta asociada a la compra: {correo}</p>
      )}
    </>
  )
}
