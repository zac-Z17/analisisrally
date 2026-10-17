// Servidor local para probar el prototipo sin internet:
//  - sirve los archivos de la carpeta indicada en /app/...
//  - imita la API de ntfy que usa la app en /r/<tema> (POST) y /r/<tema>/sse (GET)
// Uso: node relay_prueba.mjs [carpeta] [puerto]   (por defecto: la carpeta del prototipo y el puerto 8090)
import http from 'node:http';
import { readFile } from 'node:fs/promises';
import { join, extname, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const [dir = join(dirname(fileURLToPath(import.meta.url)), '..'), puerto = 8090] = process.argv.slice(2);
const temas = new Map();   // tema -> { msgs: [], subs: Set<res> }
const tema = t => { if (!temas.has(t)) temas.set(t, { msgs: [], subs: new Set() }); return temas.get(t); };
let n = 0;
const tipos = { '.html': 'text/html; charset=utf-8', '.js': 'text/javascript', '.png': 'image/png' };

http.createServer(async (req, res) => {
  const url = new URL(req.url, 'http://x');
  res.setHeader('Access-Control-Allow-Origin', '*');
  if (url.pathname.startsWith('/app/')) {
    try { res.writeHead(200, { 'Content-Type': tipos[extname(url.pathname)] || 'application/octet-stream' }); res.end(await readFile(join(dir, url.pathname.slice(5)))); }
    catch { res.writeHead(404); res.end(); }
    return;
  }
  const m = url.pathname.match(/^\/r\/([\w-]+)(\/sse)?$/);
  if (!m) { res.writeHead(404); res.end(); return; }
  const T = tema(m[1]);
  if (req.method === 'POST' && !m[2]) {
    let body = ''; req.on('data', c => (body += c)); req.on('end', () => {
      const msg = { id: 'm' + ++n, time: Math.floor(Date.now() / 1000), event: 'message', topic: m[1], message: body };
      T.msgs.push(msg);
      for (const s of T.subs) s.write(`data: ${JSON.stringify(msg)}\n\n`);
      res.writeHead(200, { 'Content-Type': 'application/json' }); res.end(JSON.stringify(msg));
    });
    return;
  }
  if (req.method === 'GET' && m[2]) {
    res.writeHead(200, { 'Content-Type': 'text/event-stream', 'Cache-Control': 'no-cache', Connection: 'keep-alive' });
    res.write(`event: open\ndata: ${JSON.stringify({ event: 'open', topic: m[1] })}\n\n`);
    const since = url.searchParams.get('since');
    const s = since && since.match(/^(\d+)([smh])$/);
    if (s) {
      const desde = Date.now() / 1000 - (+s[1]) * { s: 1, m: 60, h: 3600 }[s[2]];
      for (const msg of T.msgs) if (msg.time >= desde) res.write(`data: ${JSON.stringify(msg)}\n\n`);
    }
    T.subs.add(res); req.on('close', () => T.subs.delete(res));
    return;
  }
  res.writeHead(405); res.end();
}).listen(+puerto, '127.0.0.1', () => console.log('relay de prueba en', puerto));
