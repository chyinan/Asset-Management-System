import http from '@/api/http'
import type { PageResponse } from '@/types/api'
import type { AuditLog } from '@/types/domain'

export interface UserPayload {
  username: string
  fullName?: string
  email?: string
  departmentId?: number
  enabled?: boolean
  roles: string[]
  password?: string
}

export const listUsers = (params: { page?: number; size?: number }) =>
  http.get<PageResponse<any>>('/users', { params })

export const createUser = (payload: UserPayload) => http.post('/users', payload)

export const listRoles = (params: { page?: number; size?: number }) =>
  http.get<PageResponse<any>>('/roles', { params })

export const listPermissions = () => http.get<string[]>('/permissions')

export const listAuditLogs = (params: { page?: number; size?: number }) =>
  http.get<PageResponse<AuditLog>>('/audit-logs', { params })

