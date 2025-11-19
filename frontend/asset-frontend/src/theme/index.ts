import { themeTokens, type ThemeDefinition } from './tokens'

type Primitive = string | number | boolean

type DeepPartial<T> = {
  [K in keyof T]?: T[K] extends Primitive ? T[K] : DeepPartial<T[K]>
}

const cloneTheme = <T>(target: T): T => JSON.parse(JSON.stringify(target))

const mergeTheme = <T>(base: T, overrides?: DeepPartial<T>): T => {
  if (!overrides) return base
  Object.entries(overrides).forEach(([key, value]) => {
    if (value === undefined) {
      return
    }
    const typedKey = key as keyof T
    if (typeof value === 'object' && value !== null && typeof (base as any)[typedKey] === 'object') {
      ;(base as any)[typedKey] = mergeTheme((base as any)[typedKey], value as DeepPartial<any>)
    } else {
      ;(base as any)[typedKey] = value as T[keyof T]
    }
  })
  return base
}

const toCssVariable = (path: string[]) => `--ams-${path.join('-')}`

const applyCssVariables = (node: HTMLElement, tokens: Record<string, unknown>, path: string[] = []) => {
  Object.entries(tokens).forEach(([key, value]) => {
    const nextPath = [...path, key]
    if (typeof value === 'object' && value !== null) {
      applyCssVariables(node, value as Record<string, unknown>, nextPath)
    } else if (typeof value === 'string' || typeof value === 'number') {
      node.style.setProperty(toCssVariable(nextPath), String(value))
    }
  })
}

export const applyTheme = (overrides?: DeepPartial<ThemeDefinition>) => {
  if (typeof document === 'undefined') return themeTokens
  const nextTheme = overrides ? mergeTheme(cloneTheme(themeTokens), overrides) : themeTokens
  applyCssVariables(document.documentElement, nextTheme as unknown as Record<string, unknown>)
  return nextTheme
}

export type { ThemeDefinition, DeepPartial as ThemeOverrides }
export { themeTokens }

