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

export interface AssetTypePayload {
  name: string
  code?: string
  remark?: string
}

export const listAssetTypes = (params: { page?: number; size?: number }) =>
  http.get<PageResponse<AssetType>>('/asset-types', { params })

export const createAssetType = (payload: AssetTypePayload) => http.post('/asset-types', payload)

export const updateAssetType = (id: number, payload: AssetTypePayload) => http.put(`/asset-types/${id}`, payload)

export const deleteAssetType = (id: number) => http.delete(`/asset-types/${id}`)

export const listVendors = (params: { page?: number; size?: number }) =>
  http.get<PageResponse<Vendor>>('/vendors', { params })

