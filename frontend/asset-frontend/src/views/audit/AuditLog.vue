<template>
  <PageContainer title="审计日志" description="追踪系统关键操作，满足合规与风控要求" eyebrow="审计与风控">
    <template #actions>
      <div class="actions">
        <el-input
          v-model="keyword"
          placeholder="搜索动作/实体/详情"
          clearable
          class="toolbar-input"
          :prefix-icon="Search"
        />
        <el-select v-model="actionFilter" placeholder="动作类型" clearable class="filter-select">
          <el-option v-for="action in actionOptions" :key="action" :label="action" :value="action" />
        </el-select>
        
        <!-- Desktop Date Range -->
        <el-date-picker
          v-if="!isMobile"
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="date-range-picker"
        />
        
        <!-- Mobile Date Pickers -->
        <div v-else class="mobile-date-filters">
          <el-date-picker
            v-model="filterStart"
            type="date"
            placeholder="开始日期"
            class="mobile-date-picker"
          />
          <span class="date-separator">-</span>
          <el-date-picker
            v-model="filterEnd"
            type="date"
            placeholder="结束日期"
            class="mobile-date-picker"
          />
        </div>
      </div>
    </template>

    <div class="summary-cards">
      <div class="summary-card surface-card">
        <p>日志条数</p>
        <h3>{{ pagination.total }}</h3>
        <span>当前页 {{ filteredLogs.length }} 条</span>
      </div>
      <div class="summary-card surface-card">
        <p>涉事用户</p>
        <h3>{{ uniqueUserCount }}</h3>
        <span>最近操作 {{ latestAction }}</span>
      </div>
    </div>

    <div class="table-shell surface-card">
      <!-- Desktop Table -->
      <el-table v-if="!isMobile" :data="filteredLogs" v-loading="loading" height="520">
        <el-table-column label="时间" width="190">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column label="动作" width="160">
          <template #default="{ row }">
            <el-tag :type="actionTagType(row.action)">{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="entity" label="实体" width="160" />
        <el-table-column prop="detail" label="详情" show-overflow-tooltip />
      </el-table>

      <!-- Mobile List -->
      <div v-else-if="isMobile && filteredLogs.length" v-loading="loading" class="mobile-list">
        <div v-for="log in filteredLogs" :key="log.id" class="mobile-card">
          <div class="mobile-card-header">
            <div class="header-main">
              <el-tag :type="actionTagType(log.action)" size="small">{{ log.action }}</el-tag>
              <span class="log-entity">{{ log.entity }}</span>
            </div>
            <span class="log-time">{{ formatDate(log.createdAt) }}</span>
          </div>
          <div class="mobile-card-body">
            <div class="log-detail">{{ log.detail }}</div>
            <div class="log-user">操作人ID: {{ log.userId }}</div>
          </div>
        </div>
      </div>

      <el-skeleton v-else-if="loading" :rows="8" animated />
      <el-empty v-else-if="!loading && filteredLogs.length === 0" description="暂无审计日志" />
      <el-pagination
        class="mt-16"
        :layout="isMobile ? 'prev, pager, next' : 'total, prev, pager, next'"
        :background="!isMobile"
        :small="isMobile"
        :current-page="pagination.page"
        :page-size="pagination.size"
        :total="pagination.total"
        @current-change="handlePageChange"
      />
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { listAuditLogs } from '@/api/modules/system'
import type { AuditLog } from '@/types/domain'
import PageContainer from '@/components/common/PageContainer.vue'
import { useDevice } from '@/composables/useDevice'
import { Search } from '@element-plus/icons-vue'

type DateRange = [Date, Date] | null

const logs = ref<AuditLog[]>([])
const loading = ref(false)
const keyword = ref('')
const actionFilter = ref<string>('')
const filterStart = ref<Date | null>(null)
const filterEnd = ref<Date | null>(null)

const dateRange = computed({
  get: (): DateRange => {
    if (filterStart.value && filterEnd.value) {
      return [filterStart.value, filterEnd.value]
    }
    return null
  },
  set: (val: DateRange) => {
    if (val) {
      filterStart.value = val[0]
      filterEnd.value = val[1]
    } else {
      filterStart.value = null
      filterEnd.value = null
    }
  }
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})
const { isMobile } = useDevice()

const actionOptions = computed(() => Array.from(new Set(logs.value.map((log) => log.action))).filter(Boolean))

const filteredLogs = computed(() =>
  logs.value.filter((log) => {
    const matchesKeyword = (() => {
      if (!keyword.value) return true
      const target = keyword.value.toLowerCase()
      return (
        log.action.toLowerCase().includes(target) ||
        (log.entity || '').toLowerCase().includes(target) ||
        (log.detail || '').toLowerCase().includes(target)
      )
    })()

    const matchesAction = actionFilter.value ? log.action === actionFilter.value : true

    const matchesDate = (() => {
      if (!filterStart.value && !filterEnd.value) return true
      const ts = new Date(log.createdAt).getTime()
      
      if (filterStart.value && ts < filterStart.value.getTime()) return false
      if (filterEnd.value) {
        // Include the end date (until 23:59:59)
        const endTs = new Date(filterEnd.value).setHours(23, 59, 59, 999)
        if (ts > endTs) return false
      }
      return true
    })()

    return matchesKeyword && matchesAction && matchesDate
  })
)

const uniqueUserCount = computed(() => new Set(filteredLogs.value.map((log) => log.userId)).size)

const latestAction = computed(() => {
  if (!logs.value.length) return '—'
  return formatDate(logs.value[0].createdAt)
})

const formatDate = (value: string) =>
  new Date(value).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })

const actionTagType = (action: string) => {
  const upper = action.toUpperCase()
  if (upper.includes('DELETE') || upper.includes('REMOVE')) return 'danger'
  if (upper.includes('CREATE') || upper.includes('ADD')) return 'success'
  if (upper.includes('UPDATE') || upper.includes('MODIFY')) return 'warning'
  return 'info'
}

const fetchData = async () => {
  loading.value = true
  try {
    const data = await listAuditLogs({ page: pagination.page - 1, size: pagination.size })
    logs.value = data.content
    pagination.total = data.totalElements
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  pagination.page = page
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.toolbar-input {
  width: 240px;
}

.filter-select {
  width: 180px;
}

.date-range-picker {
  width: 260px;
}

.mobile-date-filters {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.mobile-date-picker {
  flex: 1;
  min-width: 0;
}

.date-separator {
  color: var(--ams-text-muted, #98a2b3);
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: var(--ams-spacing-md, 16px);
  margin-bottom: var(--ams-spacing-md, 16px);
}

.summary-card {
  padding: 18px 20px;
  border-radius: var(--ams-radius-lg, 18px);
}

.summary-card p {
  margin: 0;
  font-size: 13px;
  color: var(--ams-text-muted, #98a2b3);
}

.summary-card h3 {
  margin: 4px 0 2px;
  font-size: 28px;
  font-weight: 600;
}

.summary-card span {
  font-size: 13px;
  color: var(--ams-text-secondary, #475467);
}

.table-shell {
  padding: 24px;
  border-radius: var(--ams-radius-lg, 18px);
}

.mt-16 {
  margin-top: 16px;
}

/* Mobile Styles */
.mobile-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mobile-card {
  background: #fff;
  border: 1px solid var(--ams-border-subtle, rgba(15, 23, 42, 0.08));
  border-radius: 12px;
  padding: 16px;
}

.mobile-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.header-main {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.log-entity {
  font-weight: 600;
  font-size: 14px;
  color: var(--ams-text-primary, #0f172a);
}

.log-time {
  font-size: 12px;
  color: var(--ams-text-muted, #98a2b3);
  white-space: nowrap;
  margin-left: 8px;
}

.mobile-card-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.log-detail {
  font-size: 13px;
  color: var(--ams-text-secondary, #475467);
  line-height: 1.4;
}

.log-user {
  font-size: 12px;
  color: var(--ams-text-muted, #98a2b3);
}

@media (max-width: 768px) {
  .table-shell {
    padding: 16px;
  }
  
  .actions {
    flex-direction: column;
    align-items: stretch;
    width: 100%;
  }
  
  .toolbar-input,
  .filter-select,
  .date-range-picker {
    width: 100% !important;
  }
  
  .summary-cards {
    grid-template-columns: 1fr;
  }
}
</style>
