<template>
  <div class="app-shell">
    <a href="#main-content" class="skip-link">跳到主内容</a>
    
    <!-- Desktop Sidebar -->
    <aside v-if="!isMobile" :class="['shell-sidebar', { collapsed: isSidebarCollapsed }]">
      <AppSidebar 
        :menuItems="navigations" 
        :collapsed="isSidebarCollapsed"
        :activeRoute="activeRoute"
        :username="authStore.username"
      />
    </aside>

      <!-- Mobile Sidebar Drawer -->
    <el-drawer
      v-model="mobileMenuVisible"
      direction="ltr"
      :with-header="false"
      size="280px"
      class="mobile-sidebar-drawer"
    >
      <AppSidebar 
        :menuItems="navigations" 
        :collapsed="false"
        :activeRoute="activeRoute"
        :username="authStore.username"
        @item-click="mobileMenuVisible = false"
      />
    </el-drawer>

    <div class="shell-main">
      <header class="shell-header" :class="{ 'is-compact': !showHero }">
        <div class="header-left-group">
          <el-button 
            v-if="isMobile" 
            text 
            class="mobile-menu-btn"
            @click="mobileMenuVisible = true"
          >
            <el-icon :size="24"><Expand /></el-icon>
          </el-button>

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
        </div>

        <div class="header-actions">
          <el-dropdown>
            <span class="user-chip">
              <el-avatar size="small">{{ initials }}</el-avatar>
              <span class="user-name" v-if="!isMobile">{{ authStore.username || '未登录' }}</span>
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
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter, RouterView } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { usePermission } from '@/utils/permission'
import { useDevice } from '@/composables/useDevice'
import { navigationConfig, type NavigationItem } from '@/config/menu'
import AppSidebar from './components/AppSidebar.vue'
import {
  CaretBottom,
  Expand
} from '@element-plus/icons-vue'

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
  '/system/reminder-email': {
    title: '提醒邮箱设置',
    description: '配置资产归还提醒任务使用的发件邮箱',
    section: '系统管理'
  },
  '/profile': {
    title: '个人设置',
    description: '更新您的个人信息与邮箱',
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
const { isMobile } = useDevice()

const isSidebarCollapsed = ref(false)
const mobileMenuVisible = ref(false)

// Close mobile menu on route change
watch(() => route.path, () => {
  mobileMenuVisible.value = false
})

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

const goProfile = () => {
  router.push('/profile')
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
  flex-shrink: 0;
  transition: width var(--ams-motion-base, 200ms ease);
  height: 100vh;
  position: sticky;
  top: 0;
  /* Sidebar content style is handled by AppSidebar component, but we need container style here for layout */
}

.shell-sidebar.collapsed {
  width: 88px;
}

.shell-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
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

.header-left-group {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  flex: 1;
}

.mobile-menu-btn {
  padding: 0;
  margin-top: 4px; /* Align with title approx */
  height: auto;
  color: var(--ams-text-primary, #0f172a);
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
  align-self: flex-start;
  padding-top: 4px;
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

/* Mobile adjustments */
@media (max-width: 768px) {
  .shell-header {
    padding: 16px 20px;
  }
  
  .shell-content {
    padding: 20px 16px 32px;
  }
  
  .title-group h1 {
    font-size: 20px;
  }
  
  .header-actions {
    margin-left: auto;
  }
}
</style>

<style>
/* Global styles for drawer */
.mobile-sidebar-drawer .el-drawer__body {
  padding: 0 !important;
  background: #0b1224; /* Match sidebar background base */
}
</style>
