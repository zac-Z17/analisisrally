// Prueba corta: ajustes con PIN en el Sentinela, simular golpe, ajustes del receptor y agregar vecino a mano.
import { chromium } from 'playwright-core';
const BASE = 'http://127.0.0.1:8090/app/index.html?relay=' + encodeURIComponent('http://127.0.0.1:8090/r');
const OUT = 'capturas';
import { mkdirSync } from 'node:fs';
mkdirSync(OUT, { recursive: true });
// CHROMIUM_PATH: ruta a un Chrome/Chromium ya instalado; si no se da, usa el de `npx playwright-core install chromium`
const browser = await chromium.launch({ executablePath: process.env.CHROMIUM_PATH || undefined });
const errores = [];
async function telefono(n) {
  const ctx = await browser.newContext({ viewport: { width: 390, height: 844 }, deviceScaleFactor: 2, hasTouch: true, isMobile: true });
  const p = await ctx.newPage();
  p.on('pageerror', e => errores.push(`[${n}] ${e.message}`));
  p.on('console', m => { if (m.type() === 'error') errores.push(`[${n}] ${m.text()}`); });
  return p;
}
const A = await telefono('receptor');
await A.goto(BASE); await A.click('[data-act="rol:receptor"]'); await A.click('[data-act="continuar"]');
await A.fill('#fPin', '4321'); await A.click('[data-act="crearHogar"]'); await A.waitForTimeout(1200);
const codigo = (await A.textContent('.hoja .mono')).replace(/\s/g, '');
const B = await telefono('sentinela');
await B.goto(BASE + `#unir-${codigo}-s`); await B.fill('#fNombre', 'Ventana cocina'); await B.click('[data-act="lugar:ventana"]');
await B.fill('#fNombre', 'Ventana cocina'); await B.click('[data-act="unirse"]');
await B.waitForSelector('[data-act="calFabrica"]', { timeout: 15000 });
await B.screenshot({ path: `${OUT}/21_calibrar_toast.png` });
await B.click('[data-act="calFabrica"]'); await B.waitForTimeout(500);
await B.click('[data-act="ajustesSentinela"]'); await B.waitForTimeout(300);
await B.screenshot({ path: `${OUT}/22_pin.png` });
for (const d of '4321') await B.click(`[data-act="pinTecla:${d}"]`);
await B.waitForSelector('[data-act="vigSent:on"]');
await B.screenshot({ path: `${OUT}/23_ajustes_sentinela.png` });
await B.click('[data-act="vigSent:on"]'); await B.waitForSelector('text=VIGILANDO');
await B.click('[data-act="ajustesSentinela"]'); for (const d of '4321') await B.click(`[data-act="pinTecla:${d}"]`);
await A.click('.hoja [data-act="cerrarHoja"]');
await B.click('[data-act="simular:apertura"]');
await A.waitForSelector('#alarma:not([hidden])', { timeout: 8000 });
console.log('· simular apertura desde el Sentinela → alarma en el receptor:', await A.textContent('#alarma h2'));
await A.click('[data-act="cerrarAlarma"]');
// oscurecer pantalla
await B.click('[data-act="cerrarAjSent"]'); await B.waitForTimeout(16500);
console.log('· pantalla oscura tras 15 s:', !!(await B.$('.oscuro')));
await B.screenshot({ path: `${OUT}/24_oscuro.png` });
await B.click('body'); console.log('· al tocar se quita:', !(await B.$('.oscuro')));
// receptor: ajustes y vecino a mano
await A.click('[data-act="ajustes"]'); await A.waitForTimeout(300);
await A.screenshot({ path: `${OUT}/25_ajustes_receptor.png` });
await A.click('[data-act="cerrarHoja"]'); await A.click('[data-act="tab:vecinos"]'); await A.click('[data-act="contactos"]');
await A.fill('#cNombre', 'Karla'); await A.fill('#cTel', '+505 8888 0000'); await A.click('[data-act="guardarContacto"]');
await A.waitForTimeout(300); await A.screenshot({ path: `${OUT}/26_vecino_pendiente.png` });
await A.click('[data-act^="whatsPend:"]'); await A.waitForTimeout(500);
const wa = await A.getAttribute('.hoja a.btn.vio', 'href');
console.log('· enlace de WhatsApp:', decodeURIComponent(wa).slice(0, 160));
await A.screenshot({ path: `${OUT}/27_invitar_vecino.png` });
console.log('errores:', errores.length ? errores : 'ninguno');
await browser.close();
