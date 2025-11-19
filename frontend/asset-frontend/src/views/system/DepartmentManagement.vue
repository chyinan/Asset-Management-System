<template>
  <PageContainer title="部门管理" description="维护组织结构，支撑审批链路" eyebrow="系统管理">
    <template #actions>
      <div class="actions">
        <el-input
          v-model="keyword"
          placeholder="搜索部门名称"
          clearable
          style="width: 240px"
          :prefix-icon="Search"
        />
        <el-button type="primary" @click="openCreate">新建部门</el-button>
      </div>
    </template>
    <div class="table-shell surface-card">
      <el-table v-if="filteredDepartments.length" :data="filteredDepartments" v-loading="loading" stripe>
        <el-table-column prop="name" label="名称" />
        <el-table-column label="上级部门" width="180">
          <template #default="{ row }">
            {{ parentName(row.parentId) || '—' }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" text @click="confirmDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-skeleton v-else-if="loading" :rows="4" animated />
      <el-empty v-else description="暂无部门" />
  </div>
  </PageContainer>

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
    <el-form ref="formRef" :model="formModel" :rules="formRules" label-width="96px">
      <el-form-item label="部门名称" prop="name">
        <el-input v-model="formModel.name" placeholder="请输入部门名称" />
      </el-form-item>
      <el-form-item label="上级部门">
        <el-select v-model="formModel.parentId" placeholder="请选择上级部门" clearable filterable>
          <el-option
            v-for="item in parentOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formModel.remark" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { createDepartment, deleteDepartment, listDepartments, updateDepartment } from '@/api/modules/basic'
import type { Department } from '@/types/domain'
import PageContainer from '@/components/common/PageContainer.vue'
import { Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'

const departments = ref<Department[]>([])
const keyword = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const formRef = ref<FormInstance>()
const saving = ref(false)
const formModel = reactive({
  id: 0,
  name: '',
  parentId: undefined as number | null | undefined,
  remark: ''
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

const filteredDepartments = computed(() =>
  departments.value.filter((dept) => {
    if (!keyword.value) return true
    return dept.name.toLowerCase().includes(keyword.value.toLowerCase())
  })
)

const parentOptions = computed(() =>
  departments.value.filter((dept) => dept.id !== formModel.id)
)

const parentName = (parentId?: number | null) => {
  if (!parentId) return ''
  return departments.value.find((item) => item.id === parentId)?.name
}

const fetchData = async () => {
  loading.value = true
  try {
    const data = await listDepartments({ page: 0, size: 200 })
    departments.value = data.content
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  formModel.id = 0
  formModel.name = ''
  formModel.parentId = undefined
  formModel.remark = ''
  formRef.value?.clearValidate()
}

const openCreate = () => {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row: Department) => {
  dialogMode.value = 'edit'
  formModel.id = row.id
  formModel.name = row.name
  formModel.parentId = row.parentId ?? null
  formModel.remark = row.remark || ''
  dialogVisible.value = true
}

const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新建部门' : '编辑部门'))

const handleSubmit = () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const payload = {
        name: formModel.name,
        parentId: formModel.parentId ?? null,
        remark: formModel.remark
      }
      if (dialogMode.value === 'create') {
        await createDepartment(payload)
        ElMessage.success('部门创建成功')
      } else {
        await updateDepartment(formModel.id, payload)
        ElMessage.success('部门更新成功')
      }
      dialogVisible.value = false
      await fetchData()
    } finally {
      saving.value = false
    }
  })
}

const confirmDelete = (row: Department) => {
  ElMessageBox.confirm(`确定删除部门「${row.name}」？`, '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await deleteDepartment(row.id)
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
}
</style>
