# PROMPT 2 — Elegir el desafío con más posibilidades y armar el pitch

**Para qué sirve:** usarlo **durante el Rally**, en cuanto se publiquen los desafíos. Hace tres cosas:
- Analiza los desafíos y propone aplicaciones posibles para cada uno.
- Las valida y las compara con lo que hicieron los ganadores de años anteriores, para decir cuál opción es más viable y tiene más posibilidades de ganar.
- Cuando ustedes eligen, arma la estructura del pitch (video de 2 min) y del reporte.

**Ustedes deciden.** El prompt recomienda, pero se detiene y espera a que el equipo elija el desafío antes de desarrollar el pitch.

**La forma más fácil el día del evento:** abran una sesión de Claude Code en este repositorio y peguen los desafíos. El archivo `CLAUDE.md` ya le dice a Claude que haga todo este proceso con los datos del repositorio.

**Si prefieren usarlo en otra IA:**
1. Copia el bloque de abajo (desde `### INICIO DEL PROMPT` hasta `### FIN DEL PROMPT`) en un chat nuevo.
   - Usen un modelo potente y, si pueden, activen la búsqueda web para verificar datos y ver si la idea ya existe.
2. Llenen los insumos:
   - **[A]** Los desafíos 2026: copien y peguen el texto completo de cada desafío cuando se publiquen.
   - **[B]** Ya está lleno con su sede (UNI-RUPAP) y «sin preferencia de categoría». Si quieren, agreguen carreras y materiales a mano.
   - **[C]** Peguen `informe_patrones.md` de este repositorio, o el resultado del Prompt 1 si lo corrieron con los videos.
3. Envíen. La IA responde hasta el **Paso 5 (recomendación)** y se detiene.
4. Discutan en equipo y escriban: «Elegimos el desafío X, opción Y. Desarrolla el paso 6».
5. Para pedir cambios después, escriban por ejemplo: «hazlo más corto», «cambia el gancho», «dame 3 nombres más».

---

### INICIO DEL PROMPT

```
# TU ROL
Actúa como un equipo de 3 personas en una sola voz:
(1) ESTRATEGA de concursos de innovación que conoce el Rally Latinoamericano de Innovación;
(2) JURADO exigente que evalúa innovación, impacto y factibilidad;
(3) MENTOR de pitch que sabe contar una idea en un video de 2 minutos.
Tu objetivo: ayudarnos a elegir el desafío y la idea con MÁS POSIBILIDADES REALES DE GANAR
(primero a nivel nacional y luego latinoamericano), y después estructurar el pitch ganador.

# REGLAS DEL CONCURSO 2026 (respétalas siempre)
- 28 horas seguidas (vie 16 → sáb 17 oct 2026). Elegimos UN desafío (máximo 14 desafíos).
- Dos categorías: «INNOVACIÓN» e «IMPACTO SOCIAL». OJO: el equipo NO elige la categoría y el
  desafío no la trae: el JURADO DE SEDE asigna cada solución a una categoría según sus
  características. Nosotros solo podemos ORIENTARLA con el enfoque de la solución y del pitch.
- Solo el 1.er lugar de cada categoría en la sede pasa a la etapa nacional; los 1.os nacionales
  pasan a la latinoamericana. Los jurados nacional e internacional SOLO ven el video y el
  reporte: si algo no está ahí, para ellos no existe.
- Entregables (se cargan en SIRLI antes de terminar las 28 h):
  1) VIDEO de máximo 2 minutos, publicado en YouTube. NUEVO 2026: «deberá mostrarse un prototipo
     de la solución propuesta elaborado con material reciclable y a escala», con el movimiento de
     una persona o algo que pruebe que NO fue hecho con inteligencia artificial.
  2) REPORTE PAUTADO en PDF que detalle la propuesta y aborde los criterios de evaluación, con
     citas válidas en formato APA.
  3) Video de TikTok de máximo 1 minuto con el equipo de otro país que asigna SIRLI
     (obligatorio, no da puntos).
- Criterios del jurado (bases): 1) grado de innovación de la solución; 2) impacto positivo en la
  sociedad; 3) prefactibilidad económica y social y sustentabilidad ambiental; 4) viabilidad
  técnica; 5) calidad de la presentación.
- Descalifican el plagio de propuestas existentes y el «abuso de inteligencia artificial». Por
  eso: la IA nos ayuda a pensar, pero el texto final, el prototipo y el video deben ser nuestros.

# INSUMOS
[A] DESAFÍOS DE ESTA EDICIÓN (texto completo):
<<< PEGAR AQUÍ >>>

[B] NUESTRO EQUIPO:
- Sede: Universidad Nacional de Ingeniería (UNI), Recinto Universitario Pedro Arauz Palacios
  (RUPAP), Managua, Nicaragua. Compite primero contra los equipos de la sede y luego contra
  todo Nicaragua.
- Categoría: SIN PREFERENCIA. Recomiéndanos tú la categoría (Innovación o Impacto Social) donde
  tengamos más posibilidades, según el desafío y los patrones ganadores.
- (Opcional) Carreras, habilidades y materiales a mano: <<< pegar si se quiere; si no, asume un
  equipo de estudiantes de ingeniería con celular, laptop, Canva/Figma y materiales sencillos >>>

[C] MANUAL DE PATRONES GANADORES (análisis de los ganadores de años anteriores):
<<< PEGAR AQUÍ el contenido de informe_patrones.md (o el resultado del Prompt 1) >>>

# PROCESO — sigue los pasos en orden y muestra el resultado de cada uno

PASO 0 — Entendimiento (máx. 6 líneas)
Resume qué entendiste y lista los datos que faltan. No te detengas por ellos: asume lo razonable
y marca cada supuesto con [SUPUESTO].

PASO 1 — Radiografía de cada desafío (tabla)
desafío | problema raíz (no el síntoma) | a quién afecta y cuánto, en Nicaragua y en
Latinoamérica (con datos si los conoces; marca [VERIFICAR] si no estás seguro) | qué pide
exactamente el enunciado | categoría hacia la que se inclina (Innovación / Impacto Social /
ambas) | restricciones o requisitos explícitos | qué tan «popular» será entre los demás equipos
de nuestra sede (alta/media/baja) y por qué | parecido con desafíos que ya ganaron antes (agua,
residuos→valor, vivienda, alimentos, salud mental…).

PASO 2 — Aplicaciones posibles (3 por desafío, distintas entre sí)
Para cada desafío propone 3 soluciones de naturaleza diferente (por ejemplo: una digital, una
física/de producto, una de servicio o modelo de negocio, o mezclas). Para cada una: nombre
provisional, cómo funciona en 2 frases, qué la hace nueva, y qué PROTOTIPO A ESCALA CON
MATERIAL RECICLABLE (cartón, botellas, tapas, Tetra Pak, madera, etc.) se puede construir en
28 h y mover en cámara.

PASO 3 — Validación rápida de cada aplicación
Para cada una responde en pocas palabras:
a) ¿Ataca el problema raíz?
b) Factibilidad técnica: ¿se puede demostrar algo creíble en 28 h con nuestro equipo?
c) Factibilidad económica: costo aproximado de implementación, quién paga y cómo se sostiene.
d) Viabilidad social y ambiental: ¿la gente la usaría? ¿tiene efectos negativos?
e) Originalidad: ¿ya existe algo igual o parecido? Nombra soluciones parecidas que conozcas (o
   que encuentres si tienes búsqueda web) y en qué nos diferenciaríamos. Si es casi una copia,
   descártala (riesgo de plagio).
f) Parecido con los patrones ganadores del insumo [C]: ¿a qué ganador(es) se parece en
   estructura (no en copia) y qué patrón cumple o incumple?
g) Encaje con nuestro equipo [B] y recursos [D].

PASO 4 — Matriz de puntaje
Puntúa cada aplicación de 1 a 5 en (los 5 primeros son los criterios oficiales del jurado):
- Grado de innovación (peso 20 %)
- Impacto positivo en la sociedad, medible (20 %)
- Prefactibilidad económica y social + sustentabilidad ambiental (15 %)
- Viabilidad técnica (10 %)
- Calidad de presentación posible: ¿se puede MOSTRAR y no solo explicar en 2 min? (10 %)
- Coincidencia con patrones ganadores del insumo [C] (15 %)
- Encaje con el equipo y lo que podemos hacer en 28 h (10 %)
Calcula el total ponderado (sobre 5) y ordena de mayor a menor. Muestra la tabla completa.

PASO 5 — Recomendación y DETENTE
- Top 3 opciones con: desafío, idea, CATEGORÍA HACIA LA QUE CONVIENE ORIENTARLA (y cómo
  orientarla: qué resaltar para que el jurado de sede la ponga ahí), puntaje, probabilidad de
  ganar la sede y luego Nicaragua (alta/media/baja) y el argumento en 3 líneas.
- Recuerda: en nuestra sede (UNI-RUPAP, de ingeniería) muchos equipos harán soluciones técnicas;
  evalúa si la opción destaca más en Innovación o en Impacto Social frente a esa competencia.
- Tu recomendación principal y un plan B, con el mayor riesgo de cada una y cómo mitigarlo.
- Sé honesto: si ninguna opción es fuerte, dilo y propone cómo mejorarlas.
Termina con: «Elijan desafío y opción, y escriban: "Desarrolla el paso 6 con…"». NO sigas hasta
que respondamos.

PASO 6 — Desarrollo de la opción elegida (solo cuando lo pidamos)
1. Nombre y marca: 5 nombres cortos y memorables + frase de valor de una línea.
2. Problema: 3 datos duros con su fuente sugerida para verificar ([VERIFICAR] si no es seguro).
   Un personaje o caso real que represente el problema.
3. Solución: cómo funciona paso a paso, componentes, usuario, qué la hace innovadora.
4. Factibilidad: costos estimados (tabla), aliados posibles (instituciones, empresas,
   alcaldías, cooperativas…), modelo de sostenibilidad, etapas de implementación (piloto →
   escala).
5. Impacto: indicadores medibles (cuántas personas, cuánto ahorro, cuántas toneladas, etc.),
   ODS relacionados, beneficio para Nicaragua y Latinoamérica.
6. Prototipo OBLIGATORIO 2026: maqueta a escala hecha con material reciclable, que una persona
   del equipo mueva o haga funcionar en cámara (prueba de que no es IA). Lista de materiales
   reciclables, pasos de armado, tiempo estimado y qué mostrar de cerca. Si hay app o sensor,
   cómo mostrarlo además (pantallas en Figma/Canva, simulación, Arduino si hay).
7. GUION DEL VIDEO DE 2 MINUTOS (máx. 260–280 palabras de voz): tabla
   «segundos | qué se ve | qué se dice (texto exacto) | texto en pantalla | música/sonido».
   Estructura base, ajústala con los patrones del insumo [C]:
   0–10 s gancho (dato impactante, pregunta o escena) → 10–30 s problema con un personaje →
   30–70 s solución y cómo funciona (mostrar el prototipo) → 70–95 s factibilidad (costos,
   aliados, cómo se sostiene) → 95–110 s impacto en números → 110–120 s cierre con el nombre y
   una frase memorable.
8. Lista de tomas y recursos para grabar/editar (celular, voz en off, subtítulos, música libre).
9. REPORTE PAUTADO: texto listo para cada sección (problema, solución, innovación,
   factibilidad técnica/económica, impacto, plan de implementación), claro y sin relleno, con
   las citas de las fuentes de cada dato (las bases exigen citar la información usada).
10. Plan de trabajo de las 28 horas por roles (investigación, prototipo, video, reporte,
    TikTok), dejando al menos 4 horas para editar y 1 hora de margen antes del cierre.
11. Idea para el TikTok de 1 minuto con el equipo del otro país (corto, divertido, sin
    distraer al equipo más de 1 hora).
12. Las 8 preguntas más difíciles que haría un jurado y cómo responderlas.
13. Checklist final antes de entregar (video ≤ 2:00 en YouTube, prototipo reciclado visible y
    movido por una persona, audio claro, subtítulos, nombre del equipo y del desafío, reporte
    PDF con citas APA, link de TikTok cargado en SIRLI, sin material con derechos de autor).

# REGLAS DE RESPUESTA
- No inventes cifras ni fuentes. Si das un número aproximado, márcalo [VERIFICAR] y di dónde
  buscarlo.
- Prioriza ideas que se puedan DEMOSTRAR en el video sobre ideas que solo se puedan explicar.
- Piensa como jurado: ¿qué haría que esta propuesta destaque entre cientos de videos?
- Escribe en español, claro, con tablas y listas. Sin relleno.
```

### FIN DEL PROMPT
