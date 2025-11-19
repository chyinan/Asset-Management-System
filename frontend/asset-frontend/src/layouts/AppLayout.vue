<template>
  <div class="app-shell">
    <a href="#main-content" class="skip-link">跳到主内容</a>
    <aside :class="['shell-sidebar', { collapsed: isSidebarCollapsed }]">
      <div class="sidebar-brand">
        <div class="brand-copy">
          <p class="brand-title">资产管理系统</p>
          <p class="brand-subtitle">Asset Control Suite</p>
        </div>
      </div>
      <el-scrollbar class="sidebar-scroll">
        <el-menu
          class="sidebar-menu"
          :default-active="activeRoute"
          :collapse="isSidebarCollapsed"
          :collapse-transition="false"
          router
        >
          <template v-for="item in navigations" :key="item.path || item.label">
            <el-sub-menu v-if="item.children?.length" :index="item.label">
              <template #title>
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.label }}</span>
              </template>
              <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path!">
                <el-icon><component :is="child.icon" /></el-icon>
                <span>{{ child.label }}</span>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="item.path!">
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
              <el-tag v-if="item.badge && !isSidebarCollapsed" size="small" type="info" class="menu-badge">
                {{ item.badge }}
              </el-tag>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
      <footer class="sidebar-footer" v-if="!isSidebarCollapsed">
        <p class="footer-title">欢迎，{{ authStore.username || '访客' }}</p>
        <p class="footer-desc">保持资产流转透明，随时掌控</p>
      </footer>
    </aside>
    <div class="shell-main">
      <header class="shell-header" :class="{ 'is-compact': !showHero }">
        <div class="header-meta" v-if="showHero">
          <p class="header-eyebrow">{{ currentSection }}</p>
          <div class="title-group">
            <h1>{{ pageMeta.title }}</h1>
            <p v-if="pageMeta.description" class="title-desc">{{ pageMeta.description }}</p>
          </div>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumb" :key="item.label" :to="item.path || undefined">
              {{ item.label }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div v-else class="header-spacer" aria-hidden="true"></div>
        <div class="header-actions">
          <el-dropdown>
            <span class="user-chip">
              <el-avatar size="small">{{ initials }}</el-avatar>
              <span class="user-name">{{ authStore.username || '未登录' }}</span>
              <el-icon><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goProfile">个人设置</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <main id="main-content" class="shell-content">
        <div class="content-wrapper">
          <RouterView />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, type Component } from 'vue'
import { useRoute, useRouter, RouterView } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { usePermission } from '@/utils/permission'
import { Box, CaretBottom, DataAnalysis, Expand, Fold, Memo, Setting, Tickets, UserFilled } from '@element-plus/icons-vue'

interface NavigationItem {
  label: string
  path?: string
  icon: Component
  badge?: string
  permission?: string | string[]
  anyOf?: string[]
  children?: NavigationItem[]
}

const navigationConfig: NavigationItem[] = [
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
      { label: '用户管理', path: '/system/users', icon: UserFilled, permission: 'user:manage' },
      { label: '角色管理', path: '/system/roles', icon: UserFilled, permission: 'role:manage' },
      { label: '部门管理', path: '/system/departments', icon: UserFilled, permission: 'asset:admin' },
      { label: '资产类型', path: '/system/asset-types', icon: UserFilled, permission: 'asset:admin' }
    ]
  },
  { label: '审计日志', path: '/audit', icon: Memo, permission: 'audit:view' }
]

const pageCopy: Record<
  string,
  {
    title: string
    description?: string
    section: string
  }
> = {
  '/': {
    title: '仪表盘总览',
    description: '掌握资产流转、库存状态与审批进度',
    section: '运营总览'
  },
  '/requests': {
    title: '资产申请中心',
    description: '发起、审批并跟踪资产申请进度',
    section: '资产流程'
  },
  '/inventory': {
    title: '库存与流转',
    description: '实时查看库存结构与出入库明细',
    section: '资产流程'
  },
  '/system/users': {
    title: '用户权限',
    description: '为不同角色配置访问能力与安全策略',
    section: '系统管理'
  },
  '/system/roles': {
    title: '角色模型',
    description: '定义角色矩阵，保持权限可控',
    section: '系统管理'
  },
  '/system/departments': {
    title: '部门架构',
    description: '维护组织结构，支撑审批链路',
    section: '系统管理'
  },
  '/system/asset-types': {
    title: '资产类型库',
    description: '规范资产分类，统一数据字典',
    section: '系统管理'
  },
  '/audit': {
    title: '审计日志',
    description: '追踪关键操作，满足合规与审计要求',
    section: '审计与风控'
  }
}

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const { hasPermission } = usePermission()
const isSidebarCollapsed = ref(false)

const canAccess = (item: NavigationItem) => {
  if (item.permission && !hasPermission(item.permission)) return false
  if (item.anyOf && !item.anyOf.some((code) => hasPermission(code))) return false
  return true
}

const filterNav = (items: NavigationItem[]): NavigationItem[] =>
  items
    .map((item) => {
      if (item.children?.length) {
        const children = filterNav(item.children)
        return children.length ? { ...item, children } : null
      }
      return canAccess(item) ? item : null
    })
    .filter(Boolean) as NavigationItem[]

const navigations = computed(() => filterNav(navigationConfig))

const flattenNav = (items: NavigationItem[]): NavigationItem[] =>
  items.flatMap((item) =>
    item.children?.length ? flattenNav(item.children) : item.path ? [item] : []
  )

const navTrail = (items: NavigationItem[], target: string, parents: NavigationItem[] = []): NavigationItem[] | null => {
  for (const item of items) {
    const nextParents = [...parents, item]
    if (item.path === target) return nextParents
    if (item.children) {
      const found = navTrail(item.children, target, nextParents)
      if (found) return found
    }
  }
  return null
}

const activeRoute = computed(() => route.path)

const pageMeta = computed(() => {
  const meta = pageCopy[route.path]
  if (meta) return meta
  const flat = flattenNav(navigationConfig)
  const target = flat.find((item) => item.path === route.path)
  return {
    title: target?.label || (route.name?.toString() ?? '页面'),
    description: '',
    section: '资产管理'
  }
})

const breadcrumb = computed(() => {
  const trail = navTrail(navigationConfig, route.path) ?? []
  if (!trail.length) {
    return [{ label: pageMeta.value.title, path: route.path }]
  }
  return trail.map((item) => ({
    label: item.label,
    path: item.path
  }))
})

const currentSection = computed(() => pageMeta.value.section)

const showHero = computed(() => {
  const metaValue = route.meta.layoutHero
  if (typeof metaValue === 'boolean') return metaValue
  return false
})

const initials = computed(() => {
  const username = authStore.username || 'User'
  return username
    .split(/\s+/)
    .filter(Boolean)
    .map((part) => part[0]?.toUpperCase())
    .join('')
    .slice(0, 2)
})

const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}

const goProfile = () => {
  if (hasPermission('user:manage')) {
    router.push('/system/users')
  }
}

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  background: var(--ams-surface-body, #f7f8fb);
}

.shell-sidebar {
  width: var(--ams-layout-sidebarWidth, 240px);
  background: radial-gradient(circle at top, rgba(77, 103, 255, 0.35), rgba(8, 13, 36, 0.95)),
    #0b1224;
  color: #f8fbff;
  display: flex;
  flex-direction: column;
  transition: width var(--ams-motion-base, 200ms ease);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  height: 100vh;
  position: sticky;
  top: 0;
}

.shell-sidebar.collapsed {
  width: 88px;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 22px 18px 12px;
}

.brand-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.brand-subtitle {
  margin: 2px 0 0;
  font-size: 12px;
  color: rgba(248, 251, 255, 0.65);
}

.sidebar-toggle {
  margin-left: auto;
  color: rgba(255, 255, 255, 0.7);
}

.sidebar-scroll {
  flex: 1;
  padding: 0 12px 16px;
}

.shell-sidebar.collapsed .sidebar-footer {
  display: none;
}

.sidebar-menu {
  background: transparent;
  border-right: none;
}

:deep(.sidebar-menu .el-menu-item),
:deep(.sidebar-menu .el-sub-menu__title) {
  border-radius: var(--ams-radius-md, 12px);
  margin: 4px 0;
  height: 44px;
  color: rgba(248, 251, 255, 0.86);
}

:deep(.sidebar-menu .el-menu-item:hover),
:deep(.sidebar-menu .el-sub-menu__title:hover) {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

:deep(.sidebar-menu .el-menu--inline) {
  background: transparent;
}

:deep(.sidebar-menu .el-menu--inline .el-menu-item) {
  background: transparent;
}

:deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
}

.menu-badge {
  margin-left: auto;
}

.sidebar-footer {
  padding: 18px 22px 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  font-size: 13px;
  color: rgba(248, 251, 255, 0.8);
}

.footer-title {
  margin: 0 0 4px;
  font-weight: 600;
}

.footer-desc {
  margin: 0;
  color: rgba(248, 251, 255, 0.64);
}

.shell-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--ams-surface-body, #f7f8fb);
}

.shell-header {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 24px 36px;
  background: var(--ams-surface-card, #ffffff);
  border-bottom: 1px solid var(--ams-border-subtle, rgba(15, 23, 42, 0.08));
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.04);
}

.header-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.header-eyebrow {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--ams-text-muted, #98a2b3);
}

.title-group h1 {
  margin: 0;
  font-size: 26px;
  font-weight: 600;
  color: var(--ams-text-primary, #0f172a);
}

.title-desc {
  margin: 6px 0 0;
  color: var(--ams-text-secondary, #475467);
  font-size: 14px;
}

:deep(.el-breadcrumb__inner) {
  color: var(--ams-text-muted, #98a2b3);
}

.header-spacer {
  flex: 1;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon-btn {
  color: var(--ams-text-muted, #98a2b3);
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: var(--ams-radius-pill, 999px);
  background: var(--ams-surface-muted, rgba(255, 255, 255, 0.6));
  border: 1px solid var(--ams-border-subtle, rgba(15, 23, 42, 0.08));
  cursor: pointer;
}

.user-name {
  font-weight: 500;
}

.shell-content {
  flex: 1;
  padding: 32px 36px 48px;
  background: linear-gradient(180deg, rgba(77, 103, 255, 0.04), transparent);
}

.content-wrapper {
  min-height: calc(100vh - 160px);
}

.skip-link {
  position: absolute;
  left: -999px;
  top: 8px;
  background: #ffffff;
  color: #0f172a;
  padding: 8px 16px;
  border-radius: var(--ams-radius-md, 12px);
  border: 1px solid var(--ams-border-subtle, rgba(15, 23, 42, 0.08));
  z-index: 100;
}

.skip-link:focus {
  left: 16px;
}

@media (max-width: 1200px) {
  .shell-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .search-input {
    flex: 1;
  }
}

@media (max-width: 900px) {
  .shell-sidebar {
    position: fixed;
    z-index: 20;
    height: 100vh;
  }

  .shell-main {
    margin-left: 88px;
  }
}
</style>
