import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import AppLayout from '@/layouts/AppLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: AppLayout,
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'requests',
        name: 'AssetRequests',
        component: () => import('@/views/requests/RequestList.vue'),
        meta: { requiresAuth: true, permission: 'asset:view' }
      },
      {
        path: 'inventory',
        name: 'Inventory',
        component: () => import('@/views/inventory/InventoryList.vue'),
        meta: { requiresAuth: true, permission: 'asset:stockin' }
      },
      {
        path: 'system/users',
        name: 'SystemUsers',
        component: () => import('@/views/system/UserManagement.vue'),
        meta: { requiresAuth: true, permission: 'user:manage' }
      },
      {
        path: 'system/roles',
        name: 'SystemRoles',
        component: () => import('@/views/system/RoleManagement.vue'),
        meta: { requiresAuth: true, permission: 'role:manage' }
      },
      {
        path: 'system/departments',
        name: 'SystemDepartments',
        component: () => import('@/views/system/DepartmentManagement.vue'),
        meta: { requiresAuth: true, permission: 'asset:admin' }
      },
      {
        path: 'system/asset-types',
        name: 'SystemAssetTypes',
        component: () => import('@/views/system/AssetTypeManagement.vue'),
        meta: { requiresAuth: true, permission: 'asset:admin' }
      },
      {
        path: 'system/reminder-email',
        name: 'ReminderSettings',
        component: () => import('@/views/system/ReminderSettings.vue'),
        meta: { requiresAuth: true, permission: 'asset:admin' }
      },
      {
        path: 'audit',
        name: 'AuditLogs',
        component: () => import('@/views/audit/AuditLog.vue'),
        meta: { requiresAuth: true, permission: 'audit:view' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/ProfileSettings.vue'),
        meta: { requiresAuth: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { public: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, _from, next) => {
  const auth = useAuthStore()
  if (to.meta.public) {
    return next()
  }
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  if (to.meta.permission && !auth.hasPermission(to.meta.permission as string)) {
    return next({ name: 'Dashboard' })
  }
  return next()
})

export default router

