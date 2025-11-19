import http from '@/api/http'
import type { Department, AssetType, Vendor } from '@/types/domain'
import type { PageResponse } from '@/types/api'

export interface DepartmentPayload {
  name: string
  parentId?: number | null
  remark?: string
}

export const listDepartments = (params: { page?: number; size?: number }) =>
  http.get<PageResponse<Department>>('/departments', { params })

export const createDepartment = (payload: DepartmentPayload) => http.post('/departments', payload)

export const updateDepartment = (id: number, payload: DepartmentPayload) => http.put(`/departments/${id}`, payload)

export const deleteDepartment = (id: number) => http.delete(`/departments/${id}`)

export const listAssetTypes = (params: { page?: number; size?: number }) =>
  http.get<PageResponse<AssetType>>('/asset-types', { params })

export const listVendors = (params: { page?: number; size?: number }) =>
  http.get<PageResponse<Vendor>>('/vendors', { params })

