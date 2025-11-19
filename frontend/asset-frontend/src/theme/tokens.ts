export interface ThemeDefinition {
  palette: {
    primary: string
    primaryMuted: string
    primaryContrast: string
    accent: string
    accentMuted: string
    accentContrast: string
  }
  neutrals: Record<string, string>
  text: {
    primary: string
    secondary: string
    muted: string
    inverse: string
  }
  surface: {
    body: string
    card: string
    muted: string
    elevated: string
    glass: string
  }
  gradients: {
    hero: string
    accent: string
    subtle: string
  }
  semantic: {
    info: string
    success: string
    warning: string
    danger: string
  }
  border: {
    subtle: string
    medium: string
    bold: string
  }
  typography: {
    fontFamilyBase: string
    fontFamilyNumeric: string
    sizes: {
      xs: string
      sm: string
      md: string
      lg: string
      xl: string
      display: string
    }
    weights: {
      regular: number
      medium: number
      semibold: number
      bold: number
    }
    lineHeights: {
      tight: number
      snug: number
      relaxed: number
    }
  }
  spacing: {
    xs: string
    sm: string
    md: string
    lg: string
    xl: string
    xxl: string
  }
  radius: {
    sm: string
    md: string
    lg: string
    pill: string
  }
  shadow: {
    soft: string
    medium: string
    strong: string
  }
  motion: {
    fast: string
    base: string
    slow: string
  }
  layout: {
    contentMaxWidth: string
    sidebarWidth: string
    headerHeight: string
  }
  components: {
    cardPadding: string
    sectionGap: string
    borderWidth: string
  }
}

export type ThemeTokenPath = keyof ThemeDefinition

export const themeTokens: ThemeDefinition = {
  palette: {
    primary: '#4d67ff',
    primaryMuted: '#e0e7ff',
    primaryContrast: '#09123a',
    accent: '#2ec9ff',
    accentMuted: '#d4f3ff',
    accentContrast: '#042a34'
  },
  neutrals: {
    '50': '#f3f5fb',
    '100': '#e7ebf5',
    '200': '#d4dae8',
    '300': '#b1bdd4',
    '400': '#8a99b7',
    '500': '#627399',
    '600': '#4b597c',
    '700': '#3b4762',
    '800': '#262f40',
    '900': '#111827'
  },
  text: {
    primary: '#0f172a',
    secondary: '#475467',
    muted: '#98a2b3',
    inverse: '#ffffff'
  },
  surface: {
    body: '#f7f8fb',
    card: '#ffffff',
    muted: '#eef2ff',
    elevated: '#ffffff',
    glass: 'rgba(255, 255, 255, 0.72)'
  },
  gradients: {
    hero: 'linear-gradient(135deg, #4d67ff 0%, #5bc4ff 100%)',
    accent: 'linear-gradient(120deg, #4d67ff 0%, #7a5bff 50%, #2ec9ff 100%)',
    subtle: 'linear-gradient(145deg, rgba(77,103,255,0.08) 0%, rgba(46,201,255,0.12) 100%)'
  },
  semantic: {
    info: '#3b82f6',
    success: '#10b981',
    warning: '#f59e0b',
    danger: '#ef4444'
  },
  border: {
    subtle: 'rgba(15, 23, 42, 0.08)',
    medium: 'rgba(15, 23, 42, 0.14)',
    bold: 'rgba(15, 23, 42, 0.22)'
  },
  typography: {
    fontFamilyBase: "'Inter', 'HarmonyOS Sans', 'PingFang SC', 'Microsoft YaHei', sans-serif",
    fontFamilyNumeric: "'IBM Plex Sans', 'Inter', sans-serif",
    sizes: {
      xs: '12px',
      sm: '14px',
      md: '16px',
      lg: '20px',
      xl: '24px',
      display: '32px'
    },
    weights: {
      regular: 400,
      medium: 500,
      semibold: 600,
      bold: 700
    },
    lineHeights: {
      tight: 1.2,
      snug: 1.35,
      relaxed: 1.6
    }
  },
  spacing: {
    xs: '4px',
    sm: '8px',
    md: '16px',
    lg: '24px',
    xl: '32px',
    xxl: '48px'
  },
  radius: {
    sm: '6px',
    md: '12px',
    lg: '18px',
    pill: '999px'
  },
  shadow: {
    soft: '0 6px 24px rgba(15, 23, 42, 0.08)',
    medium: '0 12px 32px rgba(15, 23, 42, 0.12)',
    strong: '0 18px 48px rgba(15, 23, 42, 0.16)'
  },
  motion: {
    fast: '120ms cubic-bezier(0.4, 0, 0.2, 1)',
    base: '200ms cubic-bezier(0.4, 0, 0.2, 1)',
    slow: '320ms cubic-bezier(0.4, 0, 0.2, 1)'
  },
  layout: {
    contentMaxWidth: '1280px',
    sidebarWidth: '240px',
    headerHeight: '64px'
  },
  components: {
    cardPadding: '24px 28px',
    sectionGap: '32px',
    borderWidth: '1px'
  }
}

export type ThemeOverrides = Partial<ThemeDefinition>

