export interface TokenResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  refreshExpiresIn: number
}

export interface LoginPayload {
  username: string
  password: string
}

export interface CurrentUser {
  id: number
  username: string
  fullName?: string
  email?: string
  departmentId?: number
  roles: string[]
  permissions: string[]
}

