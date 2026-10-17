---
name: Sentinel Mobile Security
colors:
  surface: '#131C2E'
  surface-dim: '#0c1321'
  surface-bright: '#323949'
  surface-container-lowest: '#070e1c'
  surface-container-low: '#151b2a'
  surface-container: '#19202e'
  surface-container-high: '#232a39'
  surface-container-highest: '#2e3544'
  on-surface: '#dce2f6'
  on-surface-variant: '#c2c6d6'
  inverse-surface: '#dce2f6'
  inverse-on-surface: '#2a3040'
  outline: '#8c90a0'
  outline-variant: '#424654'
  surface-tint: '#b1c5ff'
  primary: '#b1c5ff'
  on-primary: '#002c71'
  primary-container: '#2f6feb'
  on-primary-container: '#fffeff'
  inverse-primary: '#0056cf'
  secondary: '#4ae176'
  on-secondary: '#003915'
  secondary-container: '#00b954'
  on-secondary-container: '#004119'
  tertiary: '#2fd9f4'
  on-tertiary: '#00363e'
  tertiary-container: '#008294'
  on-tertiary-container: '#ffffff'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#dae2ff'
  primary-fixed-dim: '#b1c5ff'
  on-primary-fixed: '#001947'
  on-primary-fixed-variant: '#00419f'
  secondary-fixed: '#6bff8f'
  secondary-fixed-dim: '#4ae176'
  on-secondary-fixed: '#002109'
  on-secondary-fixed-variant: '#005321'
  tertiary-fixed: '#a2eeff'
  tertiary-fixed-dim: '#2fd9f4'
  on-tertiary-fixed: '#001f25'
  on-tertiary-fixed-variant: '#004e5a'
  background: '#0c1321'
  on-background: '#dce2f6'
  surface-variant: '#2e3544'
  surface-elevated: '#1C2740'
  border-subtle: '#2A3654'
  text-primary: '#EAF0FB'
  text-muted: '#94A3BF'
  sentinel-light: '#4C8DFF'
  accent-purple: '#A78BFA'
  warning-amber: '#F5A524'
  alarm-red: '#FF5A5F'
  alarm-red-dark: '#D92D2D'
typography:
  display-lg:
    fontFamily: Sora
    fontSize: 32px
    fontWeight: '800'
    lineHeight: 40px
  headline-lg:
    fontFamily: Sora
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
  headline-md:
    fontFamily: Sora
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  headline-sm:
    fontFamily: Sora
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
  metric-lg:
    fontFamily: JetBrains Mono
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 34px
  metric-md:
    fontFamily: JetBrains Mono
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 22px
  code-sm:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  margin: 1.25rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

# Sentinel — Sistema de Diseño (Mobile Security)

## Visión e Identidad
Sentinel es una app móvil de seguridad para el hogar que convierte un teléfono viejo en un sensor centinela mediante acelerómetro y giroscopio. La interfaz se inspira en aplicaciones líderes de seguridad como McAfee o Bitdefender: limpia, tranquilizadora, directa y enfocada en estados de salud del sistema claros y accionables (Protegido / Vigilando, Advertencia, Alarma Crítica).

## Paleta de Colores
- **Noche (Background):** `#0B1220` (Fondo general dark mode puro)
- **Superficie (Cards / Containers):** `#131C2E`
- **Superficie 2 (Inputs / Elevated / Secondary buttons):** `#1C2740`
- **Borde:** `#2A3654`
- **Texto Principal:** `#EAF0FB`
- **Texto Suave / Muted:** `#94A3BF`

### Acentos y Roles
- **Azul Sentinel (Brand & Sentinelas emisores):** `#2F6FEB` (botones primarios) / `#4C8DFF` (íconos, insignias)
- **Cian (Receptores):** `#22D3EE`
- **Violeta (Vecinos de confianza):** `#A78BFA`
- **Verde Seguro (Vigilando / Todo Protegido):** `#22C55E`
- **Ámbar (Precaución / Batería baja / Calor / Vibración leve):** `#F5A524`
- **Rojo Alarma (Intento de intrusión):** `#FF5A5F` / `#D92D2D`

## Tipografía
- **Títulos y encabezados:** `Sora`, sans-serif (pesos 600, 700, 800)
- **Textos y botones:** `Inter`, sans-serif (pesos 400, 500, 600)
- **Códigos, números y métricas:** `JetBrains Mono`, monospace

## Componentes y Estilo
- **Radio de esquinas:** 18px en tarjetas y modales, 14px en botones.
- **Botones principales:** Mínimo 48px de altura, estados táctiles evidentes, pensados para uso nocturno o emergencias.
- **Navegación:** Barra inferior de 5 pestañas fijas (Inicio, Equipos, Horario, Vecinos, Alertas).
- **Indicador de Estado Central:** Anillo de seguridad con icono de escudo, similar a la protección de antivirus ("Tu hogar está vigilado / protegido").