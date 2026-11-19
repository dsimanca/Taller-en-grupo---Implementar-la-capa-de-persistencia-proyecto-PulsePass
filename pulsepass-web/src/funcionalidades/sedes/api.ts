import { z } from 'zod'
import { httpGet } from '../../lib/http'
import { VenueResponseSchema } from './tipos'

const ListaSedesSchema = z.array(VenueResponseSchema)

export function sedesActivasApi() {
  return httpGet('/venues/active', ListaSedesSchema)
}

export function sedeApi(code: string) {
  return httpGet(`/venues/${encodeURIComponent(code)}`, VenueResponseSchema)
}
