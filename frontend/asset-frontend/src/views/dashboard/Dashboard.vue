<template>
  <PageContainer title="仪表盘总览" description="实时洞察资产申请、库存与审计动态" eyebrow="今日概览">
    <template #actions>
      <el-button v-if="hasPermission('asset:apply')" type="primary" @click="router.push('/requests')">
        <el-icon><Tickets /></el-icon>
        新建申请
      </el-button>
      <el-button :loading="loading" @click="refreshData">刷新数据</el-button>
    </template>

    <div v-if="kpiCards.length" class="kpi-grid">
      <div v-for="card in kpiCards" :key="card.title" class="kpi-card surface-card">
        <div class="kpi-meta">
          <span>{{ card.title }}</span>
          <el-tag size="small" :type="card.statusType">{{ card.statusText }}</el-tag>
        </div>
        <p class="kpi-value">{{ card.value }}</p>
        <p class="kpi-desc">{{ card.desc }}</p>
      </div>
    </div>

    <div class="dashboard-panels">
      <el-card shadow="never" class="surface-card">
        <template #header>
          <div class="panel-header">
            <div>
              <p class="panel-eyebrow">常用入口</p>
              <h3>快捷操作</h3>
            </div>
          </div>
        </template>
        <div class="quick-grid">
          <button
            v-for="action in quickActions"
            :key="action.route"
            class="quick-card"
            type="button"
            @click="router.push(action.route)"
          >
            <div class="quick-icon">
              <el-icon><component :is="action.icon" /></el-icon>
            </div>
            <div class="quick-content">
              <p class="quick-title">{{ action.label }}</p>
              <p class="quick-desc">{{ action.desc }}</p>
            </div>
          </button>
        </div>
      </el-card>

      <el-card v-if="canViewRequests" shadow="never" class="surface-card">
        <template #header>
          <div class="panel-header">
            <div>
              <p class="panel-eyebrow">审批待办</p>
              <h3>最新资产申请</h3>
            </div>
            <el-button text type="primary" @click="router.push('/requests')">查看全部</el-button>
          </div>
        </template>
        <el-skeleton v-if="loading" :rows="4" animated />
        <el-empty v-else-if="!pendingRequests.length" description="暂无待审批申请" />
        <el-timeline v-else>
          <el-timeline-item
            v-for="item in pendingRequests"
            :key="item.id"
            :timestamp="formatDate(item.createdAt)"
            :type="statusToType(item.status)"
          >
            <p class="request-title">{{ item.requestNo }}</p>
            <p class="request-desc">{{ item.remark || '无备注' }}</p>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </div>

    <div v-if="canViewRequests || canViewInventory" class="chart-grid">
      <ChartCard
        v-if="canViewRequests"
        title="近10日申请趋势"
        eyebrow="流程监控"
        description="掌握每日资产申请量变化"
        :option="requestTrendOption"
      />
      <ChartCard
        v-if="canViewInventory"
        title="库存状态分布"
        eyebrow="库存结构"
        description="直观查看库存健康度"
        :option="inventoryStatusOption"
        :height="360"
      />
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, type Component } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '@/components/common/PageContainer.vue'
import ChartCard from '@/components/analytics/ChartCard.vue'
import { usePermission } from '@/utils/permission'
import { listAssetRequests, listInventory } from '@/api/modules/asset'
import type { AssetRequest, Inventory } from '@/types/domain'
import type { PageResponse } from '@/types/api'
import { Box, Histogram, Memo, Tickets } from '@element-plus/icons-vue'
import type { EChartsOption } from 'echarts'

interface QuickAction {
  label: string
  route: string
  permission?: string
  icon: Component
  desc: string
  tone: 'primary' | 'success' | 'warning' | 'info'
}

const router = useRouter()
const { hasPermission } = usePermission()
const loading = ref(false)
const pendingRequests = ref<AssetRequest[]>([])
const requestsSnapshot = ref<AssetRequest[]>([])
const inventorySnapshot = ref<Inventory[]>([])
const metrics = reactive({
  pending: 0,
  totalRequests: 0,
  inventoryAvailable: 0,
  inventoryTotal: 0,
  checkoutCount: 0
})

const quickActionConfigs: QuickAction[] = [
  {
    label: '资产申请',
    route: '/requests',
    icon: Tickets,
    permission: 'asset:view',
    desc: '快速发起或审批申请',
    tone: 'primary'
  },
  {
    label: '库存看板',
    route: '/inventory',
    icon: Box,
    permission: 'asset:stockin',
    desc: '入库、领用与归还',
    tone: 'success'
  },
  {
    label: '审计日志',
    route: '/audit',
    icon: Memo,
    permission: 'audit:view',
    desc: '系统操作痕迹',
    tone: 'warning'
  },
  {
    label: '角色配置',
    route: '/system/roles',
    icon: Histogram,
    permission: 'role:manage',
    desc: '维护权限矩阵',
    tone: 'info'
  }
]

const quickActions = computed(() => quickActionConfigs.filter((item) => !item.permission || hasPermission(item.permission)))

const kpiCards = computed(() => {
  const cards: {
    title: string
    value: number
    desc: string
    statusText: string
    statusType: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  }[] = []

  if (canViewRequests.value) {
    cards.push({
    title: '待审批申请',
    value: metrics.pending,
    desc: `${metrics.totalRequests} 条申请记录`,
    statusText: metrics.pending > 5 ? '需立即处理' : '负载正常',
    statusType: metrics.pending > 5 ? 'warning' : 'success'
    })
  }

  if (canViewInventory.value) {
    cards.push(
  {
    title: '可用库存',
    value: metrics.inventoryAvailable,
    desc: `总库存 ${metrics.inventoryTotal}`,
    statusText: metrics.inventoryAvailable > metrics.inventoryTotal * 0.6 ? '库存充足' : '注意补货',
    statusType: metrics.inventoryAvailable > metrics.inventoryTotal * 0.6 ? 'success' : 'danger'
  },
  {
    title: '在途/领用',
    value: metrics.checkoutCount,
    desc: '当前外借资产数量',
    statusText: metrics.checkoutCount > 0 ? '需跟进归还' : '全部在库',
    statusType: metrics.checkoutCount > 0 ? 'info' : 'success'
  }
    )
  }

  return cards
})

const formatDate = (value: string) =>
  new Intl.DateTimeFormat('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }).format(
    new Date(value)
  )

const statusToType = (status: string) => {
  const map: Record<string, 'primary' | 'success' | 'warning'> = {
    PENDING: 'warning',
    APPROVED: 'success'
  }
  return map[status] || 'primary'
}

const canViewRequests = computed(() => hasPermission('asset:view'))
const canViewInventory = computed(() => hasPermission('asset:stockin'))

const requestTrendOption = computed<EChartsOption>(() => {
  const grouped = requestsSnapshot.value.reduce<Record<string, number>>((acc, item) => {
    const key = new Date(item.createdAt).toISOString().slice(0, 10)
    acc[key] = (acc[key] || 0) + 1
    return acc
  }, {})
  const entries = Object.entries(grouped)
    .sort(([a], [b]) => (a > b ? 1 : -1))
    .slice(-10)
  const labels = entries.map(([label]) =>
    new Date(label).toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
  )
  const values = entries.map(([, value]) => value)
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 36, right: 16, top: 40, bottom: 24 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: labels,
      axisLine: { lineStyle: { color: '#cbd5f5' } }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: 'rgba(148,163,184,0.25)' } }
    },
    series: [
      {
        name: '申请总量',
        type: 'line',
        smooth: true,
        data: values,
        areaStyle: {
          opacity: 0.18,
          color: 'rgba(77,103,255,0.5)'
        },
        lineStyle: { width: 3, color: '#4d67ff' },
        symbol: 'circle',
        symbolSize: 8
      }
    ]
  }
})

const inventoryStatusOption = computed<EChartsOption>(() => {
  const statusData = [
    { name: '在库', value: metrics.inventoryAvailable },
    { name: '领用', value: metrics.checkoutCount },
    {
      name: '报废',
      value: inventorySnapshot.value.filter((item) => item.status === 'SCRAPPED').length
    }
  ]
  return {
    tooltip: { trigger: 'item' },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { color: '#475467' }
    },
    series: [
      {
        type: 'pie',
        radius: ['55%', '78%'],
        center: ['40%', '50%'],
        label: { formatter: '{b}\n{d}%', color: '#0f172a' },
        data: statusData,
        itemStyle: {
          borderColor: '#fff',
          borderWidth: 2
        }
      }
    ],
    color: ['#4d67ff', '#06b6d4', '#94a3b8']
  }
})

const resetRequestMetrics = () => {
  metrics.totalRequests = 0
  metrics.pending = 0
  pendingRequests.value = []
  requestsSnapshot.value = []
}

const resetInventoryMetrics = () => {
  metrics.inventoryTotal = 0
  metrics.inventoryAvailable = 0
  metrics.checkoutCount = 0
  inventorySnapshot.value = []
}

const refreshData = async () => {
  loading.value = true
  try {
    const [requestRes, inventoryRes] = await Promise.all([
      canViewRequests.value ? listAssetRequests({ page: 0, size: 20 }) : Promise.resolve<PageResponse<AssetRequest> | null>(null),
      canViewInventory.value ? listInventory({ page: 0, size: 50 }) : Promise.resolve<PageResponse<Inventory> | null>(null)
    ])

    if (requestRes) {
      metrics.totalRequests = requestRes.totalElements
      requestsSnapshot.value = requestRes.content
      const pending = requestRes.content.filter((item) => item.status === 'PENDING')
      metrics.pending = pending.length
      pendingRequests.value = pending.slice(0, 4)
    } else {
      resetRequestMetrics()
    }

    if (inventoryRes) {
      metrics.inventoryTotal = inventoryRes.totalElements
      const inventoryContent = inventoryRes.content as Inventory[]
      inventorySnapshot.value = inventoryContent
      metrics.inventoryAvailable = inventoryContent.filter((item) => item.status === 'IN_STOCK').length
      metrics.checkoutCount = inventoryContent.filter((item) => item.status === 'CHECKED_OUT').length
    } else {
      resetInventoryMetrics()
    }
  } finally {
    loading.value = false
  }
}

onMounted(refreshData)
</script>

<style scoped>
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: var(--ams-spacing-lg, 24px);
}

.kpi-card {
  padding: 20px;
}

.kpi-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: var(--ams-text-muted, #98a2b3);
}

.kpi-value {
  margin: 12px 0 4px;
  font-size: 32px;
  font-weight: 600;
  color: var(--ams-text-primary, #0f172a);
}

.kpi-desc {
  margin: 0;
  color: var(--ams-text-secondary, #475467);
}

.dashboard-panels {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: var(--ams-spacing-lg, 24px);
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: var(--ams-spacing-lg, 24px);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-eyebrow {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--ams-text-muted, #98a2b3);
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 16px;
}

.quick-card {
  border: none;
  border-radius: var(--ams-radius-lg, 18px);
  padding: 16px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: left;
  cursor: pointer;
  background: var(--ams-surface-muted, rgba(255, 255, 255, 0.7));
  transition: transform var(--ams-motion-fast, 120ms ease), box-shadow var(--ams-motion-fast, 120ms ease);
}

.quick-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--ams-shadow-soft, 0 6px 24px rgba(15, 23, 42, 0.08));
}

.quick-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: var(--ams-radius-md, 12px);
  margin-bottom: 12px;
  color: #fff;
}

.quick-card:nth-child(1) .quick-icon {
  background: #4d67ff;
}

.quick-card:nth-child(2) .quick-icon {
  background: #06b6d4;
}

.quick-card:nth-child(3) .quick-icon {
  background: #f59e0b;
}

.quick-card:nth-child(4) .quick-icon {
  background: #8b5cf6;
}

.quick-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--ams-text-primary, #0f172a);
}

.quick-desc {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--ams-text-secondary, #475467);
}

.request-title {
  margin: 0;
  font-weight: 600;
}

.request-desc {
  margin: 2px 0 0;
  color: var(--ams-text-secondary, #475467);
}
</style>
