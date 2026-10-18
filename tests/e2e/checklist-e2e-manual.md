# Checklist — pruebas E2E manuales PulsePass (Fase 5)

Orden de ejecución. Marcar cada caso tras registrar su resultado en
`registro-ejecucion-manual.md`. Si un prerrequisito falla, marcar los
dependientes como **BLOCKED** (nunca PASS).

**Precondiciones globales:** app levantada sobre `pulsepass_e2e`, migraciones
Flyway aplicadas, `seed-e2e.sql` aplicado, ambiente **PulsePass-local**
seleccionado, contraseñas y `runId` completados, `CONN-01` = PASS.

**Recordatorios:** copiar tokens a mano tras cada login · denegaciones IDOR
antes de mutar tickets · T1 cancelable y T2 usable son tickets distintos ·
no usar Collection Runner (envío individual con **Send**).


## Preflight

- [ ] **CONN-01** — `GET /api/venues/active` — actor `ANONYMOUS` — HTTP esperado **200**

## Flujo A — Descubrimiento público

- [ ] **E2E-VEN-01** — `GET /api/venues/{{venueActiveCode}}` — actor `ANONYMOUS` — HTTP esperado **200**
- [ ] **E2E-VEN-01N** — `GET /api/venues/NO-EXISTE-E2E` — actor `ANONYMOUS` — HTTP esperado **404**
- [ ] **E2E-VEN-02** — `GET /api/venues/active` — actor `ANONYMOUS` — HTTP esperado **200**
- [ ] **E2E-ART-01** — `GET /api/artists/{{artistId}}` — actor `ANONYMOUS` — HTTP esperado **200**
- [ ] **E2E-ART-01N** — `GET /api/artists/999999` — actor `ANONYMOUS` — HTTP esperado **404**
- [ ] **E2E-ART-02** — `GET /api/artists/by-stage-name` — actor `ANONYMOUS` — HTTP esperado **200**
- [ ] **E2E-ART-03** — `GET /api/artists/active` — actor `ANONYMOUS` — HTTP esperado **200**

## Flujo B — Registro, JWT y propiedad

- [ ] **E2E-USR-01** — `POST /api/users` — actor `ANONYMOUS` — HTTP esperado **201**
- [ ] **E2E-USR-01B** — `POST /api/users` — actor `ANONYMOUS` — HTTP esperado **201**
- [ ] **E2E-USR-01D** — `POST /api/users` — actor `ANONYMOUS` — HTTP esperado **409**
- [ ] **E2E-USR-01V** — `POST /api/users` — actor `ANONYMOUS` — HTTP esperado **400**
- [ ] **E2E-AUTH-01** — `POST /api/auth/login` — actor `ANONYMOUS` — HTTP esperado **200**
- [ ] **E2E-AUTH-01E** — `POST /api/auth/login` — actor `ANONYMOUS` — HTTP esperado **200**
- [ ] **E2E-AUTH-01B** — `POST /api/auth/login` — actor `ANONYMOUS` — HTTP esperado **200**
- [ ] **E2E-AUTH-01S** — `POST /api/auth/login` — actor `ANONYMOUS` — HTTP esperado **200**
- [ ] **E2E-AUTH-01A** — `POST /api/auth/login` — actor `ANONYMOUS` — HTTP esperado **200**
- [ ] **E2E-AUTH-01P** — `POST /api/auth/login` — actor `ANONYMOUS` — HTTP esperado **401**
- [ ] **E2E-AUTH-01X** — `POST /api/auth/login` — actor `ANONYMOUS` — HTTP esperado **401**
- [ ] **E2E-AUTH-01I** — `POST /api/auth/login` — actor `ANONYMOUS` — HTTP esperado **401**
- [ ] **E2E-AUTH-02** — `GET /api/auth/me` — actor `USER_A` — HTTP esperado **200**
- [ ] **E2E-AUTH-02S** — `GET /api/auth/me` — actor `STAFF` — HTTP esperado **200**
- [ ] **E2E-AUTH-02A** — `GET /api/auth/me` — actor `ADMIN` — HTTP esperado **200**
- [ ] **E2E-AUTH-02N** — `GET /api/auth/me` — actor `ANONYMOUS` — HTTP esperado **401**
- [ ] **E2E-USR-02** — `GET /api/users/by-email` — actor `USER_A` — HTTP esperado **200**
- [ ] **E2E-USR-03** — `GET /api/users/by-username` — actor `USER_A` — HTTP esperado **200**
- [ ] **E2E-USR-02X** — `GET /api/users/by-email` — actor `USER_B` — HTTP esperado **403**
- [ ] **E2E-USR-03X** — `GET /api/users/by-username` — actor `USER_B` — HTTP esperado **403**
- [ ] **E2E-USR-02E** — `GET /api/users/by-email` — actor `USER_A` — HTTP esperado **404**
- [ ] **E2E-USR-02A** — `GET /api/users/by-email` — actor `ADMIN` — HTTP esperado **200**

## Flujo C — Catálogo (ADMIN)

- [ ] **E2E-EVT-01** — `POST /api/events` — actor `ADMIN` — HTTP esperado **201**
- [ ] **E2E-EVT-01X** — `POST /api/events` — actor `ANONYMOUS` — HTTP esperado **401**
- [ ] **E2E-EVT-01U** — `POST /api/events` — actor `USER_A` — HTTP esperado **403**
- [ ] **E2E-EVT-02** — `GET /api/events/{{publishedEventCode}}` — actor `ANONYMOUS` — HTTP esperado **200**
- [ ] **E2E-EVT-02D** — `GET /api/events/{{draftEventCode}}` — actor `ANONYMOUS` — HTTP esperado **404**
- [ ] **E2E-EVT-02DS** — `GET /api/events/{{draftEventCode}}` — actor `STAFF` — HTTP esperado **200**
- [ ] **E2E-EVT-02DA** — `GET /api/events/{{draftEventCode}}` — actor `ADMIN` — HTTP esperado **200**
- [ ] **E2E-EVT-03** — `GET /api/events/published` — actor `ANONYMOUS` — HTTP esperado **200**
- [ ] **E2E-EVT-04** — `PATCH /api/events/{{eventToPublishCode}}/publish` — actor `ADMIN` — HTTP esperado **200**
- [ ] **E2E-EVT-04X** — `PATCH /api/events/{{eventToPublishCode}}/publish` — actor `USER_A` — HTTP esperado **403**
- [ ] **E2E-EVT-04R** — `PATCH /api/events/{{eventToPublishCode}}/publish` — actor `ADMIN` — HTTP esperado **409**
- [ ] **E2E-EVT-05** — `POST /api/events/{{createdEventCode}}/artists/{{artistId}}` — actor `ADMIN` — HTTP esperado **200**
- [ ] **E2E-EVT-05R** — `POST /api/events/{{createdEventCode}}/artists/{{artistId}}` — actor `ADMIN` — HTTP esperado **409**
- [ ] **E2E-EVT-05X** — `POST /api/events/{{createdEventCode}}/artists/{{artistId}}` — actor `USER_A` — HTTP esperado **403**
- [ ] **E2E-EVT-06** — `GET /api/events/by-artist` — actor `ANONYMOUS` — HTTP esperado **200**

## Flujo D — Compra, propiedad, cancelación y uso

- [ ] **E2E-TKT-01** — `POST /api/tickets` — actor `USER_A` — HTTP esperado **201**
- [ ] **E2E-TKT-01T** — `POST /api/tickets` — actor `USER_A` — HTTP esperado **201**
- [ ] **E2E-TKT-01X** — `POST /api/tickets` — actor `USER_A` — HTTP esperado **403**
- [ ] **E2E-TKT-01D** — `POST /api/tickets` — actor `USER_A` — HTTP esperado **409**
- [ ] **E2E-TKT-01E** — `POST /api/tickets` — actor `USER_A` — HTTP esperado **404**
- [ ] **E2E-TKT-01A** — `POST /api/tickets` — actor `USER_B` — HTTP esperado **409**
- [ ] **E2E-TKT-01M** — `POST /api/tickets` — actor `USER_A` — HTTP esperado **400**
- [ ] **E2E-TKT-02** — `GET /api/tickets/{{ticketCancelableCode}}` — actor `USER_A` — HTTP esperado **200**
- [ ] **E2E-TKT-02X** — `GET /api/tickets/{{ticketCancelableCode}}` — actor `USER_B` — HTTP esperado **403**
- [ ] **E2E-TKT-02N** — `GET /api/tickets/TCK-00000000` — actor `USER_A` — HTTP esperado **404**
- [ ] **E2E-TKT-03** — `GET /api/tickets/by-user` — actor `USER_A` — HTTP esperado **200**
- [ ] **E2E-TKT-03X** — `GET /api/tickets/by-user` — actor `USER_B` — HTTP esperado **403**
- [ ] **E2E-TKT-04** — `GET /api/events/{{publishedEventCode}}/tickets/paid` — actor `STAFF` — HTTP esperado **200**
- [ ] **E2E-TKT-04X** — `GET /api/events/{{publishedEventCode}}/tickets/paid` — actor `USER_A` — HTTP esperado **403**
- [ ] **E2E-TKT-04N** — `GET /api/events/E2E-NO-EXISTE-{{runId}}/tickets/paid` — actor `STAFF` — HTTP esperado **200**
- [ ] **E2E-TKT-05** — `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` — actor `USER_A` — HTTP esperado **200**
- [ ] **E2E-TKT-05X** — `PATCH /api/tickets/{{ticketUsableCode}}/cancel` — actor `USER_B` — HTTP esperado **403**
- [ ] **E2E-TKT-05R** — `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` — actor `USER_A` — HTTP esperado **409**
- [ ] **E2E-TKT-05V** — `GET /api/tickets/{{ticketCancelableCode}}` — actor `USER_A` — HTTP esperado **200**
- [ ] **E2E-TKT-06X** — `PATCH /api/tickets/{{ticketUsableCode}}/use` — actor `USER_B` — HTTP esperado **403**
- [ ] **E2E-TKT-06** — `PATCH /api/tickets/{{ticketUsableCode}}/use` — actor `STAFF` — HTTP esperado **200**
- [ ] **E2E-TKT-06R** — `PATCH /api/tickets/{{ticketUsableCode}}/use` — actor `STAFF` — HTTP esperado **409**
- [ ] **E2E-TKT-06V** — `GET /api/tickets/{{ticketUsableCode}}` — actor `USER_A` — HTTP esperado **200**
- [ ] **E2E-TKT-07** — `POST /api/tickets` — actor `USER_A` — HTTP esperado **201**
- [ ] **E2E-TKT-08** — `POST /api/tickets` — actor `USER_B` — HTTP esperado **409**

## Flujo E — Errores y regresión

- [ ] **E2E-SEC-01** — `GET /api/auth/me` — actor `ANONYMOUS (token malformado)` — HTTP esperado **401**
- [ ] **E2E-SEC-02** — `GET /api/auth/me` — actor `USER_A (token alterado)` — HTTP esperado **401**
- [ ] **E2E-SEC-03** — `GET /api/auth/me` — actor `ANONYMOUS` — HTTP esperado **401**
- [ ] **E2E-SEC-04** — `GET /api/events/published` — actor `ANONYMOUS (token inventado)` — HTTP esperado **401**
- [ ] **E2E-SEC-05** — `GET /api/auth/me` — actor `USER_A (token expirado)` — HTTP esperado **401**
- [ ] **E2E-SEC-06** — `GET /api/auth/me` — actor `usuario desactivado` — HTTP esperado **401**
- [ ] **E2E-SEC-07** — `POST /api/events` — actor `STAFF` — HTTP esperado **403**
- [ ] **E2E-SEC-08** — `POST /api/events/{{createdEventCode}}/artists/{{artistId}}` — actor `STAFF` — HTTP esperado **403**
- [ ] **E2E-SEC-09** — `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` — actor `STAFF` — HTTP esperado **403**
- [ ] **E2E-SEC-10** — `GET /api/auth/me` — actor `ANONYMOUS (JWT firmado con clave ajena)` — HTTP esperado **401**
- [ ] **E2E-SEC-11** — `POST /api/events` — actor `ADMIN (rol revocado)` — HTTP esperado **403**
- [ ] **E2E-ERR-01** — `POST /api/events` — actor `ADMIN` — HTTP esperado **400**
- [ ] **E2E-ERR-02** — `POST /api/users` — actor `ANONYMOUS` — HTTP esperado **400**
- [ ] **E2E-ERR-03** — `POST /api/auth/login` — actor `ANONYMOUS` — HTTP esperado **400**
- [ ] **E2E-ERR-04** — `GET /api/events/by-artist` — actor `ANONYMOUS` — HTTP esperado **400**
- [ ] **E2E-ERR-05** — `POST /api/events` — actor `ADMIN` — HTTP esperado **400**
- [ ] **E2E-ERR-06** — `POST /api/events` — actor `ADMIN` — HTTP esperado **404**
- [ ] **E2E-ERR-07** — `PATCH /api/events/E2E-NO-EXISTE-{{runId}}/publish` — actor `ADMIN` — HTTP esperado **404**
- [ ] **E2E-ERR-08** — `POST /api/events` — actor `ADMIN` — HTTP esperado **409**
- [ ] **E2E-ERR-09** — `GET /api/events/by-artist` — actor `ANONYMOUS` — HTTP esperado **200**
- [ ] **E2E-ERR-10** — `POST /api/users` — actor `ANONYMOUS` — HTTP esperado **400**

## Checkpoints de cierre (Flujos C y D)

- [ ] **E2E-FLU-C1** — `GET /api/events/{{eventToPublishCode}}` — actor `ANONYMOUS` — HTTP esperado **200**
- [ ] **E2E-FLU-C2** — `GET /api/events/published` — actor `ANONYMOUS` — HTTP esperado **200**
- [ ] **E2E-FLU-D1** — `GET /api/tickets/by-user` — actor `USER_A` — HTTP esperado **200**
- [ ] **E2E-FLU-D2** — `GET /api/events/{{publishedEventCode}}/tickets/paid` — actor `STAFF` — HTTP esperado **200**

---

## Cierre

- [ ] 22/22 rutas base con al menos un caso nominal ejecutado (ver §5 del README).
- [ ] Consolidado PASS / FAIL / BLOCKED / NOT RUN por módulo en la bitácora.
- [ ] Defectos y regresiones con precondiciones, pasos y reproducción manual.
- [ ] Evidencias sin JWT, contraseñas ni secretos.
- [ ] Ningún caso omitido declarado como aprobado.
