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

export interface RolePayload {
  code: string
  name: string
  remark?: string
  permissions: string[]
}

export const listRoles = (params: { page?: number; size?: number }) =>
  http.get<PageResponse<any>>('/roles', { params })

export const createRole = (payload: RolePayload) => http.post('/roles', payload)

export const updateRole = (id: number, payload: RolePayload) => http.put(`/roles/${id}`, payload)

export const listPermissions = () => http.get<string[]>('/permissions/codes')

export const listAuditLogs = (params: { page?: number; size?: number }) =>
  http.get<PageResponse<AuditLog>>('/audit-logs', { params })

