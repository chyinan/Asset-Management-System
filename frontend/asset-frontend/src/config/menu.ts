import type { Component } from 'vue'
import {
  Avatar,
  Box,
  Collection,
  CollectionTag,
  DataAnalysis,
  Memo,
  Message,
  OfficeBuilding,
  Setting,
  Tickets
} from '@element-plus/icons-vue'

export interface NavigationItem {
  label: string
  path?: string
  icon: Component
  badge?: string
  permission?: string | string[]
  anyOf?: string[]
  children?: NavigationItem[]
}

export const navigationConfig: NavigationItem[] = [
  { label: '仪表盘', path: '/', icon: DataAnalysis },
  { label: '资产申请', path: '/requests', icon: Tickets, permission: 'asset:view' },
  {
    label: '库存管理',
    path: '/inventory',
    icon: Box,
    permission: 'asset:stockin'
  },
  {
    label: '系统管理',
    icon: Setting,
    children: [
      { label: '用户管理', path: '/system/users', icon: Avatar, permission: 'user:manage' },
      { label: '角色管理', path: '/system/roles', icon: CollectionTag, permission: 'role:manage' },
      { label: '部门管理', path: '/system/departments', icon: OfficeBuilding, permission: 'asset:admin' },
      { label: '资产类型', path: '/system/asset-types', icon: Collection, permission: 'asset:admin' },
      { label: '提醒邮箱', path: '/system/reminder-settings', icon: Message, permission: 'asset:admin' }
    ]
  },
  { label: '审计日志', path: '/audit', icon: Memo, permission: 'audit:view' }
]

