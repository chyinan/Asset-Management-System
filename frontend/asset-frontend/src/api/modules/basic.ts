import http from '@/api/http'
import type { Department, AssetType, Vendor } from '@/types/domain'
import type { PageResponse } from '@/types/api'

export const listDepartments = (params: { page?: number; size?: number }) =>
  http.get<PageResponse<Department>>('/departments', { params })

export const listAssetTypes = (params: { page?: number; size?: number }) =>
  http.get<PageResponse<AssetType>>('/asset-types', { params })

export const listVendors = (params: { page?: number; size?: number }) =>
  http.get<PageResponse<Vendor>>('/vendors', { params })

