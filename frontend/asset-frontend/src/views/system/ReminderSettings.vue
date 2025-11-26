<template>
  <PageContainer
    title="资产提醒邮箱"
    description="配置资产归还提醒任务使用的发件邮箱及提醒策略"
    eyebrow="系统管理"
  >
    <div class="settings-card surface-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        :label-width="isMobile ? 'auto' : '120px'"
        :label-position="isMobile ? 'top' : 'right'"
        :disabled="loading"
      >
        <el-divider content-position="left">邮件服务器配置</el-divider>
        <el-form-item label="提醒邮箱" prop="senderEmail">
          <el-input v-model="form.senderEmail" placeholder="no-reply@example.com" />
        </el-form-item>
        <el-row :gutter="isMobile ? 0 : 12">
          <el-col :span="isMobile ? 24 : 12">
            <el-form-item label="SMTP 主机" prop="smtpHost">
              <el-input v-model="form.smtpHost" placeholder="smtp.example.com" />
            </el-form-item>
          </el-col>
          <el-col :span="isMobile ? 24 : 12">
            <el-form-item label="端口" prop="smtpPort">
              <el-input-number v-model="form.smtpPort" :min="1" :max="65535" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="isMobile ? 0 : 12">
          <el-col :span="isMobile ? 24 : 12">
            <el-form-item label="用户名" prop="smtpUsername">
              <el-input v-model="form.smtpUsername" placeholder="邮箱或登录账号" />
            </el-form-item>
          </el-col>
          <el-col :span="isMobile ? 24 : 12">
            <el-form-item label="密码/密钥" prop="smtpPassword">
              <el-input v-model="form.smtpPassword" placeholder="留空则不变" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="启用 TLS">
          <el-switch v-model="form.smtpUseTls" />
        </el-form-item>

        <el-divider content-position="left">提醒策略配置</el-divider>
        <el-row :gutter="isMobile ? 0 : 12">
          <el-col :span="isMobile ? 24 : 12">
            <el-form-item label="提前提醒(天)" prop="reminderStartDays">
              <el-input-number v-model="form.reminderStartDays" :min="1" :max="365" style="width: 100%" />
              <div class="form-tip">距离归还日多少天开始提醒</div>
            </el-form-item>
          </el-col>
          <el-col :span="isMobile ? 24 : 12">
            <el-form-item label="任务频率" prop="reminderCron">
               <el-select v-model="cronPreset" @change="handleCronChange" style="width: 100%">
                  <el-option label="每周一 09:00" value="0 0 9 ? * MON" />
                  <el-option label="每天 09:00" value="0 0 9 * * ?" />
                  <el-option label="每天 10:00" value="0 0 10 * * ?" />
                  <el-option label="自定义 (CRON)" value="custom" />
               </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="cronPreset === 'custom'" label="Cron 表达式" prop="reminderCron">
           <el-input v-model="form.reminderCron" placeholder="秒 分 时 日 月 周" />
           <div class="form-tip">示例: 0 0 9 ? * MON (每周一早9点)</div>
        </el-form-item>

        <el-divider />
        <el-form-item label="最后更新">
          <span v-if="metaDescription">{{ metaDescription }}</span>
          <span v-else>—</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSubmit" class="submit-btn">保存</el-button>
          <el-button :loading="loading" @click="loadData" class="refresh-btn">刷新</el-button>
        </el-form-item>
      </el-form>
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import PageContainer from '@/components/common/PageContainer.vue'
import { getReminderSettings, updateReminderSettings } from '@/api/modules/system'
import { useDevice } from '@/composables/useDevice'

const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const { isMobile } = useDevice()
const cronPreset = ref('0 0 9 ? * MON')

const form = reactive({
  senderEmail: '',
  smtpHost: '',
  smtpPort: undefined as number | undefined,
  smtpUsername: '',
  smtpPassword: '',
  smtpUseTls: false,
  reminderStartDays: 7,
  reminderCron: '0 0 9 ? * MON'
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
  ],
  reminderStartDays: [
    { required: true, message: '请输入提前提醒天数', trigger: 'blur' }
  ],
  reminderCron: [
    { required: true, message: '请输入Cron表达式', trigger: 'blur' }
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

const handleCronChange = (val: string) => {
  if (val !== 'custom') {
    form.reminderCron = val
  }
}

// Watch for manual changes in custom mode to reset preset if it matches one
watch(() => form.reminderCron, (newVal) => {
  if (newVal === '0 0 9 ? * MON') cronPreset.value = newVal
  else if (newVal === '0 0 9 * * ?') cronPreset.value = newVal
  else if (newVal === '0 0 10 * * ?') cronPreset.value = newVal
  else cronPreset.value = 'custom'
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
    form.reminderStartDays = data.reminderStartDays ?? 7
    form.reminderCron = data.reminderCron ?? '0 0 9 ? * MON'
    
    // Set preset based on loaded value
    if (['0 0 9 ? * MON', '0 0 9 * * ?', '0 0 10 * * ?'].includes(form.reminderCron)) {
      cronPreset.value = form.reminderCron
    } else {
      cronPreset.value = 'custom'
    }

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
        smtpPassword: form.smtpPassword || undefined,
        reminderStartDays: form.reminderStartDays,
        reminderCron: form.reminderCron
      }
      const data = await updateReminderSettings(payload)
      form.senderEmail = data.senderEmail
      form.smtpHost = data.smtpHost ?? ''
      form.smtpPort = data.smtpPort
      form.smtpUsername = data.smtpUsername ?? ''
      form.smtpUseTls = Boolean(data.smtpUseTls)
      form.smtpPassword = ''
      form.reminderStartDays = data.reminderStartDays ?? 7
      form.reminderCron = data.reminderCron ?? '0 0 9 ? * MON'
      
      meta.updatedAt = data.updatedAt
      meta.updatedBy = data.updatedBy
      ElMessage.success('提醒配置已更新')
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

.form-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.2;
  margin-top: 4px;
}

@media (max-width: 768px) {
  .settings-card {
    padding: 20px;
  }
  
  .submit-btn,
  .refresh-btn {
    width: 48%;
  }
}
</style>
