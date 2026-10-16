# Rally Latinoamericano de Innovación 2026 — equipo UNI-RUPAP

Habla con el usuario en **español**, claro y sin tecnicismos. Es un equipo universitario de Nicaragua que compite en la **sede UNI-RUPAP** (Universidad Nacional de Ingeniería, Managua) y **no tiene preferencia de categoría**. **Equipo (6):** 4 de Ingeniería en Sistemas, 1 de Arquitectura y 1 de Economía y Finanzas. Avance del día del evento en `analisis_desafios_2026.md`.

## Qué quieren de ti el día del evento
- **Cuándo:** viernes 16 de octubre de 2026. El inicio calculado es a las 09:00, hora de Nicaragua (sin confirmar).
- **Qué te van a pegar:** los desafíos del Rally 2026.
- **Qué hacer:** aplica el proceso del archivo `02_PROMPT_elegir_desafio_y_pitch.md` (pasos 0 a 5) con estos datos del repo:
  - `informe_patrones.md`: patrones ganadores y **reglas de decisión** (sección 8), lo más importante.
  - `datos/analisis_sirli.md`: tablas por desafío, categoría, Nicaragua y la sede UNI-RUPAP.
  - `datos/ganadores_sirli_2023_2025.csv`: los 2.407 premios oficiales (sede, nacional e internacional) con video y título.
  - `datos/desafios_sirli_2023_2025.csv`: enunciados de los desafíos 2023–2025, para comparar con los de 2026.
  - `datos/ganadores_2023_2025.csv`: ideas y cifras de los ganadores, según noticias.
  - `reglas_del_rally_2026.md`: bases, criterios, entregables y novedades 2026.
- **Resultado:** recomienda el **desafío**, la **idea** y la **categoría hacia la que conviene orientarla**, con la que tenga más probabilidades de ganar la sede, luego Nicaragua y luego Latinoamérica.
- **Después:** detente y espera a que el equipo elija. Luego desarrolla el paso 6: guion del video de 2 min, prototipo reciclado, reporte con citas APA, plan de 28 h y TikTok.
- **Rapidez:** el reloj corre (28 h en total). La primera recomendación debe salir en minutos: tablas cortas, sin relleno.

## Reglas clave 2026 (no olvidar)
- **Categoría:** la asigna el **jurado de sede**, no el equipo. Solo el 1.er lugar de cada categoría en la sede pasa a la etapa nacional.
- **Video:** máximo 2 min, en YouTube. Debe mostrar un **prototipo a escala hecho con material reciclable**, movido por una persona (prueba de que no es IA).
- **Reporte pautado:** en PDF y **con citas APA**.
- **TikTok:** de máximo 1 min con el equipo de otro país. Es obligatorio y no da puntos.
- **Descalificación:** por plagio o por «abuso de inteligencia artificial». Ayuda a pensar, pero el texto final debe ser del equipo; recuérdaselo.
- **Criterios del jurado:**
  1. Innovación.
  2. Impacto social.
  3. Prefactibilidad económica y social, y sustentabilidad ambiental.
  4. Viabilidad técnica.
  5. Calidad de la presentación.

## Estructura del repo
| Archivo | Qué es |
|---|---|
| `README.md` | Guía para el equipo |
| `01_PROMPT_analisis_ganadores.md` | Prompt para analizar los ganadores (con los videos) |
| `02_PROMPT_elegir_desafio_y_pitch.md` | Prompt para elegir el desafío y armar el pitch |
| `informe_patrones.md` | Análisis de patrones 2023–2025 |
| `reglas_del_rally_2026.md` | Bases, criterios, cronograma y desafíos de años anteriores, con fuentes |
| `datos/` | Tablas CSV y JSON con ganadores y desafíos |
| `herramientas/descargar_sirli.py` | Descarga de sirli.org los ganadores de sede, nacionales e internacionales y el banco de desafíos. Completa la cadena de certificados que el sitio envía incompleta |
| `herramientas/titulos_videos.py` | Agrega a los ganadores el título de su video de YouTube (servicio oEmbed) |
| `herramientas/analizar.py` | Genera `datos/analisis_sirli.md` |
| `herramientas/unir_datos.py` | Arma los CSV de noticias |

Al publicarse los desafíos 2026 en sirli.org (`/desafios/banco_desafios/2026`), se pueden bajar con `python herramientas/descargar_sirli.py 2026` (crea `datos/desafios_sirli_2026.csv`; si `datos/sirli_html/desafios_2026.html` quedó guardado de antes de la publicación, bórralo primero). YouTube bloquea desde la nube la descarga de subtítulos y la lectura de páginas de video: no intentes saltarte ese bloqueo.

**Red:** si sirli.org, rallydeinnovacion.org o youtube.com fallan con 403, el entorno no los tiene permitidos. Díselo al usuario: se agregan en la configuración del entorno, en «Acceso a red → Dominios permitidos».
