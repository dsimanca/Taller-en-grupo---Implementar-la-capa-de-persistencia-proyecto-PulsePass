import { Boton } from '../../../componentes/Boton'
import { Icono } from '../../../componentes/Icono'
import { Modal } from '../../../componentes/Modal'
import type { TicketType } from '../../tickets/tipos'
import type { EventResponse } from '../tipos'

export function ModalConfirmarCompra({
  abierto,
  evento,
  tipo,
  correo,
  cargando,
  error,
  onCerrar,
  onConfirmar,
}: {
  abierto: boolean
  evento: EventResponse
  tipo: TicketType
  correo: string
  cargando: boolean
  error: string | null
  onCerrar: () => void
  onConfirmar: () => void
}) {
  return (
    <Modal abierto={abierto} titulo="Confirmar compra" onCerrar={onCerrar}>
      <dl className="space-y-2 text-sm">
        <div className="flex justify-between gap-4">
          <dt className="text-slate-500 dark:text-slate-400">Evento</dt>
          <dd className="text-right font-medium text-slate-900 dark:text-white">{evento.name}</dd>
        </div>
        <div className="flex justify-between gap-4">
          <dt className="text-slate-500 dark:text-slate-400">Tipo</dt>
          <dd className="font-medium text-slate-900 dark:text-white">{tipo}</dd>
        </div>
        <div className="flex justify-between gap-4">
          <dt className="text-slate-500 dark:text-slate-400">Cuenta</dt>
          <dd className="text-right font-medium break-all text-slate-900 dark:text-white">
            {correo}
          </dd>
        </div>
      </dl>

      {error && (
        <div
          role="alert"
          className="mt-4 flex items-start gap-2 rounded-lg bg-red-100 px-3 py-2 text-sm text-red-800"
        >
          <Icono nombre="alerta" className="mt-0.5 size-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      <div className="mt-6 flex justify-end gap-3">
        <Boton variante="secundario" onClick={onCerrar}>
          Cancelar
        </Boton>
        <Boton onClick={onConfirmar} cargando={cargando}>
          Confirmar
        </Boton>
      </div>
    </Modal>
  )
}
