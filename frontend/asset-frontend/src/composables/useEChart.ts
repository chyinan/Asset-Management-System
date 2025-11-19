import { nextTick, onBeforeUnmount, onMounted, ref, shallowRef } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, DatasetComponent, TitleComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { EChartsOption } from 'echarts'

echarts.use([
  LineChart,
  BarChart,
  PieChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  DatasetComponent,
  TitleComponent,
  CanvasRenderer
])

type ChartInstance = ReturnType<typeof echarts.init>

export const useEChart = () => {
  const elRef = ref<HTMLElement | null>(null)
  const chartInstance = shallowRef<ChartInstance | null>(null)

  const initChart = () => {
    if (!elRef.value) return
    chartInstance.value = echarts.init(elRef.value)
  }

  const dispose = () => {
    chartInstance.value?.dispose()
    chartInstance.value = null
  }

  const setOption = (option: EChartsOption) => {
    if (!option) return
    if (!chartInstance.value) {
      initChart()
    }
    chartInstance.value?.setOption(option, true)
  }

  const resize = () => {
    chartInstance.value?.resize()
  }

  onMounted(() => {
    nextTick(initChart)
    window.addEventListener('resize', resize)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', resize)
    dispose()
  })

  return {
    elRef,
    setOption,
    resize
  }
}

