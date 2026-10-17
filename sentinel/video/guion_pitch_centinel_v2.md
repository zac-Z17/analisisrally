# Guion del pitch de Centinel: revisión del guion del equipo

> **Para el equipo:**
> - Esta es su versión, corregida: respeté sus 5 bloques, sus planos con el Pixel 7 Pro y su cierre.
> - Ajusten las frases a su forma de hablar y graben con sus voces (regla anti-IA).
> - Revisé el guion contra la app Android, el reporte y las bases 2026.

## 1. Correcciones obligatorias (riesgo de perder puntos o de descalificación)

| # | El guion dice | Problema | Corrección |
|---|---|---|---|
| 1 | Plano 3: «soporte CAD impreso en papel o maqueta» | **Falta el prototipo a escala de material reciclable movido por una persona**, la novedad obligatoria de 2026. El CAD impreso en papel no cuenta como prototipo | Agregar una **maqueta de cartón** (casa con puerta) y un **soporte prototipo hecho con botella PET y cartón**. La toma de la alarma se graba en un **plano continuo, sin cortes** |
| 2 | «acelerómetro, giroscopio y **magnetómetro**» | La app usa acelerómetro, aceleración lineal y giroscopio; **no usa magnetómetro** | Quitar «magnetómetro», o programarlo antes de grabar. Corregirlo también en el reporte |
| 3 | «Se comunica por red **wifi local sin consumir internet**» | **No está hecho**: `android/README.md` lo tiene en Pendientes; hoy todo pasa por internet (ntfy) | Quitarlo del video. En el reporte, presentarlo como «siguiente versión» |
| 4 | «**corta la carga**» | Android no deja que una app corte la carga. La app **avisa** «Muy caliente · Desconecta el cargador» | «Avisa de inmediato para desconectar el cargador» |
| 5 | «hay un **cortocircuito**», «**conato de incendio**» | Centinel no detecta cortocircuitos ni incendios de la casa: solo vigila **su propia batería**. Un jurado lo nota y resta credibilidad | Quitarlo del gancho. La temperatura queda como «seguridad de la batería vieja» |
| 6 | «120 políticas del PNUD», «1,4 millones de patrullajes» | **No encontré fuente para ninguna de las dos cifras**. Además el gancho **elogia la seguridad que ya existe**, debilita el problema y mete un tema político | Quitarlas. El gancho pasa a ser la **demostración** |
| 7 | «solo el **3 %** de la basura electrónica se recicla» | Ese 3 % es de **América Latina** y mide lo que se gestiona de forma adecuada (UNITAR, 2022). En el mundo el reciclaje documentado es 22,3 % (UNITAR, 2024) | «En América Latina solo el 3 % de la basura electrónica se gestiona bien» |
| 8 | «5.300 millones de celulares **duermen en cajones**» | La fuente (News24, 2022) dice que en 2022 se **desecharían o guardarían** sin uso | En la voz basta «millones». En pantalla: «5.300 M celulares desechados o guardados en 2022» |
| 9 | «un **96 %** más barato» | Se calcula contra el precio más alto (US$300). Contra US$150 sale 93 %. Además, el **precio de las alarmas necesita fuente** en el reporte | «De 15 a 30 veces menos que una alarma». La fuente del precio va en pantalla |
| 10 | «Utilidad Año 1: US$2.961 (**24,1 %**)» | 1.200 hogares × US$10 = US$12.000, y 2.961 ÷ 12.000 = **24,7 %**, no 24,1 % | Revisar la cuenta y usar exactamente el número del reporte |
| 11 | «Stack: **Kotlin Native** → iOS» | «Kotlin/Native» es otra tecnología. La app es **Android nativa en Kotlin**, e iOS no está en el plan | «App Android nativa (Kotlin)» |
| 12 | «convierte **cualquier** celular viejo» | Necesita Android 7 o superior con acelerómetro | «Convierte un celular viejo». En el reporte: «casi cualquier Android» |
| 13 | Ritmo | Los bloques 1, 2 y 5 van a 2,8–2,9 palabras por segundo (muy rápido), y los bloques 3 y 4 van lentos | Repartí el texto (ver la tabla) |
| 14 | «CAPEX», «BatteryManager», «amortizar 480 horas de ingeniería», «telemetría» | En el jurado latinoamericano no todos son ingenieros, y la «calidad de la presentación» es un criterio | Decirlo simple en la voz y dejar el detalle técnico y financiero en el reporte |

**Nombre:** la app ahora es **Centinel**, pero el reporte y la carpeta dicen «Sentinel». Usen **Centinel** en todo: reporte, título de YouTube, TikTok y textos en pantalla.

## 2. Lo que está muy bien (se mantiene)

- **Cada bloque responde a un criterio de la rúbrica.** Es la forma correcta de pensarlo.
- **El plano CAD con rótula y rejilla hexagonal** muestra el trabajo de Arquitectura y da viabilidad técnica.
- **El soporte de rPET reciclado** une el producto con lo ambiental.
- **El modelo financiero con números** muestra el trabajo de Economía.
- **Los «vecinos de la cuadra» y Villa Progreso** son comunidad y un lugar real: es el patrón de los ganadores.
- **«Centinel: tu comunidad, tu escudo»** es un buen cierre para Impacto Social.

## 3. Guion corregido

**Duración:** 1:55. **Voz:** unas 240 palabras, a 2,0–2,5 palabras por segundo. **Tono:** firme, natural y empático.

### Bloque 1 · 0:00–0:15 · Gancho y problemática

- **Qué se ve:**
  - **Plano 1:** de noche, en un salón de la UNI, un compañero duerme con su celular (Receptor) al lado.
  - **Plano 1b, sin cortes:** una mano fuerza la puerta de la **maqueta de cartón**. El Centinela está montado por dentro. El celular junto al compañero se enciende en rojo («¡INTENTO DE ENTRADA!»), suena la sirena y él despierta.
  - Primeros 4 segundos: solo la sirena, sin voz.
- **Voz:**
  > Medianoche. Fuerzan tu puerta y tu familia duerme. Una alarma comercial cuesta de 150 a 300 dólares; por eso, en muchos barrios, nadie se entera a tiempo.
- **En pantalla:**
  - «12:00 a. m.»
  - «Alarma comercial en Nicaragua: US$150–300». Fuente: *[tienda o página web]*.

### Bloque 2 · 0:15–0:32 · El residuo y la propuesta (Desafío 02 y diseño del soporte)

- **Qué se ve:**
  - **Plano 2:** una gaveta con celulares viejos.
  - Acercamiento a la laptop con el **plano CAD 3D** de Centinel (vistas frontal, de planta y 3D, con la rótula y la rejilla hexagonal). El presentador señala el diseño.
  - El presentador toma el **soporte prototipo a escala hecho con botella PET y cartón reciclado**.
- **Voz:**
  > Mientras tanto, millones de celulares viejos duermen en cajones, y en América Latina solo el 3 % de la basura electrónica se gestiona bien. Creamos Centinel: un soporte de PET reciclado que convierte ese celular viejo en un sensor de seguridad.
- **En pantalla:**
  - «5.300 M celulares desechados o guardados en 2022». Fuente: *News24, 2022*.
  - «América Latina: solo 3 % de la basura electrónica bien gestionada». Fuente: *UNITAR, 2022*.
  - «Soporte rPET: rótula + ventilación hexagonal».
  - «CENTINEL · Equipo Nanobots (UNI-RUPAP)».

### Bloque 3 · 0:32–0:58 · Solución técnica y alarma en vivo (viabilidad técnica e innovación)

- **Qué se ve:**
  - **Plano 3:** unas manos colocan el celular viejo en el soporte prototipo, **fijado en la puerta de la maqueta**. Se vincula con el QR en la Centinel App.
  - **Plano 4 [DRAMA, plano continuo sin cortes]:** el «intruso» sacude la puerta de la maqueta. En el mismo cuadro, la alarma roja salta en el celular de la familia y en el del vecino.
- **Voz:**
  > Es una app Android nativa, hecha en Kotlin. Con la pantalla apagada, el acelerómetro y el giroscopio sienten golpes, forcejeos y aperturas, y la app aprende a ignorar la lluvia o las mascotas. En segundos avisa a la familia y a los vecinos de la cuadra. Sin cámaras ni micrófonos.
- **En pantalla:**
  - «App Android nativa · Kotlin».
  - «Acelerómetro + giroscopio · pantalla apagada».
  - «Alerta en segundos a familia y vecinos».
  - «0 cámaras · 0 micrófonos».

### Bloque 4 · 0:58–1:18 · Control y seguridad de la batería (viabilidad técnica y social)

- **Qué se ve:**
  - **Plano 5 (lente 2x):** primer plano de la Centinel App real: Horario (11 p. m.–4 a. m.), Carga diaria, Vecinos de confianza, y el dato de temperatura del Centinela con el aviso «Avisar si pasa de 45 °C».
- **Voz:**
  > Desde la app, la familia elige el horario de vigilancia, la hora de carga y a sus vecinos de confianza. Y como son baterías viejas, Centinel vigila su temperatura: si pasa de 45 grados, avisa de inmediato para desconectar el cargador.
- **En pantalla:**
  - «Vigilancia: 11 p. m.–4 a. m. · Carga diaria programada».
  - «Batería > 45 °C → aviso inmediato».

### Bloque 5 · 1:18–1:55 · Modelo financiero, impacto y cierre (prefactibilidad e impacto)

- **Qué se ve:**
  - **Plano 6:** tabla en pantalla con los números.
  - Después, el equipo completo (Nanobots) frente a la cámara en la UNI, con la maqueta, el soporte reciclado y el teléfono. Sonrientes, seguros y profesionales.
  - Último plano: el Centinela marca «VIGILANDO» en verde.
- **Voz:**
  > Fabricar el kit cuesta 4,50 dólares y lo vendemos a 10, sin mensualidades: de 15 a 30 veces menos que una alarma. Con una inversión inicial de 3.000 dólares, el primer año protegemos 1.200 hogares, con ganancia, y son 1.200 celulares que no van a la basura. Somos Sistemas, Arquitectura y Economía de la UNI. Empezamos en Villa Progreso, y puede llegar a cualquier barrio de Latinoamérica. Centinel: tu comunidad, tu escudo.
- **En pantalla:**

  | Concepto | Valor |
  |---|---|
  | Costo del kit | US$4,50 |
  | Precio | US$10,00 |
  | Mensualidad | US$0 |
  | Inversión inicial | US$3.000 |
  | Año 1 | 1.200 hogares · utilidad US$2.961 *(revisar el %)* |

  - «1.200 celulares reutilizados · ODS 11 y 12».
  - «Red vecinal 100 % gratuita».
  - Cierre: «CENTINEL · Equipo Nanobots · UNI-RUPAP · Desafío 02: Sensores al servicio de la comunidad · Rally Latinoamericano de Innovación 2026».

**Cambios en el cierre:**
- Cambié «Centinel no vende tecnología: pone el valor social por encima del mercado» por frases concretas: el jurado recuerda imágenes y números, no frases abstractas.
- Agregué «puede llegar a cualquier barrio de Latinoamérica». Es lo que busca el jurado latinoamericano: que se pueda replicar en otros países.

## 4. Confirmar antes de grabar

- [ ] **Precio de las alarmas (US$150–300):** de qué tienda o página sale. Va en pantalla y en las referencias del reporte.
- [ ] **Utilidad del año 1:** que el 24,1 % o el 24,7 % y el US$2.961 coincidan con la tabla del reporte.
- [ ] **Villa Progreso:** confirmar que es el piloto que proponen y que aparece en el reporte.
- [ ] **Reporte:**
  - quitar el magnetómetro y el wifi local sin internet, o marcarlos como «siguiente versión»;
  - actualizar los costos (US$4,50 y US$10) y el nombre a Centinel.
- [ ] **Maqueta y soporte reciclado** listos, y la toma clave grabada **sin cortes** (3 intentos).
- [ ] **App:**
  - calibrar el Centinela sobre la puerta de cartón, con sensibilidad **Alta**;
  - tocar una vez la pantalla de cada celular para que suene la sirena.
- [ ] **Video final:** 2:00 o menos, con subtítulos, sin música con derechos de autor y publicado en YouTube con un título que empiece con «Centinel».
