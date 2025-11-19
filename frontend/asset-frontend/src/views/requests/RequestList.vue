<template>
  <PageContainer title="资产申请中心" description="提交、审批并跟踪资产申请，保持流程透明" eyebrow="资产流程">
    <template #actions>
      <el-button v-if="hasPermission('asset:apply')" type="primary" @click="showCreate = true">
        <el-icon><Plus /></el-icon> 新建申请
      </el-button>
    </template>

    <div class="table-shell surface-card">
      <div class="table-toolbar">
        <el-input
          v-model="keyword"
          placeholder="搜索申请编号/备注"
          clearable
          :prefix-icon="Search"
          class="toolbar-input"
        />
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 180px">
          <el-option
            v-for="option in statusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </div>
      <div class="table-summary">
        <span>总申请 {{ pagination.total }}</span>
        <span>待审批 {{ summary.pending }}</span>
        <span>已通过 {{ summary.approved }}</span>
      </div>
      <el-table v-if="filteredRequests.length" :data="filteredRequests" v-loading="loading" stripe>
        <el-table-column prop="requestNo" label="申请编号" width="160" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="说明" min-width="180" />
        <el-table-column label="明细" min-width="220">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag v-for="item in row.items" :key="item.id" size="small">
                {{ item.assetTypeId }} x{{ item.quantity }}
              </el-tag>
            </el-space>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" v-if="hasPermission('asset:approve')" @click="openApproval(row)">审批</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-skeleton v-else-if="loading" :rows="5" animated />
      <el-empty v-else description="暂无申请记录" />
      <el-pagination
        class="mt-16"
        background
        :current-page="pagination.page"
        :page-size="pagination.size"
        layout="total, prev, pager, next"
        :total="pagination.total"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog v-model="showCreate" title="新建申请" width="640px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="申请人 ID">
          <el-input-number v-model="form.requesterId" :min="1" />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="form.departmentId" placeholder="选择部门" filterable>
            <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
        <el-form-item label="申请明细">
          <div class="item-list">
            <div v-for="(item, index) in form.items" :key="index" class="item-row">
              <el-select v-model="item.assetTypeId" placeholder="资产类型" filterable>
                <el-option v-for="type in assetTypes" :key="type.id" :label="type.name" :value="type.id" />
              </el-select>
              <el-input-number v-model="item.quantity" :min="1" />
              <el-input v-model="item.purpose" placeholder="用途" />
              <el-button type="danger" link @click="removeItem(index)">删除</el-button>
            </div>
            <el-button type="primary" link @click="addItem">新增明细</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="submitRequest">提 交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showApproval" title="审批申请" width="420px">
      <el-form :model="approvalForm" label-width="80px">
        <el-form-item label="审批人ID">
          <el-input-number v-model="approvalForm.approverId" :min="1" />
        </el-form-item>
        <el-form-item label="结果">
          <el-radio-group v-model="approvalForm.result">
            <el-radio-button label="APPROVED">通过</el-radio-button>
            <el-radio-button label="REJECTED">拒绝</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="approvalForm.comment" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApproval = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="submitApproval">提 交</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listDepartments, listAssetTypes } from '@/api/modules/basic'
import { listAssetRequests, createAssetRequest, approveAssetRequest } from '@/api/modules/asset'
import type { AssetRequest, AssetType, Department } from '@/types/domain'
import { usePermission } from '@/utils/permission'
import PageContainer from '@/components/common/PageContainer.vue'
import { Plus, Search } from '@element-plus/icons-vue'

const requests = ref<AssetRequest[]>([])
const loading = ref(false)
const saving = ref(false)
const showCreate = ref(false)
const showApproval = ref(false)
const currentRequest = ref<AssetRequest | null>(null)
const departments = ref<Department[]>([])
const assetTypes = ref<AssetType[]>([])
const keyword = ref('')
const statusFilter = ref<string>()
const summary = reactive({
  pending: 0,
  approved: 0
})
const { hasPermission } = usePermission()

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

interface RequestFormItem {
  assetTypeId?: number
  quantity: number
  purpose?: string
}

interface RequestForm {
  requesterId: number
  departmentId?: number
  remark: string
  items: RequestFormItem[]
}

const form = reactive<RequestForm>({
  requesterId: 3,
  departmentId: undefined as number | undefined,
  remark: '',
  items: [
    {
      assetTypeId: undefined as number | undefined,
      quantity: 1,
      purpose: ''
    }
  ]
})

const approvalForm = reactive({
  approverId: 2,
  result: 'APPROVED',
  comment: ''
})

const statusOptions = [
  { label: '待审批', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已取消', value: 'CANCELLED' }
]

const filteredRequests = computed(() =>
  requests.value.filter((item) => {
    const matchKeyword =
      !keyword.value ||
      item.requestNo.toLowerCase().includes(keyword.value.toLowerCase()) ||
      (item.remark || '').toLowerCase().includes(keyword.value.toLowerCase())
    const matchStatus = !statusFilter.value || item.status === statusFilter.value
    return matchKeyword && matchStatus
  })
)

const fetchData = async () => {
  loading.value = true
  try {
    const data = await listAssetRequests({ page: pagination.page - 1, size: pagination.size })
    requests.value = data.content
    pagination.total = data.totalElements
    summary.pending = data.content.filter((item) => item.status === 'PENDING').length
    summary.approved = data.content.filter((item) => item.status === 'APPROVED').length
  } finally {
    loading.value = false
  }
}

const fetchMeta = async () => {
  const [dept, types] = await Promise.all([
    listDepartments({ page: 0, size: 50 }),
    listAssetTypes({ page: 0, size: 50 })
  ])
  departments.value = dept.content
  assetTypes.value = types.content
}

const handlePageChange = (page: number) => {
  pagination.page = page
  fetchData()
}

const addItem = () => {
  form.items.push({
    assetTypeId: undefined,
    quantity: 1,
    purpose: ''
  })
}

const removeItem = (index: number) => {
  form.items.splice(index, 1)
}

const submitRequest = async () => {
  saving.value = true
  try {
    await createAssetRequest({
      requesterId: form.requesterId,
      departmentId: form.departmentId,
      remark: form.remark,
      items: form.items.map((item) => ({
        assetTypeId: Number(item.assetTypeId),
        quantity: item.quantity,
        purpose: item.purpose
      }))
    })
    ElMessage.success('提交成功')
    showCreate.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

const openApproval = (row: AssetRequest) => {
  currentRequest.value = row
  showApproval.value = true
}

const submitApproval = async () => {
  if (!currentRequest.value) return
  saving.value = true
  try {
    await approveAssetRequest(currentRequest.value.id, approvalForm)
    ElMessage.success('审批完成')
    showApproval.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

const statusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待审批',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const statusTag = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
}

onMounted(() => {
  fetchData()
  fetchMeta()
})
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
  margin-bottom: 12px;
}

.toolbar-input {
  flex: 1;
  min-width: 220px;
}

.table-summary {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
  color: var(--ams-text-secondary, #475467);
  font-size: 14px;
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item-row {
  display: grid;
  grid-template-columns: 1.4fr 0.8fr 1fr 0.4fr;
  gap: 8px;
}

.mt-16 {
  margin-top: 16px;
}
</style>

