import { Boton } from '../../../componentes/Boton'
import { Icono } from '../../../componentes/Icono'
import { Modal } from '../../../componentes/Modal'
import type { Ticket } from '../tipos'

export function ModalConfirmarCancelacion({
  ticket,
  cargando,
  error,
  onCerrar,
  onConfirmar,
}: {
  ticket: Ticket | null
  cargando: boolean
  error: string | null
  onCerrar: () => void
  onConfirmar: () => void
}) {
  return (
    <Modal
      abierto={ticket !== null}
      titulo="Cancelar ticket"
      onCerrar={onCerrar}
    >
      {ticket && (
        <>
          <p className="text-sm text-slate-600 dark:text-slate-300">
            ¿Quieres cancelar el ticket{' '}
            <span className="font-mono font-semibold">{ticket.ticketCode}</span> ({ticket.type}) de{' '}
            <span className="font-semibold">{ticket.eventName}</span>?
          </p>
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
              Volver
            </Boton>
            <Boton variante="peligro" onClick={onConfirmar} cargando={cargando}>
              Cancelar ticket
            </Boton>
          </div>
        </>
      )}
    </Modal>
  )
}
