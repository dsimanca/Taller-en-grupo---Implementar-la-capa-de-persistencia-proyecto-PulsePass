import { z } from 'zod'
import { httpGet } from '../../lib/http'
import { ArtistResponseSchema, type Artista } from './tipos'

const ListaArtistasSchema = z.array(ArtistResponseSchema)

export function artistasActivosApi() {
  return httpGet('/artists/active', ListaArtistasSchema)
}

export function artistaApi(id: number) {
  return httpGet(`/artists/${id}`, ArtistResponseSchema)
}

export function artistaPorNombreApi(stageName: string) {
  return httpGet(
    `/artists/by-stage-name?stageName=${encodeURIComponent(stageName)}`,
    ArtistResponseSchema,
  )
}

export type { Artista }
