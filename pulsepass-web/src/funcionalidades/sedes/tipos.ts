import { z } from 'zod'

export const VenueResponseSchema = z.object({
  id: z.number(),
  code: z.string(),
  name: z.string(),
  city: z.string(),
  address: z.string().nullish(),
  capacity: z.number().int().nonnegative(),
  active: z.boolean(),
})
export type Sede = z.infer<typeof VenueResponseSchema>
