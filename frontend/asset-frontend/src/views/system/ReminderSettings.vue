<template>
  <PageContainer
    title="资产提醒邮箱"
    description="配置资产归还提醒任务使用的发件邮箱"
    eyebrow="系统管理"
  >
    <div class="settings-card surface-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        :disabled="loading"
      >
        <el-form-item label="提醒邮箱" prop="senderEmail">
          <el-input v-model="form.senderEmail" placeholder="no-reply@example.com" />
        </el-form-item>
        <el-divider />
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="SMTP 主机" prop="smtpHost">
              <el-input v-model="form.smtpHost" placeholder="smtp.example.com" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="端口" prop="smtpPort">
              <el-input-number v-model="form.smtpPort" :min="1" :max="65535" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="用户名" prop="smtpUsername">
              <el-input v-model="form.smtpUsername" placeholder="邮箱或登录账号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码/密钥" prop="smtpPassword">
              <el-input v-model="form.smtpPassword" placeholder="留空则不变" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="启用 TLS">
          <el-switch v-model="form.smtpUseTls" />
        </el-form-item>
        <el-form-item label="最后更新">
          <span v-if="metaDescription">{{ metaDescription }}</span>
          <span v-else>—</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
          <el-button :loading="loading" @click="loadData">刷新</el-button>
        </el-form-item>
      </el-form>
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import PageContainer from '@/components/common/PageContainer.vue'
import { getReminderSettings, updateReminderSettings } from '@/api/modules/system'

const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)

const form = reactive({
  senderEmail: '',
  smtpHost: '',
  smtpPort: undefined as number | undefined,
  smtpUsername: '',
  smtpPassword: '',
  smtpUseTls: false
})

const meta = reactive<{
  updatedBy?: string
  updatedAt?: string
}>({})

const rules: FormRules = {
  senderEmail: [
    { required: true, message: '请输入提醒邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] }
  ],
  smtpPort: [
    {
      validator: (_rule, value, callback) => {
        if (value === undefined || value === null || value === '') {
          callback()
          return
        }
        if (value < 1 || value > 65535) {
          callback(new Error('端口范围 1-65535'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const metaDescription = computed(() => {
  if (!meta.updatedAt) return ''
  const datetime = new Date(meta.updatedAt).toLocaleString()
  if (meta.updatedBy) {
    return `${meta.updatedBy} · ${datetime}`
  }
  return datetime
})

const loadData = async () => {
  loading.value = true
  try {
    const data = await getReminderSettings()
    form.senderEmail = data.senderEmail ?? ''
    form.smtpHost = data.smtpHost ?? ''
    form.smtpPort = data.smtpPort
    form.smtpUsername = data.smtpUsername ?? ''
    form.smtpUseTls = Boolean(data.smtpUseTls)
    form.smtpPassword = ''
    meta.updatedAt = data.updatedAt
    meta.updatedBy = data.updatedBy
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
      const payload = {
        senderEmail: form.senderEmail,
        smtpHost: form.smtpHost,
        smtpPort: form.smtpPort,
        smtpUsername: form.smtpUsername,
        smtpUseTls: form.smtpUseTls,
        smtpPassword: form.smtpPassword || undefined
      }
      const data = await updateReminderSettings(payload)
      form.senderEmail = data.senderEmail
      form.smtpHost = data.smtpHost ?? ''
      form.smtpPort = data.smtpPort
      form.smtpUsername = data.smtpUsername ?? ''
      form.smtpUseTls = Boolean(data.smtpUseTls)
      form.smtpPassword = ''
      meta.updatedAt = data.updatedAt
      meta.updatedBy = data.updatedBy
      ElMessage.success('提醒邮箱已更新')
    } finally {
      saving.value = false
    }
  })
}

onMounted(loadData)
</script>

<style scoped>
.settings-card {
  padding: 32px;
  border-radius: var(--ams-radius-lg, 18px);
  max-width: 720px;
}
</style>

