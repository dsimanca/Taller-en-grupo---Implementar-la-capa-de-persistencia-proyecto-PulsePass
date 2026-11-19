import { describe, expect, it } from 'vitest'
import { guardarSesion, leerSesionGuardada, limpiarSesion } from './sesion'

import type { SesionGuardada } from './tipos'

const usuario: SesionGuardada['usuario'] = {
  id: 1,
  username: 'u',
  email: 'u@test.co',
  roles: ['USER'],
}

describe('sesión', () => {
  it('guarda y restaura sesión válida', () => {
    guardarSesion({ token: 't', expiraEn: Date.now() + 900_000, usuario })
    const restaurada = leerSesionGuardada()
    expect(restaurada?.token).toBe('t')
    expect(restaurada?.usuario.username).toBe('u')
    limpiarSesion()
  })

  it('descarta sesión expirada o inválida', () => {
    guardarSesion({ token: 't', expiraEn: Date.now() - 1, usuario })
    expect(leerSesionGuardada()).toBeNull()
    sessionStorage.setItem('pulsepass.sesion', '{malo')
    expect(leerSesionGuardada()).toBeNull()
  })
})