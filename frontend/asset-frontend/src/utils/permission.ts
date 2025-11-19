import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/store/auth'

export const usePermission = () => {
  const authStore = useAuthStore()
  const { permissions } = storeToRefs(authStore)

  const hasPermission = (codes?: string | string[]) => {
    if (!codes) return true
    const list = Array.isArray(codes) ? codes : [codes]
    return list.every((code) => permissions.value.includes(code))
  }

  return { hasPermission }
}

