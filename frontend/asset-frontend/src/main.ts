import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'

import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import { useAuthStore } from '@/store/auth'
import { applyTheme } from '@/theme'

const pinia = createPinia()

const app = createApp(App)
app.use(pinia)
app.use(router)
app.use(ElementPlus)

applyTheme()

const authStore = useAuthStore()
if (authStore.accessToken) {
  authStore.parseToken(authStore.accessToken)
}

app.mount('#app')
