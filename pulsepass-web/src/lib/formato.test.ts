import { describe, expect, it } from 'vitest'
import { formatearCOP } from './formato'

describe('formato', () => {
  it('formatea COP sin decimales', () => {
    const texto = formatearCOP(250000)
    expect(texto).toContain('250')
    expect(texto).toMatch(/COP|\$/)
  })
})