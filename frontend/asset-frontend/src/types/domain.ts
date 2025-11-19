export interface Department {
  id: number
  name: string
  parentId?: number
  remark?: string
}

export interface AssetType {
  id: number
  name: string
  code?: string
  remark?: string
}

export interface Vendor {
  id: number
  name: string
  contact?: string
  phone?: string
  remark?: string
}

export interface AssetRequestItem {
  id?: number
  assetTypeId: number
  quantity: number
  purpose?: string
}

export interface AssetRequest {
  id: number
  requestNo: string
  requesterId: number
  departmentId?: number
  status: string
  createdAt: string
  remark?: string
  items: AssetRequestItem[]
}

export interface Inventory {
  id: number
  assetId: number
  assetName: string
  assetNo: string
  serialNo: string
  status: string
  location?: string
}

export interface AuditLog {
  id: number
  userId?: number
  action: string
  entity?: string
  entityId?: number
  detail?: string
  createdAt: string
}

