import { httpGet, httpPost } from '../../lib/http'
import {
  AuthResponseSchema,
  AuthenticatedUserResponseSchema,
  type LoginRequest,
} from './tipos'

export function loginApi(credenciales: LoginRequest) {
  return httpPost('/auth/login', AuthResponseSchema, credenciales, {
    skipAuth: true,
    skipUnauthorized: true,
  })
}

export function sesionApi(token?: string) {
  return httpGet('/auth/me', AuthenticatedUserResponseSchema, { token })
}
