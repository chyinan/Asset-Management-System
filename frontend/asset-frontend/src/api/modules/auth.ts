import http from '@/api/http'
import type { LoginPayload, TokenResponse } from '@/types/auth'

export const login = (payload: LoginPayload) => http.post<TokenResponse>('/auth/login', payload)

export const refresh = (refreshToken: string) =>
  http.post<TokenResponse>('/auth/refresh', { refreshToken })

