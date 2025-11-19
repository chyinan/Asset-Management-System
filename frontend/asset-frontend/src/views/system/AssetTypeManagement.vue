<template>
  <PageContainer title="资产类型库" description="规范资产分类，统一编码与配置信息" eyebrow="系统管理">
    <template #actions>
      <div class="actions">
        <el-input
          v-model="keyword"
          placeholder="搜索名称或编码"
          clearable
          style="width: 240px"
          :prefix-icon="Search"
        />
        <el-tag type="info" effect="plain">共 {{ assetTypes.length }} 类</el-tag>
        <el-button type="primary" @click="openCreate">新建类型</el-button>
      </div>
    </template>

    <div class="table-shell surface-card">
      <el-table v-if="filteredAssetTypes.length" :data="filteredAssetTypes" v-loading="loading" stripe>
        <el-table-column prop="name" label="名称" min-width="160" />
        <el-table-column label="编码" width="160">
          <template #default="{ row }">
            <el-tag type="primary" effect="dark">{{ row.code || 'N/A' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="说明" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button text type="danger" @click="confirmDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-skeleton v-else-if="loading" :rows="5" animated />
      <el-empty v-else description="暂无资产类型" />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px">
      <el-form ref="formRef" :model="formModel" :rules="formRules" label-width="88px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="formModel.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="编码" prop="code">
          <el-input v-model="formModel.code" placeholder="可选，默认自动生成" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="formModel.remark" placeholder="请输入说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import PageContainer from '@/components/common/PageContainer.vue'
import { createAssetType, deleteAssetType, listAssetTypes, updateAssetType } from '@/api/modules/basic'
import type { AssetType } from '@/types/domain'
import { Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'

const assetTypes = ref<AssetType[]>([])
const keyword = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const saving = ref(false)
const formRef = ref<FormInstance>()
const formModel = reactive({
  id: 0,
  name: '',
  code: '',
  remark: ''
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  code: [{ min: 2, message: '至少2个字符', trigger: 'blur' }]
}

const filteredAssetTypes = computed(() =>
  assetTypes.value.filter((item) => {
    if (!keyword.value) return true
    const target = keyword.value.toLowerCase()
    const name = item.name?.toLowerCase?.() || ''
    const code = item.code?.toLowerCase?.() || ''
    const remark = item.remark?.toLowerCase?.() || ''
    return name.includes(target) || code.includes(target) || remark.includes(target)
  })
)

const fetchData = async () => {
  loading.value = true
  try {
    const data = await listAssetTypes({ page: 0, size: 100 })
    assetTypes.value = data.content
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  formModel.id = 0
  formModel.name = ''
  formModel.code = ''
  formModel.remark = ''
  formRef.value?.clearValidate()
}

const openCreate = () => {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row: AssetType) => {
  dialogMode.value = 'edit'
  formModel.id = row.id
  formModel.name = row.name
  formModel.code = row.code || ''
  formModel.remark = row.remark || ''
  dialogVisible.value = true
}

const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新建资产类型' : '编辑资产类型'))

const handleSubmit = () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const payload = {
        name: formModel.name,
        code: formModel.code || undefined,
        remark: formModel.remark
      }
      if (dialogMode.value === 'create') {
        await createAssetType(payload)
        ElMessage.success('创建成功')
      } else {
        await updateAssetType(formModel.id, payload)
        ElMessage.success('更新成功')
      }
      dialogVisible.value = false
      await fetchData()
    } finally {
      saving.value = false
    }
  })
}

const confirmDelete = (row: AssetType) => {
  ElMessageBox.confirm(`确定删除类型「${row.name}」？`, '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await deleteAssetType(row.id)
      ElMessage.success('删除成功')
      await fetchData()
    })
    .catch(() => {})
}

onMounted(fetchData)
</script>

<style scoped>
.table-shell {
  padding: 24px;
  border-radius: var(--ams-radius-lg, 18px);
}

.actions {
  display: flex;
  gap: 12px;
  align-items: center;
}
</style>
