// Genera el guion v4 del pitch de Centinel en Word (.docx)
const fs = require('fs');
const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell, WidthType, ShadingType,
  AlignmentType, HeadingLevel, PageOrientation, LevelFormat, BorderStyle, Footer, PageNumber,
  VerticalAlign, TableLayoutType,
} = require('docx');

const SALIDA = process.argv[2] || 'Guion_pitch_Centinel_v4.docx';
const AZUL = '0A62E8', AMARILLO = 'B98F00', ROJO = 'C62828', VERDE = '1B7F3B', GRIS = '5B6680', TINTA = '1A2233';
const FUENTE = 'Arial';

// texto con **negrita** y _cursiva_ simples
function runs(texto, base = {}) {
  const out = [];
  const partes = texto.split(/(\*\*[^*]+\*\*|_[^_]+_)/g).filter(Boolean);
  for (const p of partes) {
    if (p.startsWith('**')) out.push(new TextRun({ font: FUENTE, ...base, text: p.slice(2, -2), bold: true }));
    else if (p.startsWith('_') && p.endsWith('_') && p.length > 2) out.push(new TextRun({ font: FUENTE, ...base, text: p.slice(1, -1), italics: true }));
    else out.push(new TextRun({ text: p, font: FUENTE, ...base }));
  }
  return out;
}
const P = (texto, opt = {}) => new Paragraph({ children: runs(texto, opt.run || {}), spacing: { after: opt.after ?? 120, before: opt.before ?? 0 }, alignment: opt.align, ...(opt.p || {}) });
const H1 = t => new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: t, font: FUENTE })], spacing: { before: 240, after: 140 } });
const H2 = t => new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: t, font: FUENTE })], spacing: { before: 200, after: 100 } });
const viñeta = (texto, nivel = 0) => new Paragraph({ numbering: { reference: 'vinetas', level: nivel }, children: runs(texto), spacing: { after: 80 } });
const numerada = (texto, nivel = 0) => new Paragraph({ numbering: { reference: 'numeros', level: nivel }, children: runs(texto), spacing: { after: 80 } });
const casilla = texto => new Paragraph({ children: [new TextRun({ text: '☐  ', font: 'Segoe UI Symbol', size: 24 }), ...runs(texto)], spacing: { after: 100 }, indent: { left: 360, hanging: 360 } });

const borde = { style: BorderStyle.SINGLE, size: 4, color: 'C9D1E0' };
const bordes = { top: borde, bottom: borde, left: borde, right: borde };
function celda(contenido, ancho, { fondo, negrita = false, cabecera = false } = {}) {
  const parrafos = (Array.isArray(contenido) ? contenido : [contenido]).map(c =>
    typeof c === 'string'
      ? new Paragraph({ children: runs(c, cabecera ? { bold: true, color: 'FFFFFF', size: 20 } : { size: 19, bold: negrita }), spacing: { after: 60 } })
      : c);
  return new TableCell({
    children: parrafos, width: { size: ancho, type: WidthType.DXA }, borders: bordes,
    shading: fondo ? { type: ShadingType.CLEAR, color: 'auto', fill: fondo } : undefined,
    margins: { top: 80, bottom: 80, left: 110, right: 110 }, verticalAlign: VerticalAlign.TOP,
  });
}
function tabla(anchos, cabecera, filas) {
  const total = anchos.reduce((a, b) => a + b, 0);
  return new Table({
    width: { size: total, type: WidthType.DXA }, columnWidths: anchos, layout: TableLayoutType.FIXED,
    rows: [
      new TableRow({ tableHeader: true, children: cabecera.map((c, i) => celda(c, anchos[i], { fondo: '0B1220', cabecera: true })) }),
      ...filas.map((f, k) => new TableRow({ cantSplit: true, children: f.map((c, i) => celda(c, anchos[i], { fondo: f.fondo || (k % 2 ? 'F4F6FB' : undefined) })) })),
    ],
  });
}
// etiqueta REAL / ANIM dentro de la columna «Imagen»
const etiqueta = (tipo, resto) => new Paragraph({
  spacing: { after: 60 },
  children: [
    new TextRun({ text: tipo === 'real' ? ' REAL ' : ' ANIM ', bold: true, size: 17, color: 'FFFFFF', font: FUENTE, shading: { type: ShadingType.CLEAR, color: 'auto', fill: tipo === 'real' ? VERDE : AZUL } }),
    new TextRun({ text: '  ', size: 19 }),
    ...runs(resto, { size: 19 }),
  ],
});
const voz = t => new Paragraph({ children: runs(t, { size: 19, italics: true, color: TINTA }), spacing: { after: 60 } });
function cuadro(lineas, fill, colorBorde) {
  const b = { style: BorderStyle.SINGLE, size: 12, color: colorBorde };
  return new Table({
    width: { size: 9360, type: WidthType.DXA }, columnWidths: [9360],
    rows: [new TableRow({ children: [new TableCell({
      width: { size: 9360, type: WidthType.DXA }, borders: { top: b, bottom: b, left: b, right: b },
      shading: { type: ShadingType.CLEAR, color: 'auto', fill }, margins: { top: 140, bottom: 140, left: 200, right: 200 },
      children: lineas,
    })] })],
  });
}
const pie = new Footer({ children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [
  new TextRun({ text: 'Centinel · Equipo Nanobots · UNI-RUPAP · Guion v4 — página ', size: 16, color: GRIS, font: FUENTE }),
  new TextRun({ children: [PageNumber.CURRENT], size: 16, color: GRIS, font: FUENTE }),
] })] });

const CARTA = { width: 12240, height: 15840 };
const margen = { top: 1080, bottom: 1080, left: 1440, right: 1440 };

// ---------- sección 1: portada e indicaciones generales ----------
const s1 = [
  new Paragraph({ children: [new TextRun({ text: 'Guion del pitch de Centinel', bold: true, size: 48, color: AZUL, font: FUENTE })], spacing: { after: 80 } }),
  new Paragraph({ children: [new TextRun({ text: 'Versión 4 · para grabar · Equipo Nanobots · UNI-RUPAP · Rally Latinoamericano de Innovación 2026 · Desafío 02', size: 20, color: GRIS, font: FUENTE })], spacing: { after: 280 },
    border: { bottom: { style: BorderStyle.SINGLE, size: 8, color: AZUL, space: 6 } } }),
  H2('Quién hace qué'),
  viñeta('**Ustedes graban** las tomas reales y la voz, siguiendo este guion.'),
  viñeta('**Yo edito todo:** animaciones del soporte con su CAD, cortes, textos en pantalla, efectos de sonido, música libre, mezcla, subtítulos y exportación final.'),
  P('', { after: 120 }),
  cuadro([
    P('**Un riesgo, para que lo decidan sabiendo**', { run: { color: '7A5600' } }),
    viñeta('Las bases de 2026 dicen que «en el video deberá mostrarse un prototipo … elaborado con material reciclable y a escala, verificando con el movimiento de una persona».'),
    viñeta('Con solo animación, el jurado podría descontar puntos.'),
    viñeta('**Lo mínimo para cubrirse (5 minutos, opcional):** en la toma B1, el celular viejo va dentro de una cajita de cartón reciclado pegada a la puerta, y alguien lo coloca ahí en cámara. No tiene que ser bonito.'),
  ], 'FFF7DB', 'E0B000'),
  P('', { after: 120 }),
  H2('Datos del video'),
  viñeta('**Duración:** 1:55 en total (el máximo de las bases es 2:00).'),
  viñeta('**Voz:** unas 232 palabras, grabadas aparte en 5 archivos (ver «Texto para leer»).'),
  new Paragraph({ numbering: { reference: 'vinetas', level: 0 }, spacing: { after: 80 }, children: [
    new TextRun({ text: 'En la tabla del guion: ', bold: true, font: FUENTE }),
    new TextRun({ text: ' REAL ', bold: true, size: 17, color: 'FFFFFF', font: FUENTE, shading: { type: ShadingType.CLEAR, color: 'auto', fill: VERDE } }),
    new TextRun({ text: ' lo graban ustedes;  ', font: FUENTE }),
    new TextRun({ text: ' ANIM ', bold: true, size: 17, color: 'FFFFFF', font: FUENTE, shading: { type: ShadingType.CLEAR, color: 'auto', fill: AZUL } }),
    new TextRun({ text: ' lo hago yo.', font: FUENTE }),
  ] }),
];

// ---------- sección 2 (horizontal): el guion ----------
const A = [1050, 1450, 4100, 4100, 2980];   // 13680 = ancho útil en horizontal
const filaGuion = (tiempo, bloque, imagen, textoVoz, pantalla) => [tiempo, `**${bloque}**`, imagen, [voz(textoVoz)], pantalla];
const FILAS = [
  filaGuion('0:00–0:15', '1 · Gancho', [etiqueta('real', '**Toma B1:** puerta de un salón de la UNI con la luz apagada. Un compañero «duerme» en una silla con su celular en la mesa. Otro fuerza la puerta desde afuera. El celular se enciende en rojo («¡INTENTO DE ENTRADA!»), suena la sirena y él despierta.')],
    '(4 s de sirena, sin voz) Medianoche. Fuerzan tu puerta y tu familia duerme. Una alarma comercial cuesta de 150 a 300 dólares; por eso, en muchos barrios, nadie se entera a tiempo.', '«12:00 a. m.» · «Alarma comercial: US$150–300» y su fuente'),
  filaGuion('0:15–0:24', '2 · Residuo', [etiqueta('real', '**Toma B2:** alguien abre una gaveta llena de celulares viejos y saca uno.')],
    'En América Latina solo el 3 % de la basura electrónica se gestiona bien, y millones de celulares viejos duermen en cajones.', '«5.300 M celulares desechados o guardados (News24, 2022)» · «AL: solo 3 % bien gestionado (UNITAR, 2022)»'),
  filaGuion('0:24–0:32', '2 · Propuesta', [etiqueta('anim', '**Animación A, el soporte:** su lámina CAD completa, acercamiento a la vista 3D y vista frontal con el celular entrando acostado por arriba. Si me pasan la animación de la caja, entra al final (2 s).')],
    'Creamos Centinel: un soporte de PET reciclado que convierte ese celular en un sensor de seguridad.', '«Diseño CAD · 14 × 8 × 3,5 cm · rPET» · «Rótula + ventilación C» · «CENTINEL — Equipo Nanobots (UNI-RUPAP)»'),
  filaGuion('0:32–0:38', '3 · Instalación', [etiqueta('anim', '**Animación B:** el soporte se atornilla en una puerta, una ventana y un portón.')],
    'Se atornilla en la puerta, la ventana o el portón.', '«Puerta · Ventana · Portón»'),
  filaGuion('0:38–0:48', '3 · Detección', [etiqueta('anim', '**Animación C:** forcejean el portón y salen ondas («acelerómetro»); el portón se abre («giroscopio»); la alerta viaja a 2 celulares.')],
    'Con la pantalla apagada, el acelerómetro siente golpes y forcejeos, y el giroscopio, cuando se abre. La app aprende a ignorar la lluvia o las mascotas,', '«Acelerómetro + giroscopio · pantalla apagada» · «App Android nativa · Kotlin»'),
  filaGuion('0:48–0:58', '3 · Prueba real', [etiqueta('real', '**Toma B3:** 2 celulares en la mesa (familia y vecino). Alguien golpea la puerta donde está el Centinela y, en el mismo plano, los dos se ponen en rojo. **Sin cortes.**')],
    'y en segundos avisa a la familia y a los vecinos de la cuadra. Sin cámaras ni micrófonos.', '«Alerta real en segundos» · «0 cámaras · 0 micrófonos»'),
  filaGuion('0:58–1:18', '4 · App y batería', [etiqueta('real', '**Grabación de pantalla G1:** Horario (11 p. m.–4 a. m.), Carga diaria, Vecinos de confianza y el dato de temperatura.')],
    'Desde la app, la familia elige el horario de vigilancia, la hora de carga y a sus vecinos de confianza. Y como son baterías viejas, Centinel vigila su temperatura: si pasa de 45 grados, avisa de inmediato para desconectar el cargador.', '«Vigilancia 11 p. m.–4 a. m. · Carga diaria» · «Batería > 45 °C → aviso»'),
  filaGuion('1:18–1:42', '5 · Números', [etiqueta('anim', '**Animación D:** tabla y cifras animadas,'), etiqueta('real', 'sobre la **toma B4** (el equipo trabajando: laptop con el CAD y la app).')],
    'Fabricar el kit cuesta 4,50 dólares y lo vendemos a 10, sin mensualidades: de 15 a 30 veces menos que una alarma. Con una inversión inicial de 3.000 dólares, el primer año protegemos 1.200 hogares, con ganancia, y son 1.200 celulares que no van a la basura.', '«Costo US$4,50 · Precio US$10 · Mensualidad US$0» · «Inversión US$3.000 · Año 1: 1.200 hogares» · «ODS 11 y 12»'),
  filaGuion('1:42–1:55', '5 · Cierre', [etiqueta('real', '**Toma B5:** el equipo completo de frente, en la UNI. Uno sostiene el celular con la app en «VIGILANDO».')],
    'Somos Sistemas, Arquitectura y Economía de la UNI. Empezamos en Villa Progreso, y puede llegar a cualquier barrio de Latinoamérica. Centinel: tu comunidad, tu escudo.', 'Logo · «Equipo Nanobots · UNI-RUPAP · Desafío 02 · Rally Latinoamericano de Innovación 2026»'),
];
const s2 = [
  H1('El guion'),
  tabla(A, ['Tiempo', 'Bloque', 'Imagen', 'Voz (se graba aparte)', 'Texto en pantalla (lo pongo yo)'], FILAS),
];

// ---------- sección 3: texto para leer y cómo grabar ----------
const VOCES = [
  ['VOZ_1', 'Bloque 1 · Gancho', '0:04–0:15 (unos 11 s; empieza después de 4 s de sirena)', 'Medianoche. Fuerzan tu puerta y tu familia duerme. Una alarma comercial cuesta de 150 a 300 dólares; por eso, en muchos barrios, nadie se entera a tiempo.'],
  ['VOZ_2', 'Bloque 2 · Residuo y propuesta', '0:15–0:32 (unos 17 s)', 'En América Latina solo el 3 % de la basura electrónica se gestiona bien, y millones de celulares viejos duermen en cajones. Creamos Centinel: un soporte de PET reciclado que convierte ese celular en un sensor de seguridad.'],
  ['VOZ_3', 'Bloque 3 · Cómo funciona', '0:32–0:58 (unos 26 s)', 'Se atornilla en la puerta, la ventana o el portón. Con la pantalla apagada, el acelerómetro siente golpes y forcejeos, y el giroscopio, cuando se abre. La app aprende a ignorar la lluvia o las mascotas, y en segundos avisa a la familia y a los vecinos de la cuadra. Sin cámaras ni micrófonos.'],
  ['VOZ_4', 'Bloque 4 · App y batería', '0:58–1:18 (unos 20 s)', 'Desde la app, la familia elige el horario de vigilancia, la hora de carga y a sus vecinos de confianza. Y como son baterías viejas, Centinel vigila su temperatura: si pasa de 45 grados, avisa de inmediato para desconectar el cargador.'],
  ['VOZ_5', 'Bloque 5 · Números y cierre', '1:18–1:55 (unos 37 s)', 'Fabricar el kit cuesta 4,50 dólares y lo vendemos a 10, sin mensualidades: de 15 a 30 veces menos que una alarma. Con una inversión inicial de 3.000 dólares, el primer año protegemos 1.200 hogares, con ganancia, y son 1.200 celulares que no van a la basura. Somos Sistemas, Arquitectura y Economía de la UNI. Empezamos en Villa Progreso, y puede llegar a cualquier barrio de Latinoamérica. Centinel: tu comunidad, tu escudo.'],
];
const s3 = [
  H1('Texto para leer (voz)'),
  P('Un archivo por bloque. Lean cada bloque **2 veces seguidas**, con 2 segundos de silencio en medio. Pueden cambiar alguna palabra para que suene más suyo, pero **no cambien las cifras**.', { run: { color: GRIS } }),
  ...VOCES.flatMap(([arch, titulo, tiempo, texto]) => [
    new Paragraph({ spacing: { before: 220, after: 60 }, keepNext: true, children: [
      new TextRun({ text: arch + '.m4a', bold: true, size: 24, color: AZUL, font: FUENTE }),
      new TextRun({ text: '   ' + titulo + ' · ' + tiempo, size: 20, color: GRIS, font: FUENTE }),
    ] }),
    new Paragraph({ children: [new TextRun({ text: texto, size: 28, font: FUENTE, color: TINTA })], spacing: { after: 120, line: 360 } }),
  ]),

  H1('Cómo grabar (Pixel 7 Pro)'),
  H2('Ajustes de la cámara'),
  viñeta('Video **1080p a 30 fps**, en **horizontal**.'),
  viñeta('En 1080p los archivos pesan menos y se suben rápido. No usen 4K.', 1),
  viñeta('Estabilización **activada**, lente 1x (2x para los primeros planos).'),
  viñeta('Antes de grabar, toquen la pantalla y **bloqueen la exposición** (mantener presionado).'),
  viñeta('Graben cada toma **2 o 3 veces**, con 2 segundos de margen antes y después de la acción.'),
  H2('Tomas reales'),
  tabla([1500, 3200, 4660], ['Toma', 'Qué grabar', 'Detalle'], [
    ['**B1** Gancho', 'Puerta, compañero dormido y alarma', 'El celular viejo con Centinel va **pegado con cinta por dentro de la puerta**, así la alarma es real. Celular del receptor en la mesa con el volumen al máximo. Un solo plano donde se vean la puerta y el compañero. Luz apagada, con algo de luz de pasillo.'],
    ['**B2** Gaveta', 'Una gaveta con 4 o más celulares viejos y alguien que saca uno', 'Primer plano, 5 s.'],
    ['**B3** Prueba real', 'Puerta golpeada y 2 celulares que se ponen rojos', 'Los 2 receptores (familia y vecino) en la mesa, frente a la cámara, y la puerta al fondo. **Sin cortes.** Antes, calibren el Centinela en la puerta con sensibilidad **Alta** y toquen una vez la pantalla de cada celular.'],
    ['**B4** Trabajo', '2 o 3 compañeros con la laptop (CAD y código) y el celular', '10 s, tomas de apoyo.'],
    ['**B5** Cierre', 'El equipo completo de frente, sonriendo', '10 s, y otra toma en la que digan juntos «¡Centinel!» (opcional).'],
    ['**G1** Pantalla', 'Grabación de pantalla de la app (deslicen hacia abajo → «Grabar pantalla»)', 'Horario, Carga diaria, Vecinos y Centinela con temperatura. Muévanse despacio, 20–30 s en total.'],
  ]),

  H1('Cómo grabar la voz'),
  P('Es lo más importante para «venderlo».', { run: { color: GRIS } }),
  numerada('**Lugar:** un cuarto pequeño y callado (un clóset con ropa es ideal), sin ventilador.'),
  numerada('**Cómo:** con la grabadora del celular, a un palmo de la boca y apuntando un poco de lado.'),
  numerada('**Un archivo por bloque:** VOZ_1.m4a, VOZ_2.m4a … VOZ_5.m4a, como en «Texto para leer».'),
  numerada('**Tono:** firme y con energía, como si le contaran a un amigo algo que les importa. Sonrían al hablar, que se nota en la voz. Si quieren, que hable una persona en los bloques 1–3 y otra en los bloques 4–5.'),
  numerada('**Ritmo:** cada bloque debe durar lo que dice su tiempo. Practíquenlo una vez con cronómetro.'),
  numerada('**Redacción:** si cambian una palabra para que suene más suyo, perfecto (regla anti-IA). Solo no cambien las cifras.'),

  H1('Cómo me pasan el material'),
  viñeta('**Nombres:** B1_toma1.mp4, B1_toma2.mp4, B3_toma1.mp4, G1.mp4, VOZ_1.m4a…'),
  viñeta('**Por dónde:**'),
  viñeta('**Google Drive** (lo mejor para archivos pesados): una carpeta llamada **«Centinel video»**, y me avisan cuando esté lista;', 1),
  viñeta('o adjunten los archivos en el chat.', 1),
  viñeta('**Agreguen también:**'),
  viñeta('la **animación de la caja**;', 1),
  viñeta('el **logo de Centinel** en PNG, si tienen una versión distinta a la de la app;', 1),
  viñeta('la **fuente del precio de las alarmas**.', 1),

  H1('Lo que hago yo en la edición'),
  viñeta('**Animaciones con su CAD:** A, el soporte y el celular entrando; B, la instalación en puerta, ventana y portón; C, forcejeo, apertura y alerta a familia y vecinos; D, las cifras. Todas llevan la etiqueta «Animación del diseño final».'),
  viñeta('**Cortes al ritmo de la voz**, subtítulos, textos en pantalla y fuentes de los datos.'),
  viñeta('**Sonido:** efectos (golpes, sirena, notificaciones, transiciones), música libre de derechos (Mixkit) bajo la voz y mezcla a volumen estándar.'),
  viñeta('**Entrega:** MP4 1080p de 2:00 o menos, listo para subir a YouTube, con el título sugerido y la descripción.'),

  H1('Pendientes de ustedes'),
  casilla('Fuente del precio de las alarmas (US$150–300).'),
  casilla('Revisar la cuenta de utilidad del año 1: US$2.961 sobre US$12.000 es 24,7 %, no 24,1 %. No sale en la voz, pero sí en el reporte.'),
  casilla('Confirmar que **Villa Progreso** es su piloto.'),
  casilla('Confirmar que el celular entra **acostado, por arriba** del soporte (así lo voy a animar).'),
  casilla('En las referencias del reporte, declarar que las animaciones se hicieron con ayuda de IA (Claude) a partir de su diseño CAD.'),
];

const doc = new Document({
  creator: 'Equipo Nanobots', title: 'Guion del pitch de Centinel (v4)',
  styles: {
    default: { document: { run: { font: FUENTE, size: 21, color: TINTA } } },
    paragraphStyles: [
      { id: 'Heading1', name: 'Heading 1', basedOn: 'Normal', next: 'Normal', quickFormat: true, run: { size: 32, bold: true, color: AZUL, font: FUENTE }, paragraph: { spacing: { before: 240, after: 140 }, outlineLevel: 0 } },
      { id: 'Heading2', name: 'Heading 2', basedOn: 'Normal', next: 'Normal', quickFormat: true, run: { size: 24, bold: true, color: TINTA, font: FUENTE }, paragraph: { spacing: { before: 200, after: 100 }, outlineLevel: 1 } },
    ],
  },
  numbering: { config: [
    { reference: 'vinetas', levels: [
      { level: 0, format: LevelFormat.BULLET, text: '•', alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 360, hanging: 260 } } } },
      { level: 1, format: LevelFormat.BULLET, text: '–', alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 760, hanging: 260 } } } },
    ] },
    { reference: 'numeros', levels: [
      { level: 0, format: LevelFormat.DECIMAL, text: '%1.', alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 400, hanging: 300 } } } },
    ] },
  ] },
  sections: [
    { properties: { page: { size: CARTA, margin: margen } }, footers: { default: pie }, children: s1 },
    { properties: { page: { size: { ...CARTA, orientation: PageOrientation.LANDSCAPE }, margin: { top: 1080, bottom: 1080, left: 1080, right: 1080 } } }, footers: { default: pie }, children: s2 },
    { properties: { page: { size: CARTA, margin: margen } }, footers: { default: pie }, children: s3 },
  ],
});
Packer.toBuffer(doc).then(b => { fs.writeFileSync(SALIDA, b); console.log('ok', SALIDA, b.length, 'bytes'); });
