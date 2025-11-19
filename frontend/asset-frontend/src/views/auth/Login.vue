<template>
  <div class="login-shell gradient-hero">
    <div class="login-hero">
      <p class="hero-eyebrow">{{ heroEyebrow }}</p>
      <h1>打造高效的资产运营中枢</h1>
      <p class="hero-desc">统一资产申请、库存与审计，体验专业级办公流畅度。</p>
      <ul class="hero-list">
        <li v-for="item in heroHighlights" :key="item">
          <el-icon><CircleCheckFilled /></el-icon>
          <span>{{ item }}</span>
        </li>
      </ul>
    </div>
    <el-card class="login-card surface-glass" shadow="never">
      <div class="panel-meta">
        <p class="panel-eyebrow">欢迎回来</p>
        <h2>使用工作账号登录</h2>
        <p class="panel-desc">输入凭证进入资产管理控制台</p>
      </div>
      <el-form
        ref="formRef"
        class="login-form"
        :model="form"
        :rules="rules"
        label-position="top"
        @keyup.enter="handleSubmit"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <div class="form-tools">
          <el-checkbox v-model="rememberMe">记住用户名</el-checkbox>
          <el-button link type="primary" @click="showForgotDialog = true">忘记密码？</el-button>
        </div>
        <el-form-item>
          <el-button class="login-btn" type="primary" :loading="loading" @click="handleSubmit">立即登录</el-button>
        </el-form-item>
      </el-form>
      <p class="support-tip">如需开通账号或重置密码，请联系 IT 服务台。</p>
    </el-card>
    <el-dialog v-model="showForgotDialog" title="忘记密码" width="360px">
      <p>如忘记密码请联系系统管理员 1817175451@qq.com</p>
      <template #footer>
        <el-button type="primary" @click="showForgotDialog = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const storageKey = 'ams:last_username'
const storedUsername = typeof window !== 'undefined' ? window.localStorage.getItem(storageKey) : ''
const rememberMe = ref(Boolean(storedUsername))
const showForgotDialog = ref(false)

const form = reactive({
  username: storedUsername || 'admin',
  password: 'admin123'
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const heroEyebrow = '今日 · 办公更高效'
const heroHighlights = ['审批分级与可视流程', '实时库存稽核', '操作全链路审计']

const handleSubmit = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await authStore.login(form)
      if (typeof window !== 'undefined') {
        if (rememberMe.value) {
          window.localStorage.setItem(storageKey, form.username)
        } else {
          window.localStorage.removeItem(storageKey)
        }
      }
      const redirect = (route.query.redirect as string) || '/'
      router.replace(redirect)
    } catch (error: any) {
      ElMessage.error(error.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  align-items: stretch;
  gap: 48px;
  padding: 60px clamp(24px, 8vw, 120px);
  color: #fff;
  background-image: var(--ams-gradients-hero, linear-gradient(135deg, #4d67ff 0%, #2ec9ff 100%));
}

.login-hero {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 16px;
  max-width: 520px;
}

.hero-eyebrow {
  margin: 0;
  font-size: 14px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  opacity: 0.9;
}

.hero-desc {
  margin: 0;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.86);
}

.hero-list {
  list-style: none;
  padding: 0;
  margin: 8px 0 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.hero-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.95);
}

.login-card {
  padding: 32px 36px;
  max-width: 420px;
  margin-left: auto;
  backdrop-filter: blur(24px);
  border-radius: var(--ams-radius-lg, 18px);
}

.panel-meta {
  text-align: left;
  margin-bottom: 24px;
  color: var(--ams-text-primary, #0f172a);
}

.panel-eyebrow {
  margin: 0;
  font-size: 13px;
  letter-spacing: 0.08em;
  color: var(--ams-text-muted, #98a2b3);
}

.panel-meta h2 {
  margin: 8px 0 6px;
  font-size: 24px;
  color: var(--ams-text-primary, #0f172a);
}

.panel-desc {
  margin: 0;
  color: var(--ams-text-secondary, #475467);
  font-size: 14px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-tools {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
}

.support-tip {
  margin-top: 18px;
  font-size: 13px;
  color: var(--ams-text-muted, #98a2b3);
  text-align: center;
}

@media (max-width: 900px) {
  .login-shell {
    padding: 40px 24px;
  }

  .login-card {
    margin: 0 auto;
  }
}
</style>
