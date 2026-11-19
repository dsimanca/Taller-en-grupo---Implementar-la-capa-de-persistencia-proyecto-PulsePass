import { httpGet, httpPost } from '../../lib/http'
import { UserResponseSchema, type RegisterUserRequest } from './tipos'

export function registrarUsuarioApi(datos: RegisterUserRequest) {
  return httpPost('/users', UserResponseSchema, datos, { skipAuth: true })
}

export function usuarioPorEmailApi(email: string) {
  return httpGet(`/users/by-email?email=${encodeURIComponent(email)}`, UserResponseSchema)
}

export function usuarioPorUsernameApi(username: string) {
  return httpGet(
    `/users/by-username?username=${encodeURIComponent(username)}`,
    UserResponseSchema,
  )
}
