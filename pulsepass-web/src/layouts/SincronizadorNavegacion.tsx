import { useEffect } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router'
import { registrarNavegador } from '../lib/navegacion'

export function SincronizadorNavegacion() {
  const navegar = useNavigate()

  useEffect(() => registrarNavegador(navegar), [navegar])

  return null
}

export function TituloPorRuta() {
  const location = useLocation()
  useEffect(() => {
    const segmento = location.pathname === '/' ? 'Inicio' : 'PulsePass'
    document.title = `${segmento} · PulsePass`
  }, [location.pathname])
  return <Outlet />
}
