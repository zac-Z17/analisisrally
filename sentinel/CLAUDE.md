# Sentinel: instrucciones para Claude Code

Habla con el equipo en **español**, claro y sin tecnicismos. Es el equipo Nanobots (UNI-RUPAP, Nicaragua) en el Rally Latinoamericano de Innovación 2026. Ya eligieron el desafío 02 y la idea **Sentinel**; ahora toca construir la app Android.

Todo el contexto del proyecto está en estos archivos (léelos antes de trabajar):

@README.md
@prototipo-web/README.md

Reglas rápidas:
- La especificación de diseño está en `diseno/diseno_app_sentinel.md`. Léela cuando toques la interfaz.
- Si cambias `prototipo-web/index.html`, corre las pruebas antes de decir que funciona:
  1. `cd prototipo-web/pruebas && npm install` (una vez).
  2. `npm run relay` en otra terminal.
  3. `npm run e2e && npm run ajustes`. Deben terminar sin errores de consola.
- Para Android: compila con `./gradlew assembleDebug`. Necesitas el SDK de Android Studio y JDK 17 o superior. No digas que funciona sin haberlo compilado.
- Trabaja en la rama `claude/fervent-galileo-gdwbzo`. Haz `git pull` antes de empezar y commits pequeños en español. No subas secretos ni APK grandes.
- Primera vez: haz la tarea de la sección 9 del README (analizar y proponer un plan) y **espera la aprobación del equipo** antes de crear el proyecto Android.
