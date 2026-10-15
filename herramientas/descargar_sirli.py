"""Descarga de sirli.org (sistema oficial del Rally Latinoamericano de Innovación):
- ganadores de sede, nacionales e internacionales
- banco de desafíos (enunciado completo) de cada año

Salidas:
  datos/ganadores_sirli_<años>.csv   nivel, año, categoría, puesto, equipo, desafío, sede, país, video
  datos/desafios_sirli.csv           año, número, título, enunciado
  datos/sirli_html/                  HTML crudo (no se sube al repo)

Uso:  python herramientas/descargar_sirli.py [años...]     (por defecto 2023 2024 2025)

Nota: sirli.org envía una cadena de certificados incompleta (le falta el intermedio
Let's Encrypt YR1). Se añaden YR1 y «Root YR» firmada por ISRG Root X1, descargados de
letsencrypt.org (herramientas/certs/cadena-sirli.pem); la verificación TLS sigue activa.
"""
import csv
import os
import re
import sys
import tempfile
import time
from pathlib import Path

import requests
from bs4 import BeautifulSoup

BASE = 'https://sirli.org'
RAIZ = Path(__file__).resolve().parent.parent
HTML = RAIZ / 'datos' / 'sirli_html'
PAUSA = 1.5
PUESTOS = {'PRIMER': 1, 'SEGUNDO': 2, 'TERCER': 3}


def sesion():
    s = requests.Session()
    s.headers['User-Agent'] = 'Mozilla/5.0 (equipo estudiantil UNI-RUPAP; analisis Rally de Innovacion)'
    base = os.environ.get('REQUESTS_CA_BUNDLE') or os.environ.get('SSL_CERT_FILE')
    if not base:
        import certifi
        base = certifi.where()
    extra = Path(__file__).resolve().parent / 'certs' / 'cadena-sirli.pem'
    bundle = Path(tempfile.gettempdir()) / 'ca_sirli.pem'
    bundle.write_text(Path(base).read_text() + '\n' + extra.read_text())
    s.verify = str(bundle)
    return s


S = sesion()


def bajar(url, nombre):
    destino = HTML / nombre
    if destino.exists() and destino.stat().st_size > 2000:
        return destino.read_text(encoding='utf-8', errors='replace')
    for intento in range(4):
        try:
            r = S.get(url, timeout=120, verify=S.verify)  # REQUESTS_CA_BUNDLE pisaría a session.verify
            if r.status_code == 200:
                r.encoding = 'utf-8'
                destino.write_text(r.text, encoding='utf-8')
                time.sleep(PAUSA)
                return r.text
            print(f'  {r.status_code} en {url}')
        except requests.RequestException as e:
            print(f'  error {e.__class__.__name__} en {url}')
        time.sleep(2 ** (intento + 1))
    return ''


def categoria_de(tarjeta, actual):
    for padre in tarjeta.parents:
        clases = ' '.join(padre.get('class') or [])
        if 'bd-callout-innovacion' in clases:
            return 'Innovación'
        if 'bd-callout-social' in clases:
            return 'Impacto Social'
    return actual


def ganadores(html, nivel, anio, url):
    sopa = BeautifulSoup(html, 'lxml')
    filas, area = [], ''
    for nodo in sopa.find_all(['h4', 'div']):
        if nodo.name == 'h4':
            t = nodo.get_text(' ', strip=True).upper()
            area = 'Innovación' if 'INNOVACI' in t else 'Impacto Social' if 'SOCIAL' in t else area
            continue
        if 'form-directorio' not in (nodo.get('class') or []):
            continue
        f = {'nivel': nivel, 'anio': anio, 'categoria': categoria_de(nodo, area), 'puesto': '',
             'equipo': '', 'desafio_num': '', 'desafio': '', 'sede': '', 'pais': '', 'video_url': '', 'fuente': url}
        lugar = nodo.find('strong')
        if lugar:
            f['puesto'] = PUESTOS.get(lugar.get_text(strip=True).split()[0].upper(), '')
        for fila in nodo.select('div.row'):
            etiqueta = fila.find('label', class_='text-secondary')
            valor = etiqueta.find_next_sibling('div') if etiqueta else None
            if not valor:
                continue
            clave, texto = etiqueta.get_text(strip=True).rstrip(':').lower(), valor.get_text(' ', strip=True)
            if clave == 'equipo':
                f['equipo'] = texto
            elif clave.startswith('categor'):
                f['categoria'] = 'Innovación' if 'INNOVACI' in texto.upper() else 'Impacto Social'
            elif clave.startswith('desaf'):
                m = re.match(r'(\d+)\s*-\s*(.*)', texto)
                f['desafio_num'], f['desafio'] = (m.group(1), m.group(2)) if m else ('', texto)
            elif clave == 'sede':
                f['sede'] = texto
            elif clave.startswith('pa'):
                f['pais'] = texto
            elif clave == 'video':
                a = valor.find('a', href=True)
                f['video_url'] = a['href'] if a else ''
        filas.append(f)
    return filas


def desafios(html, anio, url):
    sopa = BeautifulSoup(html, 'lxml')
    out = []
    for h3 in sopa.find_all('h3'):
        m = re.match(r'(\d+)\s*-\s*(.*)', h3.get_text(' ', strip=True))
        if not m:
            continue
        p = h3.find_next_sibling('p')
        texto = p.get_text('\n', strip=True) if p else ''
        out.append({'anio': anio, 'num': m.group(1), 'titulo': m.group(2),
                    'enunciado': re.sub(r'\n+', ' ', texto), 'fuente': url})
    return out


def escribir(nombre, filas, campos):
    with (RAIZ / 'datos' / nombre).open('w', newline='', encoding='utf-8-sig') as f:
        w = csv.DictWriter(f, fieldnames=campos)
        w.writeheader()
        w.writerows(filas)
    print(f'{nombre}: {len(filas)} filas')


def main():
    anios = [int(a) for a in sys.argv[1:]] or [2023, 2024, 2025]
    HTML.mkdir(parents=True, exist_ok=True)
    todos, retos = [], []
    for anio in anios:
        url = f'{BASE}/ganadores/internacional/{anio}'
        todos += ganadores(bajar(url, f'internacional_{anio}.html'), 'internacional', anio, url)
        for nivel in ('nacional', 'sede'):
            url = f'{BASE}/ganadores/{nivel}/{anio}/?page=1&ipp=Todos&field=1&sort=0'
            todos += ganadores(bajar(url, f'{nivel}_{anio}.html'), nivel, anio, url)
        url = f'{BASE}/desafios/banco_desafios/{anio}'
        retos += desafios(bajar(url, f'desafios_{anio}.html'), anio, url)
        print(anio, {n: sum(1 for t in todos if t['anio'] == anio and t['nivel'] == n)
                     for n in ('internacional', 'nacional', 'sede')}, 'desafíos:', sum(1 for r in retos if r['anio'] == anio))
    sufijo = f'{min(anios)}_{max(anios)}' if len(anios) > 1 else str(anios[0])
    escribir(f'ganadores_sirli_{sufijo}.csv', todos,
             ['nivel', 'anio', 'categoria', 'puesto', 'equipo', 'desafio_num', 'desafio', 'sede', 'pais', 'video_url', 'fuente'])
    escribir('desafios_sirli.csv', retos, ['anio', 'num', 'titulo', 'enunciado', 'fuente'])


if __name__ == '__main__':
    main()
