<template>
  <PageContainer title="库存与流转" description="追踪资产的入库、领用与归还状态" eyebrow="资产流程">
    <template #actions>
      <el-button v-if="hasPermission('asset:stockin')" type="primary" @click="showStockIn = true">入库登记</el-button>
    </template>

    <div class="table-shell surface-card">
      <div class="table-toolbar">
        <el-input
          v-model="keyword"
          placeholder="搜索资产编号/名称"
          clearable
          :prefix-icon="Search"
          class="toolbar-input"
        />
        <el-radio-group v-model="statusFilter" size="small">
          <el-radio-button label="ALL">全部</el-radio-button>
          <el-radio-button label="IN_STOCK">在库</el-radio-button>
          <el-radio-button label="CHECKED_OUT">领用中</el-radio-button>
          <el-radio-button label="SCRAPPED">报废</el-radio-button>
        </el-radio-group>
      </div>
      <div class="table-summary">
        <span>在库 {{ summary.inStock }}</span>
        <span>领用 {{ summary.checkout }}</span>
        <span>报废 {{ summary.scrapped }}</span>
      </div>
      <el-table v-if="filteredInventories.length" :data="filteredInventories" v-loading="loading" stripe>
        <el-table-column prop="assetNo" label="资产编号" width="160" />
        <el-table-column prop="assetName" label="资产名称" min-width="160" />
        <el-table-column prop="serialNo" label="序列号" width="160" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" min-width="140" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              v-if="row.status === 'IN_STOCK' && hasPermission('asset:checkout')"
              @click="openCheckout(row)"
            >
              领用
            </el-button>
            <el-button
              size="small"
              type="success"
              v-if="row.status === 'CHECKED_OUT' && hasPermission('asset:return')"
              @click="openReturn(row)"
            >
              归还
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-skeleton v-else-if="loading" :rows="6" animated />
      <el-empty v-else description="暂无资产记录" />
      <el-pagination
        class="mt-16"
        layout="total, prev, pager, next"
        background
        :current-page="pagination.page"
        :page-size="pagination.size"
        :total="pagination.total"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog v-model="showStockIn" title="资产入库" width="420px">
      <el-form :model="stockInForm" label-width="80px">
        <el-form-item label="资产 ID">
          <el-input-number v-model="stockInForm.assetId" :min="1" />
        </el-form-item>
        <el-form-item label="序列号">
          <el-input v-model="stockInForm.serialNo" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="stockInForm.location" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showStockIn = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="submitStockIn">提 交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showCheckout" title="资产领用" width="420px">
      <el-form :model="checkoutForm" label-width="80px">
        <el-form-item label="用户ID">
          <el-input-number v-model="checkoutForm.userId" :min="1" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="checkoutForm.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCheckout = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitCheckout">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showReturn" title="资产归还" width="420px">
      <el-form :model="returnForm" label-width="80px">
        <el-form-item label="用户ID">
          <el-input-number v-model="returnForm.userId" :min="1" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="returnForm.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReturn = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitReturn">确认</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { stockIn, checkoutInventory, returnInventory, listInventory } from '@/api/modules/asset'
import type { Inventory } from '@/types/domain'
import { usePermission } from '@/utils/permission'
import { Search } from '@element-plus/icons-vue'
import PageContainer from '@/components/common/PageContainer.vue'

const inventories = ref<Inventory[]>([])
const loading = ref(false)
const saving = ref(false)
const showStockIn = ref(false)
const showCheckout = ref(false)
const showReturn = ref(false)
const currentInventory = ref<Inventory | null>(null)
const keyword = ref('')
const statusFilter = ref<'ALL' | 'IN_STOCK' | 'CHECKED_OUT' | 'SCRAPPED'>('ALL')
const { hasPermission } = usePermission()

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const stockInForm = reactive({
  assetId: 1,
  serialNo: '',
  location: ''
})

const checkoutForm = reactive({
  userId: 3,
  remark: ''
})

const returnForm = reactive({
  userId: 3,
  remark: ''
})

const summary = reactive({
  inStock: 0,
  checkout: 0,
  scrapped: 0
})

const filteredInventories = computed(() =>
  inventories.value.filter((item) => {
    const matchKeyword =
      !keyword.value ||
      item.assetName.toLowerCase().includes(keyword.value.toLowerCase()) ||
      item.assetNo.toLowerCase().includes(keyword.value.toLowerCase())
    const matchStatus = statusFilter.value === 'ALL' || item.status === statusFilter.value
    return matchKeyword && matchStatus
  })
)

const fetchData = async () => {
  loading.value = true
  try {
    const data = await listInventory({ page: pagination.page - 1, size: pagination.size })
    inventories.value = data.content
    pagination.total = data.totalElements
    summary.inStock = data.content.filter((item) => item.status === 'IN_STOCK').length
    summary.checkout = data.content.filter((item) => item.status === 'CHECKED_OUT').length
    summary.scrapped = data.content.filter((item) => item.status === 'SCRAPPED').length
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  pagination.page = page
  fetchData()
}

const submitStockIn = async () => {
  saving.value = true
  try {
    await stockIn(stockInForm)
    ElMessage.success('入库成功')
    showStockIn.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

const openCheckout = (row: Inventory) => {
  currentInventory.value = row
  showCheckout.value = true
}

const submitCheckout = async () => {
  if (!currentInventory.value) return
  saving.value = true
  try {
    await checkoutInventory(currentInventory.value.id, checkoutForm)
    ElMessage.success('领用成功')
    showCheckout.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

const openReturn = (row: Inventory) => {
  currentInventory.value = row
  showReturn.value = true
}

const submitReturn = async () => {
  if (!currentInventory.value) return
  saving.value = true
  try {
    await returnInventory(currentInventory.value.id, returnForm)
    ElMessage.success('归还成功')
    showReturn.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

const statusText = (status: string) => {
  const map: Record<string, string> = {
    IN_STOCK: '在库',
    CHECKED_OUT: '已领用',
    SCRAPPED: '报废'
  }
  return map[status] || status
}

const statusType = (status: string) => {
  const map: Record<string, string> = {
    IN_STOCK: 'success',
    CHECKED_OUT: 'warning',
    SCRAPPED: 'info'
  }
  return map[status] || 'info'
}

onMounted(fetchData)
</script>

<style scoped>
.table-shell {
  padding: 24px;
  border-radius: var(--ams-radius-lg, 18px);
}

.table-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: space-between;
  margin-bottom: 16px;
}

.toolbar-input {
  flex: 1;
  min-width: 220px;
}

.table-summary {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  color: var(--ams-text-secondary, #475467);
  font-size: 14px;
}

.mt-16 {
  margin-top: 16px;
}
</style>

