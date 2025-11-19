<template>
  <PageContainer title="角色管理" description="查看并维护角色与权限矩阵" eyebrow="系统管理">
    <template #actions>
      <div class="actions">
        <el-input
          v-model="keyword"
          placeholder="搜索角色编码/名称"
          clearable
          style="width: 260px"
          :prefix-icon="Search"
        />
        <el-button type="primary" @click="openCreate">新建角色</el-button>
      </div>
    </template>
    <div class="table-shell surface-card">
      <el-table v-if="filteredRoles.length" :data="filteredRoles" v-loading="loading" stripe>
        <el-table-column prop="code" label="编码" width="160" />
        <el-table-column prop="name" label="名称" width="200" />
        <el-table-column prop="remark" label="备注" width="200" />
        <el-table-column label="权限">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag v-for="perm in row.permissions" :key="perm" size="small">{{ perm }}</el-tag>
            </el-space>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-skeleton v-else-if="loading" :rows="4" animated />
      <el-empty v-else description="暂无角色" />
  </div>
  </PageContainer>

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
    <el-form ref="formRef" :model="formModel" :rules="formRules" label-width="96px">
      <el-form-item label="角色编码" prop="code">
        <el-input v-model="formModel.code" placeholder="例如 ROLE_AUDITOR" :disabled="isEditMode" />
      </el-form-item>
      <el-form-item label="角色名称" prop="name">
        <el-input v-model="formModel.name" placeholder="请输入角色名称" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formModel.remark" placeholder="可选备注" />
      </el-form-item>
      <el-form-item label="权限集合" prop="permissions">
        <el-select v-model="formModel.permissions" multiple filterable placeholder="请选择权限">
          <el-option v-for="perm in availablePermissions" :key="perm" :label="perm" :value="perm" />
        </el-select>
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
import { createRole, listPermissions, listRoles, updateRole } from '@/api/modules/system'
import PageContainer from '@/components/common/PageContainer.vue'
import { Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'

interface RoleRow {
  id: number
  code: string
  name: string
  remark?: string
  permissions: string[]
}

const roles = ref<RoleRow[]>([])
const keyword = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const saving = ref(false)
const availablePermissions = ref<string[]>([])
const formRef = ref<FormInstance>()
const formModel = reactive({
  id: 0,
  code: '',
  name: '',
  remark: '',
  permissions: [] as string[]
})

const formRules: FormRules = {
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { min: 4, message: '至少4个字符', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  permissions: [{ required: true, type: 'array', message: '请选择至少一个权限', trigger: 'change' }]
}

const filteredRoles = computed(() =>
  roles.value.filter((role) => {
    if (!keyword.value) return true
    const target = keyword.value.toLowerCase()
    return role.code.toLowerCase().includes(target) || role.name.toLowerCase().includes(target)
  })
)

const fetchData = async () => {
  loading.value = true
  try {
    const data = await listRoles({ page: 0, size: 50 })
    roles.value = data.content.map((item: any) => ({
      id: item.id,
      code: item.code,
      name: item.name,
      remark: item.remark,
      permissions: item.permissions ?? []
    }))
  } finally {
    loading.value = false
  }
}

const fetchPermissions = async () => {
  const result = await listPermissions()
  availablePermissions.value = result
}

const resetForm = () => {
  formModel.id = 0
  formModel.code = ''
  formModel.name = ''
  formModel.remark = ''
  formModel.permissions = []
  formRef.value?.clearValidate()
}

const openCreate = () => {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row: RoleRow) => {
  dialogMode.value = 'edit'
  formModel.id = row.id
  formModel.code = row.code
  formModel.name = row.name
  formModel.remark = row.remark || ''
  formModel.permissions = [...row.permissions]
  dialogVisible.value = true
}

const isEditMode = computed(() => dialogMode.value === 'edit')
const dialogTitle = computed(() => (isEditMode.value ? '编辑角色' : '新建角色'))

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const payload = {
        code: formModel.code,
        name: formModel.name,
        remark: formModel.remark,
        permissions: formModel.permissions
      }
      if (isEditMode.value) {
        await updateRole(formModel.id, payload)
        ElMessage.success('角色已更新')
      } else {
        await createRole(payload)
        ElMessage.success('角色已创建')
      }
      dialogVisible.value = false
      await fetchData()
    } finally {
      saving.value = false
    }
  })
}

onMounted(async () => {
  await Promise.all([fetchData(), fetchPermissions()])
})
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
