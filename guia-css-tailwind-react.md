2026-11-09 

guia-css-tailwind-react.md 

# Guía paso a paso: CSS, Tailwind y React con TypeScript 

Guía práctica para aprender lo básico de CSS, pasar a Tailwind CSS y usarlos en un proyecto React con TypeScript. 

## Parte 1: CSS básico 

Paso 1. Cómo se conecta CSS al HTML 

```
<link rel="stylesheet" href="styles.css" />
```

### Paso 2. Anatomía de una regla 

```
selector {
  propiedad: valor;
}
h1 {
color: tomato;
font-size: 32px;
}
```

### Paso 3. Selectores 

```
p { }              /* etiqueta */
.tarjeta { }       /* clase (la más usada) */
#header { }        /* id */
.tarjetap { }     /* p dentro de .tarjeta */
button:hover { }   /* pseudo-clase: al pasar el mouse */
a:focus { }        /* al enfocar */
```

Regla de oro: usa **clases** . Evita `id` y `!important` . 

### Paso 4. Texto y colores 

```
.titulo {
color: #1e293b;
background-color: #f1f5f9;
font-family: system-ui, sans-serif;
font-size: 1.5rem;     /* rem = relativo al tamaño base (16px) */
font-weight: 700;
text-align: center;
```

1 / 18 

2026-11-09 

guia-css-tailwind-react.md 

```
line-height: 1.5;
}
```

Unidades: `px` (fija), `rem` (escalable, recomendada), `%` , `vw/vh` (viewport). 

Paso 5. El modelo de caja (box model) 

Todo elemento es una caja: **content → padding → border → margin** . 

```
* { box-sizing: border-box; } /* width incluye padding y border */
.caja {
width: 300px;
padding: 16px;              /* espacio interno */
border: 1px solid #cbd5e1;
margin: 24px auto;          /* espacio externo; auto centra horizontalmente */
border-radius: 8px;
}
```

### Paso 6. Display y Flexbox (layout en una dimensión) 

```
.fila {
display: flex;
flex-direction: row;          /* o column */
justify-content: space-between; /* eje principal */
align-items: center;            /* eje transversal */
gap: 16px;
}
```

|**Propiedad**|**Controla**|
|---|---|
|`justify-content`|alineación en el eje principal|
|`align-items`|alineación en el eje transversal|
|`flex-wrap: wrap`|permite saltar de línea|
|`flex: 1`|el hijo ocupa el espacio sobrante|



### Paso 7. Grid (layout en dos dimensiones) 

```
.galeria {
display: grid;
grid-template-columns: repeat(3, 1fr);
gap: 16px;
}
```

2 / 18 

2026-11-09 

guia-css-tailwind-react.md 

### Paso 8. Posicionamiento 

```
.padre { position: relative; }
.badge { position: absolute; top: 8px; right: 8px; }
.navbar { position: sticky; top: 0; }
```

### Paso 9. Responsive con media queries 

Enfoque **mobile first** : estilos base para móvil, y se amplía para pantallas grandes. 

`.galeria { display: grid; grid-template-columns: 1fr; } @media (min-width: 768px) { .galeria { grid-template-columns: repeat(3, 1fr); } }` Paso 10. Variables CSS `:root { --color-primario: #4f46e5; --radio: 8px; } .boton { background: var(--color-primario); border-radius: var(--radio); }` 

### Paso 10. Variables CSS 

### Ejercicio 1 

Crea una tarjeta con imagen, título y botón. Debe verse en 1 columna en móvil y en 3 columnas en escritorio, con un `:hover` en el botón. 

## Parte 2: Tailwind CSS 

### Paso 11. Qué es Tailwind y cómo pensar con él 

En vez de escribir CSS en un archivo aparte, aplicas **clases utilitarias** directamente en el HTML/JSX. Cada clase hace una sola cosa ( `p-4` = padding, `text-center` = centrar texto, `bg-red-500` = fondo rojo). 

CSS tradicional (2 archivos: HTML + CSS): 

```
<button class="boton">Enviar</button>
```

3 / 18 

2026-11-09 

guia-css-tailwind-react.md 

```
.boton { background: #4f46e5; color: white; padding: 8px16px; border-radius: 8px;
}
```

Tailwind (todo en un lugar): 

```
<button class="bg-indigo-600 text-white px-4 yp-2 rounded-lg">Enviar</button>
```

#### **Ventajas** 

- No inventas nombres de clases ( `.boton-primario-grande-2` ...). 

- No saltas entre archivos mientras diseñas. 

- El CSS final solo incluye las clases que usas (archivo pequeño). 

Diseño consistente: espaciados, colores y tamaños salen de una escala predefinida. 

**Desventaja (y su solución):** el HTML se ve "largo". En React se resuelve creando **componentes** (Parte 3), así escribes las clases una sola vez. 

**Cómo leer una clase.** Casi todas siguen el patrón `[variante:]propiedad-valor` : 

```
md:hover:bg-indigo-700
│   │     │  │
│   │     │  └─ valor (indigo, tono 700)
│   │     └──── propiedad (bg = background)
│   └────────── estado (al pasar el mouse)
└────────────── breakpoint (desde pantallas medianas)
```

Truco para aprender: si sabes la propiedad CSS, busca el nombre en la documentación ( `Ctrl+K` en tailwindcss.com). Con el tiempo las memorizas. 

### Paso 12. Espaciado: padding, margin y gap 

Tailwind usa una escala donde **1 unidad = 0.25rem = 4px** . 

#### **Clase Valor real** 

|`p-1`<br>4px|
|---|
|`p-2`<br>8px|
|`p-4`<br>16px|
|`p-6`<br>24px|
|`p-8`<br>32px|
|`p-12`<br>48px|



Prefijos de dirección (sirven para `p` , `m` ): 

4 / 18 

2026-11-09 

guia-css-tailwind-react.md 

|**Prefijo**|**Significa**|**Ejemplo**|
|---|---|---|
|`p-`/`m-`|todos los lados|`p-4`|
|`px-`/`mx-`|izquierda y derecha|`px-4`|
|`py-`/`my-`|arriba y abajo|`py-2`|
|`pt- pr- pb- pl-`|top, right, bottom, left|`pt-6`|
|`gap-`|espacio entre hijos de flex/grid|`gap-4`|
|`space-y-`|espacio vertical entre hijos|`space-y-2`|



#### **Ejemplo: sin y con espaciado** 

```
<!-- Todo pegado -->
<div class="bg-white">
<h2>Título</h2>
<p>Texto</p>
</div>
<!-- Con aire -->
<div class="bg-white p-6 rounded-lg space-y-2">
<h2>Título</h2>
<p>Texto</p>
</div>
```

Centrar un bloque horizontalmente: `mx-auto` (necesita un ancho, por ejemplo `max-w-md mx-auto` ). 

### Paso 13. Tipografía 

```
<h1 class="text-4xl font-bold tracking-tight text-slate-900">Título grande</h1>
<p class="text-base leading-relaxed text-slate-600">Párrafo cómodo de leer.</p>
<span class="text-sm font-medium uppercase text-indigo-600">Etiqueta</span>
<p class="truncate">Texto muy largo que se corta con puntos suspensivos...</p>
```

|**Qué**|**Clases**|
|---|---|
|Tamaño|`text-xs` `text-sm` `text-base` `text-lg` `text-xl` `text-2xl`...`text-6xl`|
|Peso|`font-light` `font-normal` `font-medium` `font-semibold` `font-bold`|
|Alineación|`text-left` `text-center` `text-right`|
|Interlineado|`leading-tight` `leading-normal` `leading-relaxed`|
|Mayúsculas|`uppercase` `lowercase` `capitalize`|
|Decoración|`underline` `line-through` `no-underline`|
|Cortar texto|`truncate`,`line-clamp-2`(máximo 2 líneas)|



5 / 18 

2026-11-09 

guia-css-tailwind-react.md 

### Paso 14. Colores 

Formato: `{utilidad}-{color}-{tono}` . Los tonos van de `50` (muy claro) a `950` (muy oscuro). 

```
<div class="bg-indigo-100 text-indigo-800 border border-indigo-300">Aviso</div>
```

|**Utilidad**|**Aplica color a**|
|---|---|
|`text-*`|texto|
|`bg-*`|fondo|
|`border-*`|borde|
|`ring-*`|anillo de enfoque|
|`shadow-*`|sombra (con color)|



Colores base: `slate` , `gray` , `red` , `orange` , `amber` , `green` , `emerald` , `teal` , `sky` , `blue` , `indigo` , `violet` , `purple` , `pink` , `rose` . También `white` , `black` , `transparent` . 

Opacidad con `/` : `bg-black/50` (negro al 50%), `text-white/80` . 

#### **Ejemplo: alertas de colores** 

`<div class="rounded-lg bg-green-100 px-4 py-3 text-green-800">` ✅ `Guardado correctamente</div> <div class="rounded-lg bg-yellow-100 px-4 py-3 text-yellow-800">` ⚠ `Revisa los datos</div> <div class="rounded-lg bg-red-100 px-4 py-3 text-red-800">` ❌ `Ocurrió un error</div>` 

Patrón útil: fondo en tono `100` + texto en tono `800` del mismo color siempre se lee bien. 

### Paso 15. Bordes, sombras y fondos 

```
<div class="rounded-xl border border-slate-200 bg-white shadow-md"></div>
<img class="h-16 w-16 rounded-full object-cover" src="..." alt="Avatar" />
<div class="bg-gradient-to-r from-indigo-500 to-purple-600"></div>
```

|**Qué**|**Clases**|
|---|---|
|Borde|`border` `border-2` `border-t` `border-slate-300`|
|Esquinas|`rounded` `rounded-lg` `rounded-xl` `rounded-full`|
|Sombra|`shadow-sm` `shadow` `shadow-md` `shadow-lg` `shadow-xl`|



6 / 18 

2026-11-09 

guia-css-tailwind-react.md 

|**Qué**|**Clases**|
|---|---|
|Anillo (foco)|`ring-2` `ring-indigo-500`|
|Imagen|`object-cover`(rellena sin deformar),`object-contain`|



### Paso 16. Tamaños: ancho y alto 

|**Clase**|**Significado**|
|---|---|
|`w-64`|ancho 16rem (256px), misma escala 1 = 4px|
|`w-full`|100% del padre|
|`w-1/2`|50%|
|`w-screen`|100% del ancho de la pantalla|
|`h-screen`/`min-h-screen`|alto de la pantalla (usa`min-h-screen`para páginas completas)|
|`max-w-md` `max-w-xl` `max-w-4xl`|ancho máximo (contenedores)|
|`size-10`|ancho y alto 40px a la vez|



#### **Ejemplo: contenedor centrado típico** 

```
<main class="mx-auto max-w-4xl px-4 py-10">
  Contenido que no se estira demasiado en pantallas grandes
</main>
```

### Paso 17. Flexbox en Tailwind 

Equivalencias directas con lo aprendido en CSS: 

|**CSS**|**Tailwind**|
|---|---|
|`display: flex`|`flex`|
|`flex-direction: column`|`flex-col`|
|`justify-content: space-between`|`justify-between`|
|`justify-content: center`|`justify-center`|
|`align-items: center`|`items-center`|
|`flex-wrap: wrap`|`flex-wrap`|
|`flex: 1`|`flex-1`|



**Ejemplo 1: centrar algo en toda la pantalla** (el clásico) 

7 / 18 

2026-11-09 

guia-css-tailwind-react.md 

```
<div class="flex min-h-screen items-center justify-center bg-slate-100">
<div class="rounded-lg bg-white p-8 shadow">Centrado perfecto</div>
</div>
```

#### **Ejemplo 2: barra de navegación** 

```
<nav class="flex items-center justify-between bg-slate-900 px-6 py-4 text-white">
<span class="text-lg font-bold">MiLogo</span>
<ul class="flex gap-6">
<li><a href="#" class="hover:text-indigo-400">Inicio</a></li>
<li><a href="#" class="hover:text-indigo-400">Precios</a></li>
<li><a href="#" class="hover:text-indigo-400">Contacto</a></li>
</ul>
</nav>
```

#### **Ejemplo 3: fila con un elemento que empuja al resto** 

```
<div class="flex items-center gap-3">
<img class="size-10 rounded-full" src="..." alt="" />
<div class="flex-1">
<p class="font-medium">Ana Pérez</p>
<p class="text-sm text-slate-500">Desarrolladora</p>
</div>
<button class="rounded bg-indigo-600 px-3 py-1 text-sm text-
white">Seguir</button>
</div>
```

Paso 18. Grid en Tailwind 

```
<!-- 3 columnas iguales -->
<div class="grid grid-cols-3 gap-4">
<div class="rounded bg-indigo-100 p-4">1</div>
<div class="rounded bg-indigo-100 p-4">2</div>
<div class="rounded bg-indigo-100 p-4">3</div>
</div>
<!-- Un elemento que ocupa 2 columnas -->
<div class="grid grid-cols-3 gap-4">
<div class="col-span-2 rounded bg-indigo-200 p-4">Ancho (2)</div>
<div class="rounded bg-indigo-100 p-4">Normal (1)</div>
</div>
```

**¿Flex o Grid?** Flex para alinear elementos en **una fila o columna** (navbar, botones). Grid para **cuadrículas** (galerías, tarjetas, dashboards). 

8 / 18 

2026-11-09 

guia-css-tailwind-react.md 

### Paso 19. Estados con prefijos (hover, focus, etc.) 

Anteponer un prefijo hace que la clase solo se aplique en ese estado. 

```
<button class="bg-indigo-600 text-white
               hover:bg-indigo-700
               focus:outline-none focus:ring-2 focus:ring-indigo-400
               active:scale-95
               disabled:cursor-not-allowed disabled:opacity-50">
  Guardar
</button>
```

|**Prefijo**|**Cuándo se aplica**|
|---|---|
|`hover:`|mouse encima|
|`focus:`|elemento enfocado (teclado/click)|
|`active:`|mientras se presiona|
|`disabled:`|elemento deshabilitado|
|`first:`/`last:`|primer / último hijo|
|`odd:`/`even:`|filas impares / pares|



#### **Ejemplo: filas de tabla con rayas** 

```
<tr class="odd:bg-white even:bg-slate-50 hover:bg-indigo-50">...</tr>
```

Paso 20. Responsive con prefijos 

Mobile first: **sin prefijo = móvil** ; `md:` , `lg:` se aplican **desde** ese ancho hacia arriba. 

|**Prefijo**|**Desde**|
|---|---|
|(ninguno)|0px|
|`sm:`|640px|
|`md:`|768px|
|`lg:`|1024px|
|`xl:`|1280px|



#### **Ejemplo 1: columnas que crecen** 

```
<div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-4">
<div class="rounded bg-white p-4 shadow">A</div>
```

9 / 18 

2026-11-09 

guia-css-tailwind-react.md 

```
<div class="rounded bg-white p-4 shadow">B</div>
<div class="rounded bg-white p-4 shadow">C</div>
<div class="rounded bg-white p-4 shadow">D</div>
</div>
```

Móvil: 1 columna → tablet: 2 → escritorio: 4. 

#### **Ejemplo 2: apilado en móvil, en fila en escritorio** 

```
<div class="flex flex-col gap-4 md:flex-row">
<img class="w-full md:w-1/3" src="..." alt="" />
<div class="md:w-2/3">
<h2 class="text-xl font-bold">Título</h2>
<p class="text-slate-600">Descripción</p>
</div>
</div>
```

#### **Ejemplo 3: mostrar/ocultar según pantalla** 

`<button class="md:hidden">` ☰ `Menú</button> <!-- solo en móvil --> <ul class="hidden md:flex gap-6">...</ul> <!-- solo en escritorio -->` 

Error típico: pensar que `md:` significa "solo en tablet". Significa "tablet **y mayores** ". Para estilos solo móviles, pon la clase sin prefijo. 

### Paso 21. Modo oscuro 

```
<div class="bg-white text-slate-900 dark:bg-slate-900 dark:text-white">
<h2 class="font-bold">Hola</h2>
<p class="text-slate-600 dark:text-slate-300">Se adapta al tema del sistema.</p>
</div>
```

Por defecto `dark:` sigue la preferencia del sistema operativo. Si quieres un botón para alternar el tema manualmente (clase `dark` en `<html>` ), agrega en tu CSS (Tailwind v4): 

```
@import"tailwindcss";
@custom-variant dark (&:where(.dark, .dark *));
```

Paso 22. Transiciones y animaciones 

10 / 18 

2026-11-09 

guia-css-tailwind-react.md 

```
<button class="rounded bg-indigo-600 px-4 py-2 text-white
               transition duration-200 ease-in-out
               hover:-translate-y-0.5 hover:bg-indigo-700 hover:shadow-lg">
  Sube al pasar el mouse
</button>
<div class="size-8 animate-spin rounded-full border-4 border-indigo-200 border-t-
indigo-600"></div>
```

`transition` activa la animación de cambios; `duration-200` fija la duración. Animaciones listas: `animate-spin` (cargando), `animate-pulse` (esqueleto), `animate-bounce` . 

### Paso 23. `group` y `peer` : reaccionar a otro elemento 

`group` en el padre permite que los hijos cambien cuando **el padre** recibe hover. 

```
<a href="#" class="group block rounded-lg border p-4 hover:border-indigo-500">
<h3 class="font-semibold group-hover:text-indigo-600">Título</h3>
<span class="text-slate-400 transition group-hover:translate-x-1">→</span>
</a>
```

`peer` hace lo mismo entre hermanos (útil en formularios): 

```
<input type="email" required placeholder="correo"
       class="peer rounded border px-3 py-2 invalid:border-red-500" />
<p class="hidden text-sm text-red-600 peer-invalid:block">Correo no válido</p>
```

### Paso 24. Valores arbitrarios y cuándo salirse de la escala 

Cuando la escala no tiene lo que necesitas, usa corchetes: 

```
<div class="w-[350px] h-[2.5rem] bg-[#1da1f2] text-[13px] top-[calc(100%-8px)]">
</div>
```

Úsalos con moderación: si repites un valor, mejor agrégalo a tu tema (Paso 35). 

### Paso 25. Ejemplos completos para practicar 

#### **A. Botón primario y secundario** 

```
<button class="rounded-lg bg-indigo-600 px-4 py-2 font-medium text-white shadow-sm
               transition hover:bg-indigo-700 focus:ring-2 focus:ring-indigo-400
focus:outline-none">
```

11 / 18 

2026-11-09 

guia-css-tailwind-react.md 

```
  Primario
</button>
<button class="rounded-lg border border-slate-300 bg-white px-4 py-2 font-medium
text-slate-700
               transition hover:bg-slate-50">
  Secundario
</button>
```

#### **B. Tarjeta de producto** 

```
<div class="max-w-xs overflow-hidden rounded-xl bg-white shadow-md transition
hover:shadow-xl">
<img class="h-40 w-full object-cover" src="https://picsum.photos/400/300" alt=""
/>
<div class="space-y-2 p-4">
<span class="rounded-full bg-green-100 px-2 py-0.5 text-xs font-medium text-
green-700">Nuevo</span>
<h3 class="text-lg font-semibold text-slate-900">Audífonos</h3>
<p class="text-sm text-slate-600">Cancelación de ruido y 30 horas de batería.
</p>
<div class="flex items-center justify-between pt-2">
<span class="text-xl font-bold text-slate-900">$89</span>
<button class="rounded-lg bg-indigo-600 px-3 py-1.5 text-sm text-white
hover:bg-indigo-700">Comprar</button>
</div>
</div>
</div>
```

#### **C. Formulario de login** 

```
<form class="mx-auto mt-10 w-full max-w-sm space-y-4 rounded-xl bg-white p-6
shadow">
<h2 class="text-center text-2xl font-bold text-slate-900">Iniciar sesión</h2>
<label class="block">
<span class="text-sm font-medium text-slate-700">Correo</span>
<input type="email"
           class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2
                  focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200
focus:outline-none" />
</label>
<label class="block">
<span class="text-sm font-medium text-slate-700">Contraseña</span>
<input type="password"
           class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2
                  focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200
focus:outline-none" />
</label>
```

12 / 18 

2026-11-09 

guia-css-tailwind-react.md 

```
<button class="w-full rounded-lg bg-indigo-600 py-2 font-medium text-white
hover:bg-indigo-700">
    Entrar
</button>
</form>
```

#### **D. Hero (portada) responsive** 

```
<section class="bg-gradient-to-br from-indigo-600 to-purple-700 px-6 py-16 text-
center text-white md:py-24">
```

```
<h1 class="text-3xl font-extrabold tracking-tight md:text-5xl">Aprende Tailwind
hoy</h1>
```

```
<p class="mx-auto mt-4 max-w-xl text-indigo-100 md:text-lg">
    Construye interfaces modernas sin salir de tu HTML.
</p>
<div class="mt-8 flex flex-col items-center justify-center gap-3 sm:flex-row">
<a href="#" class="rounded-lg bg-white px-6 py-3 font-semibold text-indigo-700
hover:bg-indigo-50">Empezar</a>
<a href="#" class="rounded-lg border border-white/60 px-6 py-3 font-semibold
hover:bg-white/10">Ver demo</a>
</div>
</section>
```

### Paso 26. Método: de un diseño a clases 

Cuando tengas un diseño o una idea, sigue siempre este orden: 

1. **Estructura primero** : escribe el HTML sin clases (qué elementos hay). 

2. **Layout** : `flex` / `grid` , `gap` , `items-*` , `justify-*` . 

3. **Espaciado y tamaños** : `p-*` , `m-*` , `w-*` , `max-w-*` . 

4. **Tipografía y colores** : `text-*` , `font-*` , `bg-*` . 

5. **Detalles** : `rounded` , `border` , `shadow` . 

6. **Estados** : `hover:` , `focus:` . 

7. **Responsive** : ajusta con `md:` / `lg:` lo que cambie en pantallas grandes. 

### Paso 27. Errores comunes de principiantes 

|**Error**|**Causa / solución**|
|---|---|
|La clase no hace nada|Revisa que esté bien escrita (`bg-indigo-600`, no`bg-indigo600`)|
|`mx-auto`no centra|Falta un ancho: agrega`w-*`o`max-w-*`|
|`md:`"no funciona" en móvil|Es lo esperado: aplica desde 768px hacia arriba|
|`justify-center`no hace<br>nada|El padre no es`flex`/`grid`|
|`h-screen`corta el contenido|Usa`min-h-screen`|



13 / 18 

2026-11-09 

guia-css-tailwind-react.md 

|**Error**|**Causa / solución**|
|---|---|
|Clases dinámicas que no<br>aparecen|No armes nombres con variables (``bg-${c}-500``)|
|Dos clases en conflicto (`p-2`|Gana la que está**más abajo en el CSS generado**, no la última escrita;|
|`p-4`)|deja solo una|



### Ejercicio 2 

Rehaz la tarjeta del Ejercicio 1 usando solo clases de Tailwind. 

### Ejercicio 3 (con ayuda de los ejemplos del Paso 25) 

Construye una página con: navbar (Ejemplo 2 del Paso 17), hero (Paso 25-D) y una cuadrícula de 3 tarjetas (Paso 25-B) que sea 1 columna en móvil, 2 en tablet y 3 en escritorio. 

## Parte 3: Tailwind en React + TypeScript 

Paso 28. Crear el proyecto (Vite + React + TypeScript + Tailwind v4) 

```
npm create vite@latest mi-app -- --template react-ts
cd mi-app
npm install
npm install tailwindcss @tailwindcss/vite
```

### Paso 29. Configurar el plugin de Vite 

`vite.config.ts` : 

```
import { defineConfig } from'vite'
import react from'@vitejs/plugin-react'
import tailwindcss from'@tailwindcss/vite'
exportdefaultdefineConfig({
  plugins: [react(), tailwindcss()],
})
```

### Paso 30. Importar Tailwind en tu CSS 

`src/index.css` (reemplaza todo su contenido): 

```
@import"tailwindcss";
```

Asegúrate de que `src/main.tsx` importe ese archivo: 

14 / 18 

2026-11-09 

guia-css-tailwind-react.md 

```
import'./index.css'
```

En TypeScript los archivos de componentes usan extensión `.tsx` ( `main.tsx` , `App.tsx` ). 

### Paso 31. Probarlo 

`src/App.tsx` : 

```
exportdefaultfunctionApp() {
return (
<h1 className="text-3xl font-bold text-indigo-600 text-center mt-10">
      ¡Tailwind funciona!
</h1>
  )
}
npm run dev
```

En React se usa `className` en lugar de `class` . 

### Paso 32. Tu primer componente 

```
interfaceTarjetaProps {
  titulo: string
  descripcion: string
  imagen: string
}
functionTarjeta({ titulo, descripcion, imagen }: TarjetaProps) {
return (
<article className="max-w-sm overflow-hidden rounded-xl bg-white shadow-md
hover:shadow-lg transition-shadow">
<img src={imagen} alt={titulo} className="h-48 w-full object-cover" />
<div className="p-4">
<h2 className="text-xl font-semibold text-slate-900">{titulo}</h2>
<p className="mt-2 text-slate-600">{descripcion}</p>
<button className="mt-4 rounded-lg bg-indigo-600 px-4 py-2 text-white
hover:bg-indigo-700">
          Ver más
</button>
</div>
</article>
  )
}
exportdefaultfunctionApp() {
```

15 / 18 

2026-11-09 

guia-css-tailwind-react.md 

```
return (
<main className="min-h-screen bg-slate-100 p-6">
<div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
<Tarjeta titulo="Uno" descripcion="Primera tarjeta"
imagen="https://picsum.photos/400/200" />
<Tarjeta titulo="Dos" descripcion="Segunda tarjeta"
imagen="https://picsum.photos/400/201" />
<Tarjeta titulo="Tres" descripcion="Tercera tarjeta"
imagen="https://picsum.photos/400/202" />
</div>
</main>
  )
}
```

### Paso 33. Clases condicionales (según props o estado) 

Forma simple con template literals: 

```
interfaceBotonProps {
  activo: boolean
  children: React.ReactNode
}
functionBoton({ activo, children }: BotonProps) {
return (
<button className={`rounded px-4 py-2 ${activo ? 'bg-indigo-600 text-white' :
'bg-slate-200 text-slate-700'}`}>
      {children}
</button>
  )
}
```

Forma recomendada, con `clsx` : 

```
npm install clsx
import clsx from'clsx'
functionBoton({ activo, children }: BotonProps) {
return (
<button
      className={clsx(
        'rounded px-4 py-2 transition-colors',
        activo ? 'bg-indigo-600 text-white' : 'bg-slate-200 text-slate-700'
      )}
    >
      {children}
```

16 / 18 

2026-11-09 

guia-css-tailwind-react.md 

```
</button>
  )
}
```

**Importante:** Tailwind detecta clases leyendo el texto de tus archivos. **No construyas nombres dinámicamente** ( ``bg-${color}-500`` NO funciona). Escribe siempre el nombre completo de la clase. 

```
// Mal
<div className={`bg-${color}-500`} />
// Bien
const colores = { rojo: 'bg-red-500', azul: 'bg-blue-500' } asconst
typeColor = keyof typeof colores // 'rojo' | 'azul'
functionPunto({ color }: { color: Color }) {
return<div className={colores[color]} />
}
```

### Paso 34. Tipar variantes y extender props nativas 

Un patrón común: un componente con variantes que además acepta todas las props de `<button>` . 

```
import clsx from'clsx'
typeVariante = 'primario' | 'secundario'
interfaceBotonPropsextendsReact.ComponentProps<'button'> {
  variante?: Variante
}
const estilos: Record<Variante, string> = {
  primario: 'bg-indigo-600 text-white hover:bg-indigo-700',
  secundario: 'bg-slate-200 text-slate-800 hover:bg-slate-300',
}
exportfunctionBoton({ variante = 'primario', className, ...props }: BotonProps)
{
return (
<button
      className={clsx('rounded-lg px-4 py-2 transition-colors', estilos[variante],
className)}
      {...props}
    />
  )
}
```

Así `<Boton variante="secundario" onClick={...} disabled />` queda tipado y autocompleta. `Record<Variante, string>` hace que TypeScript te avise si olvidas una variante. 

17 / 18 

2026-11-09 

guia-css-tailwind-react.md 

### Paso 35. Personalizar el tema (v4) 

Se hace en el propio CSS con `@theme` : 

```
@import"tailwindcss";
@theme {
  --color-marca: #4f46e5;
  --font-titulo: "Poppins", sans-serif;
}
```

Ahora puedes usar `bg-marca` , `text-marca` , `font-titulo` . 

### Paso 36. Evitar repetición 

1. **Primero** : extrae un componente React (la mejor opción, ya lo hiciste con `Tarjeta` y `Boton` ). 

2. **Si es solo un estilo repetido** : usa `@apply` con moderación. 

```
.btn-primario {
@apply rounded-lg bg-indigo-600 px-4 py-2 text-white hover:bg-indigo-700;
}
```

## Parte 4: Buenas prácticas y herramientas 

**Mobile first** : diseña para móvil y agrega `md:` / `lg:` después. 

- **Orden de clases** : instala `prettier-plugin-tailwindcss` para ordenarlas automáticamente. **Autocompletado** : extensión _Tailwind CSS IntelliSense_ en VS Code. 

- **Tipado** : tipa las props con `interface` y evita `any` ; extiende `React.ComponentProps<'elemento'>` para heredar las props nativas. 

- **Componentes** : si una lista de clases se repite 3+ veces, hazla componente. 

- **Accesibilidad** : usa etiquetas semánticas ( `button` , `nav` , `main` ), `alt` en imágenes y estados `focus:` . **Documentación** : https://tailwindcss.com/docs (busca con `Ctrl+K` ). 

## Ruta de práctica sugerida 

1. Ejercicio 1 en CSS puro (tarjeta responsive). 

2. Ejercicio 2 con Tailwind (misma tarjeta). 

3. Crear el proyecto React + TypeScript + Tailwind y portar la tarjeta como componente. 

4. Construir una landing: navbar, hero, grid de 3 tarjetas, footer. 

5. Agregar modo oscuro y un menú móvil con `useState` (TypeScript infiere el tipo: `useState(false)` ). 

18 / 18 

