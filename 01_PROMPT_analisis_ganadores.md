# PROMPT 1 — Analizar los patrones de los equipos ganadores

**Para qué sirve:** descubrir qué tienen en común los equipos que ganaron el Rally Latinoamericano de Innovación (a nivel nacional y latinoamericano). Analiza los desafíos que eligieron, las ideas que propusieron, lo que dijeron en el pitch, lo que mostraban los videos y cómo estructuraron la idea.

**Cómo usarlo:**
1. Copia todo el bloque de abajo (desde `### INICIO DEL PROMPT` hasta `### FIN DEL PROMPT`).
2. En la sección **[MATERIAL 1]** pega la tabla de ganadores (`datos/ganadores_2023_2025.csv`; ábranlo en Excel y copien las columnas que quepan).
3. En **[MATERIAL 2]** pega lo que tengan de los videos: transcripciones o notas.
   - Para sacar la transcripción, abre el video en YouTube y usa «⋯ → Mostrar transcripción».
   - Si usan una IA que puede ver videos de YouTube (por ejemplo Gemini o NotebookLM), pásele directamente los links.
   - Prioricen los **1.os lugares latinoamericanos** y los **ganadores de Nicaragua**.
4. El resultado de este prompt es el **«Manual de patrones ganadores»**. Guárdenlo: es un insumo del Prompt 2.

---

### INICIO DEL PROMPT

```
# TU ROL
Eres analista de concursos de innovación universitaria y has sido jurado del Rally Latinoamericano
de Innovación. Tu trabajo: encontrar, con evidencia, los PATRONES que llevaron a los equipos a ganar,
para que un equipo universitario de Nicaragua pueda repetirlos en la próxima edición.

# CONTEXTO DEL CONCURSO (no lo cambies)
- Rally Latinoamericano de Innovación (organizan CONFEDI-Argentina, ACOFI-Colombia y ANFEI-México).
  Competencia de 28 horas seguidas, simultánea en sedes universitarias de varios países.
- Equipos multidisciplinarios eligen UN desafío (problema real planteado por empresas, ONG o
  instituciones) y proponen una solución creativa e innovadora.
- Dos categorías: «INNOVACIÓN» e «IMPACTO SOCIAL». El equipo no elige la categoría: el jurado de
  sede asigna cada solución a una según sus características.
- Entregables: un VIDEO de máximo 2 minutos (en YouTube) y un REPORTE PAUTADO en PDF sobre la
  propuesta, su factibilidad y su impacto. Desde 2026 el video debe mostrar un prototipo a escala
  hecho con material reciclable y el reporte debe llevar citas APA.
- Criterios del jurado (bases): 1) grado de innovación de la solución; 2) impacto positivo en la
  sociedad; 3) prefactibilidad económica y social y sustentabilidad ambiental; 4) viabilidad
  técnica; 5) calidad de la presentación. Los entregables deben citar sus fuentes. Se descalifica
  el plagio.
- Evaluación en 3 niveles: jurado de sede → jurado nacional (elige 1.º, 2.º y 3.º por categoría en
  cada país) → jurado internacional (elige el podio latinoamericano entre los ganadores nacionales).
  Los jurados nacional e internacional NO están en la sede: deciden solo con el video y el reporte.

# MATERIAL QUE TE DOY
[MATERIAL 1] TABLA DE GANADORES (año, nivel, categoría, puesto, país, equipo, sede, desafío, idea,
link del video):
<<< PEGAR AQUÍ LA TABLA >>>

[MATERIAL 2] TRANSCRIPCIONES O NOTAS DE LOS VIDEOS PITCH (una por equipo, con el nombre del equipo):
<<< PEGAR AQUÍ. Si puedes abrir los links de video de la tabla, míralos también. >>>

# QUÉ DEBES HACER

## Fase 1 — Ficha de cada ganador
Para cada equipo con información suficiente, haz una fila con:
equipo | año | nivel y puesto | categoría | desafío | problema en 1 frase | solución en 1 frase |
tipo de solución (app/plataforma, dispositivo/IoT, material o producto físico, proceso o servicio,
modelo de negocio, mixta) | tecnología usada | a quién beneficia | cifra o dato de impacto que
mencionan | ¿mostraron prototipo/maqueta/mockup? ¿cuál? | gancho de apertura del video (primeros
10 s) | estructura del video (bloques con segundos aproximados) | cierre | estilo visual
(presentadores a cámara, animación, dramatización, maqueta, pantallas de app, etc.) | qué fue lo que
más llamó la atención.
Si un dato no está en el material, escribe «sin dato». NO inventes.

## Fase 2 — Patrones
Responde con evidencia (cita equipo y año en cada afirmación):
1. DESAFÍOS: ¿qué tipo de desafío eligen más los ganadores? (ambiental, agua, agro, salud,
   educación, residuos/reciclaje, energía, inclusión, etc.). ¿Los ganadores eligen desafíos
   «populares» o «raros»?
2. TIPO DE IDEA: ¿qué tipo de solución gana más en cada categoría? ¿Tecnología compleja o simple
   y bien ejecutada? ¿Producto, servicio o modelo de negocio?
3. CÓMO ESTRUCTURAN LA IDEA: orden en que presentan problema → solución → cómo funciona →
   factibilidad (costos, actores, alianzas) → impacto → cierre. ¿Qué peso le dan a cada parte?
4. QUÉ DICEN EN EL PITCH: frases, fórmulas, datos y cifras, uso de historias o personajes,
   nombre y marca de la solución, llamado a la acción.
5. QUÉ TRAEN LOS VIDEOS QUE LLAMAN LA ATENCIÓN: recursos visuales, ritmo, música, prototipos,
   simulaciones, calidad de audio, subtítulos, humor o emoción, quién habla.
6. INNOVACIÓN vs IMPACTO SOCIAL: ¿en qué se diferencian los ganadores de cada categoría?
7. NACIONAL vs LATINOAMERICANO: ¿qué tienen los que llegan a 1.º latinoamericano que no tienen
   los que se quedan en el podio nacional?
8. NICARAGUA: ¿qué hicieron los equipos nicaragüenses que ganaron? ¿Hay un «estilo Nicaragua»
   que el jurado premia?
9. TENDENCIAS: ¿cambió con los años lo que gana? (por ejemplo, más IA, más economía circular).

## Fase 3 — Lo que NO funciona
Lista los errores o ausencias típicas que se deducen del material (lo que los ganadores casi
nunca hacen). Marca claramente qué es evidencia y qué es inferencia tuya.

## Fase 4 — Manual de patrones ganadores (lo más importante)
Entrega un documento corto y accionable con:
a) Las 10 reglas de oro para ganar, ordenadas por importancia, cada una con su evidencia.
b) Plantilla del video de 2 minutos: tabla «segundos | qué se ve | qué se dice | texto en
   pantalla», basada en lo que hicieron los ganadores.
c) Plantilla del reporte pautado: qué contar en cada sección y con qué nivel de detalle.
d) Rúbrica interna de autoevaluación (8 a 10 criterios, escala 1–5) para comparar ideas
   candidatas antes de elegir.
e) Checklist de 15 puntos para revisar el video antes de subirlo.

# REGLAS
- Separa siempre EVIDENCIA (está en el material) de INFERENCIA (tu interpretación).
- No inventes equipos, cifras ni contenido de videos que no viste.
- Si el material es insuficiente para una conclusión, dilo y sugiere qué video conviene revisar.
- Escribe en español, claro y directo, con tablas cuando ayuden.
```

### FIN DEL PROMPT
