PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

# PulsePass — Product Requirements Document 

#### **Plataforma de eventos, artistas y entradas** 

_Caso de estudio académico — Fase 5: Frontend con React, TypeScript y Tailwind CSS_ 

|Documento|**PRD - PulsePass — Frontend Web**|
|---|---|
|**Versión**|1.0|
|**Estado**|Propuesto para taller académico|
|**Fecha**|8 de noviembre de 2026|
|**Tecnologías**<br>**objetivo**|Node 22.22+ / Vite / React 19.2.7+ / TypeScript (strict) / Tailwind CSS v4 / React Router v8 / TanStack Query /<br>React Hook Form + zod / Vitest + Testing Library|
|**Alcance técnico**|SPA que consume la API REST segura (20 operaciones + 3 de autenticación): páginas, rutas, diseño de vistas,<br>estado, validación de formularios, manejo de errores y pruebas de UI|
|**Predecesores**|Fase 1 Persistencia → Fase 2 Servicios → Fase 3 Controladores REST → Fase 4 Seguridad (Spring Security + JJWT)|



Documento de requisitos del producto 

# Control del documento 

|**Campo**|**Detalle**|
|---|---|
|Propósito|Definir los requisitos funcionales, el mapa de páginas, los diseños de las vistas, los contratos de datos consumidos,<br>los criterios de aceptación y la estrategia de pruebas para construir el cliente web de PulsePass sobre la API REST y la<br>capa de seguridad ya especificadas.|
|Audiencia|Estudiantes, docentes, diseñadores, equipo de desarrollo frontend y equipo QA del taller.|
|Diseños|Los diseños de alta fidelidad (32 imágenes PNG) están en la carpeta`imagenes/`junto a este documento y se<br>indexan en la sección 9.0.|
|Dependencias|Este documento continúa los PRD de Persistencia, Servicios, Controladores REST y Seguridad. No redefine el<br>dominio, las reglas de negocio ni los contratos HTTP; los consume. Como guías técnicas de referencia se usan`Guía`<br>`completa para aprender React con TypeScript.md`,`guia-rutas-react-typescript.md`(React Router v8) y<br>`guia-css-tailwind-react.md`(Tailwind v4).|
|Alcance|MVP académico de una_Single Page Application_(SPA) con catálogo público, registro/login, compra y gestión de<br>tickets propios, validación de ingreso (STAFF) y administración de eventos (ADMIN).|
|Fuera de<br>alcance|Cambios en el backend, pasarela de pagos, generación de QR, refresh tokens, MFA, recuperación de contraseña,<br>SSR/Next.js, PWA, internacionalización multi-idioma, pruebas E2E automatizadas y despliegue productivo.|



# 1. Resumen ejecutivo 

PulsePass es una plataforma para descubrir eventos y gestionar entradas para conciertos, festivales, conferencias, eventos universitarios, competencias deportivas y actividades culturales. 

La implementación del caso de estudio se desarrolla de forma incremental: 

```
Fase 1 — Persistencia
    ↓
Modelo relacional · Entidades JPA · Repositories · Flyway · PostgreSQL
Fase 2 — Servicios
    ↓
Reglas de negocio · DTOs · MapStruct · Transacciones · Unit tests
```

1 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

```
Fase 3 — Controladores REST
    ↓
20 endpoints HTTP · Validación · ErrorResponse · MockMvc
Fase 4 — Seguridad
    ↓
Login · JWT (JJWT) · Roles USER/STAFF/ADMIN · Propiedad de recursos
Fase 5 — Frontend                                   ← este documento
    ↓
React · TypeScript · Tailwind · Router v8 · TanStack Query · Vitest
```

Este PRD corresponde exclusivamente a la **Fase 5** . 

El frontend deberá transformar las interacciones de las personas usuarias en llamadas HTTP a la API existente, y transformar las respuestas y errores de la API en vistas claras, accesibles y consistentes. 

#### **Principio de producto** 

El frontend presenta y orquesta; no decide. Las reglas de negocio (edad mínima, capacidad, estados, propiedad, permisos) pertenecen al backend. La interfaz puede ocultar o deshabilitar acciones por experiencia de usuario, pero nunca las considera una medida de seguridad. 

# 2. Visión y problema 

## 2.1 Problema 

La API de PulsePass ya permite consultar eventos, registrarse, autenticarse, comprar, cancelar y validar tickets, pero únicamente puede usarse mediante herramientas como Postman o cURL. Las personas usuarias necesitan: 

descubrir eventos publicados, sus artistas y su sede; 

- crear una cuenta e iniciar sesión; 

- comprar entradas y consultar sus tickets; 

- cancelar un ticket propio cuando la regla de negocio lo permite; 

- (personal de operación) validar el ingreso marcando tickets como usados; 

- (administración) crear eventos, publicarlos y asociar artistas; 

- recibir mensajes de error comprensibles cuando el backend rechaza una operación. 

## 2.2 Visión 

Construir una aplicación web responsive y accesible, tipada de extremo a extremo, que consuma la API segura de PulsePass sin duplicar su lógica y que sirva como cliente de referencia para futuros canales (aplicación móvil, cliente administrativo). 

## 2.3 Objetivos del MVP de Frontend 

- Exponer en la interfaz las 20 operaciones REST de la Fase 3 y los 3 endpoints de autenticación de la Fase 4. 

- Definir y construir la estructura de páginas y rutas con React Router v8. 

Implementar los diseños de las vistas definidos en la sección 9 usando Tailwind CSS v4. 

- Tipar y validar en ejecución todo dato que entra desde la API (zod) y todo formulario (React Hook Form + zod). Gestionar sesión con JWT (Bearer) y guardas de ruta por rol. 

- Mapear de forma uniforme los códigos 400/401/403/404/409/500 y los errores de red a mensajes de interfaz. Ser _mobile-first_ , accesible (WCAG 2.1 AA como objetivo) y soportar modo claro/oscuro. 

- Garantizar pruebas de componentes, guardas, formularios y mapeo de errores. 

## 2.4 No objetivos 

Implementar o modificar endpoints, reglas de negocio o el modelo relacional. 

Procesar pagos reales ni generar códigos QR. 

Almacenar contraseñas o secretos en el cliente. 

Implementar refresh tokens, MFA, recuperación de contraseña o inicio de sesión social. 

2 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

Renderizado en servidor (SSR/SSG), PWA o modo offline. 

Paginación, ordenamiento dinámico o búsqueda en servidor (no existen en la API). 

Pruebas E2E con navegador real (se mencionan como evolución futura). 

# 3. Relación con los PRD anteriores 

Este documento es una extensión directa de los cuatro PRD anteriores. 

## 3.1 Fase 1 — Persistencia 

El frontend consume (indirectamente) el modelo: 

```
Venue · Event · Artist · User · UserProfile · Ticket
```

y sus enumeraciones, que se replican en el cliente como tipos literales de TypeScript: 

|**Enum**|**Valores**|
|---|---|
|`EventCategory`|`MUSIC`,`SPORTS`,`TECHNOLOGY`,`EDUCATION`,`CULTURE`,`ENTERTAINMENT`|
|`EventStatus`|`DRAFT`,`PUBLISHED`,`SOLD_OUT`,`CANCELLED`,`FINISHED`|
|`TicketType`|`GENERAL`,`VIP`,`BACKSTAGE`,`STUDENT`|
|`TicketStatus`|`RESERVED`,`PAID`,`CANCELLED`,`USED`|



## 3.2 Fase 2 — Servicios 

Los DTO de respuesta ( `VenueResponse` , `EventResponse` , `EventSummaryResponse` , `ArtistResponse` , `UserResponse` , `TicketResponse` ) y de solicitud ( `CreateEventRequest` , `RegisterUserRequest` , `PurchaseTicketRequest` ) son la fuente de los tipos del cliente. Las reglas de negocio heredadas se muestran como mensajes de error `409` ; **no se reimplementan** en la UI. 

## 3.3 Fase 3 — Controladores REST 

Se consumen las 20 operaciones (2 Venue, 6 Event, 3 Artist, 3 User, 6 Ticket) y el contrato uniforme de errores: 

```
{ "timestamp": "...", "status": 409, "error": "Conflict", "message": "...", "details": {} }
```

## 3.4 Fase 4 — Seguridad 

Se consumen `POST /api/auth/login` , `GET /api/auth/me` y `POST /api/users` (con `password` ). La API es _stateless_ con `Authorization: Bearer <JWT>` , token de acceso de 900 s (15 min) y roles `USER` , `STAFF` , `ADMIN` . 

## 3.5 Compatibilidad y brechas conocidas del backend 

El frontend se diseña **sin pedir cambios al backend** . Las siguientes brechas se aceptan como restricciones y se resuelven del lado del cliente: 

|**ID**|**Brecha del backend**|**Decisión en el frontend**|
|---|---|---|
|GAP-<br>01|No existe`GET /api/events`(listar todos). Solo`GET`<br>`/api/events/published`.|El catálogo usa eventos publicados. El panel ADMIN consulta<br>borradores por`eventCode`(los recién creados se recuerdan en la<br>sesión) y no ofrece listado completo.|
|GAP-<br>02|`GET /api/events/{eventCode}`es público solo si el<br>evento está publicado; borradores solo para<br>`STAFF`/`ADMIN`.|Un`404/403`ante un código de borrador se presenta como<br>"Evento no disponible".|



3 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

|**ID**|**Brecha del backend**|**Decisión en el frontend**|
|---|---|---|
|GAP-<br>03|No existe consulta de tickets por ID de usuario.|"Mis tickets" usa`GET /api/tickets/by-user?email=`con el<br>email devuelto por`GET /api/auth/me`.|
|GAP-|La respuesta de evento no informa entradas|Se muestra capacidad de la sede y`status`; la compra puede|
|04|disponibles.|responder`409`(capacidad,`SOLD_OUT`).|
|GAP-<br>05|La Fase 3 excluyó CORS.|En desarrollo se usa_proxy_de Vite (`/api`→ API). En producción:<br>mismo origen o CORS explícito configurado en backend (riesgo R-<br>FE-005).|
|GAP-|`PurchaseTicketRequest`solo lleva`userEmail`,|El precio se muestra tomándolo de`TicketResponse`tras la|
|06|`eventCode`,`type`; el precio lo fija el backend.|compra; la UI no calcula ni envía precios.|
|GAP-<br>07|El registro no emite token.|Flujo`registro → login`(con acceso directo a`/login`).|
|GAP-<br>08|No hay refresh token ni_logout_en servidor.|Al expirar el token (401) se cierra la sesión local y se redirige a<br>`/login`.|



# 4. Actores y necesidades 

|**Actor**|**Necesidad principal**|
|---|---|
|Visitante anónimo|Explorar eventos publicados, artistas y sedes; registrarse e iniciar sesión.|
|`USER`(asistente)|Comprar tickets, ver y cancelar sus tickets, consultar su cuenta.|
|`STAFF`(operación)|Consultar tickets pagados de un evento y marcar tickets como usados en el ingreso.|
|`ADMIN`|Crear eventos, publicarlos y asociar artistas.|
|Diseño / UX|Un sistema visual coherente, accesible y responsive.|
|Equipo frontend|Una arquitectura tipada, modular y fácil de probar.|
|Equipo QA|Criterios verificables y pruebas reproducibles sin backend real.|



# 5. Arquitectura esperada 

4 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



5 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
Navegador<br>RouterProvider<br>- React Router<br>v8<br>|__|-<br>Layout raiz con<br>Navbar y Footer<br>Guarda de ruta<br>sin sesion publica autenticada o por rol rol insuficiente<br>Redireccion a  Pagina 403<br>Pagina<br>/login<br>—_——<br>Hook TanStack<br>Query /<br>Mutation<br>a<br>api.ts por<br>funcionalidad<br>Cliente HTTP<br>lib/http.ts<br>Validacion zod<br>4xx 5xx 401 Authorization Bearer<br>de la respuesta<br>| a<br><!-- End of picture text -->

6 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

ApiError tipado AuthContext: cerrar sesion API REST PulsePass 

La separación obligatoria es: 

```
Pagina / Componente      →  presentación y composición
Hook (TanStack Query)    →  estado de servidor (carga, error, caché, invalidación)
api.ts                   →  llamadas HTTP + validación zod
lib/http.ts              →  fetch, Bearer, ErrorResponse → ApiError
AuthContext              →  sesión, token, rol
```

# 6. Principios de diseño 

|**ID**|**Principio**|
|---|---|
|FE-<br>001|La UI no implementa reglas de negocio; las traduce desde las respuestas de la API.|
|FE-<br>002|Todo dato externo (API,`sessionStorage`, parámetros de URL) se valida con zod; los tipos se derivan con`z.infer`.|
|FE-<br>003|Las rutas se centralizan en`RUTAS`(`lib/rutas.ts`); no se escriben_strings_de ruta sueltos.|
|FE-<br>004|Las guardas de ruta (`RutaProtegida`,`RutaPorRol`) son solo experiencia de usuario; la seguridad real la aplica el servidor.|
|FE-<br>005|Los datos del servidor se gestionan con TanStack Query; no se duplican en`useState`/Context.|
|FE-<br>006|Los estados asíncronos y la sesión se modelan con uniones discriminadas, no con varios booleanos.|
|FE-|Un solo enfoque de enrutamiento:`createBrowserRouter`+`RouterProvider`(`react-router/dom`). No mezclar con|
|007|`<BrowserRouter>`.|
|FE-<br>008|Las clases de Tailwind se escriben completas; nunca se construyen dinámicamente (``bg-${x}-500``). Las variantes usan<br>`Record<Variante, string>`y`clsx`.|
|FE-<br>009|_Mobile-first_: se diseña para 360 px y se amplía con`sm:`,`md:`,`lg:`.|
|FE-|Accesibilidad: HTML semántico,`label`en cada campo,`alt`en imágenes, foco visible (`focus-visible:`), contraste AA,|
|010|navegación por teclado,`aria-live`para errores.|
|FE-<br>011|Componentes funcionales con props tipadas (`interface`); sin`React.FC`; sin`any`;`unknown`+ narrowing en`catch`.|
|FE-<br>012|Toda página se carga de forma perezosa (`lazy`) excepto el layout y la página de inicio.|
|FE-<br>013|El token JWT nunca viaja en la URL ni se registra en consola; solo en el encabezado`Authorization`.|
|FE-<br>014|Cada pantalla define explícitamente sus estados: cargando, vacío, error y éxito.|



7 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

# 7. Alcance funcional del MVP 

El MVP de Frontend se divide en siete capacidades. 

## 7.1 Catálogo de eventos 

|**ID**|**Requisito**|**Descripción**|**Prioridad**|**Criterio de aceptación**|
|---|---|---|---|---|
|FR-FE-<br>EVT-<br>001|Ver<br>cartelera|Listar eventos publicados con tarjetas (nombre,<br>fecha, categoría, estado).|Must|`GET /api/events/published`→ tarjetas;<br>lista vacía muestra estado vacío.|
|FR-FE-<br>EVT-<br>002|Filtrar en<br>cliente|Filtrar la cartelera por texto y categoría usando<br>_query string_(`?q=&categoria=`).|Should|El filtro persiste en la URL y sobrevive a<br>recargar.|
|FR-FE-<br>EVT-<br>003|Ver detalle|Mostrar datos del evento, sede, artistas y acción<br>de compra.|Must|`GET /api/events/{eventCode}`→`200`<br>detalle;`404`→ "Evento no disponible".|
|FR-FE-<br>EVT-<br>004|Buscar por<br>artista|Mostrar eventos de un artista (`?artista=`).|Must|`GET /api/events/by-artist?`<br>`stageName=`→ lista.|
|FR-FE-<br>EVT-<br>005|Estado<br>visible|Mostrar`Badge`según`EventStatus`;<br>`SOLD_OUT`/`CANCELLED`/`FINISHED`deshabilitan la<br>compra.|Must|El botón "Comprar" no se muestra<br>habilitado fuera de`PUBLISHED`.|



## 7.2 Artistas y sedes 

|**ID**|**Requisito**|**Descripción**|**Prioridad**|**Criterio de aceptación**|
|---|---|---|---|---|
|FR-FE-ART-<br>001|Listar artistas<br>activos|Cuadrícula de artistas.|Must|`GET /api/artists/active`.|
|FR-FE-ART-<br>002|Detalle de artista|Perfil y eventos asociados.|Must|`GET /api/artists/{id}`y`by-artist`;`404`→<br>página no encontrada.|
|FR-FE-ART-<br>003|Buscar artista por<br>nombre|Resolver`stageName`a<br>artista.|Should|`GET /api/artists/by-stage-name?stageName=`.|
|FR-FE-<br>VEN-001|Listar sedes activas|Tarjetas de sedes (ciudad,<br>capacidad).|Must|`GET /api/venues/active`.|
|FR-FE-<br>VEN-002|Detalle de sede|Datos de la sede por<br>código.|Must|`GET /api/venues/{code}`;`404`→ no encontrada.|



## 7.3 Autenticación y cuenta 

|**ID**|**Requisito**|**Descripción**|**Prioridad**|**Criterio de aceptación**|
|---|---|---|---|---|
|FR-FE-||Formulario con username, email,||`POST /api/users`→`201`y redirección a|
|AUTH-|Registro|nombres, fecha de nacimiento y|Must|`/login`con aviso;`409`muestra duplicado sin|
|001||contraseña (≥ 12).||detalles internos.|
|FR-FE-||||`POST /api/auth/login`→ token guardado,|
|AUTH-|Login|Formulario`identifier`+`password`.|Must|`GET /api/auth/me`cargado, redirección a la|
|002||||ruta de origen.|
|FR-FE-<br>AUTH-<br>003|Credenciales<br>inválidas|Mensaje genérico.|Must|`401`muestra "Credenciales inválidas" sin revelar<br>si el usuario existe.|



8 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

|**ID**|**Requisito**|**Descripción**|**Prioridad**|**Criterio de aceptación**|
|---|---|---|---|---|
|FR-FE-||Persistir token mientras dure la|||
|AUTH-|Sesión|pestaña y restaurar la sesión al|Must|Recarga conserva sesión si el token no expiró.|
|004||recargar.|||
|FR-FE-<br>AUTH-<br>005|Expiración|Ante`401`en ruta protegida, cerrar<br>sesión y redirigir.|Must|Se muestra "Tu sesión expiró" y se vuelve a la<br>ruta tras iniciar sesión.|
|FR-FE-|||||
|AUTH-<br>006|Cerrar sesión|Eliminar token y caché de consultas.|Must|Tras salir, rutas protegidas redirigen a`/login`.|
|FR-FE-<br>AUTH-<br>007|Mi cuenta|Mostrar datos mínimos de<br>`/api/auth/me`y de`UserResponse`.|Should|`GET /api/auth/me`;`GET /api/users/by-`<br>`email?email=`del propio usuario.|



## 7.4 Compra y tickets del usuario 

|**ID**|**Requisito**|**Descripción**|**Prioridad**|**Criterio de aceptación**|
|---|---|---|---|---|
|FR-FE-<br>TKT-<br>001|Comprar<br>ticket|Seleccionar tipo (`GENERAL`,<br>`VIP`,`BACKSTAGE`,`STUDENT`) y<br>confirmar.|Must|`POST /api/tickets`con el email del principal →`201`,<br>redirección a confirmación.`409`muestra el mensaje de regla<br>de negocio.|
|FR-FE-<br>TKT-<br>002|Requiere<br>sesión|Un anónimo que pulsa<br>"Comprar" va a`/login`y<br>regresa al evento.|Must|Redirección con`state.desde`.|
|FR-FE-<br>TKT-<br>003|Mis tickets|Listar tickets propios con<br>estado y filtros por estado.|Must|`GET /api/tickets/by-user?email=`.|
|FR-FE-<br>TKT-<br>004|Detalle de<br>ticket|Mostrar código, tipo, precio,<br>estado y fecha.|Must|`GET /api/tickets/{ticketCode}`; ajeno →`403/404`→<br>pantalla correspondiente.|
|FR-FE-<br>TKT-<br>005|Cancelar<br>ticket|Acción con confirmación<br>modal.|Must|`PATCH /api/tickets/{code}/cancel`→`200`actualiza lista;<br>`409`muestra mensaje; visible solo si`status = PAID`.|



## 7.5 Operación STAFF 

|**ID**|**Requisito**|**Descripción**|**Prioridad**|**Criterio de aceptación**|
|---|---|---|---|---|
|FR-FE-||Buscar ticket por||`GET /api/tickets/{code}`+`PATCH`|
|STF-|Validar ingreso|código y marcarlo|Must|`/api/tickets/{code}/use`→`200`; segunda vez →`409`|
|001||como usado.||mostrado.|
|FR-FE-<br>STF-<br>002|Tickets<br>pagados por<br>evento|Tabla de tickets`PAID`<br>de un evento.|Must|`GET /api/events/{eventCode}/tickets/paid`.|



## 7.6 Administración ADMIN 

|**ID**|**Requisito**|**Descripción**|**Prioridad**|**Criterio de aceptación**|
|---|---|---|---|---|
|FR-FE-<br>ADM-<br>001|Crear<br>evento|Formulario con`eventCode`,`name`,<br>`description`,`category`,<br>`eventDate`,`minimumAge`,<br>`venueCode`.|Must|`POST /api/events`→`201`;`400`mapea`details`a<br>campos;`409`duplicado.|



9 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

|**ID**|**Requisito**|**Descripción**|**Prioridad**|**Criterio de aceptación**|
|---|---|---|---|---|
|FR-FE-<br>ADM-<br>002|Seleccionar<br>sede|El campo`venueCode`se elige de<br>`GET /api/venues/active`.|Should|Select poblado desde la API.|
|FR-FE-<br>ADM-<br>003|Gestionar<br>evento|Consultar un evento por código y<br>mostrar acciones.|Must|`GET /api/events/{eventCode}`(ADMIN ve<br>borradores).|
|FR-FE-<br>ADM-<br>004|Publicar<br>evento|Acción con confirmación.|Must|`PATCH /api/events/{eventCode}/publish`→`200`;<br>`409`mostrado.|
|FR-FE-<br>ADM-<br>005|Asociar<br>artista|Seleccionar artista activo y<br>asociarlo.|Must|`POST`<br>`/api/events/{eventCode}/artists/{artistId}`→<br>`200`; duplicado`409`.|



## 7.7 Transversal 

|**ID**|**Requisito**|**Descripción**|**Prioridad**|**Criterio de aceptación**|
|---|---|---|---|---|
|FR-FE-GEN-<br>001|Layout y<br>navegación|Navbar adaptada al rol, footer y menú<br>móvil.|Must|Enlaces visibles según rol;`NavLink`activo.|
|FR-FE-GEN-<br>002|Páginas de error|403, 404 y`errorElement`de ruta.|Must|Rutas inexistentes → 404; error de carga →<br>`ErrorPagina`.|
|FR-FE-GEN-<br>003|Notificaciones|Avisos (toast) de éxito/error de<br>mutaciones.|Should|`aria-live="polite"`.|
|FR-FE-GEN-<br>004|Modo oscuro|Respeta`prefers-color-scheme`y<br>permite alternar.|Could|Clases`dark:`.|



# 8. Mapa de páginas y rutas 

## 8.1 Tabla de páginas 

Convención: **P** = pública, **A** = autenticada, **U** = `USER` , **S** = `STAFF` / `ADMIN` , **AD** = `ADMIN` . Todas las páginas (salvo Inicio) se cargan con `lazy` . 

|**#**|**Ruta**|**Página (****`pages/…`)**|**Acceso**|**Endpoints consumidos**|
|---|---|---|---|---|
|1|`/`|`Inicio`|P|`GET /api/events/published`,`GET`<br>`/api/artists/active`|
|2|`/eventos`|`CatalogoEventos`|P|`GET /api/events/published`,`GET`<br>`/api/events/by-artist`|
|3|`/eventos/:eventCode`|`DetalleEvento`|P (solo<br>publicados)|`GET /api/events/{eventCode}`,`POST`<br>`/api/tickets`|
|4|`/artistas`|`ListaArtistas`|P|`GET /api/artists/active`|
|5|`/artistas/:id`|`DetalleArtista`|P|`GET /api/artists/{id}`,`GET`<br>`/api/events/by-artist`|
|6|`/sedes`|`ListaSedes`|P|`GET /api/venues/active`|
|7|`/sedes/:code`|`DetalleSede`|P|`GET /api/venues/{code}`|
|8|`/login`|`Login`|P (redirige<br>si hay<br>sesión)|`POST /api/auth/login`,`GET`<br>`/api/auth/me`|



10 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

|**#**|**Ruta**|**Página (****`pages/…`)**|**Acceso**|**Endpoints consumidos**|
|---|---|---|---|---|
|9|`/registro`|`Registro`|P (redirige<br>si hay<br>sesión)|`POST /api/users`|
|10|`/cuenta`|`Cuenta`|A|`GET /api/auth/me`,`GET /api/users/by-`<br>`email`,`GET /api/users/by-username`|
|11|`/mis-tickets`|`MisTickets`|U|`GET /api/tickets/by-user`|
|12|`/mis-tickets/:ticketCode`|`DetalleTicket`|U|`GET /api/tickets/{ticketCode}`,`PATCH`<br>`/api/tickets/{ticketCode}/cancel`|
|13|`/compra/confirmacion/:ticketCode`|`ConfirmacionCompra`|U|`GET /api/tickets/{ticketCode}`|
|14|`/staff/validacion`|`ValidacionIngreso`|S|`GET /api/tickets/{ticketCode}`,`PATCH`<br>`/api/tickets/{ticketCode}/use`|
|15|`/staff/eventos/:eventCode/tickets`|`TicketsPagadosEvento`|S|`GET`<br>`/api/events/{eventCode}/tickets/paid`|
|16|`/admin/eventos`|`AdminEventos`|AD|`GET /api/events/{eventCode}`(búsqueda<br>por código)|
|17|`/admin/eventos/nuevo`|`AdminCrearEvento`|AD|`POST /api/events`,`GET`<br>`/api/venues/active`|
|18|`/admin/eventos/:eventCode`|`AdminGestionEvento`|AD|`GET /api/events/{eventCode}`,`PATCH`<br>`…/publish`,`POST …/artists/{artistId}`,<br>`GET /api/artists/active`|
|19|`/403`|`SinPermisos`|P|—|
|20|`*`|`NoEncontrada`|P|—|



## 8.2 Árbol de rutas ( `createBrowserRouter` ) 

- `/                                   Layout (Navbar · <Outlet/> · Footer) · errorElement: ErrorPagina ├─ index                            Inicio ├─ eventos                          CatalogoEventos │  └─ :eventCode                    DetalleEvento                    (loader opcional) ├─ artistas                         ListaArtistas │  └─ :id                           DetalleArtista ├─ sedes                            ListaSedes │  └─ :code                         DetalleSede ├─ login                            Login            (SoloAnonimo) ├─ registro                         Registro         (SoloAnonimo) ├─ [RutaProtegida]                  requiere sesión │  ├─ cuenta                        Cuenta │  ├─ [RutaPorRol: USER] │  │  ├─ mis-tickets                MisTickets` 

- `│  │  ├─ mis-tickets/:ticketCode    DetalleTicket` 

- `│  │  └─ compra/confirmacion/:ticketCode   ConfirmacionCompra │  ├─ [RutaPorRol: STAFF, ADMIN] │  │  ├─ staff/validacion           ValidacionIngreso │  │  └─ staff/eventos/:eventCode/tickets  TicketsPagadosEvento │  └─ [RutaPorRol: ADMIN] │     ├─ admin/eventos              AdminEventos │     ├─ admin/eventos/nuevo        AdminCrearEvento │     └─ admin/eventos/:eventCode   AdminGestionEvento ├─ 403                              SinPermisos └─ *                                NoEncontrada` 

Reglas del árbol: 

11 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

- `RutaProtegida` y `RutaPorRol` son _layout routes_ con `<Outlet />` ; redirigen a `/login` (guardando `state.desde` ) o a `/403` . Las rutas se declaran con `lazy: async () => ({ Component: (await import('./pages/X')).default })` . `NavLink` de `/` usa `end` . 

Rutas estáticas antes que dinámicas no es necesario en Router, pero `/eventos` y `/eventos/:eventCode` son rutas distintas. 

## 8.3 Navegación por rol 



<!-- Start of picture text -->
Inicio<br>Visitante Registro<br>Catalogo Comprar sin sesion Login Cuenta<br>Detalle de<br>evento<br>Detalle de<br>USER Comprar Confirmacion Mis tickets<br>ticket<br>Cancelar<br>Validacion de<br>STAFF ingreso<br>Tickets pagados<br>Crear evento<br>por evento<br>ADMIN<br>Asociar artista<br>Admin eventos<br>Publicar<br>Gestionar<br>evento<br><!-- End of picture text -->

## 8.4 Navbar por rol 

|**Elemento**|**Anónimo**|**USER**|**STAFF**|**ADMIN**|
|---|---|---|---|---|
|Eventos / Artistas / Sedes|✔|✔|✔|✔|
|Iniciar sesión / Registrarse|✔|—|—|—|
|Mis tickets|—|✔|—|—|
|Validar ingreso / Tickets pagados|—|—|✔|✔|
|Administrar eventos|—|—|—|✔|
|Mi cuenta / Salir|—|✔|✔|✔|



# 9. Diseños de las vistas 

Los diseños tienen dos niveles de detalle complementarios: 

1. **Diseños de alta fidelidad (imágenes PNG)** en la carpeta `imagenes/` , exportados del lienzo de diseño _"PulsePass — Diseños de las vistas (Frontend)"_ (32 artboards). **Son la referencia visual vinculante** de estructura, jerarquía, textos, iconografía, estados y colores. 

2. **Wireframes ASCII** (bloques `text` ) que se conservan como descripción de estructura legible en texto plano y como apoyo a la trazabilidad. Si un wireframe ASCII y su imagen difieren, **prevalece la imagen** . 

Cada subsección 9.2–9.19 incluye la imagen correspondiente, seguida del wireframe y de la tabla de comportamiento (componentes, datos, estados, validaciones), que sigue siendo vinculante para lo que la imagen no puede expresar. 

## 9.0 Índice de diseños visuales 

|**#**<br>**Imagen**|**Vista / estado**|**Ruta**|**Rol**|**Sección**|
|---|---|---|---|---|
|1<br>`imagenes/Main.png`|Inicio|`/`|Público|9.3|



12 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

|**#**|**Imagen**|**Vista / estado**|**Ruta**|**Rol**|**Sección**|
|---|---|---|---|---|---|
|2|`imagenes/Catalogo.png`|Cartelera|`/eventos`|Público|9.4|
|||Detalle de||||
|3|`imagenes/DetalleEvento.png`|evento (USER<br>autenticada)|`/eventos/:eventCode`|USER|9.5|
|||Modal de||||
|4|`imagenes/ModalCompra.png`|compra con<br>error 409<br>(edad mínima)|`/eventos/:eventCode`|USER|9.5|
|5|`imagenes/Artistas.png`|Lista de<br>artistas|`/artistas`|Público|9.6|
|6|`imagenes/DetalleArtista.png`|Detalle de<br>artista|`/artistas/:id`|Público|9.6|
|7|`imagenes/Sedes.png`|Lista de sedes|`/sedes`|Público|9.7|
|8|`imagenes/DetalleSede.png`|Detalle de<br>sede|`/sedes/:code`|Público|9.7|
|9|`imagenes/Login.png`|Login|`/login`|Anónimo|9.8|
|10|`imagenes/Registro.png`|Registro|`/registro`|Anónimo|9.9|
|11|`imagenes/Cuenta.png`|Mi cuenta|`/cuenta`|Autenticado|9.10|
|12|`imagenes/MisTickets.png`|Mis tickets|`/mis-tickets`|USER|9.11|
|||Modal de||||
|13|`imagenes/ModalCancelar.png`|cancelación de<br>ticket|`/mis-tickets`|USER|9.11|
|14|`imagenes/DetalleTicket.png`|Detalle de<br>ticket|`/mis-tickets/:ticketCode`|USER|9.12|
|15|`imagenes/Confirmacion.png`|Confirmación<br>de compra|`/compra/confirmacion/:ticketCode`|USER|9.13|
|16|`imagenes/Validacion.png`|Validación de<br>ingreso: ticket<br>encontrado|`/staff/validacion`|STAFF/ADMIN|9.14|
|17|`imagenes/ValidacionExito.png`|Validación:<br>ingreso<br>registrado<br>(200)|`/staff/validacion`|STAFF/ADMIN|9.14|
|||Validación:||||
|18|`imagenes/ValidacionYaUsado.png`|ticket ya<br>utilizado (409)|`/staff/validacion`|STAFF/ADMIN|9.14|
|||Tickets||||
|19|`imagenes/TicketsPagados.png`|pagados por<br>evento|`/staff/eventos/:eventCode/ticket`|`s`<br>STAFF/ADMIN|9.15|
|20|`imagenes/AdminEventos.png`|Administración<br>de eventos|`/admin/eventos`|ADMIN|9.16|
|21|`imagenes/AdminCrear.png`|Crear evento|`/admin/eventos/nuevo`|ADMIN|9.17|
|22|`imagenes/AdminGestion.png`|Gestionar<br>evento|`/admin/eventos/:eventCode`|ADMIN|9.18|



13 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

|**#**|**Imagen**|**Vista / estado**|**Ruta**|**Rol**|**Sección**|
|---|---|---|---|---|---|
|23|`imagenes/Errores.png`|Páginas 403,<br>404 y error de<br>ruta|`/403`,`*`,`errorElement`|Todos|9.19|
|24|`imagenes/Estados.png`|Estados de<br>interfaz<br>(cargando,<br>vacío, error,<br>éxito)|transversal|Todos|9.20|
|||Sistema visual<br>(color,||||
|25|`imagenes/Sistema.png`|tipografía,<br>botones,<br>badges,<br>campos)|—|—|9.1|
|26|`imagenes/MovilMenuCerrado.png`|Móvil 390 px ·<br>navbar con<br>menú cerrado|Layout|Todos|9.2|
|27|`imagenes/MovilMenuAbierto.png`|Móvil 390 px ·<br>menú<br>desplegado|Layout|Todos|9.2|
|28|`imagenes/MovilInicio.png`|Móvil 390 px ·<br>Inicio|`/`|Público|9.3|
|||Móvil 390 px ·<br>Detalle de||||
|29|`imagenes/MovilEvento.png`|evento con<br>barra de<br>compra fija|`/eventos/:eventCode`|USER|9.5|
|30|`imagenes/MovilPagados.png`|Móvil 390 px ·<br>Tickets<br>pagados (tabla<br>→ tarjetas)|`/staff/eventos/:eventCode/t`|`ickets`<br>STAFF/ADMIN|9.15|
|31|`imagenes/CatalogoOscuro.png`|Modo oscuro ·<br>Cartelera|`/eventos`|Público|9.1|
|32|`imagenes/DetalleOscuro.png`|Modo oscuro ·<br>Detalle de<br>evento|`/eventos/:eventCode`|USER|9.1|
|Las<br>Las<br>co|imágenes son el entregable de diseño<br>20 páginas de la sección 8 tienen al m<br>mportamientos de mayor riesgo.|de la Fase 5 y debe<br>enos un diseño; las|n versionarse junto con el PRD (carp<br>variantes (modales, estados 200/409|eta`imagenes/`al lado del<br>, móvil, oscuro) cubren los|`.md`).|



Decisiones de diseño confirmadas por las imágenes 

|**ID**|**Decisión visible en el diseño**|**Efecto en la implementación**|
|---|---|---|
|DES-|Iconografía en SVG de trazo (sin emojis); los emojis de los wireframes|Usar un set de íconos SVG como componentes|
|01|ASCII son solo ilustrativos.|(`Icono*`),`aria-hidden`en los decorativos.|
|DES-<br>02|Las tarjetas de evento usan un panel de color por categoría con ícono (sin<br>foto) porque la API no entrega imágenes.|Mapa tipado`Record<EventCategory,`<br>`{fondo, texto, icono}>`con clases Tailwind<br>completas.|



14 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

|**ID**|**Decisión visible en el diseño**|**Efecto en la implementación**|
|---|---|---|
|DES-<br>03|Navbar por rol: anónimo →_Ingresar / Registrarse_; USER →_Mis tickets_+<br>menú de usuario con avatar de iniciales; STAFF/ADMIN →_Validar ingreso_,<br>_Tickets pagados_; ADMIN → además_Administrar eventos_.|`Navbar`calcula enlaces desde`RUTAS`y el rol<br>(tabla 8.4).|
|DES-|Páginas de gestión ADMIN y formularios usan columna estrecha centrada|Variante`ContenedorEstrecho`(`max-w-3xl`) en|
|04|(≈ 830 px) frente al contenedor`max-w-6xl`del resto.|`Layout`.|
|DES-<br>05|Los códigos (`CMF-2026`,`TKT-8F2A1C`) se muestran en fuente<br>monoespaciada.|Clase`font-mono`en`CodigoTexto`.|
|DES-<br>06|El filtro de "Mis tickets" son_chips_(Todos · Pagados · Usados · Cancelados).|Componente`FiltroChips`con`aria-`<br>`pressed`.|
|DES-<br>07|En móvil, la acción de compra se fija en una barra inferior.|`fixed bottom-0 inset-x-0`solo en`< md`,<br>con relleno inferior en la página.|
||Tokens de color:`marca-50 #eef2ff`,`marca-500 #6366f1`,`marca-600`||
|DES-|`#4f46e5`,`marca-700 #4338ca`,`acento-500 #f43f5e`,`slate-50`|`@theme`de la sección 9.1 (ver ajuste de|
|08|`#f8fafc`,`slate-900 #0f172a`. Tipografía Poppins (títulos) + Inter<br>(cuerpo).|fuentes).|
|DES-<br>09|Botones: primario, secundario (borde), peligro, fantasma y estado<br>_Cargando…_deshabilitado. Altura mínima 44–52 px.|`Boton`con`variante`y prop`cargando`.|
|DES-<br>10|Errores de modal (409) se muestran dentro del modal con fondo rojo<br>claro, ícono de advertencia y`role="alert"`, sin cerrarlo.|`Aviso`con`tono="error"`y`role="alert"`.|
|DES-|El hero de Inicio usa degradado`marca-600 → acento-500`con dos CTA|`bg-linear-to-r from-marca-600 to-`|
|11|(_Ver cartelera_,_Crear cuenta_).|`acento-500`.|



9.1 Sistema visual 

15 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
GD ruiserass cece Arse see —<br>Sistema visual<br>Color<br>marca-50 marca-500 marca-600 marca-700 acento-500 slate-50 slate-900<br>Feetatt moseent Faraes, asses masise Hotere porn<br>Tipografia<br>Inter — cuerpo de texto, formularios y tablas. Fechas es-CO y precios $ 250.000 (PrecioCOP).<br>Botones<br>Badges de estado<br>DRAFT PUBLISHED SOLD.OUT CANCELLED FINISHED<br>esenveo PAID (USED<br>Campos<br>Campo normal Campo con error<br>Texto | |<br>Puntos de quiebre<br>base <640 px: 1columna, mend hamburguesa - sm 2640: 2 columnas md 2768: navbar completa Ig 21024: 3-4 columnas, contenedor max-w-6x!<br>© 2026 PulsePass - Caso de estuato académico Sedes Artistas<br><!-- End of picture text -->

Modo oscuro (referencia visual): 

16 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

#### **Cartelera** 



<!-- Start of picture text -->
cartelera<br>. -<br>a s tr<br><!-- End of picture text -->

#### **Detalle de evento** 



<!-- Start of picture text -->
— .<br>Caribbean Music Fest 2026 oni<br>=<br><!-- End of picture text -->

### Tokens (Tailwind v4, `src/index.css` ) 

```
@import"tailwindcss";
@theme {
  --color-marca-50:  #eef2ff;
  --color-marca-500: #6366f1;
  --color-marca-600: #4f46e5;
  --color-marca-700: #4338ca;
  --color-acento-500: #f43f5e;   /* llamadas a la acción secundarias / destacados */
  --font-titulo: "Poppins", ui-sans-serif, system-ui, sans-serif;
  --font-cuerpo: "Inter", ui-sans-serif, system-ui, sans-serif;
}
```

|**Token / uso**|**Claro**|**Oscuro**|
|---|---|---|
|Fondo de página|`bg-slate-50`|`dark:bg-slate-950`|
|Superficie (tarjeta)|`bg-white`+`shadow-sm`|`dark:bg-slate-900`|
|Texto principal /<br>secundario|`text-slate-900`/`text-slate-600`|`dark:text-slate-100`/<br>`dark:text-slate-400`|
|Acción primaria|`bg-marca-600 hover:bg-marca-700 text-white`|igual|
|Peligro|`bg-red-600 hover:bg-red-700 text-white`|igual|
|Foco|`focus-visible:outline-2 focus-visible:outline-offset`<br>`focus-visible:outline-marca-600`|`-2`<br>igual|



### Puntos de quiebre y rejilla 

```
base  (<640px)   1 columna · menú hamburguesa · padding px-4
sm    (≥640px)   2 columnas en tarjetas
md    (≥768px)   navbar completa · 2–3 columnas
lg    (≥1024px)  3–4 columnas · contenedor max-w-6xl mx-auto
```

Badges de estado (mapa tipado, sin clases dinámicas) 

17 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

|**Dominio**|**Valor**|**Clases Tailwind**|
|---|---|---|
|EventStatus|`DRAFT`|`bg-slate-200 text-slate-700`|
||`PUBLISHED`|`bg-emerald-100 text-emerald-800`|
||`SOLD_OUT`|`bg-amber-100 text-amber-800`|
||`CANCELLED`|`bg-red-100 text-red-800`|
||`FINISHED`|`bg-slate-300 text-slate-800`|
|TicketStatus|`RESERVED`|`bg-sky-100 text-sky-800`|
||`PAID`|`bg-emerald-100 text-emerald-800`|
||`USED`|`bg-slate-300 text-slate-800`|
||`CANCELLED`|`bg-red-100 text-red-800`|



### Componentes base ( `src/componentes/` ) 

|**Componente**|**Props clave**|**Notas**|
|---|---|---|
|`Boton`|`variante: 'primario'|'secundario'|'peligro'|'fantasma'`,<br>extiende`ComponentProps<'button'>`,`cargando?`|`Record<Variante,string>`+`clsx`.|
|`Campo`|`etiqueta`,`error?`, extiende`ComponentProps<'input'>`|`label`asociado,`aria-invalid`,`aria-`<br>`describedby`.|
|`Selector`|igual que`Campo`para`select`||
|`Badge`|`tono`|Mapa de badges.|
|`Tarjeta`|`children`,`className?`|Contenedor visual.|
|`Modal`|`abierto`,`titulo`,`onCerrar`|Atrapa foco, cierra con`Esc`.|
|`Tabla<T>`|`columnas: {encabezado; celda:(fila:T)=>ReactNode}[]`,<br>`filas: T[]`|Genérica (guía React §10).|
|`EstadoVacio`/<br>`EstadoError`/<br>`Cargando`|`mensaje`,`onReintentar?`|Estados de FE-014.|
|`Aviso`(toast)|`tipo: 'exito'|'error'|'info'`|`role="status"`/`aria-live`.|
|||`Intl.NumberFormat('es-CO',`|
|`PrecioCOP`|`valor: number`|`{style:'currency',`<br>`currency:'COP'})`.|



## 9.2 Layout raíz (todas las páginas) 

18 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
PulsePass =<br>Descubre los mejores.<br>eventos<br>Musica, cultura, deporte y tecnologia en un<br>solo lugar.<br>Crear cuenta<br><!-- End of picture text -->



<!-- Start of picture text -->
PulsePass x<br>Eventos<br>Artistas<br>Sedes<br>Descubre los mejores.<br>eventos<br>Musica, cultura, deporte y tecnologia en un<br>solo lugar.<br>Crear cuenta<br><!-- End of picture text -->

_(El layout de escritorio se aprecia en la cabecera y pie de todas las imágenes de escritorio; ver, p. ej.,_ _`imagenes/Main.png` .)_ 

Escritorio (≥ 768 px): 



<!-- Start of picture text -->
┌──────────────────────────────────────────────────────────────────────────┐<br>│ ♪ PulsePass   Eventos  Artistas  Sedes        [rol-links…]  [Ingresar][Registrarse] │  ← Navbar<br>(sticky)<br>├──────────────────────────────────────────────────────────────────────────┤<br>│                                                                          │<br>│                           <Outlet />  (max-w-6xl, px-4, py-8)            │<br>│                                                                          │<br>├──────────────────────────────────────────────────────────────────────────┤<br>│ © 2026 PulsePass · Caso de estudio académico          Sedes · Artistas   │  ← Footer<br>└──────────────────────────────────────────────────────────────────────────┘<br><!-- End of picture text -->

Móvil (< 768 px): 

19 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
┌────────────────────────┐        ┌────────────────────────┐<br>│ ♪ PulsePass        [ ☰ ] │  →     │ ♪ PulsePass        [ ✕ ] │<br>├────────────────────────┤        ├────────────────────────┤<br>│                        │        │ Eventos                │<br>│       <Outlet />       │        │ Artistas               │<br>│                        │        │ Sedes                  │<br>└────────────────────────┘        │ ───────────            │<br>                                  │ Ingresar · Registrarse │<br>                                  └────────────────────────┘<br><!-- End of picture text -->

Con sesión, el extremo derecho muestra `[ Andrea ▾ ]` con menú: _Mi cuenta · Mis tickets · Salir_ . 

|**Aspecto**|**Definición**|
|---|---|
|Componentes|`Layout`,`Navbar`,`MenuUsuario`,`Footer`,`Aviso`.|
|Estado|`AuthContext`(rol),`useState`del menú móvil.|
|Accesibilidad|`<header>`,`<nav aria-label="Principal">`,`<main id="contenido">`, enlace "Saltar al contenido".|



## 9.3 Inicio — `/` 

20 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
Descubre los mejores<br>Musica, cultura, deporte y tecnologia en un solo lugar.<br>Préximos eventos Ver todos ><br>Noche de Jazz y Cultura Stand-up Costefio Caribbean Music Fest 2026<br>© 27 nov 2026 6 4dic 2026 6 12 dic 2026<br>© Cartagena © Santa Marta © Santa Marta<br>Artistas destacados Ver todos ><br>SB Solar Beat NW_ Neon Waves CS Caribbean Sound<br><!-- End of picture text -->

Versión móvil: 

21 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
PulsePass =<br>Descubre los mejores.<br>eventos<br>Musica, cultura, deporte y tecnologia en un<br>solo lugar.<br>Crear cuenta<br>Préximos eventos<br>CULTURE<br>Noche de Jazz y Cultura<br>© 27 nov 2026<br>® Cartagena<br>PUBLISHED<br>ENTERTAINMENT<br>Stand-up Costeho<br>6 4 dic 2026<br>© Santa Marta<br>PUBLISHED<br>MUSIC<br><!-- End of picture text -->

22 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
Caribbean Music Fest 2026<br>6 12 dic 2026<br>® Santa Marta<br>PUBLISHED<br>Artistas destacados<br>SB Solar Beat<br>NW Neon Waves<br><!-- End of picture text -->



<!-- Start of picture text -->
┌──────────────────────────────────────────────────────────────────────────┐<br>│  HERO (fondo degradado marca-600 → acento-500)                           │<br>│  Descubre los mejores eventos                                            │<br>│  Música, cultura, deporte y tecnología en un solo lugar.                 │<br>│  [ Ver cartelera ]   [ Crear cuenta ]                                    │<br>├──────────────────────────────────────────────────────────────────────────┤<br>│  Próximos eventos                                          Ver todos →   │<br>│  ┌───────────┐ ┌───────────┐ ┌───────────┐                               │<br>│  │ [imagen]  │ │ [imagen]  │ │ [imagen]  │                               │<br>│  │ MUSIC     │ │ CULTURE   │ │ SPORTS    │                               │<br>│  │ Caribbean │ │ …         │ │ …         │                               │<br>│  │ Music Fest│ │           │ │           │                               │<br>│  │ 12 dic· 🏷  │ │           │ │           │                               │<br>│  └───────────┘ └───────────┘ └───────────┘                               │<br>├──────────────────────────────────────────────────────────────────────────┤<br>│  Artistas destacados                                       Ver todos →   │<br>│  (○) Solar Beat   (○) Neon Waves   (○) Caribbean Sound                   │<br>└──────────────────────────────────────────────────────────────────────────┘<br><!-- End of picture text -->

|**Aspecto**|**Definición**|
|---|---|
|Componentes|`Hero`,`TarjetaEvento`,`AvatarArtista`,`Cargando`,`EstadoVacio`.|
|Datos|`useEventosPublicados()`(primeros 3),`useArtistasActivos()`(primeros 6).|
|Estados|Cargando (esqueletos), vacío ("Aún no hay eventos publicados"), error con reintento.|
|CTA|"Crear cuenta" se oculta con sesión.|



## 9.4 Catálogo de eventos — `/eventos` 

23 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
GB rutserass cence Asoe sae orn<br>Cartelera<br>Buscar por nombre Categoria Amtsta<br>Q Buscar por nombre. Todas ¥ Todos ¥ —— Limpiar<br>music TECHNOLOSY sports<br>Caribbean Music Fest 2026 Cumbre Tech Caribe 2027 Copa Universitaria de Fatbol<br>G 12dic 2026 65 20 ene 2027 6 6 feb 2027<br>© Santa Marta ® Barranquilla ®@ Santa Marta<br>usuisieD sowo.our PUBLISHED<br>cutture EDUCATION ENTERTAINMENT<br>Noche de Jazz y Cultura Feria de Educacién Superior Stand-up Costefio<br>6 27 nov 2026 3 15 mar 2027 6 4 dic 2026<br>® Cartagena © Bogota © Santa Marta<br>PUBLISHED PUBLISHED PUBLISHED<br><!-- End of picture text -->



<!-- Start of picture text -->
┌──────────────────────────────────────────────────────────────────────────┐<br>│  Cartelera                                                               │<br>│  [  🔍  Buscar por nombre…        ]  [ Categoría ▾ ]  [ Artista ▾ ] [Limpiar]│<br>│  12 eventos                                                              │<br>│ ┌────────────────┐ ┌────────────────┐ ┌────────────────┐                 │<br>│ │ [imagen]       │ │ [imagen]       │ │ [imagen]       │                 │<br>│ │ MUSIC          │ │ TECHNOLOGY     │ │ SPORTS         │                 │<br>│ │ Caribbean Music│ │ …              │ │ …              │                 │<br>│ │ Fest 2026      │ │                │ │                │                 │<br>│ │  📅  12 dic 2026 │ │                │ │                │                 │<br>│ │  📍  Santa Marta │ │                │ │                │                 │<br>│ │ [PUBLISHED]    │ │ [SOLD_OUT]     │ │ [PUBLISHED]    │                 │<br>│ └────────────────┘ └────────────────┘ └────────────────┘                 │<br>└──────────────────────────────────────────────────────────────────────────┘<br><!-- End of picture text -->

|**Aspecto**|**Definición**|
|---|---|
|Componentes|`BarraFiltros`,`TarjetaEvento`,`GrillaEventos`.|



`useEventosPublicados()` ; con `?artista=` usa `useEventosPorArtista(stageName)` . El texto y la categoría filtran Datos en cliente. 

24 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

|**Aspecto**|**Definición**|
|---|---|
|URL|`useSearchParams`:`?q=`,`?categoria=`,`?artista=`.|
|Estados|Cargando, vacío ("Sin resultados para tu búsqueda"), error.|
|Grilla|`grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6`.|
|Interacción|La tarjeta completa es un`Link`a`/eventos/:eventCode`.|



## 9.5 Detalle de evento y compra — `/eventos/:eventCode` 



<!-- Start of picture text -->
@ Pulserass Eventos Artistas Sedes Mis tickets A Andrea v<br>© Volver ala cartelera<br>Entradas<br>| © GENERAL |<br>dd Tipo de entrada<br>> vip<br>) BACKSTAGE<br>Music PUBLISHED<br>>) STUDENT<br>Caribbean. Music. Fest 2026<br>6 12 dic 2026 -20:00 Q Edad minima: 18 Comprar entrada<br>Tres noches de musica caribefia frente a la bahia de Santa Marta, con artistas de la<br>regién y propuestas de electronica y fusién. He lo define meatsel sist | cont<br>Artistas<br>SB Solar Beat NW Neon Waves<br>Sede<br>a Marina Convention Center<br>"Santa Marta -Capacidad 3<br>© 2026 PulsePass - Caso de estudio académico Sedes Artistas<br><!-- End of picture text -->

Modal de confirmación con error `409` (usuaria menor de edad): 

25 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
Confirmar compra x<br>Evento Caribbean Music Fest 2026<br>Tipo vie<br>Cuenta laura@pulsepass.test<br>A. Et usuario no cumple la edad minima del event.<br>Cancelar [conta]<br><!-- End of picture text -->

Versión móvil con barra de compra fija: 

26 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
PulsePass =<br>€ Volver ala cartelera<br>MUSIC PUBLISHED<br>Caribbean. Music: Fest 2026<br>6 12 dic 2026- 20:00<br>A Edad minima: 18<br>Tres noches de musica caribefa frente a la<br>bahia de Santa Marta.<br>Artistas<br>SB NW<br>Sede<br>Marina Convention Center<br>Santa Marta - Capacidad 3<br>Entradas<br>Tipo de entrada<br>| @ GENERAL |<br>O vip<br>© BACKSTAGE<br>© STUDENT<br>El precio lo define el sistema al confirmar.<br><!-- End of picture text -->

27 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 





<!-- Start of picture text -->
┌──────────────────────────────────────────────────────────────────────────┐<br>│ ← Volver a la cartelera                                                  │<br>│ ┌───────────────────────────────────┐  ┌──────────────────────────────┐  │<br>│ │ [imagen / banner]                 │  │ Entradas                     │  │<br>│ │                                   │  │ Tipo de entrada              │  │<br>│ │ MUSIC · [PUBLISHED]               │  │ (•) GENERAL                  │  │<br>│ │ Caribbean Music Fest 2026         │  │ ( ) VIP                      │  │<br>│ │  📅  12 dic 2026 · 20:00            │  │ ( ) BACKSTAGE                │  │<br>│ │  🔞  Edad mínima: 18                │  │ ( ) STUDENT                  │  │<br>│ │                                   │  │                              │  │<br>│ │ Descripción…                      │  │ [   Comprar entrada   ]      │  │<br>│ │                                   │  │ El precio lo define el       │  │<br>│ │ Artistas                          │  │ sistema al confirmar.        │  │<br>│ │ (○) Solar Beat  (○) Neon Waves    │  └──────────────────────────────┘  │<br>│ │                                   │                                    │<br>│ │ Sede                              │                                    │<br>│ │ Marina Convention Center          │                                    │<br>│ │ Santa Marta · Capacidad 3         │                                    │<br>│ └───────────────────────────────────┘                                    │<br>└──────────────────────────────────────────────────────────────────────────┘<br><!-- End of picture text -->

Modal de confirmación al pulsar "Comprar entrada": 



<!-- Start of picture text -->
        ┌────────────────────────────────────────┐<br>        │ Confirmar compra                  [ ✕ ]  │<br>        ├────────────────────────────────────────┤<br>        │ Evento:  Caribbean Music Fest 2026     │<br>        │ Tipo:    VIP                           │<br>        │ Cuenta:  andrea@pulsepass.test         │<br>        │                                        │<br>        │  ⚠  Error del servidor (si aplica):      │<br>        │   "El usuario no cumple la edad…"      │<br>        │                                        │<br>        │            [ Cancelar ]  [ Confirmar ] │<br>        └────────────────────────────────────────┘<br><!-- End of picture text -->

28 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

|**Aspecto**|**Definición**|
|---|---|
|Componentes|`DetalleEventoCabecera`,`ListaArtistasEvento`,`TarjetaSede`,`PanelCompra`,`ModalConfirmarCompra`,<br>`BadgeEstado`.|
|Datos|`useEvento(eventCode)`; mutación`useComprarTicket()`→`POST /api/tickets`con`{ userEmail:`<br>`sesión.email, eventCode, type }`.|
|Validación|`type`obligatorio (zod). El email lo toma el cliente de la sesión, nunca de un campo editable.|
|Éxito (`201`)|Invalida`['tickets', email]`; navega a`/compra/confirmacion/:ticketCode`.|
|Errores|`404`evento → "Evento no disponible";`409`→ mensaje de regla de negocio en el modal (edad, capacidad, estado,<br>usuario inactivo);`401`→ login;`403`→ aviso.|
|Estados de<br>acción|Anónimo: botón "Inicia sesión para comprar" →`/login`(con`state.desde`).`STAFF`/`ADMIN`: panel oculto. Evento ≠<br>`PUBLISHED`: botón deshabilitado con motivo.|
|Móvil|Panel de compra pasa debajo del contenido; barra inferior fija con "Comprar".|



## 9.6 Artistas — `/artistas` y `/artistas/:id` 



<!-- Start of picture text -->
G@ Pulsorass verter arsine Seas von CED<br>Artistas<br>Solar Beat Neon Waves Caribbean Sound<br><!-- End of picture text -->

29 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
GD Putcorass cvnce sits sets —<br>SB sonreansio-tecvres<br>Préximos eventos de Solar Beat<br>Caribbean Music Fest 2026<br>6 12 dic 2026<br>© Santa Marta<br><!-- End of picture text -->



<!-- Start of picture text -->
/artistas<br>┌──────────────────────────────────────────────────────────────────────────┐<br>│ Artistas                                                                 │<br>│ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐                          │<br>│ │  (○)    │ │  (○)    │ │  (○)    │ │  (○)    │                          │<br>│ │Solar Beat│ │Neon Waves│ │Caribbean│ │  …      │                          │<br>│ │ género  │ │ género  │ │ Sound   │ │         │                          │<br>│ └─────────┘ └─────────┘ └─────────┘ └─────────┘                          │<br>└──────────────────────────────────────────────────────────────────────────┘<br>/artistas/:id<br>┌──────────────────────────────────────────────────────────────────────────┐<br>│ ← Artistas                                                               │<br>│  (○ foto grande)   Solar Beat                                            │<br>│                    Nombre artístico · biografía / datos de ArtistResponse│<br>├──────────────────────────────────────────────────────────────────────────┤<br>│ Próximos eventos de Solar Beat                                           │<br>│ [TarjetaEvento] [TarjetaEvento]                                          │<br>└──────────────────────────────────────────────────────────────────────────┘<br>Aspecto Definición<br>useArtistasActivos() ,  useArtista(id) ,  useEventosPorArtista(stageName)  (el  stageName  sale de<br>Datos<br>ArtistResponse ).<br>Validación de<br>id  se convierte con  Number(id) ; si es  NaN  → 404 de UI sin llamar a la API.<br>parámetros<br>Estados Cargando, vacío ("Sin eventos próximos"),  404  →  NoEncontrada .<br><!-- End of picture text -->

9.7 Sedes — `/sedes` y `/sedes/:code` 

30 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
GD rutserass cence Asoe shes oro<br>Sedes<br>Marina Convention Center Active Centro de Convenciones active Teatro Heredia Hall Active<br>© Santa Marta: Cap. 3 Caribe © Cartagena - Cap. 800<br>© Barranquita - Cap. 1200<br>© 2026 PulsePass - Caso de estudio académico Sedes Artistas<br><!-- End of picture text -->



<!-- Start of picture text -->
rc —<br>© sedes<br>Marina Convention Center Activa<br>Cédigo VEN-SMR-01<br>Ciudad ‘Santa Marta<br>Capacidad 3 personas<br>Estado. Activa<br>© 2026 PulsePass - Caso de estudio académico Sedes Artistas<br><!-- End of picture text -->



<!-- Start of picture text -->
/sedes                                          /sedes/:code<br>┌────────────────────────────────────┐          ┌────────────────────────────────────┐<br>│ Sedes                              │          │ ← Sedes                            │<br>│ ┌────────────────────────────────┐ │          │ Marina Convention Center           │<br>│ │ Marina Convention Center       │ │          │ VEN-SMR-01 · [Activa]              │<br>│ │  📍  Santa Marta · Cap. 3        │ │          │  📍  Santa Marta                     │<br>│ │ [Activa]                       │ │          │ Capacidad: 3 personas              │<br>│ └────────────────────────────────┘ │          │ Dirección y datos de VenueResponse │<br>│ ┌────────────────────────────────┐ │          └────────────────────────────────────┘<br>│ │ …                              │ │<br>│ └────────────────────────────────┘ │<br>└────────────────────────────────────┘<br><!-- End of picture text -->

**Aspecto Definición** Datos `useSedesActivas()` , `useSede(code)` . 

31 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

#### **Aspecto Definición** 

Estados Cargando, vacío, `404` → `NoEncontrada` . 

## 9.8 Login — `/login` 



<!-- Start of picture text -->
GD rutserass cvncs Asoe sae oro<br>Inicia sesi6n<br>Usuario<br>o correo<br>andrea<br>Contrasena<br>Pererrrerrrrrrrrry ®<br>A Credenciales invalidas.<br>© 2026 PulsePass - Caso de estudio académico Sedes Artistas<br><!-- End of picture text -->



<!-- Start of picture text -->
┌──────────────────────────────────────────┐<br>│               ♪ PulsePass                │<br>│            Inicia sesión                 │<br>│                                          │<br>│  Usuario o correo                        │<br>│  [ andrea                              ] │<br>│  Contraseña                              │<br>│  [ ••••••••••••••••••••           👁  ]    │<br>│                                          │<br>│   ⚠  Credenciales inválidas.   (aria-live) │<br>│                                          │<br>│  [          Ingresar          ]          │<br>│                                          │<br>│  ¿No tienes cuenta? Regístrate           │<br>└──────────────────────────────────────────┘<br>Aspecto Definición<br>Componentes FormularioLogin ,  Campo ,  Boton ,  Aviso .<br>Validación<br>identifier : no vacío;  password : no vacía.<br>(zod)<br>POST /api/auth/login  → guardar  accessToken  y  expiresIn  →  GET /api/auth/me  →  AuthContext =<br>Flujo<br>autenticado  →  navigate(location.state?.desde ?? '/', { replace: true }) .<br>401  → mensaje genérico (nunca distinguir usuario inexistente / contraseña);  400  → errores de campo; red/ 500  →<br>Errores<br>"Inténtalo más tarde".<br><!-- End of picture text -->

32 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
Aspecto Definición<br>Sin autocompletar el token;  autocomplete="username"  /  "current-password" ; botón con estado  cargando  para<br>Seguridad<br>evitar doble envío.<br>Si ya hay<br>SoloAnonimo  redirige a  / .<br>sesión<br>9.9 Registro —  /registro<br><!-- End of picture text -->



<!-- Start of picture text -->
G] Puisorass snes susus se _<br>Crea tu cuenta<br>andrea<br>andrea@pulsepass.test<br>Andrea Ruiz<br>04/12/1998 Les}<br>Contrasefia (min. 12 caracteres)<br>Prerreerrrrrrrrrr @®<br><!-- End of picture text -->



<!-- Start of picture text -->
┌─────────────────────────────────────────────────┐<br>│                Crea tu cuenta                   │<br>│  Nombre de usuario      [ andrea              ] │<br>│  Correo electrónico     [ andrea@pulsepass.test ]│<br>│  Nombres      [ Andrea      ]  Apellidos [ Ruiz ]│<br>│  Fecha de nacimiento    [ 1998-04-12  📅  ]       │<br>│  Contraseña (mín. 12)   [ ••••••••••••••••  👁  ] │<br>│  ▓▓▓▓▓░░░ Fortaleza: media                      │<br>│                                                 │<br>│  [            Crear cuenta            ]         │<br>│  ¿Ya tienes cuenta? Inicia sesión               │<br>└─────────────────────────────────────────────────┘<br>Aspecto Definición<br>Validación username  requerido;  email  válido;  firstName ,  lastName  requeridos;  birthDate  requerida y no futura;  password  ≥<br>(zod) 12 caracteres y ≤ 72 bytes (UTF-8).<br><!-- End of picture text -->

33 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

|**Aspecto**|**Definición**|
|---|---|
|Flujo|`POST /api/users`→`201`→`navigate('/login', { state: { registrado: true } })`y aviso "Cuenta creada,<br>inicia sesión".|
|Errores|`400`→`details`mapeado a campos (`setError`);`409`→ "El usuario o correo ya está registrado" (sin detalles<br>internos).|
|Restricción|No existe selección de rol en la UI; el rol inicial es siempre`USER`.|
|Edad mínima|La UI no la valida (regla de Service por evento).|



## 9.10 Mi cuenta — `/cuenta` 



<!-- Start of picture text -->
© PulsePass Eventos Artistas Sedes Mis tickets A Andrea ¥<br>Mi cuenta<br>Andrea Ruiz Rok USER<br>A @andrea - andrea@pulsepass.test Estado: GEE<br>Nacimiento: 12/04/1998<br>© Ver mis tickets Cerrar sesion<br>© 2026 PulsePass - Caso de estudio académico Sedes Artistas<br><!-- End of picture text -->



<!-- Start of picture text -->
┌──────────────────────────────────────────────────────────────────────────┐<br>│ Mi cuenta                                                                │<br>│ ┌────────────────────────────────────────────────────────────────────┐   │<br>│ │ (A)  Andrea Ruiz                         Rol: [USER]               │   │<br>│ │      @andrea · andrea@pulsepass.test                               │   │<br>│ │      Nacimiento: 12/04/1998    Estado: [Activa]                    │   │<br>│ └────────────────────────────────────────────────────────────────────┘   │<br>│ [ Ver mis tickets ]                                    [ Cerrar sesión ] │<br>└──────────────────────────────────────────────────────────────────────────┘<br><!-- End of picture text -->

|**Aspecto**|**Definición**|
|---|---|
|Datos|Sesión (`/api/auth/me`) +`useUsuarioPorEmail(email)`para el perfil. Solo se consulta la identidad propia.|
|Estados|Cargando, error con reintento.|
|Notas|No se muestran hash, token ni claims.|



## 9.11 Mis tickets — `/mis-tickets` 

34 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
PulsePass Eventos Artistas. Sedes Mis tickets, Andrea ¥<br>Mis tickets<br>Caribbean Music Fest 2026 PAID<br>TKT-BF2AIC - VIP -$ 250,000 - 05 nov 2026<br>Ver detalle<br>Noche de Jazz y Cultura UseD<br>TKT-5C9062 -GENERAL -$ 90,000 12 oct 2026<br>Ver detalle<br>Stand-up Costefio CANCELLED<br>TKT-77A013 - STUDENT :§ 60,000 - 01 oct 2026<br>Ver detalle<br>© 2026 PulsePass - Caso de estuato académico Sedes Artistas<br><!-- End of picture text -->

#### Modal de cancelación: 



<!-- Start of picture text -->
Cancelar ticket x<br>{Quieres cancelar el ticket TKT-8F2ATC (VIP) de Caribbean<br>Music Fest 2026?<br>A Eticket no puede cancelarse en su estado actual<br>Volver Cancelar ticket<br><!-- End of picture text -->

35 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
┌──────────────────────────────────────────────────────────────────────────┐<br>│ Mis tickets                                                              │<br>│ [ Todos ] [ Pagados ] [ Usados ] [ Cancelados ]                          │<br>│ ┌──────────────────────────────────────────────────────────────────────┐ │<br>│ │ Caribbean Music Fest 2026              [PAID]                        │ │<br>│ │ TKT-8F2A1C · VIP · $ 250.000 · 05 nov 2026                           │ │<br>│ │                                 [ Ver detalle ]  [ Cancelar ]        │ │<br>│ └──────────────────────────────────────────────────────────────────────┘ │<br>│ ┌──────────────────────────────────────────────────────────────────────┐ │<br>│ │ …                                      [USED]                        │ │<br>│ └──────────────────────────────────────────────────────────────────────┘ │<br>└──────────────────────────────────────────────────────────────────────────┘<br><!-- End of picture text -->

|**Aspecto**|**Definición**|
|---|---|
|Componentes|`FiltroEstadoTicket`,`TarjetaTicket`,`BadgeEstado`,`ModalConfirmarCancelacion`.|
|Datos|`useMisTickets()`→`GET /api/tickets/by-user?email=<sesión>`. Filtro por estado en cliente.|
|Acciones|"Cancelar" visible solo si`status === 'PAID'`; abre modal;`useCancelarTicket()`→`PATCH …/cancel`; éxito<br>invalida`['tickets', email]`.|
|Errores|`409`("ticket usado / no cancelable") se muestra en el modal sin cerrar;`403/404`→ aviso.|
|Estados|Cargando, vacío ("Aún no tienes tickets" + enlace a cartelera), error.|



## 9.12 Detalle de ticket — `/mis-tickets/:ticketCode` 



<!-- Start of picture text -->
@ PulsePass Eventos Artistas Sedes Mis tickets A Andrea ¥<br>© Mis tickets<br>Caribbean Music Fest 2026 PAID<br>Codigo TKT-8F2A1C_ © Copiar<br>Tipo vie<br>Precio $ 250.000<br>Comprado el 05 nov 2026 14:32<br>Titular Andrea Ruiz<br>Presenta este cédigo en el ingreso.<br>© 2026 PulsePass - Caso de estudio académico Sedes Artistas<br><!-- End of picture text -->



<!-- Start of picture text -->
┌──────────────────────────────────────────────────────────────────────────┐<br>│ ← Mis tickets                                                            │<br>│ ┌──────────────────────────────────────────────────────────────────────┐ │<br>│ │  Caribbean Music Fest 2026                         [PAID]            │ │<br>│ │  ───────────────────────────────────────────────────────────         │ │<br>│ │  Código         TKT-8F2A1C        [ Copiar ]                         │ │<br>36 / 63<br><!-- End of picture text -->

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
│ │  Tipo           VIP                                                  │ │<br>│ │  Precio         $ 250.000                                            │ │<br>│ │  Comprado el    05 nov 2026 14:32                                    │ │<br>│ │  Titular        Andrea Ruiz                                          │ │<br>│ │                                                                      │ │<br>│ │  Presenta este código en el ingreso.                                 │ │<br>│ └──────────────────────────────────────────────────────────────────────┘ │<br>│                                                   [ Cancelar ticket ]    │<br>└──────────────────────────────────────────────────────────────────────────┘<br>Aspecto Definición<br>Datos useTicket(ticketCode) .<br>Errores 403/404  (ticket ajeno o inexistente) → pantalla "Ticket no disponible" sin revelar existencia.<br>Acciones Igual que 9.11 (cancelar con confirmación).<br>QR Fuera de alcance: se muestra el código como texto copiable.<br><!-- End of picture text -->

## 9.13 Confirmación de compra — `/compra/confirmacion/:ticketCode` 



<!-- Start of picture text -->
© PulsePass Eventos Artistas Sedes Mis tickets A Andrea v<br>iCompra realizada con éxito!<br>Caribbean Music Fest 2026<br>Ticket TKT-BF2A1C - VIP - $ 250.000<br>PAID<br>© 2026 PulsePass : Caso de estuio académico Sedes Artistas<br><!-- End of picture text -->



<!-- Start of picture text -->
┌──────────────────────────────────────────────┐<br>│                   ✓                            │<br>│        ¡Compra realizada con éxito!          │<br>│                                              │<br>│  Caribbean Music Fest 2026                   │<br>│  Ticket TKT-8F2A1C · VIP · $ 250.000 [PAID]  │<br>│                                              │<br>│  [ Ver mis tickets ]   [ Seguir explorando ] │<br>└──────────────────────────────────────────────┘<br><!-- End of picture text -->



<!-- Start of picture text -->
Aspecto Definición<br>Datos useTicket(ticketCode) .<br>Notas La página se alcanza solo tras una compra; si el ticket no es del usuario → 403/404 de UI.<br><!-- End of picture text -->

37 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

## 9.14 STAFF — Validación de ingreso — `/staff/validacion` 

Ticket encontrado: 



<!-- Start of picture text -->
PulsePass Eventos Artistas. Sedes —_Validar ingreso Tickets pagados. D Laura v<br>Validaci6n de ingreso<br>Codigo<br>de ticket<br>Caribbean Music Fest 2026 PAID<br>VIP - Titular: Andrea Ruiz<br>© 2026 PulsePass - Caso de estudio académico Sedes Artistas<br><!-- End of picture text -->

Ingreso registrado ( `200` ): 



<!-- Start of picture text -->
PulsePass — Eventos Artistas. Sedes —_Validar ingreso Tickets pagados. D) Laura v<br>Validacién de ingreso<br>Codigo<br>de ticket<br>Caribbean Music Fest 2026 useD<br>VIP - Titular: Andrea Ruiz<br>’ Ingreso registrado.<br>© 2026 PulsePass - Caso de estudio académico Sedes Antstas<br><!-- End of picture text -->

Ticket ya utilizado ( `409` ): 

38 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
G PulsePass — Eventos artistas Sedes _Validaringreso Tickets pagados © Laura ¥<br>Validacion de ingreso<br>Codigo<br>de ticket<br>Caribbean Music Fest 2026 USED<br>VIP - Titular: Andrea Ruiz<br>4 Elticket ya fue utilizado. (409)<br>© 2026 PulsePass - Caso de estudio académico Sedes Artistas<br><!-- End of picture text -->



<!-- Start of picture text -->
┌──────────────────────────────────────────────────────────────────────────┐<br>│ Validación de ingreso                                                    │<br>│ Código de ticket                                                         │<br>│ [ TKT-8F2A1C                              ]  [ Buscar ]                  │<br>│                                                                          │<br>│ ┌──────────────────────────────────────────────────────────────────────┐ │<br>│ │ Caribbean Music Fest 2026                         [PAID]             │ │<br>│ │ VIP · Titular: Andrea Ruiz                                           │ │<br>│ │                                     [  Marcar como usado  ]          │ │<br>│ └──────────────────────────────────────────────────────────────────────┘ │<br>│  ✓  Ingreso registrado.     /      ⚠  El ticket ya fue utilizado. (409)      │<br>└──────────────────────────────────────────────────────────────────────────┘<br><!-- End of picture text -->

#### **Aspecto Definición** 

Datos `useBuscarTicket(code)` (consulta bajo demanda), `useUsarTicket()` → `PATCH …/use` . Validación Código no vacío. `200` → éxito visible y badge `USED` ; `409` → mensaje del backend en tono de advertencia; `404` → "Ticket no encontrado"; Resultados `403` → "Sin permisos". Foco automático en el campo; `Enter` busca; resultado grande y de alto contraste para uso en puerta; tras éxito el campo Usabilidad se limpia. 

## 9.15 STAFF — Tickets pagados por evento — `/staff/eventos/:eventCode/tickets` 

39 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
@ Pulserass Eventos Artistas Sedes _Validar ingreso Tickets pagados > Laura ¥<br>Evento<br>Tickets pagados - CMF-2026 OMF-2026 Ir<br>Total:2<br>codigo Tipo Titular Precio Estado Accion<br>TKT-8F2A1C vip Andrea Ruiz $ 250.000 PAID [user |<br>© 2026 PulsePass - Caso de estudio académico Sedes Artistas<br><!-- End of picture text -->

Versión móvil (la tabla se transforma en tarjetas): 

40 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
PulsePass =<br>Tickets pagados - CMF-2026<br>Evento<br>CMF-2026 Ir<br>Total: 2<br>TKT-8F2A1C PAID<br>VIP - Andrea Ruiz - $ 250.000<br>TKT-91B@7D PAID<br>GENERAL- Carlos Pefia - $ 120.000<br><!-- End of picture text -->



<!-- Start of picture text -->
┌──────────────────────────────────────────────────────────────────────────┐<br>│ Tickets pagados · CMF-2026                       Evento: [   CMF-2026  ][Ir]│<br>│ Total: 2                                                                 │<br>│ ┌──────────────┬────────┬──────────────┬─────────────┬──────────┬───────┐ │<br>│ │ Código       │ Tipo   │ Titular      │ Precio      │ Estado   │ Acción│ │<br>│ ├──────────────┼────────┼──────────────┼─────────────┼──────────┼───────┤ │<br>│ │ TKT-8F2A1C   │ VIP    │ Andrea Ruiz  │ $ 250.000   │ [PAID]   │ Usar  │ │<br>│ │ TKT-91B07D   │ GENERAL│ Carlos Peña  │ $ 120.000   │ [PAID]   │ Usar  │ │<br>│ └──────────────┴────────┴──────────────┴─────────────┴──────────┴───────┘ │<br>└──────────────────────────────────────────────────────────────────────────┘<br>Aspecto Definición<br>Datos useTicketsPagadosPorEvento(eventCode) ; "Usar" reutiliza  useUsarTicket()  e invalida la lista.<br>Componentes Tabla<TicketResponse>  genérica.<br><!-- End of picture text -->

41 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
Aspecto Definición<br>Móvil La tabla se transforma en lista de tarjetas ( md:table ,  block  en base).<br>Estados Cargando, vacío ("No hay tickets pagados"),  404  evento.<br><!-- End of picture text -->



<!-- Start of picture text -->
9.16 ADMIN — Eventos —  /admin/eventos<br><!-- End of picture text -->



<!-- Start of picture text -->
G PulsePass eventos artistas Sedes__Validar ingreso Tickets pagados _Administrar eventos © Administracién<br>Administracién de eventos<br>CMF-2026 Abrir<br>Creados en esta sesion<br>CMF -2026 » Caribbean Music Fest 2026 DRAFT Gestionar<br>4 Nota: la API no ofrece un listado completo de eventos (GAP-01).<br><!-- End of picture text -->



<!-- Start of picture text -->
┌──────────────────────────────────────────────────────────────────────────┐<br>│ Administración de eventos                           [ + Nuevo evento ]   │<br>│ Buscar evento por código                                                 │<br>│ [ CMF-2026                          ]  [ Abrir ]                         │<br>│                                                                          │<br>│ Creados en esta sesión                                                   │<br>│ ┌──────────────────────────────────────────────────────────────────────┐ │<br>│ │ CMF-2026 · Caribbean Music Fest 2026       [DRAFT]    [ Gestionar ]  │ │<br>│ └──────────────────────────────────────────────────────────────────────┘ │<br>│ Nota: la API no ofrece un listado completo de eventos (GAP-01).          │<br>└──────────────────────────────────────────────────────────────────────────┘<br><!-- End of picture text -->

#### **Aspecto Definición** 

Búsqueda por código → `GET /api/events/{eventCode}` ; la lista "creados en esta sesión" se mantiene en el estado de la Datos aplicación (se pierde al cerrar la pestaña; es una limitación documentada). 

## 9.17 ADMIN — Crear evento — `/admin/eventos/nuevo` 

42 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
@ Pulserass Eventos Artistas Sedes —Validar ingreso Tickets pagados__Administrar eventos A> Administracion ¥<br>© Eventos<br>Nuevo evento<br>Cédigo del evento<br>CMF-2026<br>Nombre<br>Caribbean Music Fest 2026<br>Descripcién<br>Tres noches de musica caribefia frente a la bahia.<br>52/800<br>Categoria Edad minima<br>MUSIC ¥ 18<br>Fecha y hora<br>12/12/2026 08:00 PM 8<br>sede<br>Marina Convention Center (VEN-SMR-01) v<br>Cancelar [cree evento_]<br>© 2026 PulsePass - Caso de estudio académico Sedes Artistas<br><!-- End of picture text -->

|`┌──────────────────────────────────────────────────────────────────────────┐`<br>`│ ← Eventos                          Nuevo evento                          │`<br>|
|---|
|`│ Código del evento    [ CMF-2026                         ]                │`<br>|
|`│ Nombre               [ Caribbean Music Fest 2026        ]                │`<br>|
|`│ Descripción          [                                  ]                │`<br>`│                      [                                  ] 0/500          │`<br>|
|`│ Categoría            [ MUSIC ▾ ]      Edad mínima [ 18 ]                 │`|
|`│ Fecha y hora         [ 2026-12-12 20:00`📅`]                             │`|
|`│ Sede                 [ Marina Convention Center (VEN-SMR-01) ▾ ]         │`<br>`│                                                                          │`|
|<br>`│                                      [ Cancelar ]  [ Crear evento ]      │`<br>`└──────────────────────────────────────────────────────────────────────────┘`|



|**Aspecto**|**Definición**|
|---|---|
|Validación|`eventCode`,`name`,`venueCode`requeridos;`category`en el catálogo;`eventDate`requerida;`minimumAge`entero ≥ 0;|
|(zod)|`description`≤ longitud máxima definida por el backend.|
|Flujo|`POST /api/events`→`201`→ navega a`/admin/eventos/:eventCode`con aviso.|
|Errores|`400`→`details`por campo;`409`(código duplicado / fecha pasada / sede inválida) → aviso arriba del formulario;`403`<br>→ redirige a`/403`.|
|Datos|Select de sedes desde`useSedesActivas()`.|



## 9.18 ADMIN — Gestionar evento — `/admin/eventos/:eventCode` 

43 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
© Pulserass Eventos Artistas Sedes —Validar ingreso Tickets pagados__Administrar eventos A> Administracion ¥<br>© Eventos<br>Caribbean Music Fest 2026 DRAFT<br>CHF-2826 -MUSIC - 12 die 2026 20:00 - Min. 18 - Marina ConventionCenter<br>Publicacion<br>El evento es visible al publico una vez publicado. Publicar evento<br>Artistas asociados<br>s8 Solar Beat<br>Agregar artista<br>Neon Waves ’ + Asociar<br>Tickets pagados ><br><!-- End of picture text -->



<!-- Start of picture text -->
┌──────────────────────────────────────────────────────────────────────────┐<br>│ ← Eventos                                                                │<br>│ Caribbean Music Fest 2026 · CMF-2026                      [DRAFT]        │<br>│ MUSIC · 12 dic 2026 20:00 · Mín. 18 · Marina Convention Center           │<br>│                                                                          │<br>│ Publicación                                                              │<br>│ El evento es visible al público una vez publicado.  [ Publicar evento ]  │<br>│                                                                          │<br>│ Artistas asociados                                                       │<br>│ (○) Solar Beat                                                           │<br>│ Agregar artista   [ Neon Waves ▾ ]  [ Asociar ]                          │<br>│                                                                          │<br>│ Tickets pagados →  /staff/eventos/CMF-2026/tickets                       │<br>└──────────────────────────────────────────────────────────────────────────┘<br><!-- End of picture text -->

#### **Aspecto Definición** 

Datos `useEvento(eventCode)` , `useArtistasActivos()` ; mutaciones `usePublicarEvento()` y `useAsociarArtista()` . "Publicar" con modal de confirmación; deshabilitado si el estado ≠ `DRAFT` . "Asociar" bloquea artistas ya asociados en la UI Acciones (el backend sigue siendo la autoridad: `409` ). Éxito Invalida `['evento', eventCode]` y `['eventos','publicados']` . Errores `409` mostrado en aviso (transición inválida, artista duplicado, evento `CANCELLED` / `FINISHED` ). 

## 9.19 Páginas 403, 404 y error de ruta 

44 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
|: —_<br>No tienes permisos para acceder a esta seccién. No encontramos esta pagina.<br>Volver al inicio Volver al inicio<br>iUps! Algo salié mal.<br>Ocurrié un error inesperado. Inténtalo mas tarde.<br>| eitentar_] Volver a inicio<br>© 2026 PulsePass -Caso de estudio académico Sedes Artistas<br><!-- End of picture text -->



<!-- Start of picture text -->
┌────────────────────────────────────┐   ┌────────────────────────────────────┐<br>│               403                  │   │               404                  │<br>│       No tienes permisos para      │   │    No encontramos esta página      │<br>│       acceder a esta sección.      │   │                                    │<br>│         [ Volver al inicio ]       │   │         [ Volver al inicio ]       │<br>└────────────────────────────────────┘   └────────────────────────────────────┘<br>┌──────────────────────────────────────────────────────────────────┐<br>│ ¡Ups!  Algo salió mal.                                            │<br>│ <mensaje seguro: sin stack trace>                                 │<br>│ [ Reintentar ]   [ Volver al inicio ]                             │<br>└──────────────────────────────────────────────────────────────────┘<br>Aspecto Definición<br>errorElement Usa  useRouteError()  +  isRouteErrorResponse ; mensajes sin detalles internos.<br>403 Se muestra por  RutaPorRol  o cuando la API responde  403  en una carga de página completa.<br><!-- End of picture text -->

## 9.20 Estados transversales de interfaz 

45 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 



<!-- Start of picture text -->
GD ruiserass cece Anerson ove<br>Estados de interfaz<br>Cargando (esqueleto) Vacio Error con reintento<br>& AX Noppudimos conectar con el<br>Aun no hay eventos publicados<br>Avisos (toast) de mutaciones<br>Y Cuenta creada, inicia sesion. AN Eiticket no puede cancelarse en su estado 4 Tu sesién expiré. nicia sesiénde nuevo.<br>A Elticket ya fue utilizado.<br><!-- End of picture text -->

|**Estado**|**Diseño**|
|---|---|
|Cargando|Esqueletos (`animate-pulse`) con la forma del contenido;`aria-busy="true"`.|
|Vacío|Ícono, mensaje y acción sugerida.|
|Error|Mensaje + botón "Reintentar" (`refetch`).|
|Éxito de mutación|`Aviso`verde,`aria-live="polite"`, desaparece a los 4 s.|
|Error de mutación|`Aviso`rojo persistente hasta cierre; en formularios, mensaje junto al campo (`aria-describedby`).|
|Sesión expirada|Aviso "Tu sesión expiró" en`/login`.|



# 10. Contratos de datos consumidos 

Los tipos del cliente se **derivan de esquemas zod** ( `z.infer` ). Los nombres de campos no mostrados aquí deben tomarse de los DTO reales de la Fase 2; los campos listados son el mínimo requerido por las vistas. 

## 10.1 Enumeraciones 

```
exportconstEventCategorySchema = z.enum([
'MUSIC', 'SPORTS', 'TECHNOLOGY', 'EDUCATION', 'CULTURE', 'ENTERTAINMENT',
])
exportconstEventStatusSchema = z.enum(['DRAFT', 'PUBLISHED', 'SOLD_OUT', 'CANCELLED', 'FINISHED'])
exportconstTicketTypeSchema = z.enum(['GENERAL', 'VIP', 'BACKSTAGE', 'STUDENT'])
exportconstTicketStatusSchema = z.enum(['RESERVED', 'PAID', 'CANCELLED', 'USED'])
exportconstRolSchema = z.enum(['USER', 'STAFF', 'ADMIN'])
```

## 10.2 Respuestas y solicitudes 

|**Tipo (zod → TS)**|**Campos mínimos**<br>**Origen**|
|---|---|
|`VenueResponse`|`code`,`name`,`city`,`capacity`,`active`<br>F2/F3|



46 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

|**Tipo (zod → TS)**|**Campos mínimos**|**Origen**|
|---|---|---|
|`EventResponse`|`eventCode`,`name`,`description`,`category`,`eventDate`,`minimumAge`,`status`, sede<br>(`venueCode`/`venueName`), artistas|F2/F3|
|`EventSummaryResponse`|`eventCode`,`name`,`category`,`eventDate`,`status`, ciudad/sede|F2/F3|
|`ArtistResponse`|`id`,`stageName`,`active`(+ datos descriptivos)|F2/F3|
|`UserResponse`|`username`,`email`,`firstName`,`lastName`,`birthDate`,`active`|F2/F3 (sin<br>hash)|
|`TicketResponse`|`ticketCode`,`type`,`price`,`status`,`purchaseDate`, usuario, evento|F2/F3|
|`ErrorResponse`|`timestamp`,`status`,`error`,`message`,`details: Record<string,string>`|F3|
|`LoginRequest`|`identifier`,`password`|F4|
|`AuthResponse`|`accessToken`,`tokenType: 'Bearer'`,`expiresIn: number`|F4|
|`AuthenticatedUserResponse`|identificador,`username`,`email`,`roles`(mínimo)|F4|
|`CreateEventRequest`|`eventCode`,`name`,`description`,`category`,`eventDate`,`minimumAge`,`venueCode`|F3|
|`RegisterUserRequest`|`username`,`email`,`firstName`,`lastName`,`birthDate`,`password`|F3+F4|
|`PurchaseTicketRequest`|`userEmail`,`eventCode`,`type`|F3|
|Supuesto: los nombres exactos<br>backend en la etapa 1 del plan<br>vez confirmados.|de`AuthenticatedUserResponse`y de los campos opcionales de los DTO se confirman<br>(sección 29). Los esquemas zod usan`.passthrough()`solo donde exista duda y se end|contra el<br>urecen una|



## 10.3 Cobertura obligatoria API → Frontend 

La Fase 5 debe cubrir **las 20 operaciones** de la Fase 3 y **los 3 endpoints** de la Fase 4 con una forma de invocarlos desde la interfaz. 

|**#**|**Operación**|**Hook / mutación**|**Página**|
|---|---|---|---|
|1|`GET /api/venues/{code}`|`useSede`|`DetalleSede`|
|2|`GET /api/venues/active`|`useSedesActivas`|`ListaSedes`,`AdminCrearEvento`|
|3|`POST /api/events`|`useCrearEvento`|`AdminCrearEvento`|
||||`DetalleEvento`,|
|4|`GET /api/events/{eventCode}`|`useEvento`|`AdminGestionEvento`,|
||||`AdminEventos`|
|5|`GET /api/events/published`|`useEventosPublicados`|`Inicio`,`CatalogoEventos`|
|6|`PATCH /api/events/{eventCode}/publish`|`usePublicarEvento`|`AdminGestionEvento`|
|7|`POST`<br>`/api/events/{eventCode}/artists/{artistId}`|`useAsociarArtista`|`AdminGestionEvento`|
|8|`GET /api/events/by-artist?stageName=`|`useEventosPorArtista`|`CatalogoEventos`,<br>`DetalleArtista`|
|9|`GET /api/artists/{id}`|`useArtista`|`DetalleArtista`|
|10|`GET /api/artists/by-stage-name?stageName=`|`useArtistaPorNombre`|`CatalogoEventos`(filtro),<br>`DetalleArtista`|
|11|`GET /api/artists/active`|`useArtistasActivos`|`Inicio`,`ListaArtistas`,<br>`AdminGestionEvento`|
|12|`POST /api/users`|`useRegistrarUsuario`|`Registro`|
|13|`GET /api/users/by-email?email=`|`useUsuarioPorEmail`|`Cuenta`|
|14|`GET /api/users/by-username?username=`|`useUsuarioPorUsername`|`Cuenta`|



47 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

|**#**|**Operación**|**Hook / mutación**|**Página**|
|---|---|---|---|
|15|`POST /api/tickets`|`useComprarTicket`|`DetalleEvento`|
||||`DetalleTicket`,|
|16|`GET /api/tickets/{ticketCode}`|`useTicket`|`ConfirmacionCompra`,<br>`ValidacionIngreso`|
|17|`GET /api/tickets/by-user?email=`|`useMisTickets`|`MisTickets`|
|18|`GET /api/events/{eventCode}/tickets/paid`|`useTicketsPagadosPorEvento`|`TicketsPagadosEvento`|
|19|`PATCH /api/tickets/{ticketCode}/cancel`|`useCancelarTicket`|`MisTickets`,`DetalleTicket`|
|20|`PATCH /api/tickets/{ticketCode}/use`|`useUsarTicket`|`ValidacionIngreso`,<br>`TicketsPagadosEvento`|
|A1|`POST /api/auth/login`|`useLogin`|`Login`|
|A2|`GET /api/auth/me`|`useSesion`(en`AuthContext`)|`Layout`,`Cuenta`|
|A3|`POST /api/users`(F4)|`useRegistrarUsuario`|`Registro`|



No se considera completa la Fase 5 si existe una operación de la tabla sin una forma de invocarla desde la UI. 

# 11. Validación de formularios 

La validación estructural vive en zod + React Hook Form ( `zodResolver` ); la validación de negocio vive en el backend. 

```
// Ejemplo: LoginSchema
constLoginSchema = z.object({
  identifier: z.string().min(1, 'Ingresa tu usuario o correo'),
  password: z.string().min(1, 'Ingresa tu contraseña'),
})
typeLoginForm = z.infer<typeofLoginSchema>
```

|**Formulario**|**Reglas en el cliente**|
|---|---|
|Login|`identifier`y`password`no vacíos.|
|Registro|`username`no vacío;`email`válido;`firstName`/`lastName`no vacíos;`birthDate`válida y no futura;`password`≥ 12<br>caracteres y ≤ 72 bytes UTF-8 (`new TextEncoder().encode(p).length`).|
|Crear evento|`eventCode`,`name`,`venueCode`no vacíos;`category`del enum;`eventDate`requerida;`minimumAge`entero ≥ 0;<br>`description`con límite acorde al backend.|
|Compra|`type` ∈ `TicketTypeSchema`.|
|Validación de<br>ingreso|`ticketCode`no vacío.|



## 11.1 Límite entre UI y Backend 

La UI valida: 

```
campo obligatorio · formato de correo · longitud · valor mínimo · tipo de dato · fecha bien formada
```

El backend valida (y la UI solo muestra el resultado `409` / `404` ): 

```
usuario activo · evento existente y publicado · edad mínima · capacidad
```

48 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

```
transiciones de estado · duplicados · propiedad del recurso · permisos
```

## 11.2 Errores del servidor en formularios 

Si la API responde `400` con `details` , cada clave se asigna al campo homónimo con `setError(clave, { message })` . Si la clave no corresponde a un campo, se muestra en un aviso general del formulario. 

# 12. Autenticación y autorización en el cliente 

## 12.1 Estado de sesión (unión discriminada) 

- `type Sesion = | { estado: 'cargando' } | { estado: 'anonimo' } | { estado: 'autenticado'; token: string; expiraEn: number; usuario: UsuarioAutenticado }` 

`AuthContext` expone `sesion` , `iniciarSesion(identifier, password)` , `cerrarSesion()` y `tieneRol(...roles)` . 

## 12.2 Ciclo de vida del token 



<!-- Start of picture text -->
Login AuthContext lib/http API REST<br>Persona<br>identifier + password<br>iniciarSesion<br>POST /api/auth/login<br>credenciales<br>accessToken, expiresIn=900<br>guardar token en sessionStorage<br>GET /api/auth/me (Bearer)<br>Authorization Bearer<br>usuario y roles<br>sesion autenticada<br>redireccion a la ruta de origen<br>Cualquier 401 posterior cierra la sesion local<br>Login AuthContext lib/http API REST<br>Persona<br><!-- End of picture text -->

|**Decisión**|**Definición**|
|---|---|
|Almacenamiento|`sessionStorage`(se pierde al cerrar la pestaña); el token también vive en memoria.**Nunca** `localStorage`ni<br>cookies. El valor leído al arrancar se valida (zod) y se descarta si ya expiró.|
|Envío|Solo en`Authorization: Bearer <token>`desde`lib/http.ts`. Nunca en la URL.|



49 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

#### **Decisión Definición** 

Se programa un cierre local al vencer `expiraEn` ; además, cualquier `401` ejecuta `cerrarSesion()` y redirige a Expiración `/login` con `state.motivo = 'expirada'` . Roles Provienen de `GET /api/auth/me` ; no se decodifica el JWT para decidir permisos. 

Cierre de sesión Borra token, limpia `QueryClient` ( `queryClient.clear()` ) y navega a `/` . 

## 12.3 Guardas de ruta 

```
exportdefaultfunctionRutaPorRol({ roles }: { roles: Rol[] }) {
const { sesion } = useAuth()
const location = useLocation()
if (sesion.estado === 'cargando') return<Cargando />
if (sesion.estado === 'anonimo')
return<Navigate to={RUTAS.login} replace state={{ desde: location.pathname }} />
if (!roles.some((r) => sesion.usuario.roles.includes(r)))
return<Navigate to={RUTAS.sinPermisos} replace />
return<Outlet />
}
```

Las guardas solo protegen la **interfaz** . Cada endpoint está protegido en el servidor (Fase 4); un `403` del servidor siempre prevalece. 

## 12.4 Rol → rutas y acciones visibles 

|**Ruta / acción**|**Anónimo**|**USER**|**STAFF**|**ADMIN**|
|---|---|---|---|---|
|Catálogo, detalle, artistas, sedes|✔|✔|✔|✔|
|Comprar entrada|login|✔|—|—|
|Mis tickets / detalle / cancelar|—|✔|—|—|
|Validación de ingreso, tickets pagados|—|—|✔|✔|
|Crear/publicar evento, asociar artista|—|—|—|✔|
|Mi cuenta|—|✔|✔|✔|



Nota: STAFF/ADMIN no compran desde la UI (la API autoriza la compra a `USER` , FR-SEC-TKT-01). 

## 12.5 Seguridad en el cliente 

No hay secretos en el _bundle_ ; solo `VITE_API_BASE_URL` , que es público por definición. 

Prohibido `dangerouslySetInnerHTML` con datos de la API (mitigación XSS); React escapa por defecto. 

El token en `sessionStorage` es accesible a XSS; se mitiga con vida corta (15 min), escapado, ausencia de HTML dinámico y CSP recomendada en producción. 

El email enviado en compras se toma de la sesión; el servidor lo verifica contra el principal (anti-IDOR) y es la autoridad. No se loguean tokens, contraseñas ni respuestas completas en consola. 

# 13. Contrato de errores y mapeo HTTP → interfaz 

`lib/http.ts` convierte toda respuesta no exitosa en `ApiError` (validando `ErrorResponse` con zod; si el cuerpo no cumple, usa un error genérico). 

```
exportclassApiErrorextendsError {
constructor(
public status: number,
    message: string,
public details: Record<string, string> = {},
```

50 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

```
) { super(message) }
}
```

|**Código**|**Causa típica**|**Comportamiento de la UI**|
|---|---|---|
|Red /`fetch`<br>falla|Sin conexión / API caída|"No pudimos conectar con el servidor." + reintentar.|
|400|Bean Validation / JSON inválido|`details`→ campos del formulario; si no hay`details`, aviso general.|
|401|Sin token, token vencido/manipulado,<br>login incorrecto|Login: "Credenciales inválidas". Otras rutas:`cerrarSesion()`+<br>redirección a`/login`("Tu sesión expiró").|
|403|Rol insuficiente o recurso ajeno|Página`/403`en cargas de página; aviso en acciones.|
|404|Recurso inexistente (o ajeno si la política<br>oculta existencia)|`NoEncontrada`o "No disponible" según contexto.|
|409|Regla de negocio o duplicado|Mostrar`message`del backend en aviso/modal (texto controlado por<br>backend, ya sanitizado, renderizado como texto).|
|500|Error inesperado|"Ocurrió un error inesperado. Inténtalo más tarde." Sin detalles.|



Reglas: 

Los mensajes se muestran como **texto** (nunca HTML). 

`409` y `400` se consideran errores esperados: no disparan `errorElement` ; se manejan en la mutación/formulario. Errores de carga de página ( `useQuery` con `throwOnError` solo para 404/403/500 de página completa) pueden derivar en `errorElement` . 

# 14. Estado y datos del servidor 

## 14.1 Convenciones de TanStack Query 

|**Recurso**|**`queryKey`**|**`staleTime`**|
|---|---|---|
|Eventos publicados|`['eventos','publicados']`|60 s|
|Evento por código|`['evento', eventCode]`|30 s|
|Eventos por artista|`['eventos','artista', stageName]`|60 s|
|Artistas activos|`['artistas','activos']`|5 min|
|Artista|`['artista', id]`|5 min|
|Sedes activas|`['sedes','activas']`|5 min|
|Sede|`['sede', code]`|5 min|
|Usuario|`['usuario', email]`|60 s|
|Tickets del usuario|`['tickets','usuario', email]`|15 s|
|Ticket|`['ticket', ticketCode]`|15 s|
|Tickets pagados de evento|`['tickets','pagados', eventCode]`|15 s|



## 14.2 Invalidaciones tras mutaciones 

|**Mutación**|**Invalida**|
|---|---|
|`useComprarTicket`|`['tickets','usuario', email]`,`['evento', eventCode]`|
|`useCancelarTicket`|`['tickets','usuario', email]`,`['ticket', code]`,`['tickets','pagados', eventCode]`|
|`useUsarTicket`|`['ticket', code]`,`['tickets','pagados', eventCode]`|



51 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

|**Mutación**|**Invalida**|
|---|---|
|`useCrearEvento`|`['evento', eventCode]`(siembra con el resultado)|
|`usePublicarEvento`|`['evento', eventCode]`,`['eventos','publicados']`|
|`useAsociarArtista`|`['evento', eventCode]`,`['eventos','artista', stageName]`|
|`cerrarSesion`|`queryClient.clear()`|



## 14.3 Política de reintentos 

`retry: false` para `4xx` (no tiene sentido reintentar `401/403/404/409` ); 1 reintento para errores de red y `5xx` . Las mutaciones nunca se reintentan automáticamente (evitar compras duplicadas). 

## 14.4 Estado de interfaz local 

`useState` para modales, menú móvil y filtros efímeros; filtros compartibles viven en la URL ( `useSearchParams` ). Los datos de servidor **no** se copian a Context ni a estado local. 

# 15. Estilos y Tailwind CSS 

- Tailwind v4 con `@tailwindcss/vite` y `@import "tailwindcss"` en `src/index.css` ; tema en `@theme` (9.1). Variantes de componentes con `Record<Variante, string>` y `clsx` ; `className` externo se mezcla al final. Prohibido construir clases dinámicas; siempre mapas completos. 

- `@apply` solo para estilos repetidos que no justifiquen un componente. 

- Mobile-first con `sm:` , `md:` , `lg:` ; contenedor `mx-auto max-w-6xl px-4` . 

- Modo oscuro con `dark:` (clase o `prefers-color-scheme` , a decidir en la etapa 2). 

- Accesibilidad: `focus-visible:` , tamaños táctiles ≥ 44 px, contraste AA, `sr-only` para etiquetas visuales ocultas. Herramientas: `prettier-plugin-tailwindcss` , extensión Tailwind CSS IntelliSense. 

# 16. Restricciones técnicas del caso académico 

Node 22.22+ (exigido por React Router v8), React y React DOM 19.2.7+. 

Vite (plantilla `react-ts` ), TypeScript con `strict: true` y `noUncheckedIndexedAccess` recomendado. 

- Paquetes: `react-router` (v8), `@tanstack/react-query` , `react-hook-form` , `@hookform/resolvers` , `zod` , `clsx` , `tailwindcss` , `@tailwindcss/vite` . 

Pruebas: `vitest` , `@testing-library/react` , `@testing-library/user-event` , `@testing-library/jest-dom` , `msw` . Calidad: ESLint, Prettier + `prettier-plugin-tailwindcss` . 

- Fetch nativo (sin Axios) encapsulado en `lib/http.ts` . 

No utilizar: 

```
react-router-dom                       (eliminado en v8; importar de 'react-router')
BrowserRouter junto a RouterProvider   (un solo enfoque de enrutamiento)
React.FC · any · enum de TS para el dominio (usar uniones literales/zod enum)
localStorage o cookies para el token
token en query string
dangerouslySetInnerHTML con datos de la API
reglas de negocio dentro de componentes
clases Tailwind construidas dinámicamente
estado de servidor copiado en useState/Context
```

# 17. Estructura esperada del proyecto 

```
pulsepass-web/
├── docs/
```

52 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

```
│   └── diseno/                    # copia de imagenes/ (32 PNG de la sección 9.0)
├── index.html
├── package.json
├── vite.config.ts                 # plugins: react(), tailwindcss(); server.proxy '/api'
├── tsconfig.json
├── .env.example                   # VITE_API_BASE_URL=/api  (sin secretos)
├── src/
│   ├── main.tsx                   # QueryClientProvider · AuthProvider · RouterProvider (react-
router/dom)
│   ├── router.tsx                 # createBrowserRouter con lazy, guardas y errorElement
│   ├── index.css                  # @import "tailwindcss"; @theme { ... }
│   │
│   ├── lib/
│   │   ├── http.ts                # fetch + Bearer + ApiError + validación ErrorResponse
│   │   ├── rutas.ts               # RUTAS = { inicio: '/', evento: (c) => `/eventos/${c}`, ... }
│   │   ├── formato.ts             # fechas es-CO, moneda COP (Intl)
│   │   └── queryClient.ts
│   │
│   ├── layouts/
│   │   ├── Layout.tsx             # Navbar + Outlet + Footer
│   │   ├── RutaProtegida.tsx
│   │   ├── RutaPorRol.tsx
│   │   └── SoloAnonimo.tsx
│   │
│   ├── componentes/               # UI genérica
│   │   ├── Boton.tsx  Campo.tsx  Selector.tsx  Badge.tsx  Tarjeta.tsx
│   │   ├── Modal.tsx  Tabla.tsx  Aviso.tsx  PrecioCOP.tsx
│   │   └── Cargando.tsx  EstadoVacio.tsx  EstadoError.tsx
│   │
│   ├── funcionalidades/
│   │   ├── auth/        { api.ts  tipos.ts  AuthContext.tsx  hooks.ts  componentes/ }
│   │   ├── eventos/     { api.ts  tipos.ts  hooks.ts  componentes/ }
│   │   ├── artistas/    { api.ts  tipos.ts  hooks.ts  componentes/ }
│   │   ├── sedes/       { api.ts  tipos.ts  hooks.ts  componentes/ }
│   │   ├── usuarios/    { api.ts  tipos.ts  hooks.ts  componentes/ }
│   │   ├── tickets/     { api.ts  tipos.ts  hooks.ts  componentes/ }
│   │   └── admin/       { componentes/ }
│   │
│   ├── pages/
│   │   ├── Inicio.tsx  CatalogoEventos.tsx  DetalleEvento.tsx
│   │   ├── ListaArtistas.tsx  DetalleArtista.tsx
│   │   ├── ListaSedes.tsx  DetalleSede.tsx
│   │   ├── Login.tsx  Registro.tsx  Cuenta.tsx
│   │   ├── MisTickets.tsx  DetalleTicket.tsx  ConfirmacionCompra.tsx
│   │   ├── ValidacionIngreso.tsx  TicketsPagadosEvento.tsx
│   │   ├── AdminEventos.tsx  AdminCrearEvento.tsx  AdminGestionEvento.tsx
│   │   └── SinPermisos.tsx  NoEncontrada.tsx  ErrorPagina.tsx
│   │
│   └── test/
│       ├── setup.ts               # jest-dom, MSW server
│       ├── mocks/handlers.ts      # handlers MSW de los 23 endpoints
│       └── utils.tsx              # renderConProveedores (Router + Query + Auth)
```

Fragmento de configuración de desarrollo: 

```
// vite.config.ts
exportdefaultdefineConfig({
  plugins: [react(), tailwindcss()],
  server: { proxy: { '/api': 'http://localhost:8080' } },
})
```

# 18. Casos de uso de interfaz clave 

53 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

|**ID**|**Caso de uso**|**Ruta principal**|**Resultado esperado**|
|---|---|---|---|
|UC-FE-01|Explorar cartelera|`/eventos`|Eventos publicados visibles y filtrables.|
|UC-FE-02|Ver detalle de evento|`/eventos/CMF-2026`|Datos, artistas y sede.|
|UC-FE-03|Registrarse|`/registro`|Cuenta creada y redirección a login.|
|UC-FE-04|Iniciar sesión|`/login`|Sesión activa y retorno a la ruta de origen.|
|UC-FE-05|Comprar ticket|`/eventos/CMF-2026`|Ticket`PAID`y página de confirmación.|
|UC-FE-06|Consultar mis tickets|`/mis-tickets`|Lista de tickets propios.|
|UC-FE-07|Cancelar ticket|`/mis-tickets`|Ticket`CANCELLED`.|
|UC-FE-08|Validar ingreso|`/staff/validacion`|Ticket`USED`.|
|UC-FE-09|Crear y publicar evento|`/admin/eventos/nuevo`|Evento`PUBLISHED`.|
|UC-FE-10|Asociar artista|`/admin/eventos/CMF-2026`|Artista visible en el evento.|
|UC-FE-11|Sesión expirada|cualquier ruta protegida|Redirección a login con aviso.|
|UC-FE-12|Acceso sin permisos|`/admin/eventos`como USER|Página 403.|



# 19. Criterios de aceptación funcionales 

|**ID**|**Escenario de aceptación**|
|---|---|
|AC-FE-|Dado un visitante anónimo, cuando abre`/eventos`, entonces se listan los eventos de`GET /api/events/published`sin|
|001|pedir login.|
|AC-FE-|Dado`CMF-2026`publicado, cuando se abre`/eventos/CMF-2026`, entonces se muestran nombre, categoría, edad mínima,|
|002|artistas y sede`VEN-SMR-01`.|
|AC-FE-<br>003|Dado un código de evento inexistente o no publicado, cuando un anónimo lo abre, entonces ve "Evento no disponible" sin<br>datos del borrador.|
|AC-FE-|Dado un anónimo en el detalle, cuando pulsa "Comprar", entonces es redirigido a`/login`y tras iniciar sesión regresa al|
|004|evento.|
|AC-FE-|Dado un registro con contraseña de 11 caracteres, cuando se envía, entonces el formulario muestra el error y**no**llama a la|
|005|API.|
|AC-FE-<br>006|Dado un registro válido, cuando la API responde`201`, entonces se redirige a`/login`con aviso de éxito.|
|AC-FE-<br>007|Dado un email duplicado, cuando la API responde`409`, entonces se muestra un mensaje genérico sin detalles internos.|
|AC-FE-|Dadas credenciales incorrectas, cuando la API responde`401`, entonces se muestra "Credenciales inválidas" sin distinguir|
|008|usuario o contraseña.|
|AC-FE-|Dado un login correcto, entonces se guarda el token solo en`sessionStorage`/memoria, se carga`/api/auth/me`y se|
|009|muestra el nombre en la navbar.|
|AC-FE-|Dada Andrea autenticada (`USER`), cuando confirma la compra de un ticket`VIP`, entonces`POST /api/tickets`envía su|
|010|propio email y, ante`201`, navega a la confirmación.|
|AC-FE-|Dada Laura (17 años), cuando la API responde`409`por edad mínima, entonces el modal muestra el mensaje del backend y|
|011|no navega.|
|AC-FE-<br>012|Dado un ticket`PAID`, cuando el usuario confirma "Cancelar", entonces el estado pasa a`CANCELLED`y la lista se actualiza.|
|AC-FE-<br>013|Dado un ticket`USED`, entonces el botón "Cancelar" no se muestra; si se fuerza y la API responde`409`, se muestra el<br>mensaje.|



54 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

|**ID**|**Escenario de aceptación**|
|---|---|
|AC-FE-<br>014|Dado un ticket de Carlos, cuando Andrea abre su URL, entonces ve "Ticket no disponible" (`403/404`) sin datos del ticket.|
|AC-FE-<br>015|Dado un`USER`, cuando navega a`/admin/eventos`, entonces es redirigido a`/403`; la API seguiría respondiendo`403`.|
|AC-FE-<br>016|Dado un`STAFF`, cuando marca un ticket`PAID`como usado, entonces ve el éxito; al repetir, ve el mensaje`409`.|
|AC-FE-<br>017|Dado un`ADMIN`, cuando crea un evento con campos vacíos, entonces ve errores por campo y no se llama a la API.|
|AC-FE-<br>018|Dado un`ADMIN`, cuando publica`CMF-2026`en`DRAFT`, entonces el badge pasa a`PUBLISHED`y el evento aparece en la<br>cartelera.|
|AC-FE-<br>019|Dado un token expirado, cuando cualquier petición responde`401`, entonces se cierra la sesión, se limpia la caché y se<br>redirige a`/login`.|
|AC-FE-<br>020|Dada una ruta inexistente, entonces se muestra la página 404.|
|AC-FE-<br>021|Ningún componente contiene reglas de negocio de edad, capacidad o transiciones de estado.|
|AC-FE-<br>022|Las 20 operaciones de la Fase 3 y los 3 endpoints de la Fase 4 están alcanzables desde la UI (tabla 10.3).|



# 20. Estrategia de pruebas y calidad 

Las pruebas validan el comportamiento de la interfaz **sin backend real** : la API se simula con MSW ( _Mock Service Worker_ ) a nivel de red. 

```
Acción de usuario (user-event)
        ↓
Componente / Página / Guarda
        ↓
Hook TanStack Query → api.ts → fetch
        ↓
MSW (respuesta simulada: 200/201/400/401/403/404/409/500)
        ↓
Aserciones sobre lo que ve la persona (roles, textos)
```

## 20.1 Criterios de calidad 

|**ID**|**Criterio**|
|---|---|
|QT-FE-001|Cada página tiene al menos una prueba de renderizado de sus estados (cargando, éxito, vacío, error).|
|QT-FE-002|Las consultas usan`getByRole`/`findByRole`antes que clases o ids.|
|QT-FE-003|Cada formulario prueba: campos obligatorios, formato inválido y envío correcto.|
|QT-FE-004|Se prueban las guardas con sesión anónima,`USER`,`STAFF`y`ADMIN`.|
|QT-FE-005|Se prueba el mapeo de`400`,`401`,`403`,`404`,`409`y`500`a mensajes de UI.|
|QT-FE-006|Un formulario inválido no dispara petición HTTP (se verifica en MSW).|
|QT-FE-007|Se verifica que el token se envía solo en el encabezado`Authorization`.|
|QT-FE-008|Se verifica la invalidación de caché tras cada mutación.|
|QT-FE-009|Las pruebas son deterministas: sin red real, sin relojes reales (fechas inyectadas).|



55 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

#### **ID** 

#### **Criterio** 

QT-FE-010 `npm run lint` , `npm run build` (tsc estricto) y `npm test` finalizan sin errores. 

QT-FE-011 Auditoría de accesibilidad básica (Lighthouse/axe) sin errores críticos en las páginas principales. 

# 21. Tests mínimos requeridos 

## 21.1 Infraestructura y utilidades 

```
TEST-FE-LIB-001  http: añade Authorization Bearer cuando hay sesión
TEST-FE-LIB-002  http: ErrorResponse válido → ApiError con status y details
TEST-FE-LIB-003  http: cuerpo de error inválido → ApiError genérico
TEST-FE-LIB-004  http: 401 → dispara cierre de sesión
TEST-FE-LIB-005  formato: PrecioCOP formatea 250000 como COP
```

## 21.2 Autenticación y guardas 

```
TEST-FE-AUTH-001  login válido → sesión autenticada y redirección a 'desde'
TEST-FE-AUTH-002  login 401 → "Credenciales inválidas"
TEST-FE-AUTH-003  login sin campos → errores y 0 peticiones
TEST-FE-AUTH-004  registro con contraseña corta → error y 0 peticiones
TEST-FE-AUTH-005  registro 201 → redirección a /login
TEST-FE-AUTH-006  registro 409 → mensaje genérico
TEST-FE-AUTH-007  RutaProtegida: anónimo → /login con state.desde
TEST-FE-AUTH-008  RutaPorRol: USER en /admin → /403
TEST-FE-AUTH-009  RutaPorRol: ADMIN en /admin → renderiza
TEST-FE-AUTH-010  cerrar sesión limpia token y caché
TEST-FE-AUTH-011  token expirado al restaurar sesión → anónimo
```

## 21.3 Catálogo, artistas y sedes 

```
TEST-FE-EVT-001  cartelera: lista eventos publicados
TEST-FE-EVT-002  cartelera: lista vacía → estado vacío
TEST-FE-EVT-003  cartelera: filtro por texto/categoría actualiza la URL
TEST-FE-EVT-004  detalle: muestra evento, artistas y sede
TEST-FE-EVT-005  detalle: 404 → "Evento no disponible"
TEST-FE-EVT-006  detalle: evento SOLD_OUT → compra deshabilitada
TEST-FE-ART-001  lista de artistas activos
TEST-FE-ART-002  detalle de artista con eventos
TEST-FE-ART-003  id no numérico → 404 sin llamar a la API
TEST-FE-VEN-001  lista y detalle de sede; 404 de sede
```

## 21.4 Tickets 

```
TEST-FE-TKT-001  compra válida → 201 y navega a confirmación
TEST-FE-TKT-002  compra anónima → redirige a login
TEST-FE-TKT-003  compra 409 → mensaje del backend en el modal
TEST-FE-TKT-004  compra envía el email de la sesión
TEST-FE-TKT-005  mis tickets: lista y filtra por estado
TEST-FE-TKT-006  mis tickets: vacío
TEST-FE-TKT-007  cancelar PAID → 200 y lista actualizada
TEST-FE-TKT-008  cancelar → 409 muestra mensaje y mantiene modal
TEST-FE-TKT-009  ticket ajeno 403/404 → "Ticket no disponible"
TEST-FE-TKT-010  botón cancelar oculto si status ≠ PAID
```

56 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

## 21.5 STAFF y ADMIN 

```
TEST-FE-STF-001  validar ingreso: buscar y marcar como usado → éxito
TEST-FE-STF-002  validar ingreso: segunda vez → 409 visible
TEST-FE-STF-003  validar ingreso: ticket inexistente → 404 visible
TEST-FE-STF-004  tickets pagados de evento: tabla con filas
TEST-FE-ADM-001  crear evento: campos vacíos → errores y 0 peticiones
TEST-FE-ADM-002  crear evento: 201 → navega a gestión
TEST-FE-ADM-003  crear evento: 400 con details → errores en campos
TEST-FE-ADM-004  publicar evento → badge PUBLISHED
TEST-FE-ADM-005  publicar → 409 visible
TEST-FE-ADM-006  asociar artista → aparece en la lista; duplicado → 409
```

## 21.6 Transversales 

```
TEST-FE-GEN-001  ruta inexistente → 404
TEST-FE-GEN-002  errorElement muestra mensaje seguro (sin stack)
TEST-FE-GEN-003  navbar muestra enlaces según rol
TEST-FE-GEN-004  menú móvil abre y cierra con teclado
```

# 22. Datos de referencia del escenario 

Se mantiene el mismo escenario de los PRD anteriores para dar continuidad a las pruebas y a la demostración. 

## 22.1 Sede 

|**Campo**|**Valor**|
|---|---|
|code|`VEN-SMR-01`|
|name|Marina Convention Center|
|city|Santa Marta|
|capacity|3|
|active|true|



## 22.2 Evento principal 

|**Campo**|**Valor**|
|---|---|
|eventCode|`CMF-2026`|
|name|Caribbean Music Fest 2026|
|category|MUSIC|
|status inicial|DRAFT → PUBLISHED|
|minimumAge|18|



## 22.3 Artistas 

```
Solar Beat · Neon Waves · Caribbean Sound
```

## 22.4 Usuarios y roles de demostración 

57 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

|**Persona**|**Perfil**|**Rol de UI**|
|---|---|---|
|Andrea|Adulta, activa|`USER`|
|Carlos|Adulto, activo|`USER`|
|Laura|17 años, activa|`USER`(la compra se rechaza por edad) o`STAFF`en la demostración de operación, según F4|
|Miguel|Adulto, inactivo|`USER`(sin acceso: login rechazado)|
|Administración|—|`ADMIN`(semilla local controlada, F4)|



Tickets de referencia: Andrea `VIP` 250000 `PAID` ; Carlos `GENERAL` 120000 `PAID` . 

# 23. Requisitos no funcionales 

|**ID**|**Requisito**|
|---|---|
|NFR-FE-<br>001|Accesibilidad: objetivo WCAG 2.1 AA; navegación completa por teclado; foco visible; etiquetas en todos los campos.|
|NFR-FE-<br>002|Responsive: usable desde 360 px de ancho; sin desplazamiento horizontal de página.|
|NFR-FE-<br>003|Rendimiento: carga perezosa por ruta; primera carga de Inicio sin descargar páginas de admin/staff; Lighthouse<br>Performance ≥ 80 en Inicio.|
|NFR-FE-<br>004|Seguridad: sin secretos en el bundle; token fuera de URL, logs y`localStorage`; texto de error renderizado como texto.|
|NFR-FE-<br>005|Tipado:`strict`activo, sin`any`; datos externos validados con zod.|
|NFR-FE-<br>006|Consistencia: todos los errores de la API pasan por`ApiError`.|
|NFR-FE-<br>007|Mantenibilidad: estructura por funcionalidades; componentes ≤ ~150 líneas; UI genérica reutilizable.|
|NFR-FE-<br>008|Localización: idioma es-CO; fechas con`Intl.DateTimeFormat`, moneda COP con`Intl.NumberFormat`.|
|NFR-FE-<br>009|Determinismo de pruebas: sin dependencias de red real ni reloj del sistema.|
|NFR-FE-<br>010|Compatibilidad: últimas dos versiones de Chrome, Edge, Firefox y Safari.|



# 24. Anti-patrones prohibidos 

## 24.1 Reglas de negocio en la UI 

No permitido: 

```
if (usuario.edad < evento.minimumAge) { /* bloquear compra */ }
```

La UI envía la solicitud y muestra el `409` . 

## 24.2 Confiar en la UI como seguridad 

No permitido: considerar que ocultar un botón o una ruta protege la operación. El servidor siempre valida. 

## 24.3 Identidad editable en compras 

58 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

p 

No permitido: un campo de formulario para el email del comprador. Se toma de la sesión. 

## 24.4 Clases dinámicas 

```
// Mal
<span className={`bg-${color}-100`} />
// Bien
const tonos = { exito: 'bg-emerald-100', error: 'bg-red-100' } asconst
```

## 24.5 Estado de servidor duplicado 

No copiar `data` de `useQuery` a `useState` o Context; usar la caché. 

## 24.6 Token en lugar inseguro 

No almacenar el JWT en `localStorage` , cookies ni URL; no imprimirlo en consola. 

## 24.7 Mezclar enfoques de router 

No usar `<BrowserRouter>` junto con `RouterProvider` ; no importar de `react-router-dom` . 

## 24.8 Tipos manuales desincronizados 

No escribir `interface` a mano para respuestas de API cuando existe el esquema zod; derivar con `z.infer` . 

## 24.9 `useEffect` para cargar datos 

No usar `useEffect` + `fetch` para datos de servidor; usar TanStack Query (o `loader` ). 

## 24.10 HTML inyectado 

No usar `dangerouslySetInnerHTML` con contenido de la API. 

# 25. Riesgos y supuestos 

|**ID**|**Riesgo / supuesto**|
|---|---|
|R-FE-<br>001|El frontend depende de que las Fases 3 y 4 estén implementadas según sus PRD; cambios en DTO o rutas exigen cambios en<br>los esquemas zod.|
|R-FE-<br>002|Los nombres exactos de ciertos campos de los DTO (p. ej.`AuthenticatedUserResponse`, estructura de artistas en<br>`EventResponse`) deben confirmarse con el backend real antes de endurecer los esquemas.|
|R-FE-<br>003|GAP-01: no hay listado completo de eventos; el panel ADMIN es limitado.|
|R-FE-<br>004|GAP-03: "Mis tickets" depende del email expuesto por`/api/auth/me`.|
|R-FE-<br>005|GAP-05: sin CORS configurado en el backend, solo el proxy de desarrollo (o mismo origen) funciona.|
|R-FE-<br>006|Token en`sessionStorage`es vulnerable a XSS; se acepta en el MVP académico con mitigaciones (sección 12.5).|
|R-FE-<br>007|Token de 15 min sin refresh: la persona debe volver a iniciar sesión; se mitiga con avisos claros.|
|R-FE-<br>008|STAFF tiene alcance global sobre tickets (limitación de F4); la UI no puede restringirlo.|



59 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

|**ID**|**Riesgo / supuesto**|
|---|---|
|R-FE-<br>009|Sin disponibilidad restante en el contrato, la persona puede intentar comprar un evento agotado y recibir`409`.|
|R-FE-<br>010|Sin paginación, listas grandes impactarán el rendimiento; evolución futura.|
|R-FE-|Las versiones mayores (React Router v8, Tailwind v4, React 19.2.7+) pueden diferir de ejemplos externos; se prioriza lo|
|011|indicado en las guías del directorio.|



# 26. Fuera de alcance del taller 

- Cambios en la API o en la base de datos. Pasarela de pagos y facturación. Generación y lectura de códigos QR. Refresh tokens, MFA, recuperación y cambio de contraseña. Administración de usuarios y roles desde la UI. Gestión de sedes y artistas (crear/editar) desde la UI. Paginación, ordenamiento en servidor, búsqueda en servidor. SSR/SSG, PWA, modo offline, notificaciones push. Internacionalización a otros idiomas. Pruebas E2E con navegador (Playwright/Cypress). Despliegue productivo, CDN y CI/CD. 

# 27. Evolución futura sugerida 

- Pruebas E2E con Playwright. Refresh token con cookie `HttpOnly` y rotación. Paginación e infinite scroll cuando la API lo soporte. Endpoint de disponibilidad y compra por cantidad. Código QR en el ticket y lector de cámara para STAFF. Panel ADMIN completo (listado de eventos, sedes y artistas). Asignación de STAFF a eventos. Internacionalización (i18n) y selector de moneda. PWA y almacenamiento offline de tickets. Observabilidad en cliente (errores y métricas web). Biblioteca de componentes documentada (Storybook). Despliegue con CDN y política CSP estricta. 

# 28. Matriz de trazabilidad resumida 

|**Requisito**<br>**Frontend**|**Endpoint de origen**|**Página**|**Test principal**|
|---|---|---|---|
|FR-FE-EVT-<br>001/002|`GET /api/events/published`|`CatalogoEventos`,`Inicio`|TEST-FE-EVT-<br>001/002/003|
|FR-FE-EVT-<br>003/005|`GET /api/events/{eventCode}`|`DetalleEvento`|TEST-FE-EVT-<br>004/005/006|
|FR-FE-EVT-004|`GET /api/events/by-artist`|`CatalogoEventos`,<br>`DetalleArtista`|TEST-FE-ART-002|
|FR-FE-ART-|`*`|`ListaArtistas`,|TEST-FE-ART-|
|001/002/003|`GET /api/artists/`|`DetalleArtista`|001/002/003|



60 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

|**Requisito**<br>**Frontend**|**Endpoint de origen**|**Página**|**Test principal**|
|---|---|---|---|
|FR-FE-VEN-<br>001/002|`GET /api/venues/*`|`ListaSedes`,`DetalleSede`|TEST-FE-VEN-001|
|FR-FE-AUTH-001|`POST /api/users`|`Registro`|TEST-FE-AUTH-<br>004/005/006|
|FR-FE-AUTH-<br>002/003|`POST /api/auth/login`|`Login`|TEST-FE-AUTH-<br>001/002/003|
|FR-FE-AUTH-<br>004/005/006|`GET /api/auth/me`|`AuthContext`|TEST-FE-AUTH-<br>010/011, TEST-FE-LIB-<br>004|
|FR-FE-AUTH-007|`GET /api/users/by-email`,`by-username`|`Cuenta`|TEST-FE-AUTH-009<br>(render)|
|FR-FE-TKT-<br>001/002|`POST /api/tickets`|`DetalleEvento`|TEST-FE-TKT-001..004|
|FR-FE-TKT-003|`GET /api/tickets/by-user`|`MisTickets`|TEST-FE-TKT-005/006|
|FR-FE-TKT-004|`GET /api/tickets/{ticketCode}`|`DetalleTicket`|TEST-FE-TKT-009|
|FR-FE-TKT-005|`PATCH /api/tickets/{ticketCode}/cancel`|`MisTickets`,<br>`DetalleTicket`|TEST-FE-TKT-<br>007/008/010|
|FR-FE-STF-001|`PATCH /api/tickets/{ticketCode}/use`|`ValidacionIngreso`|TEST-FE-STF-<br>001/002/003|
|FR-FE-STF-002|`GET /api/events/{eventCode}/tickets/paid`|`TicketsPagadosEvento`|TEST-FE-STF-004|
|FR-FE-ADM-<br>001/002|`POST /api/events`,`GET /api/venues/active`|`AdminCrearEvento`|TEST-FE-ADM-<br>001/002/003|
|FR-FE-ADM-<br>003/004|`GET …/{eventCode}`,`PATCH …/publish`|`AdminGestionEvento`|TEST-FE-ADM-004/005|
|FR-FE-ADM-005|`POST`<br>`/api/events/{eventCode}/artists/{artistId}`|`AdminGestionEvento`|TEST-FE-ADM-006|
|FR-FE-GEN-<br>001/002|—|`Layout`, páginas de error|TEST-FE-GEN-001..004|



# 29. Plan de implementación incremental (taller) 

El orden evita introducir varias fuentes de fallos al mismo tiempo. 

|**Etapa**|**Actividad**|**Evidencia de entrega**|
|---|---|---|
|1. Contrato|Confirmar con el backend los campos reales de los DTO y la política<br>403/404 para recursos ajenos; fijar esquemas zod.|Archivo`tipos.ts`por funcionalidad y<br>colección Postman de referencia.|
|2. Proyecto base|Crear Vite + React + TS, Tailwind v4, ESLint/Prettier, proxy`/api`,<br>tokens`@theme`.|`npm run dev`y`npm run build`correctos;<br>pantalla de prueba con Tailwind.|
|3. UI genérica|`Boton`,`Campo`,`Badge`,`Tarjeta`,`Modal`,`Tabla<T>`, estados.|Componentes con pruebas básicas.|
|4. Router y|`router.tsx`,`Layout`,`Navbar`, páginas 403/404/`errorElement`,|Navegación entre páginas vacías; lazy|
|layouts|`RUTAS`.|verificado.|
|5. Cliente HTTP|`lib/http.ts`,`ApiError`, MSW y pruebas.|TEST-FE-LIB-*.|
|6. Catálogo<br>público|Inicio, cartelera, detalle, artistas, sedes.|TEST-FE-EVT/ART/VEN-*.|



61 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

|**Etapa**|**Actividad**|**Evidencia de entrega**|
|---|---|---|
|7. Autenticación|`AuthContext`, Login, Registro, guardas, sesión.|TEST-FE-AUTH-*.|
|8. Tickets de<br>usuario|Compra, confirmación, mis tickets, detalle, cancelar.|TEST-FE-TKT-*.|
|9. STAFF|Validación de ingreso y tickets pagados.|TEST-FE-STF-*.|
|10. ADMIN|Buscar, crear, publicar y asociar artista.|TEST-FE-ADM-*.|
|11. Calidad|Accesibilidad, responsive, modo oscuro, Lighthouse, README.|Reporte y DoD.|



# 30. Definition of Ready y Definition of Done 

## 30.1 Definition of Ready 

- `[ ] Las Fases 3 y 4 están disponibles localmente (API en http://localhost:8080)` 

- `[ ] Existen usuarios de demostración USER, STAFF y ADMIN (semilla local de F4)` 

- `[ ] Node 22.22+ instalado` 

- `[ ] Se confirmaron los nombres de campos de los DTO (R-FE-002)` 

- `[ ] Se acordó la política 403 vs 404 para recursos ajenos` 

- `[ ] El equipo leyó las tres guías del directorio` 

## 30.2 Definition of Done 

- `[ ] Existen las 20 rutas/páginas de la sección 8 con carga perezosa (excepto Inicio y Layout)` 

- `[ ] Se usa createBrowserRouter + RouterProvider (react-router/dom); no hay react-router-dom` 

- `[ ] Las 20 operaciones de la Fase 3 y los 3 endpoints de la Fase 4 son alcanzables desde la UI` 

- `[ ] Los tipos de API se derivan de esquemas zod y se valida toda respuesta` 

- `[ ] Todos los formularios usan React Hook Form + zod` 

- `[ ] El token JWT solo se almacena en memoria/sessionStorage y viaja solo en Authorization` 

- `[ ] RutaProtegida y RutaPorRol funcionan; un 403/401 del servidor siempre se respeta` 

- `[ ] 401 cierra la sesión, limpia la caché y redirige a login` 

- `[ ] 400, 401, 403, 404, 409 y 500 se muestran según la sección 13` 

- `[ ] La compra usa el email de la sesión y nunca un campo editable` 

- `[ ] No hay reglas de negocio en componentes` 

- `[ ] Existen estados de carga, vacío y error en cada pantalla de datos` 

- `[ ] Las clases de Tailwind no se construyen dinámicamente` 

- `[ ] Cada página implementada es visualmente consistente con su imagen de la sección 9.0 (estructura, textos, iconos, estados y colores)` 

- `[ ] Las variantes móvil (390 px) y modo oscuro de la sección 9.0 están implementadas` 

- `[ ] Layout responsive desde 360 px y navegación por teclado completa` 

- `[ ] No se usa any, React.FC, localStorage para el token ni dangerouslySetInnerHTML con datos de la API` 

- `[ ] Las pruebas de la sección 21 pasan con Vitest + Testing Library + MSW` 

62 / 63 

PRD_PulsePass_Frontend_React_TypeScript_Tailwind.md 

2026-11-09 

- `[ ] npm run lint, npm run build y npm test finalizan sin errores` 

- `[ ] No hay secretos en el repositorio; existe .env.example` 

- `[ ] El README documenta variables, comandos, roles de demostración y rutas principales` 

# 31. Preguntas para el equipo 

1. ¿Por qué ocultar un botón en la interfaz no equivale a proteger una operación? 

2. ¿Qué diferencia existe entre una validación estructural (zod) y una regla de negocio (backend)? 

3. ¿Cuándo debe mostrarse un `400` , un `404` y un `409` y cómo se diferencian en la UI? 

4. ¿Por qué el email de una compra se toma de la sesión y no de un campo del formulario? 

5. ¿Qué ventajas aporta TanStack Query frente a `useEffect` + `useState` para datos del servidor? 

6. ¿Por qué se derivan los tipos con `z.infer` en lugar de escribir interfaces a mano? 

7. ¿Qué riesgos tiene guardar el JWT en `sessionStorage` y cómo se mitigan? 

8. ¿Cómo se comporta la aplicación cuando el token expira en medio de una compra? 

9. ¿Por qué conviene `createBrowserRouter` y `lazy` en una aplicación con varios roles? 

10. ¿Qué cambios requeriría pasar a refresh token con cookie `HttpOnly` ? 

11. ¿Cómo adaptarías la tabla de tickets pagados a pantallas móviles? 

12. ¿Qué brechas de la API (GAP-01…08) afectan más la experiencia y qué endpoint pedirías primero? 

# 32. Resumen final 

PulsePass se considera correctamente especificado para la **Fase 5 — Frontend** cuando los estudiantes pueden tomar la API segura de las fases anteriores y construir una SPA tipada, accesible y responsive que cubra todas las operaciones del sistema, respete los roles y traduzca de forma consistente los errores del backend. 

Este PRD no redefine el dominio, las reglas de negocio ni los contratos HTTP. Completa la evolución incremental del caso de estudio: 

```
Persistencia   →  Datos consistentes
Servicio       →  Reglas de negocio
Controlador    →  API REST
Seguridad      →  Identidad, roles y propiedad
Frontend       →  Experiencia de usuario
```

El documento define el **QUÉ** debe ofrecer la interfaz —páginas, rutas, diseños de vistas, contratos de datos, criterios de aceptación y pruebas—. El estudiante debe implementar el **CÓMO** respetando la separación de responsabilidades: la UI presenta y orquesta; el backend decide y protege. 

63 / 63 

