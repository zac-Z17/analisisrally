"""Descarga de sirli.org (sistema oficial del Rally Latinoamericano de Innovación):
- ganadores de sede, nacionales e internacionales (2023-2025)
- banco de desafíos de cada año

Guarda el HTML crudo en datos/sirli_html/ y una tabla genérica en datos/sirli_filas.csv
(una fila por fila de tabla / tarjeta, con sus textos y enlaces). Después se limpia con
herramientas/limpiar_sirli.py.

Uso:  python herramientas/descargar_sirli.py [años...]     (por defecto 2023 2024 2025)
Necesita acceso a red a sirli.org. Hace pausas entre pedidos para no saturar el sitio.
"""
import csv
import hashlib
import sys
import time
from pathlib import Path
from urllib.parse import urljoin

import requests
from bs4 import BeautifulSoup

BASE = 'https://sirli.org'
RAIZ = Path(__file__).resolve().parent.parent
HTML = RAIZ / 'datos' / 'sirli_html'
PAUSA = 1.5
MAX_PAGINAS = 200

S = requests.Session()
S.headers['User-Agent'] = 'Mozilla/5.0 (equipo estudiantil UNI-RUPAP; analisis Rally de Innovacion)'


def bajar(url, nombre):
    destino = HTML / nombre
    if destino.exists() and destino.stat().st_size > 2000:
        return destino.read_text(encoding='utf-8', errors='replace')
    for intento in range(4):
        try:
            r = S.get(url, timeout=60)
            if r.status_code == 200:
                r.encoding = r.apparent_encoding or 'utf-8'
                destino.write_text(r.text, encoding='utf-8')
                time.sleep(PAUSA)
                return r.text
            print(f'  {r.status_code} en {url}')
        except requests.RequestException as e:
            print(f'  error {e.__class__.__name__} en {url}')
        time.sleep(2 ** (intento + 1))
    return ''


def filas(html, url):
    """Extrae filas de tablas (o bloques con 'Ver video') con textos y enlaces."""
    sopa = BeautifulSoup(html, 'lxml')
    out = []
    for t in sopa.find_all('table'):
        enc = [th.get_text(' ', strip=True) for th in t.find_all('th')]
        for tr in t.find_all('tr'):
            celdas = tr.find_all('td')
            if not celdas:
                continue
            textos = [c.get_text(' ', strip=True) for c in celdas]
            enlaces = [urljoin(url, a['href']) for a in tr.find_all('a', href=True)]
            out.append({'encabezados': ' | '.join(enc), 'celdas': ' | '.join(textos),
                        'enlaces': ' '.join(enlaces)})
    if not out:  # sin tablas: tarjetas con enlace "Ver video"
        for a in sopa.find_all('a', string=lambda s: s and 'video' in s.lower()):
            bloque = a
            for _ in range(4):
                if bloque.parent is None:
                    break
                bloque = bloque.parent
                if len(bloque.get_text(' ', strip=True)) > 60:
                    break
            out.append({'encabezados': '', 'celdas': bloque.get_text(' | ', strip=True),
                        'enlaces': ' '.join(urljoin(url, x['href']) for x in bloque.find_all('a', href=True))})
    return out


def recorrer(nivel, anio, plantilla):
    """Baja todas las páginas de un listado hasta que deje de traer filas nuevas."""
    vistas, total = set(), []
    for p in range(1, MAX_PAGINAS + 1):
        url = plantilla.format(anio=anio, p=p)
        html = bajar(url, f'{nivel}_{anio}_p{p:03d}.html')
        nuevas = []
        for f in filas(html, url):
            clave = hashlib.md5(f['celdas'].encode()).hexdigest()
            if clave not in vistas:
                vistas.add(clave)
                nuevas.append({'nivel': nivel, 'anio': anio, 'pagina': p, 'url': url, **f})
        print(f'{nivel} {anio} p{p}: {len(nuevas)} filas nuevas')
        total += nuevas
        if not nuevas or '{p}' not in plantilla:
            break
    return total


def main():
    anios = [int(a) for a in sys.argv[1:]] or [2023, 2024, 2025]
    HTML.mkdir(parents=True, exist_ok=True)
    todo = []
    for anio in anios:
        todo += recorrer('internacional', anio, BASE + '/ganadores/internacional/{anio}')
        # "Todos" en una sola página; si el sitio no lo respeta, se pagina de 50 en 50
        for nivel in ('nacional', 'sede'):
            filas_todas = recorrer(nivel, anio, BASE + f'/ganadores/{nivel}/{{anio}}/?page=1&ipp=Todos&field=1&sort=0')
            if len(filas_todas) < 20:
                filas_todas = recorrer(nivel, anio, BASE + f'/ganadores/{nivel}/{{anio}}/?page={{p}}&ipp=50&field=1&sort=0')
            todo += filas_todas
        todo += recorrer('desafios', anio, BASE + '/desafios/banco_desafios/{anio}')
    salida = RAIZ / 'datos' / 'sirli_filas.csv'
    with salida.open('w', newline='', encoding='utf-8') as f:
        w = csv.DictWriter(f, fieldnames=['nivel', 'anio', 'pagina', 'url', 'encabezados', 'celdas', 'enlaces'])
        w.writeheader()
        w.writerows(todo)
    print(f'Listo: {len(todo)} filas en {salida}')


if __name__ == '__main__':
    main()
