import http from '@/api/http'
import type { AssetRequest, AssetRequestItem, Inventory } from '@/types/domain'
import type { PageResponse } from '@/types/api'

export const listAssetRequests = (params: { page?: number; size?: number }) =>
  http.get<PageResponse<AssetRequest>>('/asset-requests', { params })

export const createAssetRequest = (payload: {
  requesterId: number
  departmentId?: number
  remark?: string
  items: AssetRequestItem[]
}) => http.post<AssetRequest>('/asset-requests', payload)

export const approveAssetRequest = (id: number, payload: { approverId: number; result: string; comment?: string }) =>
  http.post<AssetRequest>(`/asset-requests/${id}/approve`, payload)

export const stockIn = (payload: { assetId: number; serialNo: string; location?: string }) =>
  http.post<Inventory>('/inventory/stock-in', payload)

export const checkoutInventory = (id: number, payload: { userId: number; remark?: string }) =>
  http.post<Inventory>(`/inventory/${id}/checkout`, payload)

export const returnInventory = (id: number, payload: { userId: number; remark?: string }) =>
  http.post<Inventory>(`/inventory/${id}/return`, payload)

export const listInventory = (params: { page?: number; size?: number }) =>
  http.get<PageResponse<Inventory>>('/inventory', { params })

