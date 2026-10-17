# Sentinel: prototipo funcional

Es una app web que funciona en el navegador del celular. Usa el diseño de Stitch y la guía `diseno_app/diseno_app_sentinel.md`. Todo está en un solo archivo: `index.html`.

## Cómo abrirla

- **Enlace directo (ya funciona):** el que les pasó Claude en el chat, que empieza con `https://raw.githack.com/zac-Z17/analisisrally/...`. Ese enlace apunta a una versión fija; si se actualiza el prototipo, hay que pedir el enlace nuevo.
- **Enlace estable con GitHub Pages:**
  1. En GitHub, ir a **Settings → Pages → Build and deployment → Deploy from a branch**.
  2. Elegir la rama `claude/fervent-galileo-gdwbzo`, carpeta `/ (root)`, y guardar.
  3. Al minuto queda en `https://zac-z17.github.io/analisisrally/prototipo/`.
- **Requisitos:** usar **Chrome en Android**, porque el sitio debe ser https para que el navegador entregue los sensores. En iPhone, Safari pide permiso para los sensores.

## Prueba con 2 celulares (5 minutos)

1. **Celular de la familia (Receptor):**
   - elegir **Receptor → Crear mi hogar**;
   - escribir el nombre del hogar y un PIN de 4 números;
   - se abre «Vincular equipo» con un QR.
2. **Celular viejo (Sentinela):**
   - escanear ese QR con la cámara (abre la app con el código ya puesto) o abrir el enlace y escribir el código;
   - ponerle nombre y elegir puerta o ventana;
   - hacer la **calibración** de 3 pasos: quieta, abrir y cerrar, y 3 golpes suaves.
3. **En el Receptor:** tocar **Activar ahora**. El Sentinela pasa a **VIGILANDO**.
4. **Probar:**
   - Golpear fuerte 3 veces o abrir la puerta: el Receptor muestra **¡INTENTO DE ENTRADA!** con sirena.
   - El Sentinela también suena (se puede apagar la sirena en los detalles del equipo).
5. **Opcional, un tercer celular como vecino:** en el Receptor ir a **Vecinos → Invitar vecino**. En el otro celular elegir «Me invitaron como vecino de confianza» y escribir el código.

**Sin celulares extra:** el botón **Ver demostración con datos de ejemplo** muestra la app llena y deja simular un intento de entrada.

## Qué hace

| Parte | Funciona en el prototipo |
|---|---|
| Elegir rol, crear hogar, vincular por QR o código de 6 números (vence en 5 min) | Sí |
| Detección con acelerómetro (golpes, forcejeo = 3 golpes fuertes en 4 s, vibración leve) y giroscopio (apertura) | Sí |
| Calibración de 3 pasos y sensibilidad baja, media o alta | Sí |
| Alerta en pantalla completa con sirena y vibración en receptores y vecinos | Sí |
| Sonar la sirena del Sentinela desde el Receptor; marcar «falsa alarma» (se cierra en todos) | Sí |
| Horario (11 p. m.–4 a. m.), días, pausa de 10 min, activar o detener, modo fuera de casa | Sí |
| Carga diaria con aviso 10 min antes y batería mínima | Sí. La batería se lee en Chrome Android |
| «¿Qué fue?» (lluvia, viento, animal): el Sentinela baja su sensibilidad a vibraciones leves | Sí |
| Aviso de «Sentinela sin conexión» si deja de reportar por más de 2 min | Sí |
| Vecinos de confianza (hasta 5), desde contactos o a mano, con invitación por WhatsApp | Sí |
| Ajustes del Sentinela protegidos con PIN y botones para simular golpe o apertura | Sí |

## Límites del prototipo web (la app Android los resuelve)

- **El Sentinela debe tener la página abierta y la pantalla encendida.** La app la oscurece sola, pero el navegador pausa los sensores si se apaga.
  - La app Android usará un servicio en segundo plano (Foreground Service) con la pantalla apagada.
- **Los receptores deben tener la página abierta** para recibir la alerta. La app Android usará notificaciones push.
- **Hace falta internet:**
  - las alertas viajan por el servicio gratuito de mensajes **ntfy.sh**, por un canal con nombre aleatorio para cada hogar;
  - el aviso por wifi local sin internet necesita la app Android;
  - para la versión final se usará un servidor propio. El servidor se cambia en Ajustes o con `?relay=https://otro-servidor`.
- **Sin temperatura ni magnetómetro:** el navegador no los entrega, por eso la app muestra «N/D» en temperatura. El giroscopio cubre la apertura.
- **Probar en una sola computadora:** abrir dos pestañas con `?perfil=a` y `?perfil=b`, y en la del Sentinela usar «Simular golpe fuerte» (en Ajustes, PIN).

## Archivos

- `index.html`: la app completa (HTML, CSS y JavaScript, sin dependencias salvo el generador de QR de cdnjs y las fuentes de Google).
- `../diseno_app/stitch/`: las pantallas que generó Stitch, como referencia.
