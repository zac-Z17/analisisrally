# Sentinel: guía del proyecto para el agente del IDE

> **Para el agente:** lee este archivo completo antes de tocar código. Habla con el equipo en **español**, claro y sin tecnicismos.
>
> Primero **analiza** el proyecto y entrega un resumen con un plan. **No escribas código hasta que el equipo apruebe el plan** (ver la sección 9).

## 1. Qué es Sentinel

- **Idea:** Sentinel convierte un **celular viejo** en un **sensor de seguridad para la casa**.
  - El celular va en un soporte atornillado por dentro de una puerta o ventana.
  - Con la pantalla apagada usa el **acelerómetro** (golpes, forcejeo) y el **giroscopio** (la puerta gira, es decir, se abre).
- **Alertas:** llegan en segundos a los **celulares de la familia (receptores)** y a los **vecinos de confianza**.
- **Privacidad:** no usa cámara ni micrófono.
- **Producto:** un soporte de bajo costo (unos US$3 de costo; se vende a US$8) con un QR en la caja, más la app.

**Contexto:**
- Es el proyecto del equipo **Nanobots** (UNI-RUPAP, Managua, Nicaragua) para el **Rally Latinoamericano de Innovación 2026**, desafío **02 «Sensores al servicio de la comunidad»**.
- El equipo son 4 estudiantes de Ingeniería en Sistemas, 1 de Arquitectura y 1 de Economía.
- Lo que se entrega al Rally:
  - un video de 2 min con un prototipo a escala de material reciclable;
  - un reporte en PDF;
  - un TikTok.
- La app es la demostración técnica. **El plazo es corto (horas)**: prioriza lo que sirva para el video y para mostrar que es viable.

## 2. Qué hay en esta carpeta

```
sentinel/
├─ README.md                  ← este archivo
├─ android/                   ← APP ANDROID NATIVA (Kotlin + Compose); ver android/README.md
├─ prototipo-web/             ← prototipo web (un solo archivo), se sigue usando para el video
│  ├─ index.html              ← toda la app: HTML + CSS + JavaScript (sin framework ni build)
│  ├─ README.md               ← cómo usarla y sus límites
│  └─ pruebas/                ← pruebas automáticas (Playwright) + servidor de mensajes de prueba
│     ├─ package.json
│     ├─ relay_prueba.mjs     ← sirve la app en /app/ e imita la API de ntfy en /r/
│     ├─ prueba_e2e.mjs       ← receptor + sentinela + vecino: vincular, calibrar, alarma, «¿Qué fue?»
│     └─ prueba_ajustes.mjs   ← PIN, simulación, pantalla oscura, vecinos
├─ diseno/                    ← diseño de la app
│  ├─ diseno_app_sentinel.md  ← ESPECIFICACIÓN: paleta, pestañas, flujos, textos (léela)
│  ├─ sentinel_lamina.png     ← las 10 pantallas en una imagen
│  ├─ pantallas/*.png         ← cada pantalla suelta
│  ├─ fuente/                 ← código de la maqueta (HTML) y script que la renderiza
│  └─ stitch/                 ← pantallas que generó Google Stitch (screen.png + code.html con Tailwind) y DESIGN.md
├─ reporte/                   ← borradores del reporte del Rally (problema, solución, costos, referencias)
└─ video/                     ← guion del video de 2 min: guion_pitch_centinel_v2.md (versión del equipo corregida) y guion_video_pitch.md (borrador inicial)
```

**Estado honesto del proyecto:**
- El **prototipo web** funciona de punta a punta y se usa para validar la idea y grabar el video.
- La **app Android nativa** (`android/`, fase 2 de la sección 5) compila y pasa 37 pruebas automáticas. **Aún falta probarla en celulares reales.**
  - Usa el mismo protocolo que la página web, así que se pueden mezclar.
  - Detecta con la pantalla apagada y recibe alertas con la app cerrada.
  - Detalles y pendientes en `android/README.md`.

## 3. Cómo correr el prototipo web

- **En celulares:** se necesita **https**, porque sin https el navegador no entrega los sensores.
  - Usen el enlace de GitHub Pages (`https://zac-z17.github.io/analisisrally/sentinel/prototipo-web/`, si está activado en Settings → Pages) o el de raw.githack que les dio Claude.
  - Usen Chrome en Android.
- **En la computadora:**
  ```bash
  cd sentinel/prototipo-web/pruebas
  npm install
  npm run relay          # deja corriendo: http://127.0.0.1:8090/app/index.html?relay=http://127.0.0.1:8090/r
  ```
  - Abran 2 pestañas con `&perfil=a` y `&perfil=b` para simular 2 equipos.
  - Sin sensores, usen «Simular golpe fuerte» en los Ajustes del Sentinela (PIN).
- **Pruebas automáticas:** con el relay corriendo, en otra terminal:
  ```bash
  npx playwright-core install chromium     # una vez (o usar CHROMIUM_PATH=/ruta/a/chrome)
  npm run e2e        # guarda capturas en pruebas/capturas/
  npm run ajustes
  ```
  - Las pruebas mandan movimientos falsos con `new DeviceMotionEvent(...)`.
  - **Deben terminar con «errores de consola: ninguno».**

## 4. Cómo funciona el prototipo (para poder portarlo)

`index.html` está dividido en secciones con comentarios `// ---------- nombre ----------`:

| Sección | Qué hace |
|---|---|
| utilidades, íconos | Ayudas de formato (horas en «11:00 p. m.») e íconos Lucide en SVG |
| estado guardado | Objeto `S`, que se guarda en `localStorage`. `cfgBase()` define la configuración compartida. `limpiarCfg()` valida lo que llega por la red |
| mensajería entre equipos (`Bus`) | Publica con `fetch POST` y escucha con `EventSource` (SSE) en un servidor **ntfy** (por defecto `https://ntfy.sh`); entre pestañas usa `BroadcastChannel` |
| sonido y vibración | Sirena con Web Audio (oscilador + LFO) y `navigator.vibrate` |
| vigilancia y horario | `enHorario()`, `estadoVig()` (vigilando, reposo o pausa), `proximoInicio()` y `textoEstado()` |
| batería | `navigator.getBattery()` |
| detección (`Sensor`) | Algoritmo de detección (ver abajo) |
| calibración (`Cal`) | Los 3 pasos que ajustan los umbrales |
| eventos y mensajes | `alMensaje()` atiende cada tipo de mensaje, `emitirEvento()`, `enviarEstado()`, `latido()` |
| vinculación | `abrirVincular()` (el administrador muestra el QR y el código) y `unirse()` (el equipo nuevo) |
| alarma | Pantalla roja completa, sirena, «falsa alarma» |
| acciones (`ACC`) | Todos los botones usan `data-act="accion:argumento"` |
| vistas | `vBienvenida`, `vUnir`, `vReceptor` (pestañas `vInicio`, `vEquipos`, `vHorario`, `vVecinos`, `vAlertas`), `vHoja` (Vincular, Detalle, Ajustes), `vSentinela`, `vAjustesSentinela`, `vCalibrar`, `vVecino` |

### Roles

| Rol | Equipo | Qué hace |
|---|---|---|
| **Sentinela** (emisor) | El celular viejo en el soporte | Detecta y envía |
| **Receptor** | Celular, tablet o computadora de la familia | Recibe y controla. El primero que crea el hogar es el **administrador** y tiene un PIN de 4 dígitos |
| **Vecino** | El celular del vecino | Solo recibe los intentos de entrada |

### Canales (temas ntfy)

- `snt-<idHogar>`: id aleatorio de 12 caracteres. Por aquí va todo el tráfico del hogar.
- `snt-v-<código de 6 dígitos>`: solo para vincular; dura 5 min.

### Mensajes

Todos son JSON con `{ tipo, mid, de, t }`: `mid` es el id del mensaje, `de` el id del equipo y `t` la hora en ms.

| tipo | Lo envía | Campos | Para qué |
|---|---|---|---|
| `solicitud` | Equipo nuevo, en `snt-v-…` | `rol, nombre, lugar, ref` | Pedir unirse |
| `bienvenida` | Administrador, en `snt-v-…` | `para, hogar{id,nombre,pin}, cfg` | Aceptar al equipo |
| `hola` | Cualquiera, al arrancar | `rol, cfgv` | Pedir que todos reporten |
| `estado` | Sentinela (cada 60 s y al cambiar) | `nombre, lugar, vig, bat, carga, sensores, cfgv` | Latido y estado |
| `presencia` | Receptor y vecino (cada 120 s) | `rol, nombre, ref, admin, cfgv` | Latido |
| `cfg` | Receptor (o Sentinela con PIN) | `cfg` | Configuración compartida. **Gana la de `cfg.v` más alta** |
| `evento` | Sentinela | `clase` (`intento`, `leve`, `prueba`, `bateria`, `calor`), `detalle, pico, equipo{id,nombre}` | Alertas |
| `cmd` | Receptor | `cmd` (`sirena`, `callar`, `probar`, `resuelta`, `desvincular`), `para?, alerta?, falsa?, desde` | Órdenes |
| `etiqueta` | Receptor | `alerta, que` (`lluvia`, `viento`, `animal`, `yo`, `falsa`) | Respuesta a «¿Qué fue?» |

**Reglas de los mensajes:**
- Si un Sentinela no se reporta en 150 s, los receptores muestran «Sin conexión».
- Los comandos con más de 90 s se ignoran.

### Configuración compartida (`cfg`)

| Campo | Qué guarda |
|---|---|
| `v` | Versión, con la hora en ms |
| `modo` | `auto`, `on` u `off` |
| `modoHasta` | Hasta cuándo dura el modo elegido a mano |
| `pausaHasta` | Fin de la pausa |
| `auto` | Si el horario automático está activo |
| `inicio`, `fin` | Horario de vigilancia, `'23:00'` a `'04:00'`. La noche pertenece al día en que empieza |
| `dias` | 7 valores, de lunes a domingo |
| `carga`, `cargaIni`, `cargaFin` | Carga diaria, `'21:00'` a `'22:00'` |
| `aviso10` | Aviso 10 min antes de la carga |
| `batMin` | Batería mínima para vigilar: 60 |
| `avisoTemp`, `tempMax` | Aviso de temperatura: 45 |
| `fuera` | Modo fuera de casa |
| `equipos` | Por cada equipo: `{ nivel: 'baja'/'media'/'alta', ajuste: 1–2.5, sirena: bool }` |
| `vecinosOff` | Por cada vecino: `true` si no recibe alertas |

### Algoritmo de detección (`Sensor`)

**Medidas que usa:**
- `a`: tamaño de la **aceleración lineal**, en m/s².
  - Si no hay aceleración lineal: la aceleración con gravedad menos un filtro paso bajo de la gravedad (α = 0,9).
- `r`: tamaño de la velocidad de giro (`rotationRate`), en °/s.
- `ang`: giro acumulado.
  - Mientras `r > 4 °/s`, `ang += r·dt`.
  - Si no, baja 12 °/s.

**Umbrales de fábrica:**

| Umbral | Valor | Uso |
|---|---|---|
| `leve` | 0,8 m/s² | Vibración leve |
| `fuerte` | 2,5 m/s² | Golpe |
| `muy` | 8 m/s² | Golpe muy fuerte |
| `ang` | 15° | Apertura |

**Sensibilidad:**

| Nivel | Multiplica los umbrales de aceleración por | Giro |
|---|---|---|
| Alta | × 0,6 | 10° |
| Media | × 1 | 15° |
| Baja | × 1,6 | 25° |

- El umbral leve además se multiplica por `ajuste`.
- `ajuste` sube un 15 % cada vez que la familia responde «lluvia», «viento» o «animal» en «¿Qué fue?», hasta un máximo de 2,5.

**Cuándo hay un INTENTO:**
- cuando `ang` supera el umbral de giro (**apertura**);
- cuando `a` supera `muy`;
- cuando hay **3 golpes** (`a > fuerte`, con 180 ms o más entre uno y otro) en menos de 4 s (**forcejeo**).

Después de un intento hay 20 s de espera.

**Cuándo hay una vibración LEVE:**
- si `a` supera `leve`, se espera 2,5 s;
- si en ese tiempo no hubo intento, se envía `leve`;
- como máximo una cada 30 s.

**Calibración (`Cal`):**

| Paso | Qué hace el usuario | Qué se calcula |
|---|---|---|
| 1 | Deja la puerta quieta 5 s | `leve = clamp(p95·5, 0,35, 1,6)` |
| 2 | Abre y cierra (10 s) | `ang = clamp(giroMáx·0,45, 8, 30)` |
| 3 | Da 3 golpes suaves | `fuerte = clamp(mediana(picos)·1,25, 1,2, 6)` |

Al final, `muy = max(fuerte·3, 6)`.

## 5. Meta: app Android (plan recomendado)

### Fase 1, para el Rally: la app web dentro de Android con **Capacitor**

Es lo más rápido para tener un **APK instalable** sin reescribir lo que ya funciona y ya está probado.

```bash
# en sentinel/  (Node 18+, JDK 17+, Android Studio con SDK 34/35)
mkdir app && cd app && npm init -y
npm i @capacitor/core @capacitor/android && npm i -D @capacitor/cli
npx cap init Sentinel ni.uni.nanobots.sentinel --web-dir www
mkdir www && cp ../prototipo-web/index.html www/
npx cap add android
npx cap open android          # abre Android Studio → Run en un celular con depuración USB
```

- Capacitor sirve la app desde `https://localhost`, que es un contexto seguro, así que los sensores funcionan.
- Mantener la pantalla encendida en el Sentinela: plugin `@capacitor-community/keep-awake`, o `FLAG_KEEP_SCREEN_ON` en `MainActivity`.
- **Límite que sigue en esta fase:** si la pantalla se apaga o la app pasa a segundo plano, el WebView se pausa. Eso se resuelve en la fase 2.

### Fase 2, el producto: servicio nativo en Kotlin

La detección del Sentinela debe vivir en un **servicio en primer plano** (Foreground Service) nativo. Puede ser:
- un **plugin de Capacitor** escrito en Kotlin (se conserva la interfaz web);
- o una **app nativa con Kotlin y Jetpack Compose**.

Estructura sugerida si se hace nativa:

```
app/src/main/java/ni/uni/nanobots/sentinel/
├─ ui/            (Compose: bienvenida, vincular, inicio, equipos, horario, vecinos, alertas, alarma, sentinela, calibración)
├─ sentinela/     (SentinelService: Foreground Service + SensorManager + algoritmo de Detector + Calibrador)
├─ red/           (Mensajería: interfaz común; NSD/mDNS + WebSocket para wifi local; FCM o servidor para internet)
├─ datos/         (Room para alertas y equipos; DataStore para la configuración)
├─ horario/       (AlarmManager: inicio y fin de la vigilancia, aviso de carga)
└─ alarma/        (notificación con full-screen intent + sonido en canal de alarma)
```

**Detalles técnicos importantes:**
- **Sensores:**
  - `Sensor.TYPE_LINEAR_ACCELERATION` y `TYPE_GYROSCOPE` con `SENSOR_DELAY_GAME`.
  - Si el celular no tiene sensor lineal, usar `TYPE_ACCELEROMETER` y restar la gravedad con un filtro, como en el prototipo.
  - Algunos celulares baratos no tienen giroscopio: entonces se detectan solo los golpes. Se puede probar `TYPE_ROTATION_VECTOR` o el magnetómetro como brújula.
- **Pantalla apagada:**
  - Foreground Service con notificación fija, más un `PARTIAL_WAKE_LOCK` durante el horario.
  - Pedir que la app no use la optimización de batería (`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`).
  - En Android 14 o superior hay que declarar el tipo de servicio: `foregroundServiceType="specialUse"` con su `PROPERTY_SPECIAL_USE_FGS_SUBTYPE`.
- **Batería y temperatura:** el intent fijo `ACTION_BATTERY_CHANGED` trae `EXTRA_TEMPERATURE` en décimas de °C. Avisar si pasa de `tempMax`.
- **Alarma en el receptor:**
  - notificación de alta prioridad con `setFullScreenIntent` (permiso `USE_FULL_SCREEN_INTENT`) y canal con sonido de alarma;
  - en Android 13 o superior, pedir `POST_NOTIFICATIONS`.
- **QR:** usar **Google Code Scanner** (`play-services-code-scanner`). **No pide permiso de cámara**, así se cumple lo de «sin cámara».
- **Red:**
  - **Wifi local:** `NsdManager` (mDNS) para descubrir equipos y un socket o WebSocket para avisar al instante sin internet.
  - **Internet:** Firebase Cloud Messaging, o seguir con ntfy con un servidor propio.
  - Mantener el mismo protocolo de mensajes de la sección 4.
- **Permisos esperados:**
  - `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`, `CHANGE_WIFI_MULTICAST_STATE`;
  - `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `WAKE_LOCK`;
  - `POST_NOTIFICATIONS`, `USE_FULL_SCREEN_INTENT`;
  - `SCHEDULE_EXACT_ALARM` o `USE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, `VIBRATE`.
  - **Sin `CAMERA` ni `RECORD_AUDIO`.**
- **Versiones:**
  - `minSdk 24` (Android 7), porque los celulares viejos son el público.
  - `targetSdk 35`.

## 6. Diseño (no cambiarlo sin consultar)

- **Especificación completa:** `diseno/diseno_app_sentinel.md`. Las referencias visuales están en `diseno/sentinel_lamina.png` y `diseno/stitch/`.
- **Modo oscuro por defecto.**
- **Paleta:**

  | Uso | Color |
  |---|---|
  | Fondo | `#0B1220` |
  | Tarjetas | `#131C2E` |
  | Campos | `#1C2740` |
  | Borde | `#2A3654` |
  | Texto | `#EAF0FB` |
  | Texto secundario | `#94A3BF` |
  | Azul Sentinel | `#2F6FEB` (botones) / `#4C8DFF` (íconos) |
  | Receptores | Cian `#22D3EE` |
  | Vecinos | Violeta `#A78BFA` |
  | «Vigilando» | Verde `#22C55E` |
  | Atención | Ámbar `#F5A524` |
  | Intento de entrada | Rojo `#FF5A5F` / fondo `#D92D2D` (solo para esto) |

- **Tipografías:** Sora (títulos), Inter (texto) y JetBrains Mono (códigos y números).
- **Navegación del receptor:** 5 pestañas, Inicio · Equipos · Horario · Vecinos · Alertas.
- **Pantalla del Sentinela:** una sola, con fondo negro puro.
- **Ojo con Stitch:** algunos textos que generó **están mal** y no deben copiarse:
  - «cámaras», «micrófonos»;
  - «pestillo magnético», «sensor magnético»;
  - «Alarma sonora 100 dB»;
  - «8 horas de vigilancia».
  
  Sentinel no usa cámara, micrófono ni imán. Los textos correctos están en `prototipo-web/index.html` y en la especificación.
- **Números de emergencia de Nicaragua:**

  | Servicio | Número |
  |---|---|
  | Policía | 118 |
  | Bomberos | 115 |
  | Cruz Roja | 128 |

## 7. Reglas del Rally que afectan al proyecto

- **El video** (máximo 2 min) debe mostrar un **prototipo a escala de material reciclable movido por una persona**.
  - La toma clave: una maqueta de cartón con el celular; alguien mueve la «puerta» y en otro celular aparece la pantalla roja de alerta.
- **Uso de IA:**
  - Descalifican por **«abuso de inteligencia artificial»** y por plagio.
  - El código puede hacerse con ayuda de IA, pero **los textos del reporte y del video los redacta el equipo con sus palabras**.
  - En las referencias se declaran las IA usadas: Claude, Gemini y Stitch.
- **Dominio público:** todo lo que se entrega pasa a ser de dominio público.

## 8. Reglas de trabajo para el agente

- **Idioma:** todos los textos de la interfaz en **español de Nicaragua**, con frases cortas. Los nombres de código pueden quedar como están (en español).
- **No romper el prototipo web**: es lo que se usa para el video. Si lo cambias, corre `npm run e2e` y `npm run ajustes`; deben terminar sin errores.
- **Seguridad:**
  - No subas secretos al repo: `google-services.json` con claves privadas, contraseñas, keystores.
  - Valida todo lo que llegue por la red, como hace `limpiarCfg()`.
- **No inventes datos:** ni precios, ni cifras, ni nombres. Si falta algo, pregúntalo.
- **Commits y comprobación:** commits pequeños y en español. Antes de decir que algo funciona, compílalo (`./gradlew assembleDebug`) y pruébalo en un celular real.

## 9. Primera tarea para el agente

1. Lee este README, `prototipo-web/README.md`, `diseno/diseno_app_sentinel.md` y `prototipo-web/index.html`.
2. Entrega al equipo un **resumen corto**:
   - qué hace la app;
   - cómo está organizada;
   - riesgos técnicos (sensores con la pantalla apagada, batería, falsas alarmas);
   - qué falta para que sea una app Android.
3. Propón un **plan con tiempos**: fase 1 con Capacitor (para tener un APK hoy) y fase 2 con el servicio nativo.
4. **Espera la aprobación del equipo** antes de crear el proyecto Android.
