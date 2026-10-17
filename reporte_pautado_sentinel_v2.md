# Reporte de pautado de resolución del desafío — BORRADOR v2 (Sentinel)

> **Para el equipo:** este es un borrador para evaluar y mejorar.
> - Reescríbanlo con sus palabras antes de entregarlo (regla anti-IA).
> - Completen lo marcado **[PENDIENTE]** y confirmen lo marcado **[VERIFICAR]**; luego borren las marcas.
> - Los caracteres de cada sección ya están contados y dentro del límite (ver la tabla al final).

**EQUIPO:** Nanobots

**SEDE:** Universidad Nacional de Ingeniería, Recinto Universitario Pedro Arauz Palacios (UNI-RUPAP), Managua, Nicaragua

**Desafío seleccionado:** 02 – Sensores al servicio de la comunidad

---

## 1. DESCRIPCIÓN DE LA PROBLEMÁTICA (máx. 1.250 caracteres)

<!--S1-->
En Nicaragua, el robo a viviendas es una preocupación diaria que no distingue barrio ni ingreso. Los delincuentes suelen entrar de noche, mientras la familia duerme, forzando puertas, ventanas o levantando láminas del techo. La Policía Nacional informa cada semana detenciones por robo con fuerza (Policía Nacional, 2026), pero no hay datos públicos actualizados por barrio. Las alarmas comerciales cuestan [PENDIENTE: precio de alarmas en Nicaragua], más mensualidades, por lo que la mayoría de los hogares queda sin aviso temprano.

Al mismo tiempo, millones de celulares que funcionan duermen en cajones. Solo en 2022 se estimó que 5.300 millones de teléfonos se desecharían o guardarían sin uso (News24, 2022), y en América Latina apenas el 3 % de los residuos electrónicos se gestiona de forma adecuada (UNITAR, 2022).

Las causas son el alto costo de la tecnología de seguridad, la falta de aviso temprano y el poco valor que se le da a los dispositivos en desuso. El verdadero problema no es la falta de tecnología: es que las familias no se enteran a tiempo cuando alguien intenta entrar, aunque ya tienen en casa un sensor capaz de detectarlo, su celular viejo.
<!--/S1-->

## 2. DESCRIPCIÓN DE LA SOLUCIÓN (máx. 1.250 caracteres)

<!--S2-->
Sentinel convierte un celular en desuso en un sensor de seguridad para el hogar. Tiene dos partes:
(1) un soporte de bajo costo que se atornilla por dentro de una puerta o ventana;
(2) la plataforma Sentinel, que se instala escaneando el código QR de la caja.

Con la pantalla apagada, el celular usa el acelerómetro para detectar vibraciones y golpes cuando alguien fuerza la puerta o ventana, y el giroscopio y el magnetómetro para detectar si gira, es decir, si se abre. Ante un intento, envía en segundos una alarma a los dispositivos principales vinculados (celulares, tablet o computadora) y a los vecinos de confianza que la familia elija. Lo hace por wifi local, sin necesitar internet, o por internet si la familia está fuera.

El usuario define el horario de vigilancia (por ejemplo, de 11 p. m. a 4 a. m.), la sensibilidad y el tipo de alarma. La app le recuerda una hora diaria para cargar el equipo y vigila la temperatura de la batería.

A diferencia de las alarmas comerciales, no hay que comprar sensores ni pagar mensualidades. Y a diferencia de las apps de cámara, no graba a la familia y casi no gasta energía, porque solo «siente» la estructura de la casa.
<!--/S2-->

## 3. IMPACTO (máx. 1.000 caracteres)

<!--S3-->
Social: da seguridad accesible a cualquier familia, sin importar su ingreso, con un aviso temprano que permite reaccionar antes de que el intruso logre entrar (encender luces, alertar a los vecinos, llamar a la Policía). Cada hogar puede vincular a vecinos de confianza que reciben la misma alerta: así la comunidad se cuida entre sí y la ayuda llega en minutos, aunque la familia no pueda pedirla.

Ambiental: da una segunda vida a celulares que terminarían como basura electrónica, uno de los residuos que más crece en el mundo (62 millones de toneladas en 2022, de las que solo el 22,3 % se recicló de forma documentada; UNITAR, 2024), y evita fabricar sensores nuevos. Con 1.000 hogares usándolo, se reutilizan al menos 1.000 celulares.
<!--/S3-->

## 4. INNOVACIÓN (máx. 1.000 caracteres)

<!--S4-->
Sentinel cambia la forma de ver la seguridad del hogar: no vigila con cámaras, sino que «siente» la casa. Usa los sensores de movimiento y orientación de un celular viejo para medir algo que casi nunca se mide en un hogar: las vibraciones de puertas, ventanas y techos durante la noche.

Su algoritmo distingue los patrones de forcejeo, golpes o apertura de los de la lluvia, el viento o los animales. Funciona sin internet, por wifi local, y con la pantalla apagada, así que gasta muy poca energía.

El producto no es un aparato electrónico nuevo: es un soporte de bajo costo más una plataforma que conecta todos los dispositivos de la casa. Así, cada celular en desuso se convierte en un sensor de seguridad sin fabricar electrónica. La combinación de reutilización, detección por vibración y red familiar y vecinal no está disponible hoy en el mercado nicaragüense [VERIFICAR].
<!--/S4-->

## 5. PREFACTIBILIDAD ECONÓMICA, SOCIAL Y SUSTENTABILIDAD AMBIENTAL (máx. 1.500 caracteres)

<!--S5-->
Económica: producir el soporte cuesta unos US$3 por unidad (impresión 3D, tornillos y caja con código QR). Se venderá a US$8, muy por debajo de una alarma comercial; ese margen cubre los servidores, el mantenimiento de la plataforma y la distribución. La app básica es gratuita; una versión con funciones avanzadas (varias casas, historial, red vecinal ampliada) tendría un costo mensual mínimo. En la etapa piloto, la plataforma usa servicios en la nube con planes gratuitos.

Social: aprovecha lo que las familias ya tienen (celulares viejos y wifi), no exige conocimientos técnicos porque la instalación se guía con el QR, no graba imágenes y respeta la privacidad, y responde a una preocupación cotidiana.

Aliados potenciales:
- alcaldías y líderes comunitarios, para pilotos por barrio;
- UNEN y las universidades, para campañas de recolección de celulares en desuso;
- INATEC, para formar instaladores;
- programas de emprendimiento como Nicaragua Creativa.

Ambiental: reutiliza celulares, evita fabricar sensores nuevos y el soporte puede imprimirse con filamento reciclado de PET.

Seguridad del equipo: la app vigila la temperatura y el nivel de la batería, programa la carga diaria y advierte no usar baterías infladas.

Sostenibilidad: ingresos por la venta del soporte y suscripciones opcionales, con costos de operación bajos.
<!--/S5-->

## 6. PROPUESTAS QUE DESCARTARON

[Ajusten las razones con sus palabras.]

**De otros desafíos:**
- **Desafío 01 (motores eléctricos):** el equipo no tiene perfil de mecánica ni electricidad para darle credibilidad técnica.
- **Desafío 05 (reservorios pluviales):** es el tema más competido y nuestra fortaleza (software) quedaba en segundo plano.

**Dentro del desafío 02:**
1. **Monitoreo de bombas de agua comunitarias:** las empresas e instituciones de agua ya cuentan con sistemas de monitoreo.
2. **Detección de frenazos y baches con celulares en el transporte:** beneficiaba sobre todo a conductores y dueños de vehículos, no a toda la comunidad.
3. **Ocupación de buses con GPS y acelerómetro:** ya existen trabajos de apps de rutas (incluida una monografía de la UNI de 2019), IRTRAMMA tiene GPS en parte de la flota, y medir la ocupación con precisión resultó difícil.
4. **Alerta para adultos mayores que viven solos:** no era un problema de todos los hogares.
5. **Mapa de accesibilidad de aceras:** de menor frecuencia y alcance para el equipo.
6. **Monitoreo de edificios tras sismos:** INETER ya mide la actividad sísmica y no es un problema diario.
7. **Aviso del paso del camión de basura:** dependía de que la alcaldía adoptara el sistema.
8. **Cámara de seguridad con detección de movimiento:** con la cámara siempre encendida gasta mucha batería, calienta el equipo, no ve de noche y ya existen aplicaciones similares. Evolucionó a Sentinel, que usa solo los sensores de vibración.

## 7. REFERENCIAS

- Anthropic. (2026). *Claude* [Modelo de lenguaje grande]. https://claude.ai
  - Usado para analizar los ganadores de ediciones anteriores, generar y evaluar ideas y apoyar la redacción.
  - El contenido final fue revisado, verificado y redactado por el equipo.
- Google. (2026). *Gemini* (Argon 4) [Modelo de lenguaje grande]. https://gemini.google.com [confirmar el nombre exacto de la versión que muestra su app]
  - Usado para generar ideas sobre la problemática del desafío.
  - El contenido final fue revisado y redactado por el equipo.
- Android Developers. (s. f.). *Sensors overview*. https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview
- News24. (2022, 13 de octubre). *5.3 billion cell phones to become waste in 2022: report*. https://www.news24.com/world/53-billion-cell-phones-to-become-waste-in-2022-report-20221013
- Policía Nacional de Nicaragua. (2026). *Resultados de la actividad delictiva del país, lunes 6 al domingo 12 de abril del 2026*. https://www.policia.gob.ni/wp-content/uploads/2026/04/RESULTADOS-DE-LA-ACTIVIDAD-DELICTIVA-DEL-PAÍS-lunes-6-al-domigo-12-de-abril-del-2026.pdf
- Rally Latinoamericano de Innovación. (2026). *Desafíos edición 2026: 02 – Sensores al servicio de la comunidad*. https://sirli.org/desafios/banco_desafios/2026
- UNITAR. (2022). *Monitoreo regional de los residuos electrónicos para América Latina: resultados de los 13 países participantes en el proyecto UNIDO-GEF 5554*. https://unitar.org/sites/default/files/media/file/REM_LATAM_2022_ESP_Final.pdf
- UNITAR. (2024, 20 de marzo). *Global e-Waste Monitor 2024: Electronic waste rising five times faster than documented e-waste recycling* [Comunicado de prensa]. https://unitar.org/about/news-stories/press/global-e-waste-monitor-2024-electronic-waste-rising-five-times-faster-documented-e-waste-recycling
- [PENDIENTE: fuente de los precios de alarmas comerciales en Nicaragua, tienda local o sitio web.]

---

### Control de caracteres (contados con espacios)

| Sección | Caracteres | Límite |
|---|---|---|
| 1. Problemática | 1.170 | 1.250 |
| 2. Solución | 1.176 | 1.250 |
| 3. Impacto | 740 | 1.000 |
| 4. Innovación | 880 | 1.000 |
| 5. Prefactibilidad | 1.341 | 1.500 |
