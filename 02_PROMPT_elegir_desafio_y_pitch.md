# PROMPT 2 — Elegir el desafío con más posibilidades y armar el pitch

**Para qué sirve:** usarlo **durante el Rally**, en cuanto se publiquen los desafíos. Hace tres cosas:
- Analiza los desafíos y propone aplicaciones posibles para cada uno.
- Las valida y las compara con lo que hicieron los ganadores de años anteriores, para decir cuál opción es más viable y tiene más posibilidades de ganar.
- Cuando ustedes eligen, arma la estructura del pitch (video de 2 min) y del reporte.

**Ustedes deciden.** El prompt recomienda, pero se detiene y espera a que el equipo elija el desafío antes de desarrollar el pitch.

**Cómo usarlo:**
1. Copia el bloque de abajo (desde `### INICIO DEL PROMPT` hasta `### FIN DEL PROMPT`) en un chat nuevo de su IA.
   - Usen un modelo potente y, si pueden, activen la búsqueda web para verificar datos y ver si la idea ya existe.
2. Llenen los 4 insumos:
   - **[A]** Los desafíos 2026: copien y peguen el texto completo de cada desafío cuando se publiquen.
   - **[B]** Las preguntas de Brayan, ya respondidas (perfil del equipo).
   - **[C]** El «Manual de patrones ganadores» que salió del Prompt 1. Si no les dio tiempo, peguen `informe_patrones.md` de este repositorio.
   - **[D]** Datos prácticos: sede, categoría preferida, materiales a mano y horario.
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

# REGLAS DEL CONCURSO (respétalas siempre)
- 28 horas seguidas. Elegimos UN desafío. Hay dos categorías: «INNOVACIÓN» e «IMPACTO SOCIAL».
- Entregables: VIDEO de máximo 2 minutos + REPORTE PAUTADO (formulario sobre la propuesta, su
  factibilidad y su impacto). Pasarse de tiempo o plagiar puede descalificar.
- Criterios del jurado (bases): 1) grado de innovación de la solución; 2) impacto positivo en la
  sociedad; 3) prefactibilidad económica y social y sustentabilidad ambiental; 4) viabilidad
  técnica; 5) calidad de la presentación. Los entregables deben citar las fuentes que usan.
- Jurado de sede → jurado nacional (podio por categoría) → jurado internacional (podio
  latinoamericano). Los jurados nacional e internacional SOLO ven el video y el reporte: si algo
  no está en el video o el reporte, para ellos no existe.

# INSUMOS
[A] DESAFÍOS DE ESTA EDICIÓN (texto completo):
<<< PEGAR AQUÍ >>>

[B] PERFIL DEL EQUIPO (preguntas de Brayan respondidas: integrantes, carreras, habilidades,
herramientas que dominamos, intereses, experiencia previa, etc.):
<<< PEGAR AQUÍ >>>

[C] MANUAL DE PATRONES GANADORES (análisis de los ganadores de años anteriores):
<<< PEGAR AQUÍ >>>

[D] DATOS PRÁCTICOS: sede/universidad, país (Nicaragua), categoría que preferimos (o «sin
preferencia»), materiales y equipos que tenemos a mano, horas que realmente podremos trabajar,
quién edita video:
<<< PEGAR AQUÍ >>>

# PROCESO — sigue los pasos en orden y muestra el resultado de cada uno

PASO 0 — Entendimiento (máx. 6 líneas)
Resume qué entendiste y lista los datos que faltan. No te detengas por ellos: asume lo razonable
y marca cada supuesto con [SUPUESTO].

PASO 1 — Radiografía de cada desafío (tabla)
desafío | quién lo propone | problema raíz (no el síntoma) | a quién afecta y cuánto (con datos
si los conoces; marca [VERIFICAR] si no estás seguro) | qué espera el proponente | categoría donde
encaja mejor | restricciones o requisitos explícitos | qué tan «popular» será entre los demás
equipos (alta/media/baja) y por qué.

PASO 2 — Aplicaciones posibles (3 por desafío, distintas entre sí)
Para cada desafío propone 3 soluciones de naturaleza diferente (por ejemplo: una digital, una
física/de producto, una de servicio o modelo de negocio, o mezclas). Para cada una: nombre
provisional, cómo funciona en 2 frases, qué la hace nueva, y qué prototipo o demostración se
puede construir en 28 h.

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
Si la categoría elegida cambia lo que más pesa, explícalo.

PASO 5 — Recomendación y DETENTE
- Top 3 opciones con: desafío, idea, categoría sugerida, puntaje, probabilidad de ganar
  (alta/media/baja) y el argumento en 3 líneas.
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
6. Prototipo: qué construir o simular en 28 h para que se VEA en el video (maqueta, mockup de
   app en Figma/Canva, simulación, demo), con la lista de tareas.
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
10. Plan de trabajo de las 28 horas por roles (investigación, prototipo, video, reporte),
    dejando al menos 4 horas para editar y 1 hora de margen antes del cierre.
11. Las 8 preguntas más difíciles que haría un jurado y cómo responderlas.
12. Checklist final antes de entregar (duración ≤ 2:00, audio claro, subtítulos, nombre del
    equipo, desafío correcto, datos con fuente, sin material con derechos de autor, etc.).

# REGLAS DE RESPUESTA
- No inventes cifras ni fuentes. Si das un número aproximado, márcalo [VERIFICAR] y di dónde
  buscarlo.
- Prioriza ideas que se puedan DEMOSTRAR en el video sobre ideas que solo se puedan explicar.
- Piensa como jurado: ¿qué haría que esta propuesta destaque entre cientos de videos?
- Escribe en español, claro, con tablas y listas. Sin relleno.
```

### FIN DEL PROMPT
