"""Análisis cuantitativo de los ganadores 2023-2025 descargados de sirli.org.
Escribe datos/analisis_sirli.md con tablas por desafío, categoría, Nicaragua y la sede UNI-RUPAP.

Uso:  python herramientas/analizar.py
"""
import csv
from collections import Counter, defaultdict
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
G = list(csv.DictReader((RAIZ / 'datos' / 'ganadores_sirli_2023_2025.csv').open(encoding='utf-8-sig')))
D = {(d['anio'], d['num']): d['titulo'].capitalize().replace('tetra pak', 'Tetra Pak')
     for d in csv.DictReader((RAIZ / 'datos' / 'desafios_sirli_2023_2025.csv').open(encoding='utf-8-sig'))}
NIC = 'NICARAGUA'
out = []


def tabla(cab, filas):
    out.append('| ' + ' | '.join(cab) + ' |')
    out.append('|' + '|'.join('---' for _ in cab) + '|')
    for f in filas:
        out.append('| ' + ' | '.join(str(x) for x in f) + ' |')
    out.append('')


def pct(a, b):
    return f'{round(100 * a / b)} %' if b else '–'


def es_rupap(f):
    return 'RUPAP' in f['sede'].upper()


anios = sorted({f['anio'] for f in G}, reverse=True)

out.append('# Análisis de ganadores 2023–2025 (datos completos de sirli.org)\n')
out.append(f'Base: {len(G)} premios: '
           + ', '.join(f"{n} {k}" for k, n in Counter(f['nivel'] for f in G).items()) + '.\n')
out.append('Solo se conocen los equipos premiados (1.º a 3.º por categoría en cada nivel), no todos los participantes. '
           'Por eso «premios de sede» sirve como aproximación de cuántos equipos buenos eligieron cada desafío.\n')

# 1. Embudo por desafío (todos los países)
out.append('## 1. Embudo por desafío, todos los países\n')
out.append('**Conversión** = podios nacionales ÷ 1.os lugares de sede. Mide qué tan seguido un ganador de sede con ese desafío '
           'llega al podio de su país.\n')
for a in anios:
    out.append(f'### {a}\n')
    filas = []
    for (anio, num), titulo in sorted(D.items()):
        if anio != a:
            continue
        sel = [f for f in G if f['anio'] == a and f['desafio_num'] == num]
        sede = [f for f in sel if f['nivel'] == 'sede']
        sede1 = [f for f in sede if f['puesto'] == '1']
        nac = [f for f in sel if f['nivel'] == 'nacional']
        intl = [f for f in sel if f['nivel'] == 'internacional']
        filas.append((f'{num} {titulo}', len(sede), len(sede1), len(nac), pct(len(nac), len(sede1)),
                      len(intl) or '–', ', '.join(f"{f['puesto']}.º {f['categoria'][:3]}. {f['equipo'].title()} ({f['pais'].title()})" for f in intl) or ''))
    filas.sort(key=lambda x: (-(x[5] if x[5] != '–' else 0), -x[3]))
    tabla(['Desafío', 'Premios de sede', '1.os de sede', 'Podios nac.', 'Conversión', 'Podios latam', 'Quiénes (latam)'], filas)

# 2. Categoría por desafío
out.append('## 2. ¿A qué categoría se inclina cada desafío? (premios de sede, todos los países)\n')
out.append('El jurado de sede asigna la categoría según la solución. Si un desafío cae mayoritariamente en una categoría, '
           'las soluciones típicas de ese desafío «suenan» a esa categoría.\n')
filas = []
for (anio, num), titulo in sorted(D.items(), reverse=True):
    sede = [f for f in G if f['anio'] == anio and f['desafio_num'] == num and f['nivel'] == 'sede']
    inn = sum(1 for f in sede if f['categoria'] == 'Innovación')
    if sede:
        inclina = 'Innovación' if inn / len(sede) >= 0.6 else 'Impacto Social' if inn / len(sede) <= 0.4 else 'Ambas'
        filas.append((anio, f'{num} {titulo}', len(sede), pct(inn, len(sede)), pct(len(sede) - inn, len(sede)), inclina))
tabla(['Año', 'Desafío', 'Premios de sede', '% Innovación', '% Impacto Social', 'Se inclina a'], filas)

# 3. Nicaragua
out.append('## 3. Nicaragua\n')
for a in anios:
    nic = [f for f in G if f['anio'] == a and f['pais'] == NIC]
    sede1 = [f for f in nic if f['nivel'] == 'sede' and f['puesto'] == '1']
    out.append(f'### {a}: {len({f["sede"] for f in nic if f["nivel"] == "sede"})} sedes premiadas, '
               f'{len(sede1)} primeros lugares de sede compitieron a nivel nacional\n')
    filas = []
    for (anio, num), titulo in sorted(D.items()):
        if anio != a:
            continue
        s1 = [f for f in sede1 if f['desafio_num'] == num]
        nac = sorted([f for f in nic if f['nivel'] == 'nacional' and f['desafio_num'] == num],
                     key=lambda f: (f['categoria'], f['puesto']))
        filas.append((f'{num} {titulo}', len(s1), pct(len(s1), len(sede1)), len(nac),
                      '; '.join(f"{f['puesto']}.º {f['categoria'][:3]}. {f['equipo'].title()} ({f['sede'].title()})" for f in nac)))
    filas.sort(key=lambda x: (-x[3], -x[1]))
    tabla(['Desafío', '1.os de sede', '% del total', 'Podios nac.', 'Podio nacional'], filas)

# 4. Ganadores nacionales de Nicaragua y su resultado latinoamericano
out.append('## 4. Podio nacional de Nicaragua y su resultado latinoamericano\n')
filas = []
for f in sorted([f for f in G if f['pais'] == NIC and f['nivel'] == 'nacional'],
                key=lambda f: (f['anio'], f['categoria'], f['puesto']), reverse=False):
    intl = [g for g in G if g['nivel'] == 'internacional' and g['anio'] == f['anio'] and g['equipo'] == f['equipo']]
    filas.append((f['anio'], f['categoria'], f"{f['puesto']}.º", f['equipo'].title(), f['sede'].title(),
                  f"{f['desafio_num']} {D.get((f['anio'], f['desafio_num']), '')}",
                  f"{intl[0]['puesto']}.º latam" if intl else '–', f.get('titulo_video', '')))
filas.sort(key=lambda x: (x[0], x[1], x[2]), reverse=True)
tabla(['Año', 'Categoría', 'Puesto', 'Equipo', 'Sede', 'Desafío', 'Latam', 'Título del video'], filas)

# 5. Sede UNI-RUPAP
out.append('## 5. Su sede: UNI-RUPAP\n')
filas = []
for f in sorted([f for f in G if es_rupap(f) and f['nivel'] == 'sede'], key=lambda f: (f['anio'], f['categoria'], f['puesto']), reverse=True):
    sube = [g for g in G if g['equipo'] == f['equipo'] and g['anio'] == f['anio'] and g['nivel'] != 'sede']
    filas.append((f['anio'], f['categoria'], f"{f['puesto']}.º", f['equipo'].title(),
                  f"{f['desafio_num']} {D.get((f['anio'], f['desafio_num']), '')}",
                  ', '.join(f"{g['puesto']}.º {g['nivel']}" for g in sube) or '–', f.get('titulo_video', ''), f['video_url']))
filas.sort(key=lambda x: (x[0], x[1], x[2]))
filas.reverse()
tabla(['Año', 'Categoría', 'Puesto', 'Equipo', 'Desafío', 'Llegó a', 'Título del video', 'Video'], filas)

# 6. Universidades de Nicaragua: primeros lugares de sede y podios nacionales
def universidad(sede):
    s = sede.upper().replace('_', ' ').replace('-', ' ')
    for clave, nombre in (('UNAN', 'UNAN'), ('UNI ', 'UNI'), ('UAM', 'UAM'), ('URACCAN', 'URACCAN'), ('BICU', 'BICU'),
                          ('UNCSM', 'UNCSM'), ('AGRARIA', 'UNA (Agraria)'), ('MULTIDISCIPLINARIA', 'UNM'), ('UNICIT', 'UNICIT'),
                          ('VALDIVIESO', 'UNIAV'), ('UPOLI', 'UPOLI'), ('UCA', 'UCA')):
        if clave in s + ' ':
            return nombre
    return sede.title()


out.append('## 6. Universidades de Nicaragua: primeros lugares de sede y podios nacionales (2023–2025)\n')
s1 = Counter(universidad(f['sede']) for f in G if f['pais'] == NIC and f['nivel'] == 'sede' and f['puesto'] == '1')
pn = Counter(universidad(f['sede']) for f in G if f['pais'] == NIC and f['nivel'] == 'nacional')
tabla(['Universidad', '1.os de sede', 'Podios nacionales', 'Conversión'],
      sorted(((u, s1[u], pn[u], pct(pn[u], s1[u])) for u in s1), key=lambda x: (-x[2], -x[1]))[:14])
rup1 = [f for f in G if es_rupap(f) and f['nivel'] == 'sede' and f['puesto'] == '1']
rupn = [f for f in G if es_rupap(f) and f['nivel'] == 'nacional']
nic1 = [f for f in G if f['pais'] == NIC and f['nivel'] == 'sede' and f['puesto'] == '1']
nicn = [f for f in G if f['pais'] == NIC and f['nivel'] == 'nacional']
out.append(f'Sede UNI-RUPAP: {len(rupn)} podios nacionales de {len(rup1)} primeros lugares de sede ({pct(len(rupn), len(rup1))}). '
           f'Promedio de Nicaragua: {len(nicn)} de {len(nic1)} ({pct(len(nicn), len(nic1))}).\n')

# 7. Países en el podio latinoamericano
out.append('## 7. Países en el podio latinoamericano (2023–2025)\n')
c = defaultdict(Counter)
for f in G:
    if f['nivel'] == 'internacional':
        c[f['pais'].title()][f['puesto']] += 1
tabla(['País', '1.os', '2.os', '3.os', 'Total'],
      sorted(((p, v['1'], v['2'], v['3'], sum(v.values())) for p, v in c.items()), key=lambda x: (-x[1], -x[4])))

(RAIZ / 'datos' / 'analisis_sirli.md').write_text('\n'.join(out), encoding='utf-8')
print('\n'.join(out))
