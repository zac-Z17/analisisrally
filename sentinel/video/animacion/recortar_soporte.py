"""Recorta la vista frontal del soporte del plano CAD, borra las líneas de cota que la cruzan
y la guarda con fondo transparente (soporte_frontal.png) junto con la posición de los 4 agujeros.

Uso: python recortar_soporte.py   (lee soporte_cad.jpg de esta carpeta)
"""
import json
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

AQUI = Path(__file__).resolve().parent
X0, Y0 = 230, 280                       # origen del recorte dentro del plano (2000 x 2000)
c = Image.open(AQUI / 'soporte_cad.jpg').convert('RGB').crop((X0, Y0, 880, 810))
rgb = np.asarray(c).astype(float)
L = rgb.mean(axis=2)

# 1) líneas de cota que pasan por encima del objeto (coordenadas del recorte)
LINEAS = [((60, 12), (130, 50)), ((267, 277), (92, 530)), ((303, 377), (320, 515)), ((502, 52), (575, 0)), ((570, 116), (650, 35))]
h, w = L.shape
yy, xx = np.mgrid[0:h, 0:w]
for (x1, y1), (x2, y2) in LINEAS:
    dx, dy = x2 - x1, y2 - y1
    largo = np.hypot(dx, dy)
    ux, uy = dx / largo, dy / largo
    nx, ny = -uy, ux                                     # perpendicular
    tpar = ((xx - x1) * ux + (yy - y1) * uy)
    dist = np.abs((xx - x1) * nx + (yy - y1) * ny)
    sel = (dist <= 2.6) & (tpar >= -3) & (tpar <= largo + 3)
    ys, xs = np.where(sel)
    for y, x in zip(ys, xs):
        vecinos = []
        for k in (5, 6, -5, -6):
            px, py = int(round(x + nx * k)), int(round(y + ny * k))
            if 0 <= px < w and 0 <= py < h:
                vecinos.append(rgb[py, px])
        if vecinos:
            rgb[y, x] = np.median(vecinos, axis=0)
L = rgb.mean(axis=2)

# 2) máscara del objeto: oscuro, sin texto ni líneas sueltas (apertura) y solo la pieza conectada al centro
m = Image.fromarray(((L < 200) * 255).astype(np.uint8)).copy()
m = m.filter(ImageFilter.MinFilter(7)).filter(ImageFilter.MaxFilter(7))
ImageDraw.floodfill(m, (w // 2, h // 2), 128)
obj = np.asarray(m) == 128
# agujeros: zonas claras encerradas por el objeto
fondo = Image.fromarray((~obj).astype(np.uint8) * 255).copy()
ImageDraw.floodfill(fondo, (0, 0), 100)
enc = np.asarray(fondo) == 255
agujeros = []
etiq = Image.fromarray(enc.astype(np.uint8) * 255).copy()
for y, x in zip(*np.where(enc)):
    if etiq.getpixel((int(x), int(y))) == 255:
        ImageDraw.floodfill(etiq, (int(x), int(y)), 50 + len(agujeros))
        zona = np.asarray(etiq) == 50 + len(agujeros)
        ys2, xs2 = np.where(zona)
        if len(ys2) > 150:
            agujeros.append((float(xs2.mean()), float(ys2.mean()), float(np.sqrt(len(ys2) / np.pi))))
        else:
            obj |= zona                                  # huequitos de ruido: se rellenan

# 3) alfa con borde suave
dentro = np.asarray(Image.fromarray((obj * 255).astype(np.uint8)).filter(ImageFilter.MinFilter(3))) > 0
borde = np.asarray(Image.fromarray((obj * 255).astype(np.uint8)).filter(ImageFilter.MaxFilter(3))) > 0
suave = np.clip((235 - L) / 40, 0, 1)
alfa = np.where(dentro, 1.0, np.where(borde, suave, 0.0))
ys, xs = np.where(alfa > 0)
x0, x1, y0, y1 = xs.min(), xs.max() + 1, ys.min(), ys.max() + 1
out = np.dstack([rgb, alfa * 255]).clip(0, 255).astype(np.uint8)[y0:y1, x0:x1]
Image.fromarray(out, 'RGBA').save(AQUI / 'soporte_frontal.png')
info = {'ancho': int(x1 - x0), 'alto': int(y1 - y0), 'mm_ancho': 140,
        'agujeros': [[round(a - x0, 1), round(b - y0, 1), round(r, 1)] for a, b, r in sorted(agujeros, key=lambda t: (t[1] > h / 2, t[0]))]}
(AQUI / 'soporte_frontal.json').write_text(json.dumps(info, indent=1))
print(info)
