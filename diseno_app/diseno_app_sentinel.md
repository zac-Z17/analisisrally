# Sentinel — Diseño de la app (borrador v1)

Guía para diseñar la app en **Stitch** (stitch.withgoogle.com). Equipo Nanobots, Rally 2026.

**Archivos de esta carpeta:**

| Archivo | Qué es |
|---|---|
| `sentinel_lamina.png` | Las 10 pantallas, la paleta y las tipografías en una sola imagen |
| `pantallas/01…10_*.png` | Cada pantalla suelta, para subirla a Stitch como referencia |
| `fuente/sentinel_ui.html` | El código de la maqueta, por si quieren cambiar textos o colores y volver a sacar las imágenes |

---

## 1. La idea en una línea

Es **una sola app con dos modos**. Al abrirla por primera vez, el usuario elige qué será ese equipo:

| Rol | Qué equipo es | Qué hace | Color | Ícono |
|---|---|---|---|---|
| **Sentinela** (emisor) | El celular viejo, en el soporte atornillado a la puerta o ventana | Solo detecta y envía alertas | Azul `#4C8DFF` | Escudo |
| **Receptor** | El celular, la tablet o la computadora de la familia | Recibe alertas, pausa la vigilancia y cambia ajustes | Cian `#22D3EE` | Celular o campana |
| **Vecino de confianza** | El celular del vecino | Solo recibe las alertas fuertes de esa casa | Violeta `#A78BFA` | Personas |

- Todos los equipos de una casa forman un **hogar**, por ejemplo «Mi casa».
- El primer receptor que crea el hogar es el **administrador**: vincula equipos, quita equipos y cambia ajustes.
- **Cómo se sabe quién es quién:** cada equipo muestra siempre una etiqueta con su rol («SENTINELA · EMISOR», «RECEPTOR», «ADMIN») en su color.

## 2. Paleta de colores

La app usa **modo oscuro por defecto**: se usa de noche y en celulares viejos, y la pantalla oscura gasta menos batería y no deslumbra. Todos los colores se revisaron para que el texto se lea bien (contraste WCAG AA).

| Nombre | Hex | Uso |
|---|---|---|
| Noche | `#0B1220` | Fondo de la app |
| Superficie | `#131C2E` | Tarjetas |
| Superficie 2 | `#1C2740` | Campos, botones secundarios |
| Borde | `#2A3654` | Líneas y bordes de tarjetas |
| Texto | `#EAF0FB` | Texto principal |
| Texto suave | `#94A3BF` | Texto secundario |
| **Azul Sentinel** | `#2F6FEB` (botones) / `#4C8DFF` (íconos y texto) | Color de la marca, botones principales y Sentinelas |
| Cian | `#22D3EE` | Receptores |
| Violeta | `#A78BFA` | Vecinos de confianza |
| Verde | `#22C55E` | «Vigilando», todo bien, carga |
| Ámbar | `#F5A524` | Atención: batería baja, calor, vibración leve |
| Rojo | `#FF5A5F` (texto) / `#D92D2D` (fondo) | Intento de entrada; fondo de la pantalla de alerta |

**Regla de colores:** el rojo **solo** se usa para alertas de intento de entrada. Si aparece en otros lugares, pierde fuerza.

**Modo claro (opcional, para el día):** fondo `#F4F6FB`, tarjetas `#FFFFFF`, texto `#0E1526`, texto suave `#5B6680`, azul `#2563EB`.

**Tipografías** (todas de Google Fonts):

| Fuente | Uso |
|---|---|
| **Sora** | Títulos |
| **Inter** | Textos y botones |
| **JetBrains Mono** | Código de vinculación y números de teléfono |

**Estilo:**
- Esquinas redondeadas: 18 px en tarjetas y 14 px en botones.
- Íconos de línea (Lucide o Material Symbols Rounded).
- Botones grandes, de 48 px de alto mínimo: se usan de noche y medio dormidos.

## 3. Primer uso y vinculación (emisor ↔ receptor)

1. **Bienvenida, «¿Qué será este dispositivo?»** (pantalla 1). Hay tres opciones:
   - **Sentinela**;
   - **Receptor**;
   - el enlace «Me invitaron como vecino de confianza».
   
   También está el botón «Escanear el QR de la caja», que abre la página de descarga e instalación.
2. **El primer receptor crea el hogar:** escribe un nombre («Mi casa») y un PIN de 4 dígitos. Ese receptor queda como **administrador**.
3. **Vincular** (pantalla 2):
   - En el administrador: **Equipos → +** y elegir **Sentinela** o **Receptor**.
   - La app muestra un **QR** y un **código de 6 números**, que vencen en 5 min.
4. **En el equipo nuevo:**
   - abrir la app y elegir el rol;
   - escanear el QR o escribir el código;
   - ponerle nombre: «Puerta principal», «Ventana cocina»…
5. **Solo para el Sentinela, una calibración de 3 pasos:**
   1. «Cierra la puerta y no la toques por 5 segundos».
   2. «Ábrela y ciérrala».
   3. «Da 3 golpes suaves».
   
   Al terminar sale «¡Listo! Sensibilidad ajustada».
6. **Permisos que la app debe pedir**, cada uno con una pantalla corta que explique por qué:
   - notificaciones;
   - «no optimizar batería»;
   - mostrar alertas sobre otras apps;
   - sonar aunque el celular esté en silencio.

**Cómo viaja la alerta** (pantalla 10):
- **Wifi de la casa:** aviso al instante, aunque no haya internet.
- **Internet (nube):** se usa cuando la familia está fuera de casa.
- Cada Sentinela envía un «estoy bien» cada minuto. Si pasan 2 minutos sin ese aviso, los receptores reciben «Sentinela sin conexión».

## 4. App del Receptor: 5 pestañas abajo

| Pestaña | Ícono | Para qué |
|---|---|---|
| **Inicio** | Casa | Estado de la casa y pausa rápida |
| **Equipos** | Celular | Sentinelas, receptores y vincular |
| **Horario** | Reloj | Vigilancia y carga |
| **Vecinos** | Personas | Vecinos de confianza y números de emergencia |
| **Alertas** | Campana, con un globo rojo si hay alertas nuevas | Historial |

Los **Ajustes** se abren con el ícono de controles, arriba a la derecha de Inicio. Ver el apartado 4.6.

### 4.1 Inicio (pantalla 4)

- **Encabezado:** «Hogar» y el nombre de la casa; a la derecha, el botón de Ajustes.
- **Tarjeta de estado** con un anillo de color. Puede estar en 4 estados:

  | Estado | Color | Texto de ejemplo |
  |---|---|---|
  | Vigilando | Verde | «Hasta las 4:00 a. m.» |
  | En reposo | Gris | «Se activa a las 11:00 p. m.» |
  | En pausa | Ámbar | «Vuelve en 8 min» |
  | ¡Alerta! | Rojo | — |

- **Etiquetas** debajo del estado: «3 SENTINELAS» y «2 VECINOS».
- **Botones:**
  - **Pausar 10 min**, para abrir la puerta de noche sin que suene;
  - **Detener** (pide confirmación), o **Activar ahora** si está en reposo.
- **Lista «Sentinelas»:** nombre, punto de estado, % de batería y temperatura. Se pinta de ámbar si la batería está baja o el equipo está caliente.
- **Última actividad:** un renglón, por ejemplo «1:05 a. m. · Vibración leve en Ventana cocina (lluvia)».

### 4.2 Equipos (pantalla 5)

- **Tres bloques, cada uno con su color:**
  - **Sentinelas · emisores** (azul);
  - **Receptores** (cian), donde el administrador lleva la etiqueta «ADMIN»;
  - **Vecinos de confianza** (violeta).
- **Botón +** arriba, que lleva a la pantalla Vincular (pantalla 2).
- **Detalle de un Sentinela**, al tocarlo:
  - nombre y lugar (puerta o ventana);
  - batería, temperatura y conexión (wifi de la casa o internet);
  - **sensibilidad** (baja, media o alta);
  - **Probar detección**;
  - **Repetir calibración**;
  - **Sirena en este equipo** (activada o no);
  - **Desvincular**.
- **Detalle de un Receptor:**
  - nombre;
  - qué alertas recibe (todas o solo las fuertes);
  - hacerlo administrador;
  - desvincular.

### 4.3 Horario (pantalla 6)

- **Barra «Así queda tu día»:** muestra 24 horas, de mediodía a mediodía, con la **carga en verde** y la **vigilancia en azul**.
- **Tarjeta Vigilancia:**
  - interruptor para activarla;
  - **Empieza 11:00 p. m.** y **Termina 4:00 a. m.** (selector de hora);
  - días de la semana **L M M J V S D**, que se pueden tocar uno por uno.
- **Tarjeta Carga diaria:**
  - interruptor;
  - de **9:00 a 10:00 p. m.**, de modo que termine una hora antes de vigilar;
  - «Avisarme 10 min antes»;
  - «Batería mínima para vigilar: 60 %». Si el equipo tiene menos, el receptor recibe el aviso «Carga la Ventana cocina antes de las 11».
  - «Avisar si pasa de 45 °C».
- **Modo fuera de casa:** vigila todo el día mientras la familia está de viaje y usa internet.

### 4.4 Vecinos (pantalla 7)

- **Texto de arriba:** «Reciben la alerta solo cuando alguien intenta entrar. Puedes agregar hasta 5.»
- **Lista de vecinos:** inicial en un círculo violeta, nombre, referencia («Casa de al lado», «Enfrente») e interruptor «recibe alertas».
  - Si el vecino aún no aceptó, aparece la etiqueta **PENDIENTE**.
- **Botón «Invitar vecino»:** se puede invitar por **WhatsApp** (envía un enlace), **código QR** o **contactos**.
- **Números de emergencia de Nicaragua**, con botón para llamar:
  - Policía Nacional **118**;
  - Bomberos **115**;
  - Cruz Roja **128**.
- **Lo que ve el vecino en su app:** una sola pantalla con las casas que lo invitaron y su estado. No ve horarios ni ajustes.

### 4.5 Alertas, el historial (pantalla 8)

- **Filtros:** Todas · Intentos · Leves · Sistema.
- **Tipos de evento:**

  | Tipo | Color | Ejemplo |
  |---|---|---|
  | Intento de entrada | Rojo | «Golpes fuertes y la puerta se abrió», con la etiqueta «2 VECINOS AVISADOS» |
  | Vibración leve | Ámbar | Lleva la pregunta **«¿Qué fue?»**: Lluvia · Viento · Animal · Fui yo. **Esto enseña al sistema a no dar falsas alarmas** |
  | Sistema | Ámbar o gris | Batería baja, sin conexión, calor |
  | Noche tranquila | Verde | «Vigilancia completa sin intentos» |

### 4.6 Ajustes (desde el ícono de Inicio)

- **Perfil:**
  - nombre del hogar;
  - cambiar el PIN de administrador.
- **Tipo de alarma:**
  - sonido;
  - volumen;
  - vibración;
  - «sonar aunque esté en silencio».
- **Sensibilidad general:** baja, media o alta.
- **Sirena en los Sentinelas:** al detectar un intento, el celular viejo suena fuerte para asustar al intruso.
- **Apariencia:** modo oscuro o claro.
- **Privacidad:**
  - «Sentinel no usa cámara ni micrófono»;
  - borrar el historial.
- **Ayuda:**
  - cómo instalar el soporte;
  - cuidados de la batería («no uses baterías infladas»).

## 5. App del Sentinela (emisor), pantalla 3

No tiene pestañas: es **una sola pantalla**, porque el celular está atornillado a la puerta.

- **Fondo negro puro** (`#04070D`), para gastar menos batería.
- **Arriba:**
  - etiqueta «SENTINELA · EMISOR»;
  - nombre: «Ventana cocina»;
  - hogar y tipo de conexión.
- **Centro:** el anillo de estado y **VIGILANDO** en verde (o EN REPOSO en gris, o CARGANDO en verde con un rayo), con «hasta las 4:00 a. m.».
- **Vibración en vivo:** una línea pequeña que se mueve, con la etiqueta NORMAL, LEVE o FUERTE.
- **Tres datos:** batería, temperatura y señal.
- **Abajo:**
  - el texto «La pantalla se apaga en 10 s»;
  - los botones **Probar** y **Ajustes 🔒**. Ajustes pide el PIN del administrador.

## 6. Pantalla de ¡ALERTA! (pantalla 9)

Sale en **pantalla completa** en todos los receptores y vecinos, y suena aunque el celular esté en silencio.

- **Fondo rojo** con un ícono de advertencia grande.
- **Texto:**
  - **¡INTENTO DE ENTRADA!**;
  - «Puerta principal»;
  - «2:41 a. m. · golpes fuertes y apertura».
- **Etiqueta:** «Doña Marta y Don José ya fueron avisados».
- **Botones, de más a menos importante:**
  1. **Llamar a la Policía · 118** (blanco, el más grande);
  2. **Sonar sirena en el Sentinela**;
  3. **Estoy en casa · fue falsa alarma**.

**Dos niveles de alerta:**
- **Leve** (vibración rara): solo una notificación normal a la familia.
- **Fuerte** (forcejeo o apertura): la pantalla roja, y avisa también a los vecinos.

## 7. Textos de estado (usar siempre los mismos)

| Estado | Texto |
|---|---|
| Vigilando | «Vigilando · hasta las 4:00 a. m.» |
| En reposo | «En reposo · se activa a las 11:00 p. m.» |
| En pausa | «En pausa · vuelve en 8 min» |
| Cargando | «Cargando · 64 %» |
| Batería baja | «Batería baja · 28 %» |
| Calor | «Muy caliente · 46 °C. Desconecta el cargador» |
| Sin conexión | «Sin conexión desde hace 3 min» |
| Alerta fuerte | «¡Intento de entrada!» |
| Alerta leve | «Vibración leve» |

---

## 8. Textos para pegar en Stitch

**Cómo usarlos:**
1. Pegar primero el **prompt base**.
2. Luego pedir **una pantalla a la vez**.
3. Si Stitch deja subir una imagen, subir el PNG de esa pantalla (carpeta `pantallas/`) junto con el texto.
4. Elegir **App (Mobile)**.

### Prompt base

```
Diseña una app móvil llamada "Sentinel" (Android). Convierte un celular viejo en un sensor de seguridad para el hogar: se atornilla en un soporte por dentro de una puerta o ventana y, con la pantalla apagada, detecta golpes, forcejeo y aperturas con el acelerómetro y el giroscopio. Las alertas llegan a los celulares de la familia y de vecinos de confianza. No usa cámara.
Una sola app con dos modos: "Sentinela" (emisor, el celular viejo) y "Receptor" (celular, tablet o computadora de la familia que recibe las alertas). Los vecinos de confianza reciben solo alertas fuertes.
Público: familias de Nicaragua de cualquier nivel económico. Todo el texto en español de Nicaragua, frases cortas y claras.
Estilo: modo oscuro, moderno, limpio, confiable; tarjetas con esquinas de 18 px, botones grandes de 48 px de alto, íconos de línea redondeados.
Colores: fondo #0B1220, tarjetas #131C2E, campos #1C2740, bordes #2A3654, texto #EAF0FB, texto secundario #94A3BF.
Azul Sentinel #2F6FEB para botones principales y #4C8DFF para íconos y Sentinelas; cian #22D3EE para Receptores; violeta #A78BFA para Vecinos; verde #22C55E para "Vigilando"; ámbar #F5A524 para avisos (batería, calor); rojo #FF5A5F y fondo #D92D2D solo para alertas de intento de entrada.
Tipografías: Sora para títulos, Inter para textos, JetBrains Mono para códigos y números de teléfono.
Barra de navegación inferior del Receptor con 5 pestañas: Inicio, Equipos, Horario, Vecinos, Alertas.
```

### Prompts por pantalla

**1. Elegir rol**
```
Pantalla de bienvenida de Sentinel. Arriba el logo (escudo con check en un cuadro con degradado azul a cian) y "Sentinel – Tu celular viejo cuida tu casa". Título "¿Qué será este dispositivo?" y subtítulo "Elige una opción. Puedes cambiarla después." Dos tarjetas grandes seleccionables: "Sentinela" con etiqueta azul "EMISOR", ícono de escudo y el texto "El celular viejo que va en el soporte de la puerta o ventana. Detecta golpes y aperturas." (seleccionada, con borde azul); "Receptor" con etiqueta cian "RECEPTOR", ícono de campana y el texto "Recibe las alertas: tu celular, la tablet o la computadora de la casa." Enlace violeta "Me invitaron como vecino de confianza". Abajo, botón secundario "Escanear el QR de la caja".
```

**2. Vincular**
```
Pantalla "Vincular equipo" (con flecha de regreso). Control segmentado: "Sentinela" (activo, azul) | "Receptor". Tarjeta con un código QR grande sobre fondo blanco, debajo el código "482 913" en JetBrains Mono grande y "El código vence en 4:32". Tarjeta con 3 pasos numerados: 1. En el celular viejo, abre Sentinel y elige Sentinela. 2. Escanea este código o escribe los 6 números. 3. Ponle nombre: «Puerta principal», «Ventana cocina». Abajo un punto verde y "Buscando en el wifi de la casa…".
```

**3. Pantalla del Sentinela (emisor)**
```
Pantalla única del celular que hace de Sentinela, fondo negro puro #04070D, sin barra de navegación. Etiqueta azul "SENTINELA · EMISOR", título "Ventana cocina", subtítulo "Hogar «Mi casa» · conectado por wifi". En el centro un anillo grande verde con un escudo con check y debajo "VIGILANDO" en verde y "hasta las 4:00 a. m.". Tarjeta "Vibración en vivo" con una línea de onda verde pequeña y la etiqueta "NORMAL". Tres tarjetas pequeñas: batería 82 %, temperatura 31 °C, señal "Bien". Abajo el texto "La pantalla se apaga en 10 s para ahorrar batería" y dos botones: "Probar" y "Ajustes" con candado.
```

**4. Inicio (Receptor)**
```
Pantalla Inicio del Receptor. Encabezado "Hogar" / "Mi casa" y botón redondo de ajustes. Tarjeta de estado con un ligero degradado verde: anillo verde con escudo, "Vigilando" en verde, "Hasta las 4:00 a. m.", etiquetas "3 SENTINELAS" (azul) y "2 VECINOS" (violeta); botones "Pausar 10 min" (azul) y "Detener" (secundario); nota pequeña "«Pausar» sirve para abrir la puerta de noche sin que suene la alarma". Sección "SENTINELAS": Puerta principal (Vigilando, 82 %, 31 °C), Ventana cocina (Vigilando, 64 %, 33 °C), Ventana cuarto (Batería baja en ámbar, 28 %). Renglón final con ícono de actividad ámbar: "1:05 a. m. · Vibración leve en Ventana cocina (lluvia)". Barra inferior con Inicio activo.
```

**5. Equipos**
```
Pantalla "Equipos" con botón + azul arriba a la derecha. Tres bloques con títulos de color: "SENTINELAS · EMISORES" (azul, contador 3): Puerta principal, Ventana cocina, Ventana cuarto (con "Batería baja · 28 %" en ámbar), cada uno con ícono de puerta o ventana en un cuadro azul. "RECEPTORES" (cian, 3): Mi celular (etiqueta ADMIN), Celular de mamá, Computadora de la sala. "VECINOS DE CONFIANZA" (violeta, 2): Doña Marta y Don José, "Solo reciben alertas fuertes". Barra inferior con Equipos activo.
```

**6. Horario**
```
Pantalla "Horario". Tarjeta "Así queda tu día" con una barra horizontal de 24 horas (de mediodía a mediodía): bloque verde de 9 a 10 p. m. (Carga) y bloque azul de 11 p. m. a 4 a. m. (Vigilancia), marcas 12 m., 6 p. m., 12 a. m., 6 a. m., 12 m., y leyenda. Tarjeta "Vigilancia" con ícono de luna e interruptor encendido: dos campos "Empieza 11:00 p. m." y "Termina 4:00 a. m.", y fila de días L M M J V S D seleccionados. Tarjeta "Carga diaria" con ícono de rayo verde, "9:00 – 10:00 p. m." e interruptor; filas "Avisarme 10 min antes" (interruptor), "Batería mínima para vigilar 60 %", "Avisar si pasa de 45 °C" (interruptor). Tarjeta "Modo fuera de casa – Vigila todo el día si la familia sale" con interruptor apagado. Barra inferior con Horario activo.
```

**7. Vecinos**
```
Pantalla "Vecinos de confianza". Texto: "Reciben la alerta solo cuando alguien intenta entrar. Puedes agregar hasta 5." Lista: Doña Marta (Casa de al lado, interruptor encendido), Don José (Enfrente, interruptor encendido), Karla (Invitación enviada…, etiqueta ámbar PENDIENTE); cada uno con su inicial en un círculo violeta. Botón violeta "Invitar vecino" y tres opciones pequeñas: WhatsApp, Código QR, Contactos. Sección "NÚMEROS DE EMERGENCIA": Policía Nacional 118, Bomberos 115, Cruz Roja 128, cada uno con un botón verde redondo de llamar. Barra inferior con Vecinos activo.
```

**8. Alertas**
```
Pantalla "Alertas" (historial). Filtros tipo chip: Todas (activo), Intentos, Leves, Sistema. Grupo "HOY": tarjeta roja "Intento de entrada – Puerta principal · 2:41 a. m. – Golpes fuertes y la puerta se abrió" con etiqueta violeta "2 VECINOS AVISADOS"; tarjeta ámbar "Vibración leve – Ventana cocina · 1:05 a. m." con la pregunta "¿Qué fue?" y chips Lluvia (seleccionado), Viento, Animal, Fui yo. Grupo "AYER": "Batería baja (28 %) – Ventana cuarto · 11:30 p. m." (ámbar) y "Sin conexión por 3 min – Ventana cuarto · 10:58 p. m." (gris). Barra inferior con Alertas activo y un globo rojo con 1.
```

**9. ¡Alerta! (pantalla completa)**
```
Pantalla de alarma a pantalla completa, fondo rojo #D92D2D con degradado radial más claro arriba. Ícono grande de advertencia blanco dentro de círculos concéntricos semitransparentes. Título "¡INTENTO DE ENTRADA!" en blanco, Sora extra negrita. "Puerta principal" y "2:41 a. m. · golpes fuertes y apertura". Píldora oscura semitransparente: "Doña Marta y Don José ya fueron avisados". Abajo, tres botones a todo el ancho: "Llamar a la Policía · 118" (fondo blanco, texto rojo, el más grande), "Sonar sirena en el Sentinela" (oscuro semitransparente), "Estoy en casa · fue falsa alarma" (borde blanco).
```

---

## 9. Pendientes que el diseño debe resolver con el equipo

- **Nombre del hogar y PIN:** decidir si se piden al crear el hogar o después.
- **Vecinos:**
  - Si el vecino no tiene la app, ¿recibe la alerta por SMS o Telegram? Para el prototipo basta con la app.
  - La app del vecino puede ser la misma app en el modo «Vecino».
- **Logo:** el de la maqueta es temporal (escudo con check). Si hacen uno propio, que funcione en un cuadrito de 48 px.
- **Para el video del Rally:** graben la pantalla de **¡Alerta!** en el receptor justo cuando alguien mueve la maqueta. Es la prueba más clara de que funciona.
