# Análisis Rally Latinoamericano de Innovación — equipo UNI-RUPAP

Base de trabajo para elegir el desafío y la categoría con más posibilidades de ganar en el **Rally Latinoamericano de Innovación 2026**, y para armar un pitch ganador.

| | |
|---|---|
| **Fechas** | Viernes 16 y sábado 17 de octubre de 2026, 28 horas seguidas |
| **Hora** | Inicio calculado: 09:00 hora de Nicaragua. **Confírmenlo con la persona responsable de su sede** |
| **Sede** | UNI-RUPAP, Managua |
| **Resultados** | Sede y nacionales: sáb. 17 · Latinoamericanos: mar. 20 |

## Qué hay aquí

| Archivo | Para qué |
|---|---|
| [`informe_patrones.md`](informe_patrones.md) | **Empiecen por aquí.** Qué desafíos e ideas ganan, cómo funciona el embudo sede → nacional → Latinoamérica, hacia qué categoría se inclina cada tipo de desafío, estructura del pitch, videos para ver y reglas de decisión |
| [`02_PROMPT_elegir_desafio_y_pitch.md`](02_PROMPT_elegir_desafio_y_pitch.md) | Prompt para el día del evento: analiza los desafíos, propone y puntúa ideas, recomienda desafío y categoría, y luego arma video, reporte y plan de 28 h |
| [`01_PROMPT_analisis_ganadores.md`](01_PROMPT_analisis_ganadores.md) | Prompt para analizar a fondo los videos ganadores (si los ven antes del Rally) |
| [`reglas_del_rally_2026.md`](reglas_del_rally_2026.md) | Bases 2026, criterios del jurado, entregables y novedades, con fuentes |
| [`datos/ganadores_sirli_2023_2025.csv`](datos/ganadores_sirli_2023_2025.csv) | **Los 2.407 premios oficiales 2023–2025** (sede, nacional e internacional): categoría, puesto, desafío, sede, país, link y título del video |
| [`datos/analisis_sirli.md`](datos/analisis_sirli.md) | Tablas: embudo por desafío, categoría por desafío, Nicaragua año por año, la sede UNI-RUPAP y las universidades |
| [`datos/desafios_sirli_2023_2025.csv`](datos/desafios_sirli_2023_2025.csv) | Enunciado completo de los 26 desafíos 2023–2025 |
| [`datos/ganadores_2023_2025.csv`](datos/ganadores_2023_2025.csv) | Ideas y cifras de los ganadores, según noticias |

## Cómo usarlo el día del Rally
1. Cuando publiquen los desafíos, **copien el texto completo de todos**.
2. **Opción A (recomendada):** abran una sesión de Claude Code en este repositorio y peguen los desafíos. Claude ya sabe qué hacer: lo dice `CLAUDE.md`.
3. **Opción B:** usen el `02_PROMPT…` en cualquier IA, pegando los desafíos y el `informe_patrones.md`.
4. La IA recomienda las 3 mejores opciones (desafío + idea + categoría) y **se detiene**. Ustedes deciden.
5. Pidan el desarrollo: guion del video de 2 min, prototipo reciclado, reporte con citas APA, plan de 28 h y TikTok.

## Las 5 reglas 2026 que no pueden olvidar
1. **El video dura máximo 2 min y va en YouTube.** Debe mostrar un **prototipo a escala hecho con material reciclable**, movido por alguien del equipo (prueba de que no es IA).
2. **El reporte pautado va en PDF, con citas APA.**
3. **El TikTok de máximo 1 min con un equipo de otro país es obligatorio** (SIRLI arma las parejas).
4. **La categoría la asigna el jurado de sede.** Solo el **1.er lugar de cada categoría** pasa a la etapa nacional.
5. **Descalifican por plagio o por abuso de IA.** Usen la IA para pensar, no para entregar textos o imágenes hechos por ella.

## Ganadores latinoamericanos 2023–2025 (resumen)

| Año | Categoría | 1.º | 2.º | 3.º |
|---|---|---|---|---|
| 2025 | Innovación | **STEM Synergy** (Nicaragua, UAM) · 05 Huertos verticales | The Last Resort (Chile) · 02 Reciclado textil | AESIR (Colombia) · 04 Alerta temprana |
| 2025 | Impacto Social | **Mentes Volcánicas** (Nicaragua, **UNI-RUPAP**) · 01 Tetra Pak | Idronix (El Salvador) · 04 Alerta temprana | HojaBeta (Chile) · 02 Reciclado textil |
| 2024 | Innovación | **TECHRIDERS** (Nicaragua, UNI-RUSB) · 01 Micropresas | Catalizadores del Cambio (Chile) · 08 Desperdicio alimentario | Tecnobioq (México) · 01 Micropresas |
| 2024 | Impacto Social | Mexitech (México) · 08 Desperdicio alimentario | UNABINNOVA (Chile) · 01 Micropresas | Los Innovadores (Argentina) · 03 Enlazando generaciones |
| 2023 | Innovación | **Güegüense Pinolero** (Nicaragua, UNM) · 05 Vivienda digna | Ingeniu's Version CT (México) · 09 Gris a verde | HIT (Ecuador) · 02 Siempre en contacto |
| 2023 | Impacto Social | Mentes Conectadas (Ecuador) · 08 Tratar de estar mejor | Empate: EcoRally Explorers (Colombia), **Nicarq Sinergy** (Nicaragua, UNI-RUSB), Vida e Saúde (Brasil) | Empate: El Octágono (Argentina), Mexcol Innova (México) |

## Ganadores nacionales de Nicaragua (1.os lugares)

| Año | Innovación | Impacto Social |
|---|---|---|
| 2025 | STEM Synergy (UAM) · Huertos verticales | Mentes Volcánicas (**UNI-RUPAP**) · Tetra Pak |
| 2024 | TECHRIDERS (UNI-RUSB) · Micropresas | Hydrostudio (UNCSM) · Micropresas |
| 2023 | Güegüense Pinolero (UNM) · Vivienda digna | Nicarq Sinergy (UNI-RUSB) · Vivienda digna |

## Su sede: UNI-RUPAP (1.os lugares de sede)

| Año | Innovación | Impacto Social |
|---|---|---|
| 2025 | Ingenio «SAT KUNAN» · Alerta temprana | **Mentes Volcánicas «ReVida Móvil» · Tetra Pak → 1.º nacional y 1.º latam** |
| 2024 | UNITechnos «Nuvis Collecta» · Micropresas | Volcanic «IGNI» · Evacuación |
| 2023 | IngenioNica · Vivienda digna | **Tlacatlyani · Alimentación de calidad → 3.º nacional** |

De los 6 primeros lugares de su sede, 2 llegaron al podio nacional (33 %, contra el 7 % de Nicaragua), y los dos en Impacto Social.

Los 2.os y 3.os lugares, todos los países, las sedes y los links de video están en `datos/ganadores_sirli_2023_2025.csv`.

## Pendiente
- **Ver los videos ganadores.** Desde la nube no se pueden leer los subtítulos de YouTube. La lista priorizada, con links y comparaciones, está en `informe_patrones.md`, sección 7.

## Fuentes principales
- SIRLI, sistema oficial del Rally (ganadores y banco de desafíos): https://sirli.org/ganadores/
- Sitio oficial y bases 2026: https://www.rallydeinnovacion.org/edicion_2026/
- Noticias: CONFEDI, ANFEI, ACOFI, CONDEFI, Nicaragua Creativa, Canal 4, El 19 Digital, TN8, CNU.

Cada fila de los CSV trae sus propias fuentes.
