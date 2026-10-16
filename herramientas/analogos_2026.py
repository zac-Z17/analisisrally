"""Para cada desafío 2026, mide cómo les fue a los desafíos parecidos de 2023-2025
(sede → nacional → latinoamericano, en todos los países, en Nicaragua y en UNI-RUPAP)
y prueba si las soluciones digitales rinden distinto que las físicas (por el título del video).

Escribe datos/analogos_2026.md.   Uso:  python herramientas/analogos_2026.py
"""
import csv
import re
from collections import Counter
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
G = list(csv.DictReader((RAIZ / 'datos' / 'ganadores_sirli_2023_2025.csv').open(encoding='utf-8-sig')))
D = {(d['anio'], d['num']): d['titulo'].capitalize().replace('tetra pak', 'Tetra Pak')
     for d in csv.DictReader((RAIZ / 'datos' / 'desafios_sirli_2023_2025.csv').open(encoding='utf-8-sig'))}
N26 = {d['num']: d['titulo'].strip().capitalize()
       for d in csv.DictReader((RAIZ / 'datos' / 'desafios_sirli_2026.csv').open(encoding='utf-8-sig'))}

# Desafíos anteriores más parecidos a cada desafío 2026 (por lo que piden resolver)
ANALOGOS = {
    '01': ([('2025', '01'), ('2025', '02'), ('2024', '02'), ('2024', '06')],
           'dar segunda vida a un objeto o material desechado para beneficiarios concretos'),
    '02': ([('2023', '07'), ('2023', '02'), ('2025', '04'), ('2025', '03')],
           'sensores o dispositivos de medición / detección (Crowdsensing usaba los sensores del celular)'),
    '03': ([('2025', '05'), ('2023', '04')], 'producción de alimentos y agro para comunidades'),
    '04': ([('2023', '06'), ('2024', '04'), ('2024', '06')], 'residuo orgánico o municipal convertido en insumo'),
    '05': ([('2024', '01'), ('2025', '04'), ('2023', '03'), ('2023', '09')],
           'agua de lluvia, monitoreo de inundaciones y calidad de agua'),
    '06': ([('2025', '06'), ('2024', '05'), ('2025', '09')],
           'dispositivo manual portátil de bajo costo / respuesta a desastres'),
    '07': ([('2023', '06'), ('2024', '04'), ('2025', '02')], 'residuo doméstico convertido en subproducto'),
    '08': ([('2025', '02'), ('2025', '01'), ('2024', '06')], 'segunda vida o reciclaje seguro de un residuo difícil'),
    '09': ([('2025', '09'), ('2024', '05'), ('2024', '08')], 'coordinación y logística en emergencias'),
}

DIGITAL = re.compile(r'\bapps?\b|aplicaci|plataforma|software|\bweb\b|chat ?bot|\bbot\b|digital|\bia\b|'
                     r'inteligencia artificial|realidad virtual|\bvr\b|videojuego|\bjuego\b|página', re.I)
out = []


def pct(a, b):
    return f'{round(100 * a / b)} %' if b else '–'


def tabla(cab, filas):
    out.append('| ' + ' | '.join(cab) + ' |')
    out.append('|' + '|'.join('---' for _ in cab) + '|')
    out.extend('| ' + ' | '.join(str(x) for x in f) + ' |' for f in filas)
    out.append('')


def sel(claves, **k):
    return [f for f in G if (f['anio'], f['desafio_num']) in claves and all(f[c] == v for c, v in k.items())]


out.append('# Desafíos 2026 frente a sus parecidos de 2023–2025\n')
out.append('Datos: 2.407 premios oficiales de sirli.org. «Conversión» = podios nacionales ÷ primeros lugares de sede.\n')
out.append('Promedios de referencia: todos los países 152 podios nacionales / 675 primeros de sede; '
           'Nicaragua 18 / 269 (7 %).\n')

out.append('## 1. Resumen por desafío 2026\n')
filas, detalle = [], []
for num, (claves, porque) in ANALOGOS.items():
    claves = set(claves)
    s1 = sel(claves, nivel='sede', puesto='1')
    nac = sel(claves, nivel='nacional')
    lat = sel(claves, nivel='internacional')
    ni_s1 = [f for f in s1 if f['pais'] == 'NICARAGUA']
    ni_n = [f for f in nac if f['pais'] == 'NICARAGUA']
    rup = [f for f in sel(claves, nivel='sede') if 'RUPAP' in f['sede'].upper()]
    sede_all = sel(claves, nivel='sede')
    inn = sum(1 for f in sede_all if f['categoria'] == 'Innovación')
    filas.append((f'{num} {N26[num]}', len(s1), pct(len(nac), len(s1)), f"{len(lat)} ({sum(1 for f in lat if f['puesto'] == '1')} 1.os)",
                  len(ni_s1), f"{len(ni_n)} ({pct(len(ni_n), len(ni_s1))})", sum(1 for f in ni_n if f['puesto'] == '1'),
                  len(rup), f"{pct(inn, len(sede_all))} Inn."))
    detalle.append((num, claves, porque, nac, lat, ni_n, rup))
tabla(['Desafío 2026', '1.os sede (todos)', 'Conv. nacional (todos)', 'Podios latam', '1.os sede NI', 'Podios nac. NI (conv.)',
       '1.os nac. NI', 'Premios en RUPAP', 'Categoría (sede)'], filas)

out.append('## 2. Detalle: qué ganó en los desafíos parecidos\n')
for num, claves, porque, nac, lat, ni_n, rup in detalle:
    out.append(f'### {num} {N26[num]}\n')
    out.append(f'Parecidos por: {porque}. Desafíos: ' + '; '.join(f'{a} {D[(a, n)]}' for a, n in sorted(claves)) + '.\n')
    if lat:
        out.append('- **Latinoamérica:** ' + '; '.join(
            f"{f['puesto']}.º {f['categoria']} {f['equipo'].title()} ({f['pais'].title()}) «{f['titulo_video'][:60]}»" for f in lat))
    if ni_n:
        out.append('- **Podio nacional Nicaragua:** ' + '; '.join(
            f"{f['anio']} {f['puesto']}.º {f['categoria']} {f['equipo'].title()} ({f['sede'].title()}) «{f['titulo_video'][:60]}»" for f in ni_n))
    if rup:
        out.append('- **En UNI-RUPAP:** ' + '; '.join(
            f"{f['anio']} {f['puesto']}.º {f['categoria']} {f['equipo'].title()} «{f['titulo_video'][:50]}»" for f in rup))
    out.append('')

out.append('## 3. ¿Rinden distinto las soluciones digitales? (por el título del video)\n')
out.append(f'Se marca «digital» si el título menciona app, aplicación, plataforma, software, web, bot, IA, realidad virtual o juego. '
           f'Regex: `{DIGITAL.pattern}`\n')
ni1 = [f for f in G if f['pais'] == 'NICARAGUA' and f['nivel'] == 'sede' and f['puesto'] == '1' and f['titulo_video']]
nac_ni = {(f['anio'], f['equipo']) for f in G if f['pais'] == 'NICARAGUA' and f['nivel'] == 'nacional'}
filas = []
for tipo in ('digital', 'no digital'):
    grupo = [f for f in ni1 if bool(DIGITAL.search(f['titulo_video'])) == (tipo == 'digital')]
    sube = [f for f in grupo if (f['anio'], f['equipo']) in nac_ni]
    filas.append((tipo, len(grupo), len(sube), pct(len(sube), len(grupo))))
out.append('**Nicaragua: primeros lugares de sede con título → podio nacional**\n')
tabla(['Tipo', '1.os de sede', 'Llegaron al podio nacional', 'Conversión'], filas)
nac_all = [f for f in G if f['nivel'] == 'nacional' and f['titulo_video']]
lat_set = {(f['anio'], f['equipo']) for f in G if f['nivel'] == 'internacional'}
filas = []
for tipo in ('digital', 'no digital'):
    grupo = [f for f in nac_all if bool(DIGITAL.search(f['titulo_video'])) == (tipo == 'digital')]
    sube = [f for f in grupo if (f['anio'], f['equipo']) in lat_set]
    filas.append((tipo, len(grupo), len(sube), pct(len(sube), len(grupo))))
out.append('**Todos los países: podio nacional → podio latinoamericano**\n')
tabla(['Tipo', 'Podios nacionales', 'Llegaron al podio latam', 'Conversión'], filas)
dig_ni = [f for f in ni1 if DIGITAL.search(f['titulo_video'])]
out.append('Títulos digitales en primeros lugares de sede de Nicaragua (muestra): ' +
           '; '.join(f"«{f['titulo_video'][:50]}»" for f in dig_ni[:12]) + '\n')

out.append('## 4. Categoría en las sedes de Nicaragua vs. podio nacional\n')
ni_nac = [f for f in G if f['pais'] == 'NICARAGUA' and f['nivel'] == 'nacional']
c = Counter((f['categoria'], f['puesto']) for f in ni_nac)
out.append('Los podios nacionales de Nicaragua se reparten 9 y 9 por categoría (3 por año). '
           'Los primeros lugares latinoamericanos nicaragüenses: Innovación 3 (2023, 2024, 2025) e Impacto Social 1 (2025).\n')

(RAIZ / 'datos' / 'analogos_2026.md').write_text('\n'.join(out), encoding='utf-8')
print('\n'.join(out))
