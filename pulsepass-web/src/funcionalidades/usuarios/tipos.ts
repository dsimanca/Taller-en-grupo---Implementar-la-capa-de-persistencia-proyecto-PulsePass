import { z } from 'zod'

export const UserResponseSchema = z.object({
  id: z.number(),
  username: z.string(),
  email: z.email(),
  active: z.boolean(),
  firstName: z.string(),
  lastName: z.string(),
  phone: z.string(),
  city: z.string(),
  birthDate: z.string(),
})
export type Usuario = z.infer<typeof UserResponseSchema>

export const RegisterUserRequestSchema = z.object({
  username: z.string().min(1, 'El nombre de usuario es obligatorio'),
  email: z.email('Ingresa un correo válido'),
  password: z
    .string()
    .min(12, 'La contraseña debe tener al menos 12 caracteres')
    .max(72, 'La contraseña no puede superar 72 caracteres')
    .refine((valor) => new TextEncoder().encode(valor).length <= 72, {
      message: 'La contraseña no puede superar 72 bytes UTF-8',
    }),
  firstName: z.string().min(1, 'Los nombres son obligatorios'),
  lastName: z.string().min(1, 'Los apellidos son obligatorios'),
  phone: z.string().min(1, 'El teléfono es obligatorio'),
  city: z.string().min(1, 'La ciudad es obligatoria'),
  birthDate: z
    .string()
    .regex(/^\d{4}-\d{2}-\d{2}$/, 'Selecciona una fecha válida')
    .refine((valor) => new Date(`${valor}T00:00:00Z`) < new Date(), {
      message: 'La fecha de nacimiento debe estar en el pasado',
    }),
})
export type RegisterUserRequest = z.infer<typeof RegisterUserRequestSchema>
export type RegistroForm = z.infer<typeof RegisterUserRequestSchema>
