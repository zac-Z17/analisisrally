// Renderiza la lámina completa y cada pantalla suelta a PNG.
// Uso: node render.mjs <html> <carpeta_salida>
import { chromium } from 'playwright-core';
import { mkdirSync } from 'node:fs';
import { resolve } from 'node:path';

const [html, out] = process.argv.slice(2);
mkdirSync(out, { recursive: true });
const browser = await chromium.launch({ executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome' });
const page = await browser.newPage({ viewport: { width: 2240, height: 1200 }, deviceScaleFactor: 1 });
await page.goto('file://' + resolve(html));
await page.evaluate(() => document.fonts.ready);
await page.waitForTimeout(400);

// contenido que se sale de su caja
const desbordes = await page.evaluate(() => [...document.querySelectorAll('.body')].map((b, i) =>
  b.scrollHeight > b.clientHeight + 1 ? `pantalla ${b.closest('.item').dataset.i}: +${b.scrollHeight - b.clientHeight}px` : null).filter(Boolean));
console.log('desbordes:', desbordes.length ? desbordes : 'ninguno');
const fuentes = await page.evaluate(() => [...document.fonts].filter(f => f.status === 'loaded').map(f => f.family + ' ' + f.weight));
console.log('fuentes:', [...new Set(fuentes)].join(', '));

await page.screenshot({ path: `${out}/sentinel_lamina.png`, fullPage: true });
const nombres = ['01_elegir_rol', '02_vincular', '03_sentinela_emisor', '04_inicio', '05_equipos', '06_horario', '07_vecinos', '08_alertas', '09_alerta_pantalla_completa', '10_como_se_conectan'];
const page2 = await browser.newPage({ viewport: { width: 2240, height: 1200 }, deviceScaleFactor: 2 });
await page2.goto('file://' + resolve(html));
await page2.evaluate(() => document.fonts.ready);
await page2.waitForTimeout(400);
const items = await page2.$$('.item');
for (let i = 0; i < items.length; i++) {
  const el = (await items[i].$('.ph')) || (await items[i].$(':scope > div'));
  await el.screenshot({ path: `${out}/pantallas/${nombres[i]}.png`, omitBackground: true });
}
await browser.close();
