"""Une los JSON de datos/busqueda_web/ en tablas CSV fáciles de abrir en Excel o pegar en una IA.

Salidas:
  datos/ganadores_2023_2025.csv   (nacionales e internacionales desde 2023; foco del análisis)
  datos/ganadores_historico.csv   (todo lo encontrado, 2016-2025)
"""
import csv
import json
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
FUENTES = RAIZ / 'datos' / 'busqueda_web'
ARCHIVOS = ['internacional_2023_2025.json', 'internacional_2016_2022.json',
            'nacional_nicaragua.json', 'nacional_otros_paises.json']
COLUMNAS = ['anio', 'nivel', 'ranking_de', 'categoria', 'puesto', 'equipo', 'pais', 'sede',
            'universidad', 'desafio_num', 'desafio_nombre', 'desafio_resumen', 'idea_propuesta',
            'detalles_pitch', 'video_url', 'confianza', 'notas', 'fuentes']

# Datos encontrados después de la búsqueda de los agentes
CORRECCIONES = {
    ('MT SOLUCIONES CREATIVAS', 2021): {
        'idea_propuesta': 'Vivienda con generación renovable: dos turbinas eólicas y un digestor anaerobio '
                          'para biogás; aprovecha residuos orgánicos para compost y tiene cosecha de agua '
                          'de lluvia con filtración.',
        'fuente_extra': 'https://www.tn8.tv/nacionales/nicaragua-primer-lugar-rally-latinoamericano-innovacion/',
    },
}


def normal(r):
    fila = {c: r.get(c, '') for c in COLUMNAS}
    fila['ranking_de'] = 'Latinoamérica' if r.get('nivel') == 'internacional' else r.get('pais_ranking', r.get('pais', ''))
    fila['fuentes'] = ' '.join(r.get('fuentes') or [])
    fila['puesto'] = '' if r.get('puesto') is None else r.get('puesto')
    extra = CORRECCIONES.get(((r.get('equipo') or '').upper(), r.get('anio')))
    if extra:
        fila['idea_propuesta'] = extra['idea_propuesta']
        fila['fuentes'] = (fila['fuentes'] + ' ' + extra['fuente_extra']).strip()
    return fila


def main():
    filas = []
    for nombre in ARCHIVOS:
        filas += [normal(r) for r in json.loads((FUENTES / nombre).read_text(encoding='utf-8'))]
    orden_nivel = {'internacional': 0, 'nacional': 1}
    filas.sort(key=lambda f: (-int(f['anio']), orden_nivel.get(f['nivel'], 2), f['ranking_de'],
                              f['categoria'], str(f['puesto'] or 9)))
    for nombre, sel in (('ganadores_historico.csv', filas),
                        ('ganadores_2023_2025.csv', [f for f in filas if int(f['anio']) >= 2023])):
        with (RAIZ / 'datos' / nombre).open('w', newline='', encoding='utf-8-sig') as f:
            w = csv.DictWriter(f, fieldnames=COLUMNAS)
            w.writeheader()
            w.writerows(sel)
        print(nombre, len(sel), 'filas')


if __name__ == '__main__':
    main()
