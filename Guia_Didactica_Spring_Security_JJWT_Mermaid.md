Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 creacion


# Guía didáctica — Spring Security con JWT usando JJWT 

Java 21 + Spring Boot 4 + Spring Security + `io.jsonwebtoken` 

## Propósito 

Esta guía enseña de forma progresiva cómo proteger una API REST con **Spring Security** y **JWT** , utilizando **JJWT (** **`io.jsonwebtoken` )** para crear, firmar y validar los tokens. 

La meta no es solamente que el código funcione. Al finalizar, el estudiante deberá comprender: 

- qué problema resuelve Spring Security; 

- la diferencia entre autenticación y autorización; 

- cómo funcionan `AuthenticationManager` , `AuthenticationProvider` , `UserDetailsService` y `PasswordEncoder` ; 

- qué contiene un JWT; 

- cómo JJWT firma y verifica un token; 

- por qué necesitamos un filtro; 

- cómo se construye un `Authentication` ; 

- para qué sirve `SecurityContextHolder` ; 

- cómo se protegen endpoints según roles; 

- qué significa una arquitectura `STATELESS` ; 

- cómo evolucionar desde usuarios en memoria hacia PostgreSQL. 

## Resultado de aprendizaje 

Flujo de login: 

1 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



<!-- Start of picture text -->
Cliente<br>username + password<br>t<br>POST<br>/api/auth/login<br>AuthenticationManager<br>DaoAuthenticationProvider<br>UserDetailsService PasswordEncoder Authentication<br>válida<br>JwtService<br>aa<br>JJWT<br>—<br>JWT firmado<br><!-- End of picture text -->

Flujo de una petición autenticada: 

2 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



3 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



<!-- Start of picture text -->
Cliente<br>Authorization: Bearer JWT<br>E<br>SecurityFilterChain<br>a<br>JwtAuthenticationFilter<br>[|__|<br>JwtService<br>_<br>JJWT valida<br>firma y claims<br>_<br>UserDetailsService<br>a<br>UserDetails<br>_<br>UsernamePasswordAuthenticationToken<br>SecurityContextHolder<br>| oe<br>Reglas de<br>autorización<br>= Controller<br><!-- End of picture text -->

4 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



# FUNDAMENTOS 

## ¿Qué es Spring Security? 

Spring Security es el framework del ecosistema Spring utilizado principalmente para: 

- autenticación; 

- autorización; 

- protección de endpoints; 

- contexto del usuario autenticado; 

- manejo de contraseñas; 

- seguridad a nivel de métodos. 



<!-- Start of picture text -->
Authentication<br>I<br>Authorization<br>I<br>Security Filters<br>I<br>Spring Security<br>SecurityContext<br>I<br>Password<br>Handling<br>|<br>Method Security<br>I<br><!-- End of picture text -->

## Authentication 

5 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

#### **Authentication** responde: 

¿Quién eres? 

Ejemplo: 



<!-- Start of picture text -->
username: student<br>password: ********<br>Componentes principales:<br>UserDetailsService<br>—<br>AuthenticationManager AuthenticationProvider<br>PasswordEncoder<br>a<br>Authorization<br><!-- End of picture text -->

#### **Authorization** responde: 

Ya sabemos quién eres. ¿Qué puedes hacer? 

Por ejemplo: 

`ROLE_USER` puede consultar su perfil. `ROLE_ADMIN` puede administrar eventos. Authentication Authorization ~~c=~~ 

Primero identificamos al usuario. Después evaluamos sus permisos. 

## Authentication vs Authorization 

|**Concepto**|**Pregunta**|
|---|---|
|Authentication|¿Quién eres?|
|Authorization|¿Qué puedes hacer?|



6 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



<!-- Start of picture text -->
student /<br>student123<br>Credenciales<br>válidas?<br>No Sí<br>401  Usuario<br>Unauthorized autenticado<br>Tiene<br>ROLE_ADMIN?<br>No Sí<br>403 Forbidden Acceso<br>permitido<br><!-- End of picture text -->

7 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

# JWT 

## ¿Qué es JWT? 

JWT significa **JSON Web Token** . 

Un JWT firmado suele tener tres partes: 



<!-- Start of picture text -->
HEADER.PAYLOAD.SIGNATURE<br>Header Payload Signature<br>ee e e ee<br><!-- End of picture text -->

## Header 

Ejemplo: 



<!-- Start of picture text -->
{<br>  "alg": "HS256",<br>  "typ": "JWT"<br>}<br><!-- End of picture text -->

## Payload 

El Payload contiene **claims** . 



<!-- Start of picture text -->
{<br>  "iss": "pulsepass-api",<br>  "sub": "student",<br>  "roles": ["USER"],<br>  "iat": 1780000000,<br>  "exp": 1780000900<br>}<br><!-- End of picture text -->

#### **Claim Significado** 

|`iss`<br>Issuer|
|---|
|`sub`<br>Subject|



8 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

|**Claim**|**Significado**|
|---|---|
|`iat`|Issued At|
|`exp`|Expiration|
|`aud`|Audience|
|`jti`|JWT ID|



También pueden existir claims propios como `roles` , `email` , `tenant` o `permissions` . 

## Signature 

La firma permite comprobar que el token no fue modificado desde que fue emitido. 



<!-- Start of picture text -->
Header Payload Secret Key<br>Algoritmo de<br>firma<br>a _<br>Signature<br>|<br><!-- End of picture text -->

## JWT no significa información cifrada 

El Payload normalmente está **codificado** , no cifrado. 

Nunca almacenar dentro del JWT: 

```
password
secret keys
números de tarjeta
información sensible innecesaria
```

Un JWT firmado proporciona principalmente: 

9 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

```
integridad
autenticidad
```

No confidencialidad. 

# SPRING SECURITY + JJWT 

## ¿Qué es JJWT? 

JJWT permite: 

- crear JWT; 

- firmarlos; 

- leer claims; 

- verificar firmas; 

- validar tokens. 

10 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



<!-- Start of picture text -->
Autenticación<br>|<br>Autorización<br>|<br>Spring Security<br>SecurityContext<br>|<br>Filter Chain<br>|<br>Crear JWT<br>|<br>Firmar JWT<br>|<br>JJWT Parsear JWT<br>|<br>Verificar firma<br>|<br>Leer claims<br>|<br><!-- End of picture text -->

**Spring Security** administra la seguridad de la aplicación. **JJWT** administra el token. 

## Endpoints de ejemplo 

```
POST /api/auth/login
GET  /api/public/events
```

11 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

```
GET  /api/profile
GET  /api/admin/report
```



<!-- Start of picture text -->
Endpoint Acceso<br>/api/auth/** Público<br>/api/public/** Público<br>/api/profile Autenticado<br>/api/admin/** ROLE_ADMIN<br>HTTP Request<br>Ruta<br>/api/auth/** /api/public/** /api/profile /api/admin/**<br>Permit All Authenticated ROLE_ADMIN<br><!-- End of picture text -->

# PROYECTO Y DEPENDENCIAS 

## Dependencias Maven 

Spring: 

```
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-web</artifactId>
```

12 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

```
</dependency>
```

```
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

#### JJWT: 

```
<properties>
```

```
<jjwt.version>0.13.0</jjwt.version>
</properties>
```

```
<dependency>
```

```
<groupId>io.jsonwebtoken</groupId>
<artifactId>jjwt-api</artifactId>
<version>${jjwt.version}</version>
</dependency>
```

```
<dependency>
```

```
<groupId>io.jsonwebtoken</groupId>
<artifactId>jjwt-impl</artifactId>
<version>${jjwt.version}</version>
<scope>runtime</scope>
</dependency>
```

```
<dependency>
<groupId>io.jsonwebtoken</groupId>
<artifactId>jjwt-jackson</artifactId>
<version>${jjwt.version}</version>
<scope>runtime</scope>
</dependency>
```

#### Testing: 

```
<dependency>
```

```
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-test</artifactId>
<scope>test</scope>
</dependency>
```

### ¿Por qué tres artefactos JJWT? 

`jjwt-api` : API pública. `jjwt-impl` : implementación interna. 

`jjwt-jackson` : serialización/deserialización JSON mediante Jackson. 

13 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

# CLAVE JWT 

## HS256 

Utilizaremos `HS256` . 

La misma clave se utiliza para firmar y verificar: 



<!-- Start of picture text -->
Secret Key<br>——<br>Sign JWT Verify JWT<br>HS eS<br><!-- End of picture text -->

Para HS256 necesitamos una clave criptográficamente fuerte. 

## 16. Generar el secreto 

```
openssl rand -base64 32
```

Guardar: 

```
JWT_SECRET=<valor-generado>
```

No construir una clave criptográfica a partir de una frase arbitraria mediante: 

```
"mi-clave".getBytes()
```

## application.yml 

```
security:
  jwt:
    secret: ${JWT_SECRET}
    expiration: 15m
    issuer: pulsepass-api
```

14 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

## JwtProperties 

```
package com.pulsepass.security;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "security.jwt")
publicrecordJwtProperties(
        String secret,
        Duration expiration,
        String issuer
) {
}
```

Habilitar: 

```
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
publicclassSecurityConfig {
}
```

# AUTENTICACIÓN USERNAME/PASSWORD 

## PasswordEncoder 



<!-- Start of picture text -->
@Bean<br>PasswordEncoder passwordEncoder() {<br>return new BCryptPasswordEncoder();<br>}<br>Password  Autenticación<br>Sí<br>recibida continúa<br>PasswordEncoder Coinciden?<br>Hash  Autenticación<br>No<br>almacenado falla<br><!-- End of picture text -->

Nunca comparar manualmente password en texto plano contra el hash almacenado. 

15 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

## Usuarios en memoria 

`@Bean UserDetailsService userDetailsService( PasswordEncoder passwordEncoder) { UserDetails student = User.withUsername("student") .password(passwordEncoder.encode("student123")) .roles("USER") .build(); UserDetails admin = User.withUsername("admin") .password(passwordEncoder.encode("admin123")) .roles("USER", "ADMIN") .build(); return new InMemoryUserDetailsManager( student, admin ); }` student ROLE_USER ~~ee~~ ROLE_USER admin ROLE_ADMIN > |— 

## UserDetailsService 

16 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



<!-- Start of picture text -->
username<br>I<br>password hash<br>username UserDetailsService UserDetails<br>authorities<br>enabled /<br>disabled<br>|<br><!-- End of picture text -->

## DaoAuthenticationProvider 

```
@Bean
AuthenticationProvider authenticationProvider(
        UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder) {
```

```
DaoAuthenticationProviderprovider =
newDaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
```

```
return provider;
}
```

17 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



<!-- Start of picture text -->
DaoAuthenticationProvider<br>UserDetailsService PasswordEncoder<br>Verificación<br>UserDetails<br>password<br>Autenticación<br>válida?<br><!-- End of picture text -->

## AuthenticationManager 



<!-- Start of picture text -->
@Bean<br>AuthenticationManager authenticationManager(<br>        AuthenticationConfiguration configuration)<br>throws Exception {<br>return configuration.getAuthenticationManager();<br>}<br>AuthenticationManager AuthenticationProvider Authentication<br><!-- End of picture text -->

## Authentication 

18 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

Después de un login correcto, `Authentication` representa al usuario autenticado. 

#### Ejemplo: 

```
principal = admin
authenticated = true
authorities = ROLE_USER, ROLE_ADMIN
```

# LOGIN Y JWT 

## DTOs 

```
publicrecordLoginRequest(
        String username,
        String password
) {
}
publicrecordTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
```

## Responsabilidad de JwtService 

19 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



<!-- Start of picture text -->
Generate token<br>a<br>Extract<br>username<br>JwtService Read claims<br>a<br>Verify signature<br>a<br>Validate token<br>a<br><!-- End of picture text -->

## Construir la clave de firma 

```
@Service
publicclassJwtService {
privatefinal JwtProperties properties;
publicJwtService(JwtProperties properties) {
this.properties = properties;
    }
private SecretKey getSigningKey() {
byte[] keyBytes =
                Decoders.BASE64.decode(
                    properties.secret()
                );
return Keys.hmacShaKeyFor(
                keyBytes
        );
    }
}
```

20 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

Imports: 

```
import javax.crypto.SecretKey;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
```

## Generar el token 

```
public String generateToken(
        UserDetails userDetails) {
Instantnow = Instant.now();
InstantexpiresAt =
            now.plus(
                properties.expiration()
            );
    List<String> roles =
            userDetails
                .getAuthorities()
                .stream()
                .map(
                    GrantedAuthority::getAuthority
                )
                .filter(
                    authority ->
                        authority.startsWith(
"ROLE_"
                        )
                )
                .map(
                    authority ->
                        authority.substring(5)
                )
                .toList();
return Jwts.builder()
            .issuer(
                properties.issuer()
            )
            .subject(
                userDetails.getUsername()
            )
            .issuedAt(
```

21 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

```
                Date.from(now)
            )
            .expiration(
                Date.from(expiresAt)
            )
            .claim(
"roles",
                roles
            )
            .signWith(
                getSigningKey()
            )
            .compact();
}
```

## Claims generados 

Ejemplo conceptual: 

```
{
  "iss": "pulsepass-api",
  "sub": "student",
  "iat": 1780000000,
  "exp": 1780000900,
  "roles": [
"USER"
  ]
}
```

## Flujo de generación del JWT 

22 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



<!-- Start of picture text -->
AuthenticationService JwtService JJWT Builder SecretKey<br>generateToken(userDetails)<br>Obtener username y roles<br>Calcular issuedAt y expiration<br>Jwts.builder()<br>Agregar claims<br>getSigningKey()<br>SecretKey<br>signWith(secretKey)<br>compact()<br>JWT<br>AuthenticationService JwtService JJWT Builder SecretKey<br><!-- End of picture text -->

## Parsear y verificar el JWT 

```
private Claims extractAllClaims(
        String token) {
return Jwts.parser()
            .verifyWith(
                getSigningKey()
            )
            .requireIssuer(
                properties.issuer()
            )
            .build()
            .parseSignedClaims(
```

23 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

```
                token
            )
            .getPayload();
}
```

Aquí JJWT: 

1. parsea el token; 

2. verifica la firma; 

3. exige el issuer esperado; 

4. procesa los claims; 

5. rechaza tokens inválidos. 

## Extraer el username 

```
public String extractUsername(
        String token) {
return extractAllClaims(token)
            .getSubject();
}
```

El username está almacenado en: 

```
sub
```

## Validar el token 

```
publicbooleanisTokenValid(
        String token,
        UserDetails userDetails) {
Claimsclaims =
            extractAllClaims(token);
Stringusername =
            claims.getSubject();
Dateexpiration =
            claims.getExpiration();
return username.equals(
                userDetails.getUsername()
```

24 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

```
           )
           &&
           expiration.after(
newDate()
           );
}
```

La firma ya fue validada durante: 

```
parseSignedClaims(...)
```

# AUTHENTICATION SERVICE 

## AuthenticationService 

```
@Service
publicclassAuthenticationService {
privatefinal AuthenticationManager
            authenticationManager;
privatefinal JwtService jwtService;
privatefinal JwtProperties properties;
publicAuthenticationService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            JwtProperties properties) {
this.authenticationManager =
                authenticationManager;
this.jwtService =
                jwtService;
this.properties =
                properties;
    }
public TokenResponse login(
            LoginRequest request) {
Authenticationauthentication =
                authenticationManager
                    .authenticate(
newUsernamePasswordAuthenticationToken(
```

25 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

```
                            request.username(),
                            request.password()
                        )
                    );
UserDetailsuserDetails =
                (UserDetails)
                    authentication.getPrincipal();
Stringtoken =
                jwtService.generateToken(
                    userDetails
                );
returnnewTokenResponse(
                token,
"Bearer",
                properties.expiration()
                          .toSeconds()
        );
    }
}
```

## Flujo completo de login 



<!-- Start of picture text -->
AuthenticationController AuthenticationService AuthenticationManager DaoAuthenticationProvider UserDetailsService PasswordEncoder JwtService JJWT<br>Client<br>POST /api/auth/login<br>login(username, password)<br>authenticate(...)<br>authenticate(...)<br>loadUserByUsername(username)<br>UserDetails<br>matches(raw, encoded)<br>true<br>Authentication<br>Authentication<br>generateToken(UserDetails)<br>builder + claims + signWith<br>JWT<br>token<br>TokenResponse<br>accessToken<br>AuthenticationController AuthenticationService AuthenticationManager DaoAuthenticationProvider UserDetailsService PasswordEncoder JwtService JJWT<br>Client<br><!-- End of picture text -->

## AuthenticationController 

```
@RestController
@RequestMapping("/api/auth")
```

26 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

```
publicclassAuthenticationController {
privatefinal AuthenticationService
            authenticationService;
publicAuthenticationController(
            AuthenticationService
                    authenticationService) {
this.authenticationService =
                authenticationService;
    }
@PostMapping("/login")
public TokenResponse login(
            @RequestBody
            LoginRequest request) {
return authenticationService
                .login(request);
    }
}
```

# FILTRO JWT 

¿Por qué necesitamos un filtro? 

El cliente enviará: 

```
Authorization: Bearer eyJ...
```

Alguien debe: 

1. leer `Authorization` ; 

2. extraer el JWT; 3. verificarlo; 

4. identificar el usuario; 

5. cargar authorities; 

6. crear un `Authentication` ; 

7. colocarlo en `SecurityContextHolder` . 

Ese componente será: 

```
JwtAuthenticationFilter
```

27 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

## OncePerRequestFilter 

```
@Component
publicclassJwtAuthenticationFilter
extendsOncePerRequestFilter {
```

`OncePerRequestFilter` es una base adecuada para implementar lógica de filtrado una vez por request. 

## Dependencias del filtro 

```
privatefinal JwtService jwtService;
privatefinal UserDetailsService
        userDetailsService;
publicJwtAuthenticationFilter(
        JwtService jwtService,
        UserDetailsService userDetailsService) {
this.jwtService =
            jwtService;
this.userDetailsService =
            userDetailsService;
}
```

## Flujo del filtro 

28 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



29 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



<!-- Start of picture text -->
HTTP Request<br>Authorization<br>header existe?<br>Sí<br>Empieza por<br>Bearer?<br>Sí<br>Extraer JWT<br>JwtService.extractUsername<br>JJWT verifica<br>firma<br>No Inválido Válido<br><!-- End of picture text -->

30 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 2026-10-02 No UserDetailsService | Limpiar UserDetails SecurityContext JwtService.isTokenValid Sí Crear No Authentication SecurityContextHolder Continuar FilterChain <u>Ne=</u> Implementación del filtro `@Override protected void doFilterInternal( HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException { String authHeader = request.getHeader( HttpHeaders.AUTHORIZATION );` 

31 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

```
if (
        authHeader == null
        ||
        !authHeader.startsWith("Bearer ")
    ) {
        filterChain.doFilter(
                request,
                response
        );
return;
    }
Stringtoken =
            authHeader.substring(7);
try {
Stringusername =
                jwtService
                    .extractUsername(token);
AuthenticationcurrentAuthentication =
                SecurityContextHolder
                    .getContext()
                    .getAuthentication();
if (
            username != null
            &&
            currentAuthentication == null
        ) {
UserDetailsuserDetails =
                    userDetailsService
                        .loadUserByUsername(
                            username
                        );
if (
                jwtService.isTokenValid(
                    token,
                    userDetails
                )
            ) {
UsernamePasswordAuthenticationToken
authentication =
newUsernamePasswordAuthenticationToken(
                        userDetails,
null,
                        userDetails.getAuthorities()
                    );
```

32 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

```
                authentication.setDetails(
newWebAuthenticationDetailsSource()
                        .buildDetails(request)
                );
                SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                        authentication
                    );
            }
        }
    } catch (
        JwtException |
        IllegalArgumentException ex
    ) {
        SecurityContextHolder
            .clearContext();
    }
    filterChain.doFilter(
            request,
            response
    );
}
```

## ¿Por qué volver a cargar UserDetails? 

En esta versión educativa: 



<!-- Start of picture text -->
Authorities<br>JWT username UserDetailsService Authentication<br>actuales<br><!-- End of picture text -->

Ventajas: 

los roles actuales pueden reflejar cambios; 

se puede comprobar si el usuario sigue habilitado; 

conecta JWT con los conceptos centrales de Spring Security. 

Costo: 

si `UserDetailsService` consulta PostgreSQL, puede existir una consulta por request. 

En una arquitectura más avanzada se pueden obtener las authorities directamente desde claims confiables del token. 

33 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

# SECURITY CONTEXT 

## SecurityContextHolder 

`SecurityContextHolder` mantiene la identidad autenticada durante el request. 



<!-- Start of picture text -->
Authorization<br>JwtAuthenticationFilter Authentication SecurityContext SecurityContextHolder Controller<br>Service<br><!-- End of picture text -->

La instrucción clave es: 

```
SecurityContextHolder
    .getContext()
    .setAuthentication(
        authentication
    );
```

# SECURITY FILTER CHAIN 

## Configuración 

```
@Bean
SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        JwtAuthenticationFilter jwtFilter,
        AuthenticationProvider authenticationProvider)
throws Exception {
    http
        .csrf(
            csrf ->
                csrf.disable()
        )
        .sessionManagement(
            session ->
                session
```

34 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

```
                    .sessionCreationPolicy(
                        SessionCreationPolicy
                            .STATELESS
                    )
        )
        .authenticationProvider(
            authenticationProvider
        )
        .authorizeHttpRequests(
            auth -> auth
                .requestMatchers(
"/api/auth/**"
                )
                .permitAll()
                .requestMatchers(
                    HttpMethod.GET,
"/api/public/**"
                )
                .permitAll()
                .requestMatchers(
"/api/admin/**"
                )
                .hasRole("ADMIN")
                .anyRequest()
                .authenticated()
        )
        .addFilterBefore(
            jwtFilter,
            UsernamePasswordAuthenticationFilter
                .class
        );
return http.build();
}
```

## Orden conceptual 

35 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



<!-- Start of picture text -->
HTTP Request<br>oS<br>SecurityFilterChain<br>[|__|<br>JwtAuthenticationFilter<br>[| ___<br>SecurityContext<br>_<br>Authorization<br>rules<br>ee<br>Permitido No autenticado Sin permisos<br>Controller 401 403<br>554<br><!-- End of picture text -->

## ¿Qué significa STATELESS? 

```
SessionCreationPolicy.STATELESS
```

indica que una `HttpSession` no se utilizará como mecanismo para recordar la autenticación. 

36 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



<!-- Start of picture text -->
API<br>Client<br>Request 1 + JWT<br>Response<br>Request 2 + JWT<br>Response<br>Request 3 + JWT<br>Response<br>API<br>Client<br><!-- End of picture text -->

Cada request debe presentar nuevamente el token. 

## CSRF 

Para esta API: 

stateless; token Bearer en header; sin autenticación basada en cookie de sesión; 

se deshabilita CSRF: 

```
csrf.disable()
```

Esto no significa que CSRF sea irrelevante en todas las arquitecturas. 

37 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

# ENDPOINTS DE PRUEBA 

## DemoController 

```
@RestController
publicclassDemoController {
@GetMapping("/api/public/events")
public String publicEvents() {
return"Public events";
    }
@GetMapping("/api/profile")
public String profile(
            Authentication authentication) {
return"Hello "
                + authentication.getName();
    }
@GetMapping("/api/admin/report")
public String adminReport() {
return"Admin report";
    }
}
```

# PRUEBAS MANUALES 

## Endpoint público 

```
GET /api/public/events
```

Resultado sin token: 

```
200 OK
```

## Endpoint protegido sin token 

```
GET /api/profile
```

38 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

Resultado: 

```
401 Unauthorized
```

## Login 

```
POST /api/auth/login
Content-Type: application/json
{
  "username": "student",
  "password": "student123"
}
```

Respuesta: 

```
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

## Endpoint autenticado 

```
GET /api/profile
Authorization: Bearer eyJ...
```

Resultado: 

```
200 OK
```

USER intentando acceder como ADMIN 

39 / 62 



<!-- Start of picture text -->
Guia_Didactica_Spring_Security_JJWT_Mermaid.md 2026-10-02<br>API Security<br>Student<br>GET /api/admin/report + JWT USER<br>Autorizar request<br>ROLE_ADMIN requerido<br>403 Forbidden<br>= API Security<br>Student<br>ADMIN accediendo<br>API Security<br>Admin<br>GET /api/admin/report + JWT ADMIN<br>Autorizar request<br>Acceso permitido<br>200 OK<br>=i API Security<br>Admin<br><!-- End of picture text -->

# 401 VS 403 

Diferencia 

40 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



<!-- Start of picture text -->
Request<br>protegido<br>{<br>Authentication<br>válida?<br>MO<br>No Sí<br>i ;<br>401<br>Unauthorized<br>Tiene authority<br>requerida?<br>Me<br>No Sí<br>' '<br>403 Forbidden Operación<br>permitida<br><!-- End of picture text -->

### 401 

No existe una autenticación válida. 

#### Ejemplos: 

token ausente; token expirado; 

41 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

firma inválida; 

token malformado. 

403 

El usuario está autenticado pero carece del permiso necesario. 

# METHOD SECURITY 

## Activar seguridad en métodos 

```
@Configuration
@EnableMethodSecurity
publicclassSecurityConfig {
}
```

Ejemplo: 

```
@PreAuthorize("hasRole('ADMIN')")
publicvoiddeleteEvent(Long id) {
}
```

Otro ejemplo: 

```
@PreAuthorize(
    "hasAnyRole('ADMIN', 'ORGANIZER')"
)
publicvoidcreateEvent(...) {
}
```

## URL Security vs Method Security 

42 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



<!-- Start of picture text -->
ete@<br>ge<br><!-- End of picture text -->



<!-- Start of picture text -->
Syntax error in textSyntax error in text<br>mermaid version 12.1.0mermaid version 12.1.0<br><!-- End of picture text -->



<!-- Start of picture text -->
Parse error on line 6: ...-> E[Service]E --> F[@PreAuthorize]Parse error on line 6: ...-> E[Service]E --> F[@PreAuthorize]<br>----------------------^ Expecting 'AMP', 'COLON', 'PIPE', 'TESTSTR',----------------------^ Expecting 'AMP', 'COLON', 'PIPE', 'TESTSTR',<br>'DOWN', 'DEFAULT', 'NUM', 'COMMA', 'NODE_STRING', 'BRKT', 'MINUS', 'MULT','DOWN', 'DEFAULT', 'NUM', 'COMMA', 'NODE_STRING', 'BRKT', 'MINUS', 'MULT',<br>'UNICODE_TEXT', got 'LINK_ID''UNICODE_TEXT', got 'LINK_ID'<br><!-- End of picture text -->

`requestMatchers(...)` protege rutas. 

`@PreAuthorize(...)` protege operaciones concretas. 

# USUARIOS EN POSTGRESQL 

## Evolución desde memoria 

Hasta ahora: 



<!-- Start of picture text -->
UserDetailsService —> InMemoryUserDetailsManager<br><!-- End of picture text -->

Evolución: 



<!-- Start of picture text -->
UserDetailsService JpaUserDetailsService UserRepository<br>—> >» —> PostgreSQL<br><!-- End of picture text -->

## AppUser 

```
@Entity
@Table(name = "app_users")
publicclassAppUser {
```

```
@Id
@GeneratedValue(
        strategy = GenerationType.IDENTITY
    )
```

43 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

```
private Long id;
@Column(
        nullable = false,
        unique = true
    )
private String username;
@Column(nullable = false)
private String password;
@Column(nullable = false)
privateboolean active;
// getters / constructors
}
```

## UserRepository 

```
publicinterfaceUserRepository
extendsJpaRepository<AppUser, Long> {
    Optional<AppUser>
findByUsername(
            String username
        );
}
```

## JpaUserDetailsService 

```
@Service
publicclassJpaUserDetailsService
implementsUserDetailsService {
privatefinal UserRepository repository;
publicJpaUserDetailsService(
            UserRepository repository) {
this.repository = repository;
    }
@Override
public UserDetails loadUserByUsername(
            String username) {
AppUseruser =
```

44 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

```
                repository
                    .findByUsername(username)
                    .orElseThrow(
                        () ->
newUsernameNotFoundException(
"User not found: "
                                + username
                            )
                    );
return User
            .withUsername(
                user.getUsername()
            )
            .password(
                user.getPassword()
            )
            .roles("USER")
            .disabled(
                !user.isActive()
            )
            .build();
    }
}
```

## Flujo con PostgreSQL 



<!-- Start of picture text -->
AuthenticationManager DaoAuthenticationProvider JpaUserDetailsService UserRepository PostgreSQL PasswordEncoder<br>authenticate(credentials)<br>loadUserByUsername()<br>findByUsername()<br>SELECT<br>user row<br>AppUser<br>UserDetails<br>matches(raw, hash)<br>true/false<br>Authentication / error<br>AuthenticationManager DaoAuthenticationProvider JpaUserDetailsService UserRepository PostgreSQL PasswordEncoder<br>Guardar contraseñas<br>= 45 / 62<br><!-- End of picture text -->

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

Incorrecto: 

```
user.setPassword(
    request.password()
);
```

Correcto: 

```
user.setPassword(
    passwordEncoder.encode(
        request.password()
    )
);
```

Nunca almacenar el password original. 

# CICLO DE VIDA DEL TOKEN 

## Estados conceptuales 

46 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



<!-- Start of picture text -->
Generated<br>firmado correctamente<br>Valid<br>llega exp firma o claims inválidos<br>Expired Invalid<br><!-- End of picture text -->

## Duración del access token 

En esta guía: 

```
15 minutos
```

Un access token de duración limitada reduce la ventana de exposición si es robado. 

## Refresh Token 

No se implementa en esta primera guía. 

Conceptualmente: 

47 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



<!-- Start of picture text -->
Authentication Server<br>Client<br>Login<br>Access Token + Refresh Token<br>Access Token expira<br>Refresh Token<br>Nuevo Access Token<br>Authentication Server<br>Client<br><!-- End of picture text -->

# HS256 VS RS256 

## HS256 



<!-- Start of picture text -->
Sign<br>Secret Key<br>Verify<br>=<br>Ventajas:<br><!-- End of picture text -->

- simple; 

- adecuado para un laboratorio; 

- fácil de configurar. 

Consideración: 

48 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

quien conoce la clave puede tanto verificar como firmar. 

## 68. RS256 



<!-- Start of picture text -->
Private Key Sign<br>e e .<br>Public Key Verify<br>a<br><!-- End of picture text -->

Ventaja: 

otros servicios pueden verificar tokens con la clave pública sin tener acceso a la clave privada. 

Esto es útil en arquitecturas distribuidas. 

# JWT VS OAUTH2 

## Diferencia 



<!-- Start of picture text -->
Formato de<br>JWT<br>token<br>CH<br>Framework de<br>OAuth2<br>autorización<br>C T H<br><!-- End of picture text -->

Usar JJWT no convierte automáticamente la aplicación en un Authorization Server OAuth2. 

## Arquitectura educativa 

49 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



<!-- Start of picture text -->
Autentica<br>usuario<br>|<br>PulsePass API Genera JWT<br>—— Valida JWT<br>[<br><!-- End of picture text -->

## Arquitectura empresarial común 



<!-- Start of picture text -->
Identity Provider Client Resource Server<br>User<br>Login<br>Access Token<br>Bearer Access Token<br>Validar token<br>Recurso protegido<br>Identity Provider Client Resource Server<br>User<br><!-- End of picture text -->

#### Ejemplos de Identity Provider: 

```
Keycloak
Spring Authorization Server
Auth0
Microsoft Entra ID
Okta
```

50 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

# BUENAS PRÁCTICAS 

## Gestión de secretos 

Nunca hardcodear: 

```
JWT_SECRET
```

dentro del repositorio. 

Preferir: 

variables de entorno; 

secret managers; 

vaults; 

mecanismos administrados por la plataforma. 

## Claims 

Evitar incluir: 

```
passwords
credenciales internas
información sensible innecesaria
```

## Logging 

Evitar: 

```
logger.info(
"JWT = {}",
    token
);
```

Un access token es una credencial. 

## Token inválido 

Si JJWT detecta: 

expiración; 

51 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

firma inválida; 

token malformado; 

issuer incorrecto; 

el token debe tratarse como inválido. 

# ERRORES COMUNES 

## Confundir JWT con cifrado 

Incorrecto: 

```
JWT = información secreta
```

Correcto: 

```
JWT firmado = contenido verificable,
pero normalmente visible
```

## Utilizar una clave HMAC débil 

Evitar: 

```
123456
secret
myjwt
password
```

## Confiar en claims antes de verificar 

52 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



<!-- Start of picture text -->
JWT<br>Verificar firma<br>Válida Inválida<br>= | __]<br>Leer claims  Rechazar token<br>confiables<br>eS<br><!-- End of picture text -->

## Crear Authentication pero no guardarlo 

No basta con construir: 

```
newUsernamePasswordAuthenticationToken(...)
```

Debe llegar al contexto: 

```
SecurityContextHolder
    .getContext()
    .setAuthentication(authentication);
```

## Olvidar registrar el filtro 

`JwtAuthenticationFilter` debe formar parte de: 

```
SecurityFilterChain
```

## Comparar password manualmente 

No: 

53 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

```
rawPassword.equals(
    storedHash
)
```

Utilizar `PasswordEncoder` . 

# PRUEBAS 

## Casos mínimos 

|**Caso**|**Resultado esperado**|
|---|---|
|Login correcto|JWT|
|Password incorrecta|401|
|Endpoint público sin JWT|200|
|Endpoint protegido sin JWT|401|
|JWT válido|200|
|JWT expirado|401|
|JWT modificado|401|
|USER accede a ADMIN|403|
|ADMIN accede a ADMIN|200|



## Unit test de JwtService 

```
classJwtServiceTest {
private JwtService jwtService;
private UserDetails userDetails;
@Test
voidshouldGenerateAndReadToken() {
Stringtoken =
                jwtService.generateToken(
                    userDetails
                );
        assertThat(token)
            .isNotBlank();
```

54 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

```
        assertThat(
            jwtService.extractUsername(
                token
            )
        )
        .isEqualTo("student");
        assertThat(
            jwtService.isTokenValid(
                token,
                userDetails
            )
        )
        .isTrue();
    }
}
```

Casos adicionales: 

```
token expirado
token firmado con otra clave
subject diferente
issuer incorrecto
```

# EJERCICIOS 

Ejercicio 1 — ROLE_ORGANIZER 

Crear: 

```
ROLE_ORGANIZER
```

Permitir: 

```
POST /api/events
```

para: 

```
ADMIN
ORGANIZER
```

55 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

## Ejercicio 2 — Claim email 

Agregar al JWT: 

```
email
```

Crear: 

```
public String extractEmail(
        String token)
```

en `JwtService` . 

## Ejercicio 3 — Expiración 

Temporalmente: 

```
security:
  jwt:
    expiration: 10s
```

Pasos: 

1. realizar login; 

2. guardar token; 

3. esperar expiración; 

4. llamar `/api/profile` ; 

5. comprobar `401` . 

## Ejercicio 4 — Manipular JWT 

Pasos: 

1. obtener JWT válido; 

2. modificar manualmente el Payload; 

3. intentar usarlo. 

Resultado esperado: 

```
401 Unauthorized
```

Explicar por qué la firma dejó de coincidir. 

56 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

## Ejercicio 5 — PostgreSQL 

Reemplazar: 

```
InMemoryUserDetailsManager
```

por: 

```
JpaUserDetailsService
UserRepository
PostgreSQL
```

# CHECKPOINTS DE APRENDIZAJE 

## Checkpoint 1 

Antes de continuar, explicar: 

- Authentication; 

- Authorization; 

- `AuthenticationManager` ; 

- `UserDetailsService` ; 

- `PasswordEncoder` . 

## Checkpoint 2 

Debe poder explicar: 

- Header; 

- Payload; 

- Signature; 

- claims; 

- `sub` ; 

- `exp` ; 

por qué JWT no implica cifrado. 

## Checkpoint 3 

Debe poder seguir este flujo: 

57 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



<!-- Start of picture text -->
Bearer Token<br>a<br>JwtAuthenticationFilter<br>{|__|<br>JwtService<br>_<br>JJWT<br>a<br>UserDetailsService<br>_<br>Authentication<br>a<br>SecurityContextHolder<br>a<br>Authorization<br>|<br><!-- End of picture text -->

# PREGUNTAS DE COMPRENSIÓN 

## Preguntas 

1. ¿Qué diferencia existe entre Authentication y Authorization? 

2. ¿Qué responsabilidad tiene Spring Security? 

3. ¿Qué responsabilidad tiene JJWT? 

4. ¿Qué es un JWT? 

5. ¿Cuáles son sus tres partes? 

6. ¿Qué es un claim? 

7. ¿JWT cifra el Payload? 

58 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

8. ¿Qué representa `sub` ? 

9. ¿Qué representa `exp` ? 

10. ¿Qué hace `AuthenticationManager` ? 

11. ¿Qué hace `AuthenticationProvider` ? 

12. ¿Qué hace `UserDetailsService` ? 

13. ¿Qué hace `PasswordEncoder` ? 

14. ¿Qué hace `JwtService` ? 

15. ¿Qué hace `JwtAuthenticationFilter` ? 

16. ¿Qué es `SecurityContextHolder` ? 

17. ¿Por qué utilizar `OncePerRequestFilter` ? 

18. ¿Qué significa `STATELESS` ? 

19. ¿Qué diferencia existe entre 401 y 403? 

20. ¿Por qué el JWT se envía con `Bearer` ? 

21. ¿Por qué la clave HMAC debe ser criptográficamente adecuada? 

22. ¿Qué ocurre si se modifica el Payload? 

23. ¿Qué diferencia existe entre HS256 y RS256? 

24. ¿Qué diferencia existe entre JWT y OAuth2? 

25. ¿Por qué una organización podría utilizar Keycloak en lugar de emitir JWT directamente desde su API? 

# ESTRUCTURA FINAL DEL PROYECTO 

## Organización sugerida 

```
src/main/java/com/pulsepass
│
├── config
│   └── SecurityConfig.java
│
├── security
│   ├── JwtProperties.java
│   ├── JwtService.java
│   └── JwtAuthenticationFilter.java
│
├── auth
│   ├── AuthenticationController.java
│   ├── AuthenticationService.java
│   ├── LoginRequest.java
│   └── TokenResponse.java
│
├── controller
│   └── DemoController.java
│
└── ...
```

Con persistencia: 

59 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 

```
security/
└── JpaUserDetailsService.java
domain/
└── AppUser.java
repository/
└── UserRepository.java
```

# MAPA CONCEPTUAL FINAL 

## Mapa completo 

60 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



<!-- Start of picture text -->
Spring Security<br>Authentication<br>i<br>AuthenticationManager JwtService<br>=<br>DaoAuthenticationProvider JJWT<br>PasswordEncoder JWT<br>=<br>JwtAuthenticationFilter<br>a=<br>UserDetailsService _ Ge SecurityContextHolder<br>sa oS<br>Authorization<br>_—<br>Roles /<br>Authorities<br>_<br>Controller /<br>Service<br>||<br><!-- End of picture text -->

Separación de responsabilidades 

61 / 62 

Guia_Didactica_Spring_Security_JJWT_Mermaid.md 

2026-10-02 



<!-- Start of picture text -->
Construye JWT<br>—<br>Firma JWT<br>—<br>JJWT<br>Parsea JWT<br>—<br>Verifica firma<br>a<br>Authentication<br>—<br>SecurityContext<br>a<br>Spring Security<br>Authorization<br>—<br>Protección de<br>métodos<br>a<br><!-- End of picture text -->

La idea central es: 

#### **JJWT administra la construcción y validación criptográfica del token; Spring Security administra la identidad autenticada, el contexto de seguridad y la autorización de la aplicación.** 

Entender esta separación es más importante que memorizar el código. 

62 / 62 

