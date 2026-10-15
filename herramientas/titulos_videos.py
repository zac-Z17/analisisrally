"""Agrega a datos/ganadores_sirli_2023_2025.csv el título y el canal de cada video de YouTube
(el título suele traer el nombre de la solución). Usa el servicio público oEmbed de YouTube.

Por defecto solo consulta ganadores internacionales, nacionales y de sedes de Nicaragua.
Uso:  python herramientas/titulos_videos.py [--todos] [--prioridad]
"""
import csv
import json
import re
import sys
import time
from pathlib import Path

import requests

RAIZ = Path(__file__).resolve().parent.parent
TABLA = RAIZ / 'datos' / 'ganadores_sirli_2023_2025.csv'
CACHE = RAIZ / 'datos' / 'videos_oembed.json'
PAUSA = 0.4


def id_video(url):
    m = re.search(r'(?:youtu\.be/|v=|shorts/|embed/|live/)([A-Za-z0-9_-]{11})', url or '')
    return m.group(1) if m else ''


def main():
    todos = '--todos' in sys.argv
    filas = list(csv.DictReader(TABLA.open(encoding='utf-8-sig')))
    cache = json.loads(CACHE.read_text(encoding='utf-8')) if CACHE.exists() else {}
    pedir = {id_video(f['video_url']) for f in filas
             if todos or f['nivel'] != 'sede' or f['pais'] == 'NICARAGUA'} - set(cache) - {''}
    # primero internacionales, nacionales y la sede UNI-RUPAP
    primero = {id_video(f['video_url']) for f in filas if f['nivel'] != 'sede' or 'RUPAP' in f['sede'].upper()}
    orden = sorted(pedir, key=lambda v: (v not in primero, v))
    if '--prioridad' in sys.argv:
        orden = [v for v in orden if v in primero]
    print(f'{len(orden)} videos por consultar')
    for i, vid in enumerate(orden, 1):
        try:
            r = requests.get('https://www.youtube.com/oembed',
                             params={'url': f'https://www.youtube.com/watch?v={vid}', 'format': 'json'}, timeout=30)
            cache[vid] = r.json() if r.status_code == 200 else {'error': r.status_code}
        except (requests.RequestException, ValueError) as e:
            cache[vid] = {'error': e.__class__.__name__}
        if i % 50 == 0:
            CACHE.write_text(json.dumps(cache, ensure_ascii=False, indent=0), encoding='utf-8')
            print(i)
        time.sleep(PAUSA)
    CACHE.write_text(json.dumps(cache, ensure_ascii=False, indent=0), encoding='utf-8')
    campos = list(filas[0].keys())
    for c in ('titulo_video', 'canal'):
        if c not in campos:
            campos.insert(campos.index('video_url'), c)
    for f in filas:
        info = cache.get(id_video(f['video_url']), {})
        f['titulo_video'] = info.get('title', '')
        f['canal'] = info.get('author_name', '')
    with TABLA.open('w', newline='', encoding='utf-8-sig') as fh:
        w = csv.DictWriter(fh, fieldnames=campos)
        w.writeheader()
        w.writerows(filas)
    print('con título:', sum(1 for f in filas if f['titulo_video']))


if __name__ == '__main__':
    main()
