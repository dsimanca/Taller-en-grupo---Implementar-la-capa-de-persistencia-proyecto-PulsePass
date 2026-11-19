import { http, HttpResponse } from 'msw'

const API = '*/api'

export const eventoPublicado = {
  id: 1,
  eventCode: 'CMF-2026',
  name: 'Caribbean Music Fest 2026',
  description: 'Festival de música caribeña.',
  category: 'MUSIC',
  status: 'PUBLISHED',
  eventDate: '2026-12-12T20:00:00',
  minimumAge: 18,
  venueCode: 'VEN-SMR-01',
  venueName: 'Marina Convention Center',
  artists: [
    { id: 1, stageName: 'Solar Beat', country: 'CO', genre: 'Pop', active: true },
  ],
}

export const resumenEvento = {
  id: 1,
  eventCode: 'CMF-2026',
  name: 'Caribbean Music Fest 2026',
  category: 'MUSIC',
  status: 'PUBLISHED',
  eventDate: '2026-12-12T20:00:00',
  venueName: 'Marina Convention Center',
}

export const artista = {
  id: 1,
  stageName: 'Solar Beat',
  country: 'Colombia',
  genre: 'Pop',
  active: true,
}

export const sede = {
  id: 1,
  code: 'VEN-SMR-01',
  name: 'Marina Convention Center',
  city: 'Santa Marta',
  address: 'Centro',
  capacity: 3,
  active: true,
}

export const usuarioAutenticado = {
  id: 10,
  username: 'andrea',
  email: 'andrea@pulsepass.test',
  roles: ['USER'],
}

export const usuarioPerfil = {
  id: 10,
  username: 'andrea',
  email: 'andrea@pulsepass.test',
  active: true,
  firstName: 'Andrea',
  lastName: 'Ruiz',
  phone: '3001234567',
  city: 'Bogotá',
  birthDate: '1998-04-12',
}

export const ticketPagado = {
  id: 1,
  ticketCode: 'TKT-8F2A1C',
  type: 'VIP',
  price: 250000,
  status: 'PAID',
  purchaseDate: '2026-11-05T14:32:00',
  userEmail: 'andrea@pulsepass.test',
  eventCode: 'CMF-2026',
  eventName: 'Caribbean Music Fest 2026',
}

export const handlers = [
  http.get(`${API}/events/published`, () => HttpResponse.json([resumenEvento])),
  http.get(`${API}/events/:eventCode`, ({ params }) => {
    if (params.eventCode === 'CMF-2026') return HttpResponse.json(eventoPublicado)
    return HttpResponse.json(
      { status: 404, error: 'Not Found', message: 'Event not found', details: {} },
      { status: 404 },
    )
  }),
  http.get(`${API}/events/by-artist`, () => HttpResponse.json([resumenEvento])),
  http.get(`${API}/artists/active`, () => HttpResponse.json([artista])),
  http.get(`${API}/artists/:id`, ({ params }) => {
    if (params.id === '1') return HttpResponse.json(artista)
    return HttpResponse.json(
      { status: 404, error: 'Not Found', message: 'Artist not found', details: {} },
      { status: 404 },
    )
  }),
  http.get(`${API}/artists/by-stage-name`, () => HttpResponse.json(artista)),
  http.get(`${API}/venues/active`, () => HttpResponse.json([sede])),
  http.get(`${API}/venues/:code`, ({ params }) => {
    if (params.code === 'VEN-SMR-01') return HttpResponse.json(sede)
    return HttpResponse.json(
      { status: 404, error: 'Not Found', message: 'Venue not found', details: {} },
      { status: 404 },
    )
  }),
  http.post(`${API}/auth/login`, () => HttpResponse.json({ accessToken: 'token', tokenType: 'Bearer', expiresIn: 900 })),
  http.get(`${API}/auth/me`, () => HttpResponse.json(usuarioAutenticado)),
  http.post(`${API}/users`, () => HttpResponse.json(usuarioPerfil, { status: 201 })),
  http.get(`${API}/users/by-email`, () => HttpResponse.json(usuarioPerfil)),
  http.get(`${API}/users/by-username`, () => HttpResponse.json(usuarioPerfil)),
  http.get(`${API}/tickets/by-user`, () => HttpResponse.json([ticketPagado])),
  http.get(`${API}/tickets/:ticketCode`, () => HttpResponse.json(ticketPagado)),
  http.post(`${API}/tickets`, () => HttpResponse.json(ticketPagado, { status: 201 })),
  http.patch(`${API}/tickets/:ticketCode/cancel`, () =>
    HttpResponse.json({ ...ticketPagado, status: 'CANCELLED' }),
  ),
  http.patch(`${API}/tickets/:ticketCode/use`, () =>
    HttpResponse.json({ ...ticketPagado, status: 'USED' }),
  ),
  http.get(`${API}/events/:eventCode/tickets/paid`, () => HttpResponse.json([ticketPagado])),
  http.post(`${API}/events`, () => HttpResponse.json({ ...eventoPublicado, status: 'DRAFT' }, { status: 201 })),
  http.patch(`${API}/events/:eventCode/publish`, () => HttpResponse.json(eventoPublicado)),
  http.post(`${API}/events/:eventCode/artists/:artistId`, () => HttpResponse.json(eventoPublicado)),
]