# PulsePass Web

Frontend de **PulsePass** (Fase 5), construido con React 19, TypeScript, Tailwind CSS v4, React Router v8, TanStack Query, React Hook Form + zod y Vitest.

Consume la API REST del backend Spring Boot (`/api`) sin reimplementar reglas de negocio.

## Requisitos

- Node.js 22.22+
- npm
- Backend de PulsePass ejecutándose en `http://localhost:8080` (Fases 3 y 4)

## Variables de entorno

Copiar `.env.example`:

```bash
VITE_API_BASE_URL=/api
```

En desarrollo Vite hace proxy de `/api` hacia `http://localhost:8080` (ver `vite.config.ts`).

## Comandos

```bash
npm install
npm run dev      # servidor de desarrollo
npm run build    # typecheck + build de producción
npm run lint     # ESLint
npm run test     # pruebas con Vitest + Testing Library + MSW
npm run preview  # previsualización del build
```

## Rutas principales

Públicas: `/`, `/eventos`, `/eventos/:eventCode`, `/artistas`, `/artistas/:id`, `/sedes`, `/sedes/:code`, `/login`, `/registro`.

Autenticadas: `/cuenta`.

USER: `/mis-tickets`, `/mis-tickets/:ticketCode`, `/compra/confirmacion/:ticketCode`.

STAFF/ADMIN: `/staff/validacion`, `/staff/eventos/:eventCode/tickets`.

ADMIN: `/admin/eventos`, `/admin/eventos/nuevo`, `/admin/eventos/:eventCode`.

Errores: `/403`, `*` (404).

## Roles de demostración

Según la semilla del backend (Fase 4): cuentas `USER` (p. ej. Andrea/Carlos), `STAFF` y `ADMIN` para operación y administración. El registro público crea siempre usuarios con rol `USER`.

## Arquitectura

- `src/lib/`: cliente HTTP (`http.ts`, `ApiError`), rutas, formato, query client, helpers de formularios.
- `src/funcionalidades/*`: `tipos.ts` (zod), `api.ts`, `hooks.ts`, componentes por dominio (auth, eventos, artistas, sedes, usuarios, tickets).
- `src/componentes/`: UI genérica reutilizable.
- `src/layouts/` y `src/pages/`: layout, guardas y páginas con carga perezosa.
- `src/router.tsx`: `createBrowserRouter` (React Router v8).

## Notas

- La sesión JWT se guarda en memoria y `sessionStorage`; nunca en `localStorage` ni en la URL.
- Los datos de la API se validan con zod y el estado de servidor se gestiona con TanStack Query.
- Las reglas de negocio y la autorización real las aplica el backend.