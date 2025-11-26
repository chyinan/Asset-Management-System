import { ref, onMounted, onUnmounted, computed } from 'vue'

const width = ref(window.innerWidth)

export function useDevice() {
  const updateWidth = () => {
    width.value = window.innerWidth
  }

  onMounted(() => {
    window.addEventListener('resize', updateWidth)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', updateWidth)
  })

  const isMobile = computed(() => width.value < 768)
  const isTablet = computed(() => width.value >= 768 && width.value < 1200)
  const isDesktop = computed(() => width.value >= 1200)

  return {
    width,
    isMobile,
    isTablet,
    isDesktop
  }
}



