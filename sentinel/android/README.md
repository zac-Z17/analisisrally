# Sentinel: app Android nativa

Es la versión Android del prototipo web (`../prototipo-web/index.html`), hecha con **Kotlin y Jetpack Compose**. Tiene las mismas pantallas, textos y colores, y usa el mismo protocolo de mensajes. Por eso **la app y la página web se entienden entre sí**: por ejemplo, un Sentinela con la app puede avisarle a un Receptor que usa la página.

## Qué resuelve frente al prototipo web

| Límite del prototipo web | En la app |
|---|---|
| El Sentinela debía tener la pantalla encendida y la página abierta | Un **servicio en primer plano** con `PARTIAL_WAKE_LOCK` lee los sensores **con la pantalla apagada** |
| Los receptores debían tener la página abierta | El mismo servicio escucha las alertas **con la app cerrada** y abre la pantalla roja **sobre la pantalla de bloqueo** |
| Sin temperatura | Lee la temperatura de la batería y avisa si pasa del máximo configurado («Avisar si pasa de 45 °C») |
| La sirena no sonaba con el celular en silencio | La sirena suena por el canal de **alarma**, que no se silencia con el timbre |
| Al reiniciar el celular había que volver a abrir la página | Vuelve a arrancar sola al encender el celular |

**Sin cámara ni micrófono:** el QR se lee con *Google Code Scanner*, que no pide permiso de cámara.

## Cómo compilar

**Requisitos:**
- JDK 17 o superior;
- Android SDK 35;
- Android Studio, o solo las herramientas de línea de comandos.

```bash
cd sentinel/android
./gradlew assembleDebug        # APK en app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest    # 37 pruebas: detector, calibración, horario, protocolo, vinculación, red
```

**Para instalarla en un celular con depuración USB:**

```bash
./gradlew installDebug
```

- `local.properties` (con la ruta del SDK) no se sube al repositorio. Android Studio lo crea solo.
- La versión de prueba (*debug*) acepta un servidor de mensajes por `http`, para usar el relay local de `prototipo-web/pruebas`. La versión final (*release*) solo acepta `https`.

## Arquitectura

Es una arquitectura limpia por capas. El dominio no depende de Android: es Kotlin puro y se prueba sin celular.

```
ni.uni.nanobots.sentinel
├─ domain/                    ← reglas del negocio (sin Android)
│  ├─ modelo/                 Config, Equipo, Alerta, Mensaje (protocolo), EstadoApp
│  ├─ reglas/                 Detector, Calibrador, Horario, Umbrales: funciones puras con pruebas
│  ├─ puertos/                interfaces: Mensajeria, AlmacenEstado, Efectos, FuenteSensores, Reloj
│  └─ hogar/                  RepositorioEstado (única fuente de verdad), Coordinador (protocolo),
│                             Vinculador (código de 6 números), Vigilante (sensores → detector → alerta)
├─ data/                      ← implementaciones de los puertos
│  ├─ json/                   codificadores con validación de todo lo que llega por la red (= limpiarCfg)
│  ├─ red/NtfyMensajeria      POST + flujo JSON de ntfy, con reconexión automática
│  ├─ local/AlmacenDataStore  DataStore
│  └─ sensores/               acelerómetro lineal (o con gravedad filtrada) + giroscopio
├─ servicio/                  ServicioHogar (primer plano), sirena (AudioTrack), vibración,
│                             notificaciones, arranque al encender el celular
├─ di/Contenedor              inyección de dependencias manual (una instancia de cada cosa)
└─ ui/                        Compose + ViewModels (flujo de datos en una sola dirección)
   ├─ tema/                   paleta, tipografías (Sora, Inter, JetBrains Mono) e íconos Lucide
   ├─ comp/                   componentes: Tarjeta, Boton, Chip, Interruptor, Anillo, QR…
   ├─ inicio/                 bienvenida, crear hogar y unirse
   ├─ receptor/               5 pestañas (Inicio, Equipos, Horario, Vecinos, Alertas) y paneles
   ├─ sentinela/              pantalla negra, gráfica en vivo, calibración de 3 pasos, ajustes con PIN
   ├─ vecino/                 pantalla del vecino de confianza
   └─ alarma/                 ¡INTENTO DE ENTRADA! a pantalla completa
```

**Cómo viaja una alerta:**

1. `FuenteSensoresAndroid` entrega las lecturas.
2. `Vigilante` se las pasa al `Detector`.
3. `Coordinador.emitirEvento()` la envía por `Mensajeria` (ntfy).
4. En el receptor, `Coordinador.alMensaje()` recibe el mensaje y llama a `Efectos.mostrarAlarma()`.
5. Se ven la notificación a pantalla completa y `AlarmaActivity`, y suena la sirena.

**Decisiones:**
- **El protocolo y el algoritmo son copias exactas del prototipo.** Están en `sentinel/README.md`, sección 4. Las pruebas `CodecTest` y `DetectorTest` lo comprueban.
- **Inyección manual en vez de Hilt:** el proyecto es pequeño, así compila más rápido y es más fácil de leer.
- **Un solo `EstadoApp` en un `StateFlow`:** las pantallas, el servicio y la red ven siempre lo mismo.
- **Mensajes validados a mano con `JsonObject` (sin reflexión):** si un campo viene mal, se usa el valor por defecto, igual que `limpiarCfg()`.

## Pruebas

| Prueba | Qué comprueba |
|---|---|
| `DetectorTest` | Apertura, golpe muy fuerte, forcejeo (3 golpes en 4 s), espera de 20 s, vibración leve, modo prueba |
| `CalibradorTest` | Los 3 pasos y sus límites (`clamp`) |
| `HorarioTest` | La noche pertenece al día en que empieza, la pausa, los modos manuales y el próximo inicio |
| `CodecTest` | Lee mensajes del prototipo web, hace la ida y vuelta de todos los tipos y rechaza la basura |
| `CoordinadorTest` | Alarma sin repetidos, vecinos pausados, gana la configuración más nueva, se ignoran las órdenes viejas, «¿Qué fue?» y **vincular un Sentinela y recibir su alerta de punta a punta** |
| `NtfyMensajeriaTest` | El POST y la lectura del flujo de ntfy, contra un servidor falso |

## Pendiente

- **Probar en celulares reales viejos:** algunos fabricantes (Xiaomi, Huawei, Tecno) cierran los servicios aunque estén en primer plano. Hay que pedir «no optimizar batería» (la app ya lo pide) y, en algunos, activar el «inicio automático».
- **Wifi local sin internet** (`NsdManager` + socket): no está hecho; hoy todo pasa por ntfy.
- **Notificaciones push con FCM:** serían mejores que una conexión abierta para los receptores, pero requieren un servidor propio.
- **APK de versión final (*release*):** hace falta una llave de firma, que **no se sube al repositorio**.

## Licencias de terceros

- **Íconos:** Lucide (ISC).
- **Tipografías:** Sora, Inter y JetBrains Mono (SIL Open Font License 1.1), incluidas en `app/src/main/res/font/`.
