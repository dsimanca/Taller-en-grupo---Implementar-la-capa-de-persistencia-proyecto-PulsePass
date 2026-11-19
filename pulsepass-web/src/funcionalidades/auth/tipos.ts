import { z } from 'zod'

export const RolSchema = z.enum(['USER', 'STAFF', 'ADMIN'])
export type Rol = z.infer<typeof RolSchema>

export const LoginRequestSchema = z.object({
  identifier: z.string().min(1, 'Ingresa tu usuario o correo'),
  password: z.string().min(1, 'Ingresa tu contraseña'),
})
export type LoginRequest = z.infer<typeof LoginRequestSchema>
export type LoginForm = z.infer<typeof LoginRequestSchema>

export const AuthResponseSchema = z.object({
  accessToken: z.string().min(1),
  tokenType: z.literal('Bearer'),
  expiresIn: z.number().positive(),
})
export type AuthResponse = z.infer<typeof AuthResponseSchema>

export const AuthenticatedUserResponseSchema = z.object({
  id: z.number(),
  username: z.string(),
  email: z.email(),
  roles: z.array(RolSchema),
})
export type UsuarioAutenticado = z.infer<typeof AuthenticatedUserResponseSchema>

export const SesionGuardadaSchema = z.object({
  token: z.string().min(1),
  expiraEn: z.number().positive(),
  usuario: AuthenticatedUserResponseSchema,
})
export type SesionGuardada = z.infer<typeof SesionGuardadaSchema>
