<template>
  <div class="sidebar-content">
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
        :collapse="collapsed"
        :collapse-transition="false"
        :unique-opened="false"
        router
      >
        <template v-for="item in menuItems" :key="item.path || item.label">
          <el-sub-menu v-if="item.children?.length" :index="item.label">
            <template #title>
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
            </template>
            <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path!" @click="handleMenuClick">
              <el-icon><component :is="child.icon" /></el-icon>
              <span>{{ child.label }}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="item.path!" @click="handleMenuClick">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
            <el-tag v-if="item.badge && !collapsed" size="small" type="info" class="menu-badge">
              {{ item.badge }}
            </el-tag>
          </el-menu-item>
        </template>
      </el-menu>
    </el-scrollbar>
    <footer class="sidebar-footer" v-if="!collapsed">
      <p class="footer-title">欢迎，{{ username || '访客' }}</p>
      <p class="footer-desc">保持资产流转透明，随时掌控</p>
    </footer>
  </div>
</template>

<script setup lang="ts">
import type { NavigationItem } from '@/config/menu'

const props = defineProps<{
  menuItems: NavigationItem[]
  collapsed: boolean
  activeRoute: string
  username: string
}>()

const emit = defineEmits(['item-click'])

const handleMenuClick = () => {
  emit('item-click')
}
</script>

<style scoped>
.sidebar-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  color: #f8fbff;
  background: radial-gradient(circle at top, rgba(77, 103, 255, 0.35), rgba(8, 13, 36, 0.95)),
    #0b1224;
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

.sidebar-scroll {
  flex: 1;
  padding: 0 12px 16px;
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
</style>

