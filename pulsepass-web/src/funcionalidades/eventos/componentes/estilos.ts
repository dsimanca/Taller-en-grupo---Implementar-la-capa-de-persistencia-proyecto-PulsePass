import type { NombreIcono } from '../../../componentes/Icono'
import type { EventCategory } from '../tipos'

export const ESTILO_CATEGORIA: Record<
  EventCategory,
  { fondo: string; texto: string; icono: NombreIcono; etiqueta: string }
> = {
  MUSIC: {
    fondo: 'bg-indigo-100 dark:bg-indigo-950',
    texto: 'text-indigo-700 dark:text-indigo-300',
    icono: 'marca',
    etiqueta: 'Música',
  },
  SPORTS: {
    fondo: 'bg-emerald-100 dark:bg-emerald-950',
    texto: 'text-emerald-700 dark:text-emerald-300',
    icono: 'escudo',
    etiqueta: 'Deportes',
  },
  TECHNOLOGY: {
    fondo: 'bg-sky-100 dark:bg-sky-950',
    texto: 'text-sky-700 dark:text-sky-300',
    icono: 'rayo',
    etiqueta: 'Tecnología',
  },
  EDUCATION: {
    fondo: 'bg-amber-100 dark:bg-amber-950',
    texto: 'text-amber-700 dark:text-amber-300',
    icono: 'libro',
    etiqueta: 'Educación',
  },
  CULTURE: {
    fondo: 'bg-violet-100 dark:bg-violet-950',
    texto: 'text-violet-700 dark:text-violet-300',
    icono: 'marca',
    etiqueta: 'Cultura',
  },
  ENTERTAINMENT: {
    fondo: 'bg-rose-100 dark:bg-rose-950',
    texto: 'text-rose-700 dark:text-rose-300',
    icono: 'boleto',
    etiqueta: 'Entretenimiento',
  },
}
