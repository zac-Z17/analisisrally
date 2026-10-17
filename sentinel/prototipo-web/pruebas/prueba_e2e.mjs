// Prueba de punta a punta: Receptor crea hogar → Sentinela se une y calibra →
// activar vigilancia → golpes → alarma en receptor y vecino → falsa alarma → vibración leve → «¿Qué fue?»
import { chromium } from 'playwright-core';
import { mkdirSync } from 'node:fs';

const BASE = 'http://127.0.0.1:8090/app/index.html?relay=' + encodeURIComponent('http://127.0.0.1:8090/r');
const OUT = process.argv[2] || 'capturas';
mkdirSync(OUT, { recursive: true });
const espera = ms => new Promise(r => setTimeout(r, ms));
// CHROMIUM_PATH: ruta a un Chrome/Chromium ya instalado; si no se da, usa el de `npx playwright-core install chromium`
const browser = await chromium.launch({ executablePath: process.env.CHROMIUM_PATH || undefined });
const errores = [];
async function telefono(nombre) {
  const ctx = await browser.newContext({ viewport: { width: 390, height: 844 }, deviceScaleFactor: 2, hasTouch: true, isMobile: true });
  const p = await ctx.newPage();
  p.on('console', m => { if (m.type() === 'error') errores.push(`[${nombre}] ${m.text()}`); });
  p.on('pageerror', e => errores.push(`[${nombre}] ${e.message}`));
  return p;
}
const foto = (p, n) => p.screenshot({ path: `${OUT}/${n}.png` });
const paso = (txt) => console.log('·', txt);
// envía movimientos sintéticos al Sentinela: lista de [aceleración m/s², giro °/s, duración ms]
async function mover(p, tramos) {
  await p.evaluate(async tr => {
    for (const [a, r, ms] of tr) {
      const fin = performance.now() + ms;
      while (performance.now() < fin) {
        const ruido = (Math.random() - .5) * .04;
        window.dispatchEvent(new DeviceMotionEvent('devicemotion', { acceleration: { x: a + ruido, y: ruido, z: ruido }, accelerationIncludingGravity: { x: a, y: 0, z: 9.8 }, rotationRate: { alpha: r, beta: 0, gamma: 0 }, interval: 16 }));
        await new Promise(res => setTimeout(res, 16));
      }
    }
  }, tramos);
}

// 1. Receptor: crear hogar
const A = await telefono('receptor');
await A.goto(BASE); await A.waitForTimeout(800);
await foto(A, '01_bienvenida');
await A.click('[data-act="rol:receptor"]'); await A.click('[data-act="continuar"]');
await A.fill('#fCasa', 'Mi casa'); await A.fill('#fNombre', 'Mi celular'); await A.fill('#fPin', '1234');
await A.click('[data-act="crearHogar"]'); await A.waitForTimeout(1200);
const codigo = (await A.textContent('.hoja .mono')).replace(/\s/g, '');
paso('código de vinculación ' + codigo);
await foto(A, '02_vincular');

// 2. Sentinela: entra por el enlace del QR
const B = await telefono('sentinela');
await B.goto(BASE + `#unir-${codigo}-s`); await B.waitForTimeout(600);
await B.fill('#fNombre', 'Puerta principal');
await foto(B, '03_unir_sentinela');
await B.click('[data-act="unirse"]');
await B.waitForSelector('text=Paso 1 de 3', { timeout: 15000 });
paso('el sentinela se unió y abrió la calibración');
await A.waitForSelector('.banner.c-verde', { timeout: 8000 });
await foto(A, '04_vinculado');
await foto(B, '05_calibrar_1');

// 3. Calibración
await B.click('[data-act="calPaso:1"]'); await mover(B, [[0, 0, 5300]]); await B.waitForTimeout(400);
await B.click('[data-act="calPaso:2"]'); await mover(B, [[0, 0, 300], [.3, 70, 900], [0, 0, 400], [.3, -70, 900], [0, 0, 1200]]);
await B.waitForSelector('text=Paso 3 de 3', { timeout: 12000 });
await B.click('[data-act="calPaso:3"]'); await mover(B, [[0, 0, 300], [1.6, 0, 50], [0, 0, 450], [1.6, 0, 50], [0, 0, 450], [1.6, 0, 50], [0, 0, 600]]);
await B.waitForSelector('[data-act="calGuardar"]', { timeout: 12000 });
await foto(B, '06_calibrado');
const cal = await B.evaluate(() => JSON.parse(localStorage.getItem('sentinel.prototipo.v1')).cal);
paso('calibración ' + JSON.stringify(cal));
await B.click('[data-act="calGuardar"]'); await B.waitForTimeout(800);
await foto(B, '07_sentinela_reposo');

// 4. Receptor activa la vigilancia
await A.click('.hoja [data-act="cerrarHoja"]'); await A.waitForTimeout(1500);
await foto(A, '08_inicio_reposo');
await A.click('[data-act="activar"]');
await B.waitForSelector('text=VIGILANDO', { timeout: 8000 });
paso('el sentinela recibió la orden y está VIGILANDO');
await A.waitForTimeout(1500);
await foto(A, '09_inicio_vigilando');
await mover(B, [[0, 0, 1500]]);
await foto(B, '10_sentinela_vigilando');

// 5. Vecino de confianza se une
await A.click('[data-act="tab:vecinos"]'); await A.click('[data-act="invitar"]'); await A.waitForTimeout(1200);
const codV = (await A.textContent('.hoja .mono')).replace(/\s/g, '');
const C = await telefono('vecino');
await C.goto(BASE); await C.waitForTimeout(500);
await C.click('[data-act="rol:vecino"]');
await C.fill('#fCodigo', codV); await C.fill('#fNombre', 'Doña Marta'); await C.fill('#fRef', 'Casa de al lado');
await C.click('[data-act="unirse"]');
await C.waitForSelector('text=Cuidas la casa', { timeout: 15000 });
paso('el vecino se unió');
await A.click('.hoja [data-act="cerrarHoja"]'); await A.waitForTimeout(800);
await foto(A, '11_vecinos');
await foto(C, '12_vecino');

// 6. Forcejeo: 3 golpes fuertes
await mover(B, [[0, 0, 300], [4.5, 0, 40], [0, 0, 350], [4.5, 0, 40], [0, 0, 350], [4.5, 0, 40], [0, 0, 300]]);
await A.waitForSelector('#alarma:not([hidden])', { timeout: 8000 });
await C.waitForSelector('#alarma:not([hidden])', { timeout: 8000 });
paso('ALARMA recibida en el receptor y en el vecino');
await foto(A, '13_alarma_receptor'); await foto(C, '14_alarma_vecino'); await foto(B, '15_sentinela_intento');

// 7. Falsa alarma desde el receptor → se cierra en el vecino
await A.click('[data-act="resolver:falsa"]');
await C.waitForSelector('#alarma[hidden]', { state: 'attached', timeout: 8000 });
paso('«falsa alarma» cerró la alarma en el vecino');

// 8. Apertura de la puerta (giro) tras la espera de 20 s del sentinela
await B.evaluate(() => { Sensor.ultIntento = -1e9; });
await mover(B, [[.2, 60, 700], [0, 0, 300]]);
await A.waitForSelector('#alarma:not([hidden])', { timeout: 8000 });
paso('apertura detectada por el giroscopio');
await A.click('[data-act="cerrarAlarma"]'); await C.click('[data-act="cerrarAlarma"]').catch(() => {});

// 9. Vibración leve y «¿Qué fue?»
await B.evaluate(() => { Sensor.ultIntento = -1e9; Sensor.ultLeve = -1e9; });
await mover(B, [[0, 0, 200]]);
const leve = await B.evaluate(() => Sensor.umbrales().leve);
await mover(B, [[leve * 1.3, 0, 60], [0, 0, 3200]]);
await A.click('[data-act="tab:alertas"]'); await A.waitForTimeout(800);
await A.waitForSelector('[data-act^="que:"]', { timeout: 8000 });
await A.click('[data-act$=",lluvia"]'); await A.waitForTimeout(1500);
const ajuste = await B.evaluate(() => Object.values(S.cfg.equipos)[0]?.ajuste);
paso('vibración leve → «Lluvia» → ajuste de sensibilidad en el sentinela: ' + ajuste);
await foto(A, '16_alertas');

// 10. Otras pestañas
await A.click('[data-act="tab:horario"]'); await A.waitForTimeout(500); await foto(A, '17_horario');
await A.click('[data-act="tab:equipos"]'); await A.waitForTimeout(500); await foto(A, '18_equipos');
await A.click('[data-act^="detalle:"]'); await A.waitForTimeout(500); await foto(A, '19_detalle');
await A.click('.hoja [data-act="cerrarHoja"]');

// 11. Demostración
const D = await telefono('demo');
await D.goto(BASE); await D.waitForTimeout(500); await D.click('[data-act="demo"]'); await D.waitForTimeout(800);
await foto(D, '20_demo_inicio');

console.log('errores de consola:', errores.length ? errores : 'ninguno');
await browser.close();
