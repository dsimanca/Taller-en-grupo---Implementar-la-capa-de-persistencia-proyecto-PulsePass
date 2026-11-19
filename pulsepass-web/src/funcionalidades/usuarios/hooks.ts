import { useMutation, useQuery } from '@tanstack/react-query'
import { registrarUsuarioApi, usuarioPorEmailApi, usuarioPorUsernameApi } from './api'

export const CLAVES_USUARIOS = {
  porEmail: (email: string) => ['usuario', email] as const,
  porUsername: (username: string) => ['usuario', 'username', username] as const,
}

export function useRegistrarUsuario() {
  return useMutation({ mutationFn: registrarUsuarioApi })
}

export function useUsuarioPorEmail(email: string | null) {
  return useQuery({
    queryKey: CLAVES_USUARIOS.porEmail(email ?? ''),
    queryFn: () => usuarioPorEmailApi(email as string),
    enabled: Boolean(email),
    staleTime: 60_000,
    retry: false,
  })
}

export function useUsuarioPorUsername(username: string | null) {
  return useQuery({
    queryKey: CLAVES_USUARIOS.porUsername(username ?? ''),
    queryFn: () => usuarioPorUsernameApi(username as string),
    enabled: Boolean(username),
    staleTime: 60_000,
    retry: false,
  })
}
