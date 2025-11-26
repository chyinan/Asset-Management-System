<template>
  <PageContainer title="用户管理" description="维护系统账号与权限，保障访问安全" eyebrow="系统管理">
    <template #actions>
      <el-input
        v-model="keyword"
        placeholder="搜索用户名/姓名"
        clearable
        style="width: 240px"
        :prefix-icon="Search"
      />
      <el-button type="primary" @click="showCreate = true">新增用户</el-button>
    </template>

    <div class="table-shell surface-card">
      <!-- Desktop Table -->
      <el-table v-if="!isMobile && filteredUsers.length" :data="filteredUsers" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" width="160" />
        <el-table-column prop="fullName" label="姓名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="角色" min-width="200">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag v-for="role in row.roles" :key="role" size="small">{{ role }}</el-tag>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <!-- Mobile List -->
      <div v-else-if="isMobile && filteredUsers.length" v-loading="loading" class="mobile-list">
        <div v-for="user in filteredUsers" :key="user.username" class="mobile-card">
          <div class="mobile-card-header">
            <span class="username">{{ user.username }}</span>
            <span class="fullname">{{ user.fullName || '未设置姓名' }}</span>
          </div>
          <div class="mobile-card-body">
            <div class="info-row" v-if="user.email">
              <span class="label">邮箱：</span>
              <span class="value">{{ user.email }}</span>
            </div>
            <div class="roles-row">
              <el-tag v-for="role in user.roles" :key="role" size="small" class="role-tag">{{ role }}</el-tag>
            </div>
          </div>
        </div>
      </div>

      <el-skeleton v-else-if="loading" :rows="5" animated />
      <el-empty v-else description="暂无用户" />
    </div>

    <el-dialog v-model="showCreate" title="新增用户" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.fullName" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roles" multiple>
            <el-option label="ROLE_ADMIN" value="ROLE_ADMIN" />
            <el-option label="ROLE_DEPT_ADMIN" value="ROLE_DEPT_ADMIN" />
            <el-option label="ROLE_USER" value="ROLE_USER" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">确认</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listUsers, createUser, type UserPayload } from '@/api/modules/system'
import { useDevice } from '@/composables/useDevice'
import PageContainer from '@/components/common/PageContainer.vue'
import { Search } from '@element-plus/icons-vue'

interface UserRow {
  username: string
  fullName?: string
  email?: string
  roles: string[]
}

const users = ref<UserRow[]>([])
const keyword = ref('')
const loading = ref(false)
const saving = ref(false)
const showCreate = ref(false)
const { isMobile } = useDevice()

const form = reactive<UserPayload>({
  username: '',
  password: '123456',
  fullName: '',
  email: '',
  roles: ['ROLE_USER']
})

const filteredUsers = computed(() =>
  users.value.filter((user) => {
    if (!keyword.value) return true
    const target = keyword.value.toLowerCase()
    return (
      user.username.toLowerCase().includes(target) ||
      (user.fullName || '').toLowerCase().includes(target)
    )
  })
)

const fetchData = async () => {
  loading.value = true
  try {
    const data = await listUsers({ page: 0, size: 50 })
    users.value = data.content
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  saving.value = true
  try {
    await createUser(form)
    ElMessage.success('创建成功')
    showCreate.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.table-shell {
  padding: 24px;
  border-radius: var(--ams-radius-lg, 18px);
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
  align-items: center;
  margin-bottom: 12px;
}

.username {
  font-weight: 600;
  font-size: 15px;
  color: var(--ams-text-primary, #0f172a);
}

.fullname {
  font-size: 13px;
  color: var(--ams-text-secondary, #475467);
}

.mobile-card-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  font-size: 13px;
  color: var(--ams-text-secondary, #475467);
}

.roles-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 4px;
}

@media (max-width: 768px) {
  .table-shell {
    padding: 16px;
  }
}
</style>

