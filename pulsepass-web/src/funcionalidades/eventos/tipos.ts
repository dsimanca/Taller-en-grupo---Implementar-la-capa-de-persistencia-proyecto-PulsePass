import { z } from 'zod'
import { ArtistResponseSchema } from '../artistas/tipos'

export const EventCategorySchema = z.enum([
  'MUSIC',
  'SPORTS',
  'TECHNOLOGY',
  'EDUCATION',
  'CULTURE',
  'ENTERTAINMENT',
])
export const EventStatusSchema = z.enum([
  'DRAFT',
  'PUBLISHED',
  'SOLD_OUT',
  'CANCELLED',
  'FINISHED',
])
export type EventCategory = z.infer<typeof EventCategorySchema>
export type EventStatus = z.infer<typeof EventStatusSchema>

export const EventSummaryResponseSchema = z.object({
  id: z.number(),
  eventCode: z.string(),
  name: z.string(),
  category: EventCategorySchema,
  status: EventStatusSchema,
  eventDate: z.string(),
  venueName: z.string().nullish(),
})
export type EventSummaryResponse = z.infer<typeof EventSummaryResponseSchema>

export const EventResponseSchema = EventSummaryResponseSchema.extend({
  description: z.string().nullish(),
  minimumAge: z.number().int().nonnegative(),
  venueCode: z.string(),
  artists: z.array(ArtistResponseSchema),
})
export type EventResponse = z.infer<typeof EventResponseSchema>

export const CreateEventRequestSchema = z.object({
  eventCode: z.string().min(1, 'El código del evento es obligatorio'),
  name: z.string().min(1, 'El nombre es obligatorio'),
  description: z.string().max(500, 'La descripción no puede superar 500 caracteres'),
  category: EventCategorySchema,
  eventDate: z.string().min(1, 'La fecha y hora son obligatorias'),
  minimumAge: z.number().int().nonnegative('La edad mínima debe ser 0 o mayor'),
  venueCode: z.string().min(1, 'Selecciona una sede'),
})
export type CreateEventRequest = z.infer<typeof CreateEventRequestSchema>
export type CreateEventForm = z.infer<typeof CreateEventRequestSchema>
