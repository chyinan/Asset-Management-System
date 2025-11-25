<template>
  <PageContainer title="个人设置" description="更新您的显示名称和邮箱">
    <div class="form-card surface-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" :disabled="loading">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.fullName" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
          <el-button :loading="loading" @click="fetchProfile">刷新</el-button>
        </el-form-item>
      </el-form>
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import PageContainer from '@/components/common/PageContainer.vue'
import { useAuthStore } from '@/store/auth'
import http from '@/api/http'

interface ProfileResponse {
  id: number
  username: string
  fullName?: string
  email?: string
}

const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const authStore = useAuthStore()

const form = reactive({
  username: '',
  fullName: '',
  email: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] }]
}

const fetchProfile = async () => {
  loading.value = true
  try {
    const data = await http.get<ProfileResponse>('/users/me')
    form.username = data.username
    form.fullName = data.fullName ?? ''
    form.email = data.email ?? ''
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      await http.put<ProfileResponse>('/users/me', form)
      ElMessage.success('更新成功')
      authStore.username = form.username
    } finally {
      saving.value = false
    }
  })
}

onMounted(fetchProfile)
</script>

<style scoped>
.form-card {
  max-width: 520px;
  border-radius: var(--ams-radius-lg, 18px);
  padding: 32px;
}
</style>

